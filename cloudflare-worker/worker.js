// Pets & Pals cloud adoption API
// Phase 1: POST /v1/adopt, GET /v1/pets/:uuid (starter pet only)
// Phase 2: additional-pet adoption gated by a 3-day cooldown, skippable with a
//          cloud-tracked currency purchased via Stripe. See migrations/0002_*.sql
//          and cloudflare-worker/README.md for the schema/design rationale.
// D1 binding: env.DB (database "pets-and-pals")
// Secrets (wrangler secret put): STRIPE_SECRET_KEY, STRIPE_WEBHOOK_SECRET

const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/;
// species ids are PetList enum names lowercased, e.g. "copper_golem" - loose format
// check rather than a hardcoded allowlist (unlike VALID_SPECIES below): the roster
// this validates against is ~84 entries and client-driven (MenagerieScreen only
// offers species it already knows are real), so keeping a second server-side copy
// of that list in sync isn't worth the drift risk for what this endpoint actually
// needs to guard against (garbage/injection-shaped input, not "is this a real pet")
const SPECIES_RE = /^[a-z][a-z0-9_]{0,31}$/;

// Phase 1 ships fox-only for the *starter* pet; the picker (later phase) will widen this.
const VALID_SPECIES = new Set(["fox"]);

const THREE_DAYS_MS = 3 * 24 * 60 * 60 * 1000;
const SKIP_COOLDOWN_COST = 500;

// Ad-hoc Stripe Checkout line items (Stripe's price_data, not pre-created Dashboard
// Price objects) - keeps setup to just the two secrets below, no manual product
// catalog step in the Stripe Dashboard. Adjust freely; nothing else depends on
// these values except the checkout endpoint and the webhook trusting the amount
// *it* set in metadata (never anything Stripe echoes back from the client side).
const CURRENCY_PACKS = {
  small: { name: "250 Paw Coins", unitAmountCents: 199, currencyAmount: 250 },
  medium: { name: "600 Paw Coins", unitAmountCents: 399, currencyAmount: 600 },
  large: { name: "1500 Paw Coins", unitAmountCents: 799, currencyAmount: 1500 },
};

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    const { pathname } = url;

    if (request.method === "POST" && pathname === "/v1/adopt") {
      return handleAdopt(request, env);
    }

    const petsMatch = pathname.match(/^\/v1\/pets\/([^/]+)$/);
    if (request.method === "GET" && petsMatch) {
      return handleGetPet(petsMatch[1], env);
    }

    if (request.method === "POST" && pathname === "/v1/pets/adopt-additional") {
      return handleAdoptAdditional(request, env);
    }

    const ownedMatch = pathname.match(/^\/v1\/pets\/mine\/([^/]+)$/);
    if (request.method === "GET" && ownedMatch) {
      return handleGetOwnedPets(ownedMatch[1], env);
    }

    const balanceMatch = pathname.match(/^\/v1\/currency\/balance\/([^/]+)$/);
    if (request.method === "GET" && balanceMatch) {
      return handleCurrencyBalance(balanceMatch[1], request, env);
    }

    if (request.method === "POST" && pathname === "/v1/currency/checkout") {
      return handleCreateCheckout(request, env);
    }

    if (request.method === "POST" && pathname === "/v1/stripe/webhook") {
      return handleStripeWebhook(request, env);
    }

    if (request.method === "GET" && pathname === "/checkout/success") {
      return html("<h1>Thanks!</h1><p>Your Paw Coins are on the way - you can close this tab and go back to Minecraft.</p>");
    }
    if (request.method === "GET" && pathname === "/checkout/cancel") {
      return html("<h1>Checkout canceled</h1><p>No charge was made. You can close this tab.</p>");
    }

    return json({ error: "not_found" }, 404);
  },
};

async function handleAdopt(request, env) {
  let body;
  try {
    body = await request.json();
  } catch {
    return json({ error: "invalid_json" }, 400);
  }

  const uuid = typeof body.uuid === "string" ? body.uuid.trim().toLowerCase() : null;
  const species = typeof body.species === "string" ? body.species.trim().toLowerCase() : null;
  const clientModVersion = typeof body.clientModVersion === "string" ? body.clientModVersion.slice(0, 64) : null;

  if (!uuid || !UUID_RE.test(uuid)) {
    return json({ error: "invalid_uuid" }, 400);
  }
  if (!species || !VALID_SPECIES.has(species)) {
    return json({ error: "invalid_species" }, 400);
  }

  const existing = await env.DB.prepare(
    `SELECT id AS pet_id, species, adopted_at FROM pets WHERE owner_uuid = ?1 AND is_starter_pet = 1`
  ).bind(uuid).first();
  if (existing) {
    return json({ error: "already_adopted", petId: existing.pet_id, species: existing.species, adoptedAt: existing.adopted_at }, 409);
  }

  const now = Date.now();
  const petId = crypto.randomUUID();
  const syncToken = crypto.randomUUID();
  const playerSecret = base64UrlEncode(crypto.getRandomValues(new Uint8Array(32)));
  const secretHash = await sha256Hex(playerSecret);

  try {
    await env.DB.batch([
      env.DB.prepare(
        `INSERT INTO players (uuid, created_at, last_seen_at, player_secret_hash)
         VALUES (?1, ?2, ?2, ?3)
         ON CONFLICT(uuid) DO UPDATE SET last_seen_at = ?2`
      ).bind(uuid, now, secretHash),
      env.DB.prepare(
        `INSERT INTO pets (id, owner_uuid, species, adopted_at, is_starter_pet)
         VALUES (?1, ?2, ?3, ?4, 1)`
      ).bind(petId, uuid, species, now),
      env.DB.prepare(
        `INSERT INTO pet_bond_stats (pet_id, bond_seconds, last_sync_at, last_sync_token, updated_at)
         VALUES (?1, 0, ?2, ?3, ?2)`
      ).bind(petId, now, syncToken),
      env.DB.prepare(
        `INSERT INTO adoption_events (owner_uuid, pet_id, species, client_mod_version, adopted_at)
         VALUES (?1, ?2, ?3, ?4, ?5)`
      ).bind(uuid, petId, species, clientModVersion, now),
    ]);
  } catch (err) {
    // most likely the unique-starter-pet index rejecting a race between our SELECT
    // and this INSERT - re-check rather than assume, and only 500 if it's genuinely
    // something else
    const raced = await env.DB.prepare(
      `SELECT id AS pet_id, species, adopted_at FROM pets WHERE owner_uuid = ?1 AND is_starter_pet = 1`
    ).bind(uuid).first();
    if (raced) {
      return json({ error: "already_adopted", petId: raced.pet_id, species: raced.species, adoptedAt: raced.adopted_at }, 409);
    }
    return json({ error: "internal_error", detail: String(err) }, 500);
  }

  return json({
    petId,
    species,
    adoptedAt: now,
    playerSecret, // returned exactly once - the client is responsible for persisting it
    bondSeconds: 0,
    lastSyncToken: syncToken,
  }, 201);
}

async function handleGetPet(rawUuid, env) {
  const uuid = rawUuid.trim().toLowerCase();
  if (!UUID_RE.test(uuid)) {
    return json({ error: "invalid_uuid" }, 400);
  }

  // deliberately unauthenticated: a Minecraft UUID is not secret (derivable from a
  // public username via Mojang's API), and this only exposes non-sensitive fields
  // (species/bond seconds, never the player secret) - required so the "already
  // adopted, reinstalled the mod, lost my local secret" recovery check works even
  // without the secret that was lost. See the plan's "known open gap" note.
  const row = await env.DB.prepare(
    `SELECT p.id AS pet_id, p.species, p.nickname, b.bond_seconds, b.last_sync_at
     FROM pets p JOIN pet_bond_stats b ON b.pet_id = p.id
     WHERE p.owner_uuid = ?1 AND p.is_starter_pet = 1`
  ).bind(uuid).first();

  if (!row) {
    return json({ error: "not_found" }, 404);
  }

  return json({
    petId: row.pet_id,
    species: row.species,
    nickname: row.nickname,
    bondSeconds: row.bond_seconds,
    lastSyncAt: row.last_sync_at,
  });
}

async function handleGetOwnedPets(rawUuid, env) {
  const uuid = rawUuid.trim().toLowerCase();
  if (!UUID_RE.test(uuid)) {
    return json({ error: "invalid_uuid" }, 400);
  }

  // unauthenticated for the same reason handleGetPet is: the species/timestamp list
  // isn't sensitive (no secret, no currency amounts), and this is exactly the kind
  // of read the Menagerie needs on open before it has any reason to prompt for auth
  const rows = await env.DB.prepare(
    `SELECT species, adopted_at, is_starter_pet FROM pets WHERE owner_uuid = ?1 ORDER BY adopted_at ASC`
  ).bind(uuid).all();

  const species = rows.results.map((r) => r.species);
  const lastNonStarter = rows.results
      .filter((r) => r.is_starter_pet === 0)
      .reduce((max, r) => Math.max(max, r.adopted_at), 0);

  return json({ uuid, species, lastNonStarterAdoptionAt: lastNonStarter });
}

// --- additional-pet adoption (3-day cooldown, currency-skippable) ---

async function handleAdoptAdditional(request, env) {
  let body;
  try {
    body = await request.json();
  } catch {
    return json({ error: "invalid_json" }, 400);
  }

  const uuid = typeof body.uuid === "string" ? body.uuid.trim().toLowerCase() : null;
  const secret = typeof body.secret === "string" ? body.secret : null;
  const species = typeof body.species === "string" ? body.species.trim().toLowerCase() : null;
  const skipCooldown = body.skipCooldown === true;
  const clientModVersion = typeof body.clientModVersion === "string" ? body.clientModVersion.slice(0, 64) : null;

  if (!(await authenticate(uuid, secret, env))) {
    return json({ error: "invalid_credentials" }, 401);
  }
  if (!species || !SPECIES_RE.test(species)) {
    return json({ error: "invalid_species" }, 400);
  }

  const now = Date.now();
  const petId = crypto.randomUUID();

  if (!skipCooldown) {
    // Free path: a single guarded conditional insert. D1 serializes all writes to
    // the database, so this WHERE NOT EXISTS is evaluated atomically against
    // whatever's currently committed - two concurrent free-adopt requests can't
    // both win the same way a plain unique index prevents a duplicate row, even
    // though "spaced >= 3 days apart" isn't itself a uniqueness constraint.
    const result = await env.DB.batch([
      env.DB.prepare(
        `INSERT INTO pets (id, owner_uuid, species, adopted_at, is_starter_pet)
         SELECT ?1, ?2, ?3, ?4, 0
         WHERE NOT EXISTS (
           SELECT 1 FROM pets
           WHERE owner_uuid = ?2 AND is_starter_pet = 0 AND adopted_at > ?4 - ?5
         )`
      ).bind(petId, uuid, species, now, THREE_DAYS_MS),
      env.DB.prepare(
        `INSERT INTO adoption_events (owner_uuid, pet_id, species, client_mod_version, adopted_at)
         SELECT ?1, ?2, ?3, ?4, ?5
         WHERE EXISTS (SELECT 1 FROM pets WHERE id = ?2)`
      ).bind(uuid, petId, species, clientModVersion, now),
    ]);
    const petInserted = result[0].meta.changes === 1;
    if (!petInserted) {
      const last = await env.DB.prepare(
        `SELECT MAX(adopted_at) AS last_adopted_at FROM pets WHERE owner_uuid = ?1 AND is_starter_pet = 0`
      ).bind(uuid).first();
      const nextEligibleAt = (last.last_adopted_at ?? 0) + THREE_DAYS_MS;
      return json({ error: "cooldown_active", nextEligibleAt }, 409);
    }
    return json({ petId, species, adoptedAt: now, skippedCooldown: false }, 201);
  }

  // Paid path: debit + pet insert + audit insert in one batch. The no-overdraft
  // trigger (migrations/0002_*.sql) throws a genuine SQL error on insufficient
  // balance, which per D1's documented batch semantics rolls back the whole
  // sequence - a debit can never commit without the pet it paid for, or vice versa.
  try {
    await env.DB.batch([
      env.DB.prepare(
        `INSERT INTO currency_ledger (owner_uuid, amount, reason, related_pet_id, created_at)
         VALUES (?1, ?2, 'cooldown_skip', ?3, ?4)`
      ).bind(uuid, -SKIP_COOLDOWN_COST, petId, now),
      env.DB.prepare(
        `INSERT INTO pets (id, owner_uuid, species, adopted_at, is_starter_pet)
         VALUES (?1, ?2, ?3, ?4, 0)`
      ).bind(petId, uuid, species, now),
      env.DB.prepare(
        `INSERT INTO adoption_events (owner_uuid, pet_id, species, client_mod_version, adopted_at)
         VALUES (?1, ?2, ?3, ?4, ?5)`
      ).bind(uuid, petId, species, clientModVersion, now),
    ]);
  } catch (err) {
    const balRow = await env.DB.prepare(
      `SELECT COALESCE(SUM(amount), 0) AS balance FROM currency_ledger WHERE owner_uuid = ?1`
    ).bind(uuid).first();
    if (balRow.balance < SKIP_COOLDOWN_COST) {
      return json({ error: "insufficient_currency", balance: balRow.balance, required: SKIP_COOLDOWN_COST }, 402);
    }
    return json({ error: "internal_error", detail: String(err) }, 500);
  }

  return json({ petId, species, adoptedAt: now, skippedCooldown: true }, 201);
}

async function handleCurrencyBalance(rawUuid, request, env) {
  const uuid = rawUuid.trim().toLowerCase();
  const secret = request.headers.get("x-player-secret");
  if (!(await authenticate(uuid, secret, env))) {
    return json({ error: "invalid_credentials" }, 401);
  }
  const row = await env.DB.prepare(
    `SELECT COALESCE(SUM(amount), 0) AS balance FROM currency_ledger WHERE owner_uuid = ?1`
  ).bind(uuid).first();
  return json({ uuid, balance: row.balance });
}

// --- Stripe: checkout session creation + webhook ---

async function handleCreateCheckout(request, env) {
  if (!env.STRIPE_SECRET_KEY) {
    return json({ error: "stripe_not_configured" }, 500);
  }
  let body;
  try {
    body = await request.json();
  } catch {
    return json({ error: "invalid_json" }, 400);
  }

  const uuid = typeof body.uuid === "string" ? body.uuid.trim().toLowerCase() : null;
  const secret = typeof body.secret === "string" ? body.secret : null;
  const packId = typeof body.packId === "string" ? body.packId : null;

  if (!(await authenticate(uuid, secret, env))) {
    return json({ error: "invalid_credentials" }, 401);
  }
  const pack = packId ? CURRENCY_PACKS[packId] : null;
  if (!pack) {
    return json({ error: "invalid_pack" }, 400);
  }

  const origin = new URL(request.url).origin;
  const params = new URLSearchParams();
  params.set("mode", "payment");
  params.set("payment_method_types[0]", "card"); // card only - keeps checkout.session.completed == paid, see README
  params.set("line_items[0][quantity]", "1");
  params.set("line_items[0][price_data][currency]", "usd");
  params.set("line_items[0][price_data][unit_amount]", String(pack.unitAmountCents));
  params.set("line_items[0][price_data][product_data][name]", pack.name);
  params.set("client_reference_id", uuid);
  // set server-side from our own fixed catalog, never trusted from the client -
  // this is what makes the webhook's metadata read safe (only the secret-key holder
  // can set Checkout Session metadata; the customer can't alter it mid-checkout)
  params.set("metadata[currency_amount]", String(pack.currencyAmount));
  params.set("success_url", `${origin}/checkout/success`);
  params.set("cancel_url", `${origin}/checkout/cancel`);

  let stripeResp;
  try {
    stripeResp = await fetch("https://api.stripe.com/v1/checkout/sessions", {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${env.STRIPE_SECRET_KEY}`,
        "Content-Type": "application/x-www-form-urlencoded",
        // dedupes an accidental double-click on "buy" from creating two sessions
        // for the same purchase attempt
        "Idempotency-Key": await sha256Hex(`${uuid}:${packId}:${Math.floor(Date.now() / 60000)}`),
      },
      body: params.toString(),
    });
  } catch (err) {
    return json({ error: "stripe_error", detail: String(err) }, 502);
  }

  if (!stripeResp.ok) {
    const detail = await stripeResp.text();
    return json({ error: "stripe_error", detail }, 502);
  }

  const session = await stripeResp.json();
  return json({ checkoutUrl: session.url, sessionId: session.id }, 201);
}

async function handleStripeWebhook(request, env) {
  if (!env.STRIPE_WEBHOOK_SECRET) {
    return json({ error: "stripe_not_configured" }, 500);
  }
  const sigHeader = request.headers.get("stripe-signature");
  // raw body MUST be captured before any JSON parsing - once request.json()/text()
  // is called once, the stream is consumed and the exact bytes needed for the HMAC
  // (Stripe signs the literal request body, not a re-serialization of it) are gone
  const rawBody = await request.text();
  if (!(await verifyStripeSignature(rawBody, sigHeader, env.STRIPE_WEBHOOK_SECRET))) {
    return json({ error: "invalid_signature" }, 400);
  }

  let event;
  try {
    event = JSON.parse(rawBody);
  } catch {
    return json({ error: "invalid_json" }, 400);
  }

  if (event.type !== "checkout.session.completed") {
    return json({ received: true });
  }

  const session = event.data.object;
  if (session.payment_status !== "paid") {
    // async payment methods can fire "completed" before funds settle - we only
    // offer card payment methods (see handleCreateCheckout), so this should be
    // rare/never in practice, kept as cheap defense-in-depth
    return json({ received: true });
  }

  const uuid = typeof session.client_reference_id === "string"
      ? session.client_reference_id.trim().toLowerCase() : null;
  const amount = Number.parseInt(session.metadata?.currency_amount, 10);
  const maxPackAmount = Math.max(...Object.values(CURRENCY_PACKS).map((p) => p.currencyAmount));

  if (!uuid || !UUID_RE.test(uuid) || !Number.isInteger(amount) || amount <= 0 || amount > maxPackAmount) {
    console.error("stripe_webhook_malformed_metadata", event.id, session.id);
    // ack anyway - a malformed event is our own bug, retrying won't fix it, and
    // NOT acking just makes Stripe hammer the same broken event forever
    return json({ received: true });
  }

  // ON CONFLICT DO NOTHING (rather than try/catch) never throws, so
  // result.meta.changes cleanly distinguishes "newly credited" (1) from "Stripe
  // retried/resent an event we already processed" (0) - either way we return 200
  // so Stripe stops retrying, we just log which case it was
  const result = await env.DB.prepare(
    `INSERT INTO currency_ledger (owner_uuid, amount, reason, stripe_event_id, stripe_session_id, created_at)
     VALUES (?1, ?2, 'stripe_purchase', ?3, ?4, ?5)
     ON CONFLICT(stripe_event_id) DO NOTHING`
  ).bind(uuid, amount, event.id, session.id, Date.now()).run();

  return json({ received: true, credited: result.meta.changes === 1 });
}

const STRIPE_SIG_TOLERANCE_SECONDS = 300; // matches Stripe's own default

async function verifyStripeSignature(rawBody, sigHeader, webhookSecret) {
  if (!sigHeader) return false;

  const parts = Object.fromEntries(
    sigHeader.split(",").map((kv) => {
      const i = kv.indexOf("=");
      return [kv.slice(0, i), kv.slice(i + 1)];
    })
  );
  const timestamp = parts.t;
  const v1 = parts.v1; // ignore v0 (legacy scheme) per Stripe's own guidance
  if (!timestamp || !v1) return false;

  const nowSeconds = Math.floor(Date.now() / 1000);
  if (Math.abs(nowSeconds - Number(timestamp)) > STRIPE_SIG_TOLERANCE_SECONDS) return false;

  const signedPayload = `${timestamp}.${rawBody}`;
  const key = await crypto.subtle.importKey(
    "raw",
    new TextEncoder().encode(webhookSecret),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["verify"]
  );
  const sigBytes = hexToBytes(v1);
  if (!sigBytes) return false;

  // crypto.subtle.verify does a timing-safe comparison internally
  return crypto.subtle.verify("HMAC", key, sigBytes, new TextEncoder().encode(signedPayload));
}

function hexToBytes(hex) {
  if (hex.length % 2 !== 0) return null;
  const bytes = new Uint8Array(hex.length / 2);
  for (let i = 0; i < bytes.length; i++) {
    const byte = Number.parseInt(hex.substr(i * 2, 2), 16);
    if (Number.isNaN(byte)) return null;
    bytes[i] = byte;
  }
  return bytes;
}

// --- shared auth ---

async function authenticate(uuid, secret, env) {
  if (!uuid || !UUID_RE.test(uuid) || typeof secret !== "string" || secret.length === 0) return false;
  const row = await env.DB.prepare(
    `SELECT player_secret_hash FROM players WHERE uuid = ?1`
  ).bind(uuid).first();
  if (!row) return false;
  const candidateHash = await sha256Hex(secret);
  return candidateHash === row.player_secret_hash;
}

function json(obj, status = 200) {
  return new Response(JSON.stringify(obj), {
    status,
    headers: { "content-type": "application/json" },
  });
}

function html(body) {
  return new Response(`<!doctype html><meta charset="utf-8"><body style="font-family:sans-serif;text-align:center;padding:3em">${body}</body>`, {
    headers: { "content-type": "text/html; charset=utf-8" },
  });
}

async function sha256Hex(text) {
  const digest = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(text));
  return [...new Uint8Array(digest)].map((b) => b.toString(16).padStart(2, "0")).join("");
}

function base64UrlEncode(bytes) {
  let binary = "";
  for (const b of bytes) binary += String.fromCharCode(b);
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

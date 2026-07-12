// Pets & Pals cloud adoption API - Phase 1
// Endpoints: POST /v1/adopt, GET /v1/pets/:uuid
// D1 binding: env.DB (database "pets-and-pals")

const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/;

// Phase 1 ships fox-only; the picker (Phase 4) will widen this.
const VALID_SPECIES = new Set(["fox"]);

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

function json(obj, status = 200) {
  return new Response(JSON.stringify(obj), {
    status,
    headers: { "content-type": "application/json" },
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

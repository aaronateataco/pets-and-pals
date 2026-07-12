# Pets & Pals cloud API

Backs the Phase 1 cloud starter-pet adoption feature. Cloudflare Worker
(`worker.js`) + D1 database (`pets-and-pals`, id `07969e68-f337-4308-be93-c96c189e4459`).
Not part of the Gradle build - this directory is just the source of truth for the
Worker, kept in the repo for version control.

Live at `https://pets-and-pals-api.zeraoralegendary.workers.dev` (matches the
`BASE_URL` constant in `PetsCloudClient.java`).

## Redeploying

```sh
npx wrangler deploy
```

(requires `wrangler login` or a `CLOUDFLARE_API_TOKEN` env var with Workers edit
permission - the deploy this session was done via the raw Cloudflare API instead of
wrangler, since wrangler isn't installed in the dev container; either works).

## Schema

See the CREATE TABLE statements run directly against D1 when this was first set up
(not currently tracked as a migrations/ folder - if the schema changes, add one).
Tables: `players`, `pets`, `pet_bond_stats`, `sync_audit_log`, `adoption_events`.
`pet_bond_stats`/`sync_audit_log` exist but are unused until Phase 2/3 (bond-time
tracking + sync) ship.

## Endpoints (Phase 1)

- `POST /v1/adopt` - `{uuid, species, clientModVersion}` -> `201` with a one-time
  `playerSecret`, or `409 already_adopted` if that UUID already has a starter pet
  (no secret re-issued on 409, by design).
- `GET /v1/pets/:uuid` - unauthenticated (UUIDs aren't secret), returns adoption +
  bond state. Used for "reinstalled the mod, check if already adopted" recovery.

See `/home/aaron/.claude/plans/unified-coalescing-matsumoto.md` (or the repo's own
copy if moved) for the full phased design, including the bond-time anti-tamper
scheme for later phases.

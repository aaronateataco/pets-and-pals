-- Currency ledger (event-sourced: balance is always SUM(amount), never a mutable
-- column - see cloudflare-worker/README.md for why) plus an index that speeds up
-- the additional-adoption cooldown check. Run with:
--   npx wrangler d1 execute pets-and-pals --file=migrations/0002_currency_and_additional_adoptions.sql
-- (add --local first to smoke-test, especially the trigger below, before --remote)

CREATE TABLE currency_ledger (
  id                INTEGER PRIMARY KEY AUTOINCREMENT,
  owner_uuid        TEXT NOT NULL,
  amount            INTEGER NOT NULL CHECK (amount != 0),
  reason            TEXT NOT NULL CHECK (reason IN (
                      'stripe_purchase', 'cooldown_skip', 'cooldown_skip_refund', 'admin_adjustment'
                    )),
  stripe_event_id   TEXT,
  stripe_session_id TEXT,
  related_pet_id    TEXT,
  created_at        INTEGER NOT NULL
);

-- SQLite treats every NULL as distinct under a plain UNIQUE index, so debit/refund
-- rows (stripe_event_id/stripe_session_id = NULL) are unaffected - only a second row
-- with the SAME non-null Stripe id collides. This is what makes the webhook handler's
-- "ON CONFLICT(stripe_event_id) DO NOTHING" an exact, unambiguous conflict target.
CREATE UNIQUE INDEX idx_currency_ledger_stripe_event ON currency_ledger(stripe_event_id);
CREATE UNIQUE INDEX idx_currency_ledger_stripe_session ON currency_ledger(stripe_session_id);
CREATE INDEX idx_currency_ledger_owner_created ON currency_ledger(owner_uuid, created_at);

-- Enforces "balance can never go negative" as a real DB constraint, not app logic.
-- A thrown error here aborts and rolls back the whole enclosing env.DB.batch([...])
-- call per D1's documented batch semantics, so a currency debit can never commit
-- without the pet insert it paid for actually succeeding too, and vice versa.
CREATE TRIGGER trg_currency_ledger_no_overdraft
BEFORE INSERT ON currency_ledger
WHEN NEW.amount < 0
  AND (SELECT COALESCE(SUM(amount), 0) FROM currency_ledger WHERE owner_uuid = NEW.owner_uuid) + NEW.amount < 0
BEGIN
  SELECT RAISE(ABORT, 'insufficient_currency_balance');
END;

-- Speeds up the additional-adoption cooldown check's WHERE NOT EXISTS subquery -
-- no new column needed on pets, the existing is_starter_pet 0/1 split already
-- distinguishes the one starter pet from additional (cooldown-gated) adoptions.
CREATE INDEX idx_pets_owner_nonstarter_adopted ON pets(owner_uuid, is_starter_pet, adopted_at);

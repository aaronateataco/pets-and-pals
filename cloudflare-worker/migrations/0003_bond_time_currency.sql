-- Bond-time free currency: passive Paw Coins just for playing, no ads/payment
-- required. Run with:
--   npx wrangler d1 execute pets-and-pals --file=migrations/0003_bond_time_currency.sql
-- (add --local first to smoke-test)

ALTER TABLE players ADD COLUMN last_bond_claim_at INTEGER;

-- SQLite/D1 can't ALTER a CHECK constraint directly - rebuild the table with the
-- widened constraint, then reattach everything that was dropped along with the old
-- table (indexes/trigger don't survive losing the table they're attached to).
CREATE TABLE currency_ledger_new (
  id                INTEGER PRIMARY KEY AUTOINCREMENT,
  owner_uuid        TEXT NOT NULL,
  amount            INTEGER NOT NULL CHECK (amount != 0),
  reason            TEXT NOT NULL CHECK (reason IN (
                      'stripe_purchase', 'cooldown_skip', 'cooldown_skip_refund',
                      'admin_adjustment', 'bond_time'
                    )),
  stripe_event_id   TEXT,
  stripe_session_id TEXT,
  related_pet_id    TEXT,
  created_at        INTEGER NOT NULL
);
INSERT INTO currency_ledger_new SELECT * FROM currency_ledger;
DROP TABLE currency_ledger;
ALTER TABLE currency_ledger_new RENAME TO currency_ledger;

CREATE UNIQUE INDEX idx_currency_ledger_stripe_event ON currency_ledger(stripe_event_id);
CREATE UNIQUE INDEX idx_currency_ledger_stripe_session ON currency_ledger(stripe_session_id);
CREATE INDEX idx_currency_ledger_owner_created ON currency_ledger(owner_uuid, created_at);

CREATE TRIGGER trg_currency_ledger_no_overdraft
BEFORE INSERT ON currency_ledger
WHEN NEW.amount < 0
  AND (SELECT COALESCE(SUM(amount), 0) FROM currency_ledger WHERE owner_uuid = NEW.owner_uuid) + NEW.amount < 0
BEGIN
  SELECT RAISE(ABORT, 'insufficient_currency_balance');
END;

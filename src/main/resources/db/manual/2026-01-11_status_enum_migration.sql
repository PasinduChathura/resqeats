-- Manual migration for status enum refactor + outlet availability split
--
-- Applies the following changes:
-- - users.status: INACTIVE -> DISABLED
-- - merchants.status: PENDING -> PENDING_APPROVAL
-- - outlets.status: INACTIVE -> DISABLED, PENDING -> PENDING_APPROVAL
-- - outlets.availability_status: NEW column (OPEN/CLOSED)
--
-- NOTE: The app uses spring.jpa.hibernate.ddl-auto=validate, so schema must be updated before startup.

-- 1) Add availability_status column to outlets
ALTER TABLE outlets
    ADD COLUMN availability_status VARCHAR(20) NOT NULL DEFAULT 'OPEN' AFTER status;

-- 2) Backfill/normalize enum values (safe to run multiple times)
UPDATE users
SET status = 'DISABLED'
WHERE status = 'INACTIVE';

UPDATE merchants
SET status = 'PENDING_APPROVAL'
WHERE status = 'PENDING';

UPDATE outlets
SET status = 'PENDING_APPROVAL'
WHERE status = 'PENDING';

UPDATE outlets
SET status = 'DISABLED'
WHERE status = 'INACTIVE';

-- 3) Ensure disabled/suspended outlets aren't marked OPEN
UPDATE outlets
SET availability_status = 'CLOSED'
WHERE status IN ('DISABLED', 'SUSPENDED');

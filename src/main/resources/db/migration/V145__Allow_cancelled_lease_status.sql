-- V143__Allow_cancelled_lease_status.sql
-- Include CANCELLED in the lease status check constraint.

ALTER TABLE lease_agreements DROP CONSTRAINT IF EXISTS chk_lease_status;
ALTER TABLE lease_agreements ADD CONSTRAINT chk_lease_status
    CHECK (status IN ('DRAFT', 'PENDING_RENTER', 'PENDING_LANDLORD', 'ACTIVE', 'EXPIRED', 'TERMINATED', 'REJECTED', 'CANCELLED'));

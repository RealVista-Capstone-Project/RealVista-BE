-- V146__Add_appointment_reschedule_and_audit_log.sql

-- 1. Update appointments table to support rescheduling
ALTER TABLE appointments
    ADD COLUMN last_modified_by_user_id UUID,
    ADD COLUMN reschedule_reason TEXT;

ALTER TABLE appointments
    ADD CONSTRAINT fk_appointment_last_modifier
    FOREIGN KEY (last_modified_by_user_id) REFERENCES users (user_id);

-- 2. Update status constraint for appointments
-- Drop the old constraint first (need to find its name or assume from previous migrations)
-- In V2 it was 'chk_appointment_status'
ALTER TABLE appointments DROP CONSTRAINT IF EXISTS chk_appointment_status;
ALTER TABLE appointments
    ADD CONSTRAINT chk_appointment_status
    CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELED', 'COMPLETED', 'RESCHEDULE_PENDING'));

-- 3. Create appointment_audit_logs table
CREATE TABLE appointment_audit_logs
(
    audit_log_id   UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    appointment_id UUID      NOT NULL,
    actor_id       UUID      NOT NULL,
    action         VARCHAR(50) NOT NULL,
    old_status     VARCHAR(20),
    new_status     VARCHAR(20),
    old_start_time TIMESTAMP,
    new_start_time TIMESTAMP,
    notes          TEXT,
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),

    FOREIGN KEY (appointment_id) REFERENCES appointments (appointment_id) ON DELETE CASCADE,
    FOREIGN KEY (actor_id) REFERENCES users (user_id)
);

CREATE INDEX idx_appt_audit_appointment ON appointment_audit_logs (appointment_id);
CREATE INDEX idx_appt_audit_actor ON appointment_audit_logs (actor_id);

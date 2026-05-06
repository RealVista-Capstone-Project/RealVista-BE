-- V143__Add_proposed_times_to_appointments.sql
ALTER TABLE appointments 
ADD COLUMN proposed_start_time TIMESTAMP,
ADD COLUMN proposed_end_time TIMESTAMP;

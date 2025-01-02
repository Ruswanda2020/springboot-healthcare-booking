ALTER TABLE doctor_specialization
    ADD COLUMN consultation_type VARCHAR(50) NOT NULL;

ALTER TABLE doctor_specialization
DROP CONSTRAINT doctor_specialization_doctor_id_specialization_id_key;

ALTER TABLE doctor_specialization
    ADD CONSTRAINT doctor_specialization_unique UNIQUE (doctor_id, specialization_id, consultation_type);

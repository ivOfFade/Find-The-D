ALTER TABLE complaints
    ADD COLUMN resolved BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN resolved_at TIMESTAMP,
    ADD COLUMN reporter_id INT REFERENCES tenants(person_id) ON DELETE SET NULL;

UPDATE complaints SET reporter_id = sender_id WHERE reporter_id IS NULL;

CREATE TABLE complaint_notifications (
    notification_id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    complaint_id      INT NOT NULL REFERENCES complaints(complaint_id) ON DELETE CASCADE,
    tenant_id         INT NOT NULL REFERENCES tenants(person_id) ON DELETE CASCADE,
    message           VARCHAR(1000) NOT NULL,
    date_posted       TIMESTAMP NOT NULL DEFAULT now(),
    is_read           BOOLEAN NOT NULL DEFAULT false
);

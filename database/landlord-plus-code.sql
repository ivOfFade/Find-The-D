-- Ask the database owner to run this once for the landlord feature.
-- Standard full Plus Codes contain 11 characters including +.
-- This only increases the column size; it preserves all existing rows.
ALTER TABLE dorms ALTER COLUMN plus_code TYPE VARCHAR(50);

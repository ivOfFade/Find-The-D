-- Find-The-D demo data. Run schema.sql first and run this seed once.
-- All demo accounts use the password: FindTheD123!

BEGIN;

-- Full Plus Codes require 11 characters, including the separator.
ALTER TABLE dorms ALTER COLUMN plus_code TYPE VARCHAR(16);

INSERT INTO persons (name, age, contact_num, email, password_hash, role)
VALUES
    ('Maria Santos', 42, '09170000001', 'maria.landlord@example.com',
     '$2a$12$iF1wY17C4eqmAIR95ABtW.mOlY6UcwHB3YF4ct0VPoSU8suScAUeC', 'LANDLORD'),
    ('Jose Reyes', 45, '09170000002', 'jose.landlord@example.com',
     '$2a$12$iF1wY17C4eqmAIR95ABtW.mOlY6UcwHB3YF4ct0VPoSU8suScAUeC', 'LANDLORD'),
    ('Lourdes Mendoza', 39, '09170000003', 'lourdes.landlord@example.com',
     '$2a$12$iF1wY17C4eqmAIR95ABtW.mOlY6UcwHB3YF4ct0VPoSU8suScAUeC', 'LANDLORD'),
    ('Ana Cruz', 20, '09170000004', 'ana.tenant@example.com',
     '$2a$12$iF1wY17C4eqmAIR95ABtW.mOlY6UcwHB3YF4ct0VPoSU8suScAUeC', 'TENANT'),
    ('Mark Garcia', 22, '09170000005', 'mark.tenant@example.com',
     '$2a$12$iF1wY17C4eqmAIR95ABtW.mOlY6UcwHB3YF4ct0VPoSU8suScAUeC', 'TENANT'),
    ('Noah Flores', 21, '09170000006', 'noah.tenant@example.com',
     '$2a$12$iF1wY17C4eqmAIR95ABtW.mOlY6UcwHB3YF4ct0VPoSU8suScAUeC', 'TENANT'),
    ('Leah Ramos', 19, '09170000007', 'leah.tenant@example.com',
     '$2a$12$iF1wY17C4eqmAIR95ABtW.mOlY6UcwHB3YF4ct0VPoSU8suScAUeC', 'TENANT'),
    ('Nico Lim', 23, '09170000008', 'nico.tenant@example.com',
     '$2a$12$iF1wY17C4eqmAIR95ABtW.mOlY6UcwHB3YF4ct0VPoSU8suScAUeC', 'TENANT'),
    ('Bea Dizon', 20, '09170000009', 'bea.tenant@example.com',
     '$2a$12$iF1wY17C4eqmAIR95ABtW.mOlY6UcwHB3YF4ct0VPoSU8suScAUeC', 'TENANT');

INSERT INTO landlords (person_id, other_contact)
SELECT person_id, CASE email
    WHEN 'maria.landlord@example.com' THEN 'facebook.com/maria.santos'
    WHEN 'jose.landlord@example.com' THEN 'facebook.com/jose.reyes'
    ELSE 'facebook.com/lourdes.mendoza'
END
FROM persons
WHERE email IN (
    'maria.landlord@example.com',
    'jose.landlord@example.com',
    'lourdes.landlord@example.com'
);

INSERT INTO tenants (person_id, current_dorm_id)
SELECT person_id, NULL
FROM persons
WHERE email IN (
    'ana.tenant@example.com',
    'mark.tenant@example.com',
    'noah.tenant@example.com',
    'leah.tenant@example.com',
    'nico.tenant@example.com',
    'bea.tenant@example.com'
);

INSERT INTO dorms (
    owner_id, dorm_name, location, plus_code, description,
    total_slots, available_slots, rate, rules, date_posted
)
SELECT
    owner.person_id,
    sample.dorm_name,
    sample.location,
    sample.plus_code,
    sample.description,
    sample.total_slots,
    sample.total_slots,
    sample.rate,
    sample.rules,
    CURRENT_TIMESTAMP - sample.posted_ago
FROM (VALUES
    ('maria.landlord@example.com', 'Sampaguita Dormitory', 'Batangas City, Batangas',
     '7Q53Q345+J8', 'Furnished shared rooms near schools, shops, and public transport.',
     6, 3500.00::NUMERIC, 'No smoking. Keep shared spaces clean.', INTERVAL '12 days'),
    ('jose.landlord@example.com', 'Mahogany Residence', 'Lipa City, Batangas',
     '7Q53W5R7+C6', 'Quiet student accommodation with a shared kitchen and study area.',
     5, 4200.00::NUMERIC, 'Visitors must register with management.', INTERVAL '9 days'),
    ('maria.landlord@example.com', 'Acacia Student House', 'Tanauan City, Batangas',
     '7Q6334MX+6W', 'Bright rooms with Wi-Fi and convenient access to local transport.',
     4, 5000.00::NUMERIC, 'Quiet hours are from 10 PM to 7 AM.', INTERVAL '5 days'),
    ('lourdes.landlord@example.com', 'Orchid Place', 'Malvar, Batangas',
     '7Q6325V5+MC', 'Small, clean residence with shared laundry and kitchen facilities.',
     3, 3000.00::NUMERIC, 'No overnight guests without prior approval.', INTERVAL '2 days')
) AS sample(owner_email, dorm_name, location, plus_code, description,
            total_slots, rate, rules, posted_ago)
JOIN persons AS owner ON owner.email = sample.owner_email;

-- Existing residents demonstrate occupied listings; Leah, Nico, and Bea have no dorm yet.
UPDATE tenants AS tenant
SET current_dorm_id = dorm.dorm_id
FROM persons AS account, persons AS owner, dorms AS dorm
WHERE tenant.person_id = account.person_id
  AND account.email = 'ana.tenant@example.com'
  AND owner.email = 'maria.landlord@example.com'
  AND dorm.owner_id = owner.person_id
  AND dorm.dorm_name = 'Sampaguita Dormitory';

UPDATE tenants AS tenant
SET current_dorm_id = dorm.dorm_id
FROM persons AS account, persons AS owner, dorms AS dorm
WHERE tenant.person_id = account.person_id
  AND account.email = 'mark.tenant@example.com'
  AND owner.email = 'maria.landlord@example.com'
  AND dorm.owner_id = owner.person_id
  AND dorm.dorm_name = 'Sampaguita Dormitory';

UPDATE tenants AS tenant
SET current_dorm_id = dorm.dorm_id
FROM persons AS account, persons AS owner, dorms AS dorm
WHERE tenant.person_id = account.person_id
  AND account.email = 'noah.tenant@example.com'
  AND owner.email = 'jose.landlord@example.com'
  AND dorm.owner_id = owner.person_id
  AND dorm.dorm_name = 'Mahogany Residence';

UPDATE dorms AS dorm
SET available_slots = dorm.total_slots - (
    SELECT COUNT(*)
    FROM tenants AS tenant
    WHERE tenant.current_dorm_id = dorm.dorm_id
);

INSERT INTO dorm_amenities (dorm_id, amenity)
SELECT dorm.dorm_id, sample.amenity
FROM (VALUES
    ('maria.landlord@example.com', 'Sampaguita Dormitory', 'Wi-Fi'),
    ('maria.landlord@example.com', 'Sampaguita Dormitory', 'Laundry'),
    ('maria.landlord@example.com', 'Sampaguita Dormitory', 'Kitchen'),
    ('jose.landlord@example.com', 'Mahogany Residence', 'Wi-Fi'),
    ('jose.landlord@example.com', 'Mahogany Residence', 'Air Conditioning'),
    ('jose.landlord@example.com', 'Mahogany Residence', 'Kitchen'),
    ('maria.landlord@example.com', 'Acacia Student House', 'Wi-Fi'),
    ('maria.landlord@example.com', 'Acacia Student House', 'Gym'),
    ('lourdes.landlord@example.com', 'Orchid Place', 'Laundry'),
    ('lourdes.landlord@example.com', 'Orchid Place', 'Kitchen')
) AS sample(owner_email, dorm_name, amenity)
JOIN persons AS owner ON owner.email = sample.owner_email
JOIN dorms AS dorm ON dorm.owner_id = owner.person_id
                  AND dorm.dorm_name = sample.dorm_name;

-- No dorm-photo assets are included in the repository; dorm_photos is intentionally empty.

INSERT INTO announcements (dorm_id, author_id, message, date_posted)
SELECT dorm.dorm_id, owner.person_id, sample.message, CURRENT_TIMESTAMP - sample.posted_ago
FROM (VALUES
    ('maria.landlord@example.com', 'Sampaguita Dormitory',
     'Water maintenance is scheduled for Saturday from 9 AM to 11 AM.', INTERVAL '1 day'),
    ('maria.landlord@example.com', 'Sampaguita Dormitory',
     'Please keep the kitchen clean after use. Thank you!', INTERVAL '3 days'),
    ('jose.landlord@example.com', 'Mahogany Residence',
     'The monthly residents meeting will be held in the study area on Friday.', INTERVAL '2 days'),
    ('maria.landlord@example.com', 'Acacia Student House',
     'Wi-Fi maintenance is complete. Please reconnect your devices.', INTERVAL '4 days'),
    ('lourdes.landlord@example.com', 'Orchid Place',
     'Laundry room hours are 7 AM to 9 PM daily.', INTERVAL '1 day')
) AS sample(owner_email, dorm_name, message, posted_ago)
JOIN persons AS owner ON owner.email = sample.owner_email
JOIN dorms AS dorm ON dorm.owner_id = owner.person_id
                  AND dorm.dorm_name = sample.dorm_name;

INSERT INTO establishments (
    establishment_name, type, plus_code, nearby_radius_meters
)
VALUES
    ('Batangas State University', 'SCHOOL', '7Q53Q345+J8', 1000),
    ('Batangas City Public Market', 'GROCERY', '7Q53Q345+J8', 800),
    ('Sampaguita Community Pharmacy', 'PHARMACY', '7Q53Q345+J8', 500),
    ('Lipa Central School', 'SCHOOL', '7Q53W5R7+C6', 1000),
    ('Mahogany Wash and Fold', 'LAUNDRY', '7Q53W5R7+C6', 700),
    ('Tanauan Family Grocery', 'GROCERY', '7Q6334MX+6W', 900),
    ('Orchid Convenience Store', 'STORE', '7Q6325V5+MC', 500),
    ('Malvar Community Center', 'OTHER', '7Q6325V5+MC', 1200);

INSERT INTO interested_dorms (tenant_id, dorm_id)
SELECT tenant.person_id, dorm.dorm_id
FROM (VALUES
    ('leah.tenant@example.com', 'maria.landlord@example.com', 'Acacia Student House'),
    ('nico.tenant@example.com', 'jose.landlord@example.com', 'Mahogany Residence'),
    ('bea.tenant@example.com', 'maria.landlord@example.com', 'Sampaguita Dormitory'),
    ('ana.tenant@example.com', 'maria.landlord@example.com', 'Acacia Student House')
) AS sample(tenant_email, owner_email, dorm_name)
JOIN persons AS tenant ON tenant.email = sample.tenant_email
JOIN persons AS owner ON owner.email = sample.owner_email
JOIN dorms AS dorm ON dorm.owner_id = owner.person_id
                  AND dorm.dorm_name = sample.dorm_name;

INSERT INTO applications (
    tenant_id, dorm_id, status, transfer_confirmed, date_submitted, date_reviewed
)
SELECT
    tenant.person_id,
    dorm.dorm_id,
    sample.status,
    sample.transfer_confirmed,
    CURRENT_TIMESTAMP - sample.submitted_ago,
    CASE
        WHEN sample.status = 'PENDING' THEN NULL
        ELSE CURRENT_TIMESTAMP - sample.reviewed_ago
    END
FROM (VALUES
    ('leah.tenant@example.com', 'jose.landlord@example.com', 'Mahogany Residence',
     'PENDING', false, INTERVAL '2 days', NULL::INTERVAL),
    ('ana.tenant@example.com', 'maria.landlord@example.com', 'Acacia Student House',
     'PENDING', true, INTERVAL '1 day', NULL::INTERVAL),
    ('bea.tenant@example.com', 'lourdes.landlord@example.com', 'Orchid Place',
     'PENDING', false, INTERVAL '4 hours', NULL::INTERVAL),
    ('mark.tenant@example.com', 'maria.landlord@example.com', 'Sampaguita Dormitory',
     'ACCEPTED', false, INTERVAL '12 days', INTERVAL '11 days'),
    ('noah.tenant@example.com', 'jose.landlord@example.com', 'Mahogany Residence',
     'ACCEPTED', false, INTERVAL '9 days', INTERVAL '8 days'),
    ('nico.tenant@example.com', 'maria.landlord@example.com', 'Acacia Student House',
     'REJECTED', false, INTERVAL '6 days', INTERVAL '5 days')
) AS sample(tenant_email, owner_email, dorm_name, status,
            transfer_confirmed, submitted_ago, reviewed_ago)
JOIN persons AS tenant ON tenant.email = sample.tenant_email
JOIN persons AS owner ON owner.email = sample.owner_email
JOIN dorms AS dorm ON dorm.owner_id = owner.person_id
                  AND dorm.dorm_name = sample.dorm_name;

INSERT INTO complaints (dorm_id, sender_id, message, date_posted, anonymous)
SELECT
    dorm.dorm_id,
    CASE WHEN sample.anonymous THEN NULL ELSE sender.person_id END,
    sample.message,
    CURRENT_TIMESTAMP - sample.posted_ago,
    sample.anonymous
FROM (VALUES
    ('maria.landlord@example.com', 'Sampaguita Dormitory',
     'ana.tenant@example.com', 'The hallway light on the second floor is not working.', false, INTERVAL '1 day'),
    ('maria.landlord@example.com', 'Sampaguita Dormitory',
     NULL, 'The shared kitchen sink is draining slowly.', true, INTERVAL '3 days'),
    ('jose.landlord@example.com', 'Mahogany Residence',
     'noah.tenant@example.com', 'Could the study area be available later in the evening?', false, INTERVAL '2 days')
) AS sample(owner_email, dorm_name, sender_email, message, anonymous, posted_ago)
JOIN persons AS owner ON owner.email = sample.owner_email
JOIN dorms AS dorm ON dorm.owner_id = owner.person_id
                  AND dorm.dorm_name = sample.dorm_name
LEFT JOIN persons AS sender ON sender.email = sample.sender_email;

COMMIT;

-- Find-The-D sample data
-- Run schema.sql first.
-- This seed is intended for a fresh database or empty tables.

BEGIN;

-- 1. PERSONS
-- IDs are generated automatically, so we retrieve them by email later.

INSERT INTO persons (name, age, contact_num, email, password_hash, role)
VALUES
    ('Maria Santos', 42, '09171234501', 'maria.landlord@example.com', 'seed_hash_maria', 'LANDLORD'),
    ('Jose Reyes', 45, '09171234502', 'jose.landlord@example.com', 'seed_hash_jose', 'LANDLORD'),
    ('Ana Cruz', 20, '09171234503', 'ana.tenant@example.com', 'seed_hash_ana', 'TENANT'),
    ('Mark Garcia', 22, '09171234504', 'mark.tenant@example.com', 'seed_hash_mark', 'TENANT'),
    ('Leah Flores', 19, '09171234505', 'leah.tenant@example.com', 'seed_hash_leah', 'TENANT');

-- 2. LANDLORDS
INSERT INTO landlords (person_id, other_contact)
SELECT person_id, 'maria.alt@example.com'
FROM persons WHERE email = 'maria.landlord@example.com';

INSERT INTO landlords (person_id, other_contact)
SELECT person_id, 'jose.alt@example.com'
FROM persons WHERE email = 'jose.landlord@example.com';

-- 3. TENANTS
INSERT INTO tenants (person_id, current_dorm_id)
SELECT person_id, NULL
FROM persons
WHERE email IN (
    'ana.tenant@example.com',
    'mark.tenant@example.com',
    'leah.tenant@example.com'
);

-- 4. DORMS
INSERT INTO dorms (
    owner_id, dorm_name, location, plus_code, description,
    total_slots, available_slots, rate, rules
)
SELECT
    p.person_id,
    'Sampaguita Dormitory',
    'Batangas City, Batangas',
    '6QX2+5M',
    'Affordable shared rooms near schools and public transport.',
    10, 6, 3500.00,
    'No smoking. Keep shared spaces clean.'
FROM persons p
WHERE p.email = 'maria.landlord@example.com';

INSERT INTO dorms (
    owner_id, dorm_name, location, plus_code, description,
    total_slots, available_slots, rate, rules
)
SELECT
    p.person_id,
    'Mahogany Residence',
    'Lipa City, Batangas',
    '6QX3+8P',
    'Quiet student accommodation with shared kitchen facilities.',
    8, 3, 4200.00,
    'Visitors must register with management.'
FROM persons p
WHERE p.email = 'jose.landlord@example.com';

-- 5. UPDATE TENANTS WITH THEIR CURRENT DORMS
-- Ana currently rents at Sampaguita Dormitory.

UPDATE tenants t
SET current_dorm_id = d.dorm_id
FROM persons p, dorms d
WHERE t.person_id = p.person_id
    AND p.email = 'ana.tenant@example.com'
    AND d.dorm_name = 'Sampaguita Dormitory';

-- 6. NEARBY ESTABLISHMENTS
INSERT INTO establishments (
    establishment_name, type, plus_code, nearby_radius_meters
)
VALUES
    ('Batangas State University', 'SCHOOL', '6QX2+7R', 1000),
    ('City Health Clinic', 'HEALTH', '6QX2+4W', 1500),
    ('Public Market', 'MARKET', '6QX2+6J', 800),
    ('Community Pharmacy', 'HEALTH', '6QX2+5V', 500);

COMMIT;
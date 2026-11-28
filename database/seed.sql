
BEGIN;

INSERT INTO persons (name, age, contact_num, email, password_hash, role)
VALUES
    ('Diluc Ragnvindr', 31, '09123456789', 'diluc@gmail.com',
     '$2a$12$rXc66q9UW7ptlQEaw8aLi.alMs3dZrFWan5x/r00TysBw1o3.hSFK', 'LANDLORD'),
    ('Raiden Ei', 28, '09123456789', 'raiden@gmail.com',
     '$2a$12$rXc66q9UW7ptlQEaw8aLi.alMs3dZrFWan5x/r00TysBw1o3.hSFK', 'LANDLORD'),
    ('Eula Lawrence', 30, '09123456789', 'eula2@gmail.com',
     '$2a$12$rXc66q9UW7ptlQEaw8aLi.alMs3dZrFWan5x/r00TysBw1o3.hSFK', 'LANDLORD'),
    ('Kaeya Alberich', 18, '09123456789', 'kaeya@gmail.com',
     '$2a$12$rXc66q9UW7ptlQEaw8aLi.alMs3dZrFWan5x/r00TysBw1o3.hSFK', 'TENANT');

INSERT INTO landlords (person_id)
SELECT person_id FROM persons
WHERE email IN ('diluc@gmail.com', 'raiden@gmail.com', 'eula2@gmail.com');

INSERT INTO tenants (person_id)
SELECT person_id FROM persons WHERE email = 'kaeya@gmail.com';

INSERT INTO dorms (
    owner_id, dorm_name, location, plus_code, description,
    total_slots, available_slots, rate, rules
)
SELECT owner.person_id, sample.dorm_name, sample.location, sample.plus_code,
       sample.description, sample.total_slots, sample.total_slots, sample.rate,
       sample.rules
FROM (VALUES
    ('diluc@gmail.com', 'Figlio Unico',
     'Universe 1, Golden Country Homes, Alangilan, Batangas',
     '7Q53Q3PF+2P', '1 minute walking distance to BSU Alangilan. 4 people in 1 room.',
     1, 3000.00::NUMERIC, NULL::TEXT),
    ('raiden@gmail.com', 'Inhinyero',
     'Neptune St., Golden Country Homes, Alangilan, Batangas',
     '7Q53Q3PC+3P4', 'Bedspace only.',
     3, 2500.00::NUMERIC, NULL::TEXT),
    ('eula2@gmail.com', 'Silk Residence',
     'Maria Clara St., Sta. Cruz Manila',
     '7Q62JX7P+RRW',
     'Studio type lofted unit. Okay for small families and is pet friendly. 1 slot, whole unit.',
     1, 9000.00::NUMERIC, 'Curfew: 10pm')
) AS sample(owner_email, dorm_name, location, plus_code, description,
            total_slots, rate, rules)
JOIN persons AS owner ON owner.email = sample.owner_email;

UPDATE tenants AS tenant
SET current_dorm_id = dorm.dorm_id
FROM persons AS account, persons AS owner, dorms AS dorm
WHERE tenant.person_id = account.person_id
  AND account.email = 'kaeya@gmail.com'
  AND owner.email = 'diluc@gmail.com'
  AND dorm.owner_id = owner.person_id
  AND dorm.dorm_name = 'Figlio Unico';

UPDATE dorms AS dorm
SET available_slots = dorm.total_slots - (
    SELECT COUNT(*) FROM tenants AS tenant
    WHERE tenant.current_dorm_id = dorm.dorm_id
)
WHERE dorm.owner_id = (SELECT person_id FROM persons WHERE email = 'diluc@gmail.com')
  AND dorm.dorm_name = 'Figlio Unico';

INSERT INTO dorm_amenities (dorm_id, amenity)
SELECT dorm.dorm_id, sample.amenity
FROM (VALUES
    ('diluc@gmail.com', 'Figlio Unico', 'Kitchen'),
    ('diluc@gmail.com', 'Figlio Unico', 'Study Area'),
    ('diluc@gmail.com', 'Figlio Unico', 'Wi-fi'),
    ('raiden@gmail.com', 'Inhinyero', 'Study Area'),
    ('raiden@gmail.com', 'Inhinyero', 'Wi-fi'),
    ('eula2@gmail.com', 'Silk Residence', 'Air Condition'),
    ('eula2@gmail.com', 'Silk Residence', 'Kitchen'),
    ('eula2@gmail.com', 'Silk Residence', 'Security')
) AS sample(owner_email, dorm_name, amenity)
JOIN persons AS owner ON owner.email = sample.owner_email
JOIN dorms AS dorm ON dorm.owner_id = owner.person_id
                  AND dorm.dorm_name = sample.dorm_name;

INSERT INTO dorm_photos (dorm_id, photo_url)
SELECT dorm.dorm_id, sample.photo_url
FROM (VALUES
    ('diluc@gmail.com', 'Figlio Unico', 'assets/dorms/Figlio.jpg'),
    ('diluc@gmail.com', 'Figlio Unico', 'assets/dorms/Figlio (2).jpg'),
    ('diluc@gmail.com', 'Figlio Unico', 'assets/dorms/Figlio (3).jpg'),
    ('diluc@gmail.com', 'Figlio Unico', 'assets/dorms/Figlio (4).jpg'),
    ('raiden@gmail.com', 'Inhinyero', 'assets/dorms/Inhinyero.jpg'),
    ('raiden@gmail.com', 'Inhinyero', 'assets/dorms/Inhinyero (2).jpg'),
    ('raiden@gmail.com', 'Inhinyero', 'assets/dorms/Inhinyero (3).jpg'),
    ('raiden@gmail.com', 'Inhinyero', 'assets/dorms/Inhinyero (4).jpg'),
    ('eula2@gmail.com', 'Silk Residence', 'assets/dorms/Silk Residence.jpg'),
    ('eula2@gmail.com', 'Silk Residence', 'assets/dorms/Silk Residence (2).jpg'),
    ('eula2@gmail.com', 'Silk Residence', 'assets/dorms/Silk Residence (3).jpg'),
    ('eula2@gmail.com', 'Silk Residence', 'assets/dorms/Silk Residence (4).jpg')
) AS sample(owner_email, dorm_name, photo_url)
JOIN persons AS owner ON owner.email = sample.owner_email
JOIN dorms AS dorm ON dorm.owner_id = owner.person_id
                  AND dorm.dorm_name = sample.dorm_name;

-- Closest Geoapify place per available type for each seeded dorm; distances are straight-line meters.
-- Source: Geoapify and OpenStreetMap contributors.
INSERT INTO dorm_nearby_establishments
    (dorm_id, establishment_name, type, plus_code, distance_meters)
SELECT dorm.dorm_id, sample.establishment_name, sample.type,
       sample.plus_code, sample.distance_meters
FROM (VALUES
    ('diluc@gmail.com', 'Figlio Unico', 'Batangas State University - Alangilan Campus', 'SCHOOL', '7Q53Q3MF+MMW99RQ', 93),
    ('diluc@gmail.com', 'Figlio Unico', 'Alfamart Golden Country Homes', 'STORE', '7Q53Q3PC+3X9PR33', 198),
    ('diluc@gmail.com', 'Figlio Unico', 'Gu Tum Mi', 'GROCERY', '7Q53Q3J9+R5WFHQC', 761),
    ('diluc@gmail.com', 'Figlio Unico', 'Watsons', 'PHARMACY', '7Q53Q3VC+3G37GVF', 903),
    ('raiden@gmail.com', 'Inhinyero', 'Alfamart Golden Country Homes', 'STORE', '7Q53Q3PC+3X9PR33', 71),
    ('raiden@gmail.com', 'Inhinyero', 'Casa del Bambino Emmanuel Montessori School', 'SCHOOL', '7Q53Q3PC+R9F74W7', 235),
    ('raiden@gmail.com', 'Inhinyero', 'Gu Tum Mi', 'GROCERY', '7Q53Q3J9+R5WFHQC', 537),
    ('raiden@gmail.com', 'Inhinyero', 'Watsons', 'PHARMACY', '7Q53Q3VC+3G37GVF', 835),
    ('eula2@gmail.com', 'Silk Residence', 'TGP', 'PHARMACY', '7Q62JX7Q+WH9RRGH', 205),
    ('eula2@gmail.com', 'Silk Residence', 'Uncle John''s', 'STORE', '7Q62JX8Q+CHX769G', 263),
    ('eula2@gmail.com', 'Silk Residence', 'Perpetual Help College of Manila', 'SCHOOL', '7Q62JX7Q+28Q4CJM', 265),
    ('eula2@gmail.com', 'Silk Residence', 'Relaxing Laundry', 'LAUNDRY', '7Q62JX9Q+5MVMGQ3', 438),
    ('eula2@gmail.com', 'Silk Residence', 'Famili-Juan General Merchandise', 'GROCERY', '7Q62JX6M+52MF4MG', 672)
) AS sample(owner_email, dorm_name, establishment_name, type, plus_code, distance_meters)
JOIN persons AS owner ON owner.email = sample.owner_email
JOIN dorms AS dorm ON dorm.owner_id = owner.person_id
                  AND dorm.dorm_name = sample.dorm_name;

UPDATE dorms AS dorm
SET nearby_search_completed = true
FROM persons AS owner
WHERE dorm.owner_id = owner.person_id
  AND (owner.email, dorm.dorm_name) IN (
      ('diluc@gmail.com', 'Figlio Unico'),
      ('raiden@gmail.com', 'Inhinyero'),
      ('eula2@gmail.com', 'Silk Residence')
  );

COMMIT;

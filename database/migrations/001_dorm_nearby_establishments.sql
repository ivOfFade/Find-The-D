ALTER TABLE dorms
    ADD COLUMN IF NOT EXISTS nearby_search_completed BOOLEAN NOT NULL DEFAULT false;

CREATE TABLE IF NOT EXISTS dorm_nearby_establishments (
    dorm_id             INT NOT NULL REFERENCES dorms(dorm_id) ON DELETE CASCADE,
    establishment_name  VARCHAR(100) NOT NULL,
    type                VARCHAR(10) NOT NULL
                            CHECK (type IN ('STORE', 'LAUNDRY', 'GROCERY', 'SCHOOL', 'PHARMACY', 'OTHER')),
    plus_code           VARCHAR(16) NOT NULL,
    distance_meters     INT NOT NULL
                            CHECK (distance_meters BETWEEN 0 AND 1000),
    PRIMARY KEY (dorm_id, establishment_name, type, plus_code)
);

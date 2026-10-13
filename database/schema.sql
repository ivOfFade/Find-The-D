
-- person
-- model (done)
CREATE TABLE persons (
    person_id     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    age           INT CHECK (age BETWEEN 16 AND 120),
    contact_num   VARCHAR(20)  NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE
                    CHECK (email = lower(email)),
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(10)  NOT NULL
                    CHECK (role IN ('TENANT', 'LANDLORD')),
    is_logged_in  BOOLEAN NOT NULL DEFAULT false
);

-- landlord inherits person
-- model
CREATE TABLE landlords (
    person_id     INT PRIMARY KEY
                    REFERENCES persons(person_id) ON DELETE CASCADE,
    other_contact VARCHAR(100)
);

-- for dorms
-- model
CREATE TABLE dorms (
    dorm_id      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    owner_id     INT NOT NULL
                    REFERENCES landlords(person_id) ON DELETE CASCADE,
    dorm_name    VARCHAR(150) NOT NULL,
    location     VARCHAR(255),
    plus_code    VARCHAR(10),
    description  TEXT,
    total_slots  INT NOT NULL CHECK (total_slots >= 1),
    available_slots INT NOT NULL CHECK (available_slots >= 0 AND available_slots <= total_slots),
    rate         NUMERIC(10,2) NOT NULL CHECK (rate > 0),
    rules        TEXT,
    date_posted  TIMESTAMP NOT NULL DEFAULT now()

    -- tenants (has own table)
    -- amenities    (has own table)
    -- photos       (has own table)
    -- interestedtenants (interested_dorms)
    -- announcements (has own table)
    -- complaints (has own table)
);

-- put to dorms model
CREATE TABLE dorm_amenities (
    dorm_id   INT NOT NULL REFERENCES dorms(dorm_id) ON DELETE CASCADE,
    amenity   VARCHAR(100) NOT NULL,
    PRIMARY KEY (dorm_id, amenity)
);

-- put to dorms model
CREATE TABLE dorm_photos (
    dorm_id   INT NOT NULL REFERENCES dorms(dorm_id) ON DELETE CASCADE,
    photo_url VARCHAR(255) NOT NULL,
    PRIMARY KEY (dorm_id, photo_url)
);

-- own model
CREATE TABLE announcements (
    announcement_id          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    dorm_id     INT NOT NULL REFERENCES dorms(dorm_id) ON DELETE CASCADE,
    author_id   INT NOT NULL REFERENCES landlords(person_id),
    message     VARCHAR(1000) NOT NULL
                CHECK (length(trim(message)) > 0),
    date_posted TIMESTAMP NOT NULL DEFAULT now()
);

-- own model
CREATE TABLE establishments (
    establishment_id      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    establishment_name    VARCHAR(100),
    type                  VARCHAR(10), --has types
    plus_code             VARCHAR(50),
    nearby_radius_meters INT NOT NULL DEFAULT 0
);

-- for tenants
-- own model (done)
CREATE TABLE tenants (
    person_id       INT PRIMARY KEY
                    REFERENCES persons(person_id) ON DELETE CASCADE,
    current_dorm_id INT
                    REFERENCES dorms(dorm_id) ON DELETE SET NULL
);

-- stick to tenants model
CREATE TABLE interested_dorms (
    tenant_id INT NOT NULL REFERENCES tenants(person_id) ON DELETE CASCADE,
    dorm_id   INT NOT NULL REFERENCES dorms(dorm_id)          ON DELETE CASCADE,
    PRIMARY KEY (tenant_id, dorm_id)
);

-- own model
CREATE TABLE applications (
    application_id     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenant_id          INT NOT NULL REFERENCES tenants(person_id) ON DELETE CASCADE,
    dorm_id            INT NOT NULL REFERENCES dorms(dorm_id)          ON DELETE CASCADE,
    status             VARCHAR(10) NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),
    transfer_confirmed BOOLEAN NOT NULL DEFAULT false,
    date_submitted     TIMESTAMP NOT NULL DEFAULT now(),
    date_reviewed      TIMESTAMP
);

-- own model (done)
CREATE TABLE complaints (
    complaint_id      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    dorm_id           INT NOT NULL REFERENCES dorms(dorm_id) ON DELETE CASCADE,
    sender_id         INT REFERENCES tenants(person_id) ON DELETE SET NULL,
    message           VARCHAR(1000) NOT NULL
                        CHECK (length(trim(message)) > 0),
    date_posted       TIMESTAMP NOT NULL DEFAULT now(),
    anonymous         BOOLEAN NOT NULL DEFAULT false
                        CHECK (NOT anonymous OR sender_id IS NULL)
);
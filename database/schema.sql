DROP TABLE IF EXISTS applications, complaints, announcements, dorm_interests, tenants, dorms, landlords, persons CASCADE;

CREATE TABLE persons (
    id            INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    age           INT CHECK (age BETWEEN 16 AND 120),
    contact_num   VARCHAR(20)  NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE
                    CHECK (email = lower(email)),
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(10)  NOT NULL
                    CHECK (role IN ('TENANT', 'LANDLORD'))
);

CREATE TABLE landlords (
    person_id     INT PRIMARY KEY
                    REFERENCES persons(id) ON DELETE CASCADE,
    other_contact VARCHAR(100)
);

CREATE TABLE dorms (
    id           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    owner_id     INT NOT NULL
                    REFERENCES landlords(person_id) ON DELETE CASCADE,
    dorm_name    VARCHAR(150) NOT NULL,
    location     VARCHAR(255),
    description  TEXT,
    total_slots  INT CHECK (total_slots >= 1),
    rate         NUMERIC(10,2) CHECK (rate > 0),
    rules        TEXT,
    date_posted  TIMESTAMP NOT NULL DEFAULT now(),
    amenities    TEXT[] NOT NULL DEFAULT '{}',
    photos       TEXT[] NOT NULL DEFAULT '{}'
);

CREATE TABLE tenants (
    person_id       INT PRIMARY KEY
                    REFERENCES persons(id) ON DELETE CASCADE,
    current_dorm_id INT
                    REFERENCES dorms(id) ON DELETE SET NULL
);

CREATE TABLE dorm_interests (
    tenant_id INT NOT NULL REFERENCES tenants(person_id) ON DELETE CASCADE,
    dorm_id   INT NOT NULL REFERENCES dorms(id)          ON DELETE CASCADE,
    PRIMARY KEY (tenant_id, dorm_id)
);

CREATE TABLE announcements (
    id          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    dorm_id     INT NOT NULL REFERENCES dorms(id) ON DELETE CASCADE,
    author_id   INT NOT NULL REFERENCES landlords(person_id),
    message     VARCHAR(1000) NOT NULL
                CHECK (length(trim(message)) > 0),
    date_posted TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE complaints (
    id          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    dorm_id     INT NOT NULL REFERENCES dorms(id) ON DELETE CASCADE,
    sender_id   INT REFERENCES tenants(person_id) ON DELETE SET NULL,
    message     VARCHAR(1000) NOT NULL
                CHECK (length(trim(message)) > 0),
    date_posted TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE applications (
    id                 INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenant_id          INT NOT NULL REFERENCES tenants(person_id) ON DELETE CASCADE,
    dorm_id            INT NOT NULL REFERENCES dorms(id)          ON DELETE CASCADE,
    status             VARCHAR(10) NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),
    transfer_confirmed BOOLEAN NOT NULL DEFAULT false,
    date_submitted     TIMESTAMP NOT NULL DEFAULT now(),
    date_reviewed      TIMESTAMP
);


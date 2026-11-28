# Database setup

## Requirements
- PostgreSQL installed locally (psql available in your terminal)

## First-time setup
1. Create the database:
   psql -U postgres -c "CREATE DATABASE findtheddb;"

2. From the repository root, run the schema:
   psql -U postgres -d findtheddb -v ON_ERROR_STOP=1 -1 -f database/schema.sql

3. Load the sample accounts and project data once:
   psql -U postgres -d findtheddb -v ON_ERROR_STOP=1 -f database/seed.sql

4. Verify the tables:
   psql -U postgres -d findtheddb -c "\dt"

## Existing databases
Run each migration once on databases created before its feature was added.
Apply them in order:

   psql -U postgres -d findtheddb -v ON_ERROR_STOP=1 -1 -f database/migrations/001_dorm_nearby_establishments.sql

   psql -U postgres -d findtheddb -v ON_ERROR_STOP=1 -1 -f database/migrations/20261128_complaint_resolution.sql

The complaint-resolution migration adds complaint status and the tenant
notification table. The application expects these tables and columns to exist.

When a landlord posts a dorm with a Plus Code, the application queries
Geoapify's hosted Places API (using OpenStreetMap data) before saving the
listing, then stores nearby places and their straight-line distances in
`dorm_nearby_establishments`. The free Geoapify plan currently allows up to
3,000 requests per day; check its current limits and terms before use. If the
lookup fails, the listing is not saved; the landlord can retry when the service
is available. Existing listings are not backfilled.

## Connection
JDBC URL: jdbc:postgresql://localhost:5432/findtheddb
Use your own local Postgres username and password. Do not commit them.
For the Java application, create a `.env` file in the repository root with:

```text
DB_URL=jdbc:postgresql://localhost:5432/findtheddb
DB_USER=your_postgres_username
DB_PASSWORD=your_postgres_password
GEOAPIFY_API_KEY=your_geoapify_api_key
```

Create a Geoapify account at https://myprojects.geoapify.com/, create a project,
and copy its API key into `GEOAPIFY_API_KEY`. The key is required to post a dorm
with a Plus Code. Keep `.env` local; do not commit credentials or API keys.
The results UI includes Geoapify and OpenStreetMap attribution links.

## Resetting
To start over, drop and recreate the database:
   psql -U postgres -c "DROP DATABASE findtheddb;"
   psql -U postgres -c "CREATE DATABASE findtheddb;"
then run step 2 again.

## Tables
See `database/schema.sql` for the complete schema, including the related dorm,
amenity, photo, application, complaint, announcement, and nearby-establishment
tables.

## Adding values
All sample accounts use the password `FindTheD123!`. Examples:

- Landlord: `maria.landlord@example.com`
- Tenant: `ana.tenant@example.com`

The seed is for a fresh database and should be run once; it does not clear or
overwrite existing project data.

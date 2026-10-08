# Database setup

## Requirements
- PostgreSQL installed locally (psql available in your terminal)

## First-time setup
1. Create the database:
   psql -U postgres -c "CREATE DATABASE findtheddb;"

2. Run the schema (from this folder):
   psql -U postgres -d findtheddb -v ON_ERROR_STOP=1 -1 -f schema.sql

3. Verify (should list 8 tables):
   psql -U postgres -d findtheddb -c "\dt"

## Connection
JDBC URL: jdbc:postgresql://localhost:5432/findtheddb
Use your own local Postgres username and password. Do not commit them.

## Resetting
To start over, drop and recreate the database:
   psql -U postgres -c "DROP DATABASE findtheddb;"
   psql -U postgres -c "CREATE DATABASE findtheddb;"
then run step 2 again.

## Tables
persons, landlords, tenants, dorms, dorm_interests,
announcements, complaints, applications
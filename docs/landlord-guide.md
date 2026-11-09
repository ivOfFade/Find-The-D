# Simple landlord backend guide

The project uses plain Java and JDBC, without Maven. The landlord service classes are concrete classes: no separate service interfaces, sample services, or JDBC wrappers are needed. Tests and stubs are not included, following the team's latest decision.

The landlord implementation is present, but the complete project currently has compilation errors in teammate-owned files. The user chose to leave those files' behavior unchanged. This guide does not claim the complete project builds, connects to PostgreSQL, or has passed live verification.

## Folder layout

```text
src/main/
    Main.java
    model/          Account, dorm, application, and screen-data classes
    enums/          Role, status, dashboard, and establishment categories
    exception/      Named validation/access errors
    util/           Existing validation, hashing, environment, database helpers
    repository/     Existing repositories plus LandlordServiceRepository
    service/        Landlord operations and OpenStreetMap lookup
    ui/             Existing team screens
lib/                Required JAR files
database/           Schema, seed, and the Plus Code column change
docs/service-api.md  Exact service signatures and exceptions
build.ps1           Compiles src/main to out using lib/*
```

Packages now match these folders: for example, `package model;` and `import model.Dorm;`. The old `java/com/ivofade/findthed` path and package prefix are removed. The existing team UI and unrelated backend files were relocated with their package/import references updated; the landlord work does not wire new UI screens.

## Reading order

1. Read `docs/service-api.md` for the methods the frontend calls.
2. Read `src/main/model/DormDetails.java`, then Dorm/Application/Announcement, for the existing validation and business rules.
3. Read `src/main/service/ListingService.java` for a basic database operation.
4. Read DashboardService, AnnouncementService, and LandlordComplaintService.
5. Read ApplicationReviewService for the placement/transfer transaction.
6. Read `src/main/repository/LandlordServiceRepository.java` for the SQL used by these services.
7. Read PlusCode, Establishment, and NearbyEstablishmentService for location and distance.

## How the files work together

```text
Screen -> service -> model rules and repository -> PostgreSQL
```

A model represents a person, dorm, or application and applies its rules. A service completes a screen operation, such as accepting an application. A repository performs SQL. Keeping these jobs separate means a screen can call `publish` without knowing how to insert an announcement row.

The services use the existing `util.DatabaseConnection` directly. They share one new LandlordServiceRepository for landlord-specific SQL. The existing repositories remain available for the other group members.

## Issue [10]: public service methods

`docs/service-api.md` documents each operation's inputs, output, and checked exceptions. The five service classes are ListingService, DashboardService, AnnouncementService, LandlordComplaintService, and ApplicationReviewService, all in `src/main/service/`.

The original issue requested stubs. The team's latest instruction removes that requirement, so there are no fake-data implementations. Database-backed landlord operations need PostgreSQL; nearby lookup needs internet. There is no claim that these services work offline.

## Issue [11]: listing management and dashboard

Create `new ListingService(landlord)` with the saved Landlord returned by login. The instance remembers that account for update/remove, whose signatures have no landlord parameter. A new login needs a new ListingService.

| Method | What it does |
|---|---|
| getMyDorms | Returns only the current landlord's listings |
| postDorm | Creates a validated Dorm and saves the listing, amenities, and photos together |
| updateDorm | Reloads the owned dorm and actual residents, calls updateDetails, and saves the result |
| removeDorm | Rejects occupied listings and removes an empty owned listing |
| DashboardService.getSummary | Returns owner-only dorm/slot totals and pending application count |

The key ownership condition is `WHERE dorm_id = ? AND owner_id = ?`. A supplied dorm ID is not enough to grant access. Nonexistent/nonowned management IDs produce the same access error. Services receive a saved landlord; the existing login flow, rather than this ID check, authenticates the password.

`src/main/service/LandlordAccess.java` is a small shared helper. `requireSavedLandlord` rejects a null account or an ID that has not been assigned. `requireSameLandlord` also checks that the supplied account matches the account remembered by ListingService. It does not check passwords or create a new login system.

Dorm's resident-loading constructor restores the current residents and their currentDorm references. Availability is calculated from actual residents. `updateDetails` rejects a new totalSlots value below that count. For example, four residents cannot fit into three total slots.

`src/main/model/DashboardSummary.java` holds dorm count, total slots, occupied slots, available slots, and pending application count. Its getters provide values for dashboard labels.

## Issue [12]: communication and application decisions

| Class/method | What it does |
|---|---|
| ApplicationReviewService.reviewApplications | Verifies ownership and lists pending applications only |
| ApplicationReviewService.acceptApplication | Reloads and locks the relevant records, calls Application.accept, and saves the placement/confirmed transfer |
| ApplicationReviewService.rejectApplication | Saves rejection and review date without changing current residency |
| AnnouncementService.publish | Reloads the owned dorm, creates a validated Announcement, and saves its ID/date |
| LandlordComplaintService.getComplaints | Returns a read-only list of sender-free ComplaintView objects for an owned dorm |

The existing `Application.accept` method checks pending status, ownership, free slots, and transfer confirmation. It calls `Dorm.addTenant`, assigns the tenant's current dorm, and removes their previous residency when transferring. The service persists the result; it does not copy those business rules into a second implementation.

Acceptance saves the tenant's current dorm, target dorm availability, previous dorm availability when applicable, accepted status/date, and accepted-interest removal together. Related records are locked while the operation runs so simultaneous requests cannot take the same final slot. Dorm locks use a consistent ID order for transfers.

A transaction means all these writes form one operation. `setAutoCommit(false)` starts explicit transaction handling; `commit()` saves all writes after success; `rollback()` undoes them if an operation fails. The service passes the error to the caller so the screen can show a message and avoid claiming the change was saved.

`src/main/model/ComplaintView.java` holds only ID, message, and date. The landlord query does not retrieve sender identity, even for nonanonymous complaints. Returning this small object prevents the landlord screen from obtaining an account through a raw Complaint. There are no landlord complaint editing, replying, or deleting methods.

Interest notifications are left out because approval in [01] was not supplied. Existing interest records are not turned into notification messages.

### LandlordServiceRepository methods

All methods below take the service's current Connection. They do not open a second connection or commit the service's transaction.

| Methods | Purpose |
|---|---|
| findMyDorms / findOwnedDorm | Read owner-filtered listings; findOwnedDorm can lock a row before a change |
| findPreviousDorm | Restore the applicant's existing dorm while performing a confirmed move |
| insertDorm / updateDorm / deleteDorm | Save listing details and child rows, or remove an owned empty dorm |
| getSummary | Count the owner's listings, slots, residents, and pending applications |
| findPendingApplications / findOwnedApplication | List pending owned applications or reload one owned application, optionally with a row lock |
| saveDecision / removeAcceptedInterest | Save the decision/date and remove the accepted dorm from that tenant's interest records |
| insertAnnouncement / findComplaints | Save an owner-authorized announcement or read sender-free complaint fields |
| lockTenantCurrentDorm / lockDorms | Read current residency under a lock and lock affected dorms in a consistent order |
| updateCurrentDorm / refreshAvailability | Save a successful placement and calculate free slots from actual residents |

`ApplicationReviewService.loadForReview` first locates the owned application, locks its tenant and dorms, and restores the previous resident object when the tenant already has a dorm. It then reloads and locks the application so status/capacity are current after any wait. The acceptance and rejection methods both use this helper.

Write operations call rollback on failure and pass the error to the caller. If the connection also fails during rollback, the caller receives that SQLException.

## OpenStreetMap and straight-line distance

The owner supplies the dorm's full Plus Code, for example `7Q63J3JQ+22`. Use the actual dorm code; a short code such as `J3JQ+22` lacks the location prefix and is not accepted.

1. `src/main/model/PlusCode.java` validates a full Open Location Code and decodes its center latitude/longitude locally.
2. `src/main/service/NearbyEstablishmentService.java` sends one Overpass request using a 1000-meter search radius.
3. Overpass searches OpenStreetMap's supported shop/amenity tags. Gson reads the JSON response.
4. The service turns supported points/building centers into Establishment objects, filters nearby results, removes duplicate OSM type/ID entries, and sorts nearest first.
5. `src/main/model/Establishment.java` uses haversine to calculate approximate straight-line meters. The overloads accept either a Dorm or a PlusCode.

This is a center-to-center distance across the Earth's surface. It does not follow roads or calculate walking/driving time. An OSM building center may differ from its entrance. OSM coverage can be incomplete, and separate mapped objects for the same business can still appear separately.

Supported categories are convenience stores, groceries/supermarkets, laundries, pharmacies, and schools/colleges/universities. Search results are temporary objects with ID 0, not saved database rows. A successful empty list is different from a network failure: failures throw IOException. Public requests have timeouts and no automatic retry loop.

No Google API key, Google Cloud account, or Google billing is required. The Google Open Location Code library only handles the location encoding locally. Gson is only a JSON reader. Display `© OpenStreetMap contributors` with a link to https://www.openstreetmap.org/copyright beside live results.

## Database setup for these services

Run from the project root so the existing environment loader can find `.env`. Supply your own values locally:

```dotenv
DB_URL=jdbc:postgresql://localhost:5432/findtheddb
DB_USER=your_postgresql_user
DB_PASSWORD=your_postgresql_password
```

Do not commit real credentials. Existing environment variables can also supply these settings; the current Env helper reads `.env` values first.

The original schema's `dorms.plus_code` limit is too short for a standard full code. The database owner should run the supplied `database/landlord-plus-code.sql` against an existing database. It widens the column to 50 characters without deleting rows; schema.sql itself is unchanged.

The existing seed file contains short placeholder Plus Codes. Replace them with actual full codes before using those seeded dorms through the models. The program cannot infer the missing prefix from a short code without a reference location, and it does not invent one from address text.

## Compile and run

Use an installed JDK (Java 17 or newer) and the JARs in `lib/`. From the project root:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

The script attempts to compile all Java files under src/main into out and reports compilation failure. It is a compilation check, not an automated test suite, and it does not prove the database is configured or reachable. Resolve the teammate blockers below before expecting a complete application build.

### Current teammate compilation blockers

These were left unchanged after the user explicitly chose to preserve teammate-owned compilation errors:

- `src/main/model/Tenant.java`: a duplicate empty Tenant declaration appears before the imports and the complete class.
- `src/main/model/Application.java` and `src/main/repository/ApplicationRepository.java`: the repository calls `assignApplicationId`, which the model does not define; the ID is currently final.
- `src/main/model/Complaint.java` and `src/main/repository/ComplaintRepository.java`: the repository calls nonexistent `assignComplaintId` and `getSender` methods; the ID is currently final.

The landlord-specific SQL uses LandlordServiceRepository rather than these broken mappers. That does not remove the errors when compiling every project source file. The owning teammates need to reconcile their model and repository APIs before a full build can succeed. Additional compiler errors may appear after the initial blockers are resolved.

### Nearby CLI after compilation is available

After the required classes can be compiled, the nearby CLI takes a full code and needs internet, without a database:

```powershell
java -cp "out;lib/*" service.NearbyEstablishmentService "7Q63J3JQ+22"
```

If Java reports a PKIX/certificate-trust error on this Windows computer, use the Windows trust store for that Java process:

```powershell
java '-Djavax.net.ssl.trustStoreType=Windows-ROOT' '-Djavax.net.ssl.trustStore=NONE' -cp "out;lib/*" service.NearbyEstablishmentService "7Q63J3JQ+22"
```

These options keep TLS verification enabled. They do not change installed certificates or disable certificate checks. No global JVM setting is changed.

The backend provides callable services, not new screen wiring. Frontend code should keep database/HTTP calls off the Swing event thread and refresh displayed data after successful changes.

## Local feature branches

OpenStreetMap was implemented first on `feature/openstreetmap-nearby`, followed
by the source move. `feature/landlord-service-stubs` contains the revised API
documentation (the old branch name is retained, but there are no stubs).
`feature/backend-landlord-listing` adds listings/dashboard, and
`feature/backend-landlord-communication` adds application decisions,
announcements, and complaint views. Each later branch includes its dependencies.

The communication branch contains the complete written implementation. Nothing
has been pushed or merged into develop. The preserved compilation blockers and
database setup must be resolved before database behavior can be verified and
Neil can review the features as ready to merge. Delete a feature branch only
after the corresponding reviewed merge.

The attempted full compile reported eight import-position errors in
`src/main/model/Tenant.java`, caused by the duplicate declaration before its
imports. The Application/Complaint API mismatches above are also visible in the
preserved source, but compilation stops at the Tenant parsing errors first.
No tests or stubs were created or run.

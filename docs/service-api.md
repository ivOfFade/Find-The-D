# Landlord service API

The landlord services are ordinary Java classes in `src/main/service/`, with package `service`. They call PostgreSQL directly through `DatabaseConnection` and `LandlordServiceRepository`. There are no sample services, stubs, or separate JDBC service classes.

The methods below describe the implemented landlord code. The complete project still has compilation errors in teammate-owned files, which were preserved at the user's request. These additions do not mean the full application currently builds or has been verified against a running database. See the blocker list in `docs/landlord-guide.md`.

## Who calls what

The frontend calls a service. The service uses model methods for the business rules and repository methods for SQL. The frontend does not need to write SQL.

```text
Landlord screen -> service -> model rules and repository -> PostgreSQL
```

Use the saved `Landlord` from the existing login flow. An account ID by itself is not authentication; the existing login flow remains responsible for checking the password.

## Listing management

Construct one listing service for the signed-in landlord:

```java
ListingService listings = new ListingService(landlord);
```

The constructor throws `UnauthorizedActionException` if the landlord is null or has no saved ID. Create another service when a different landlord signs in. The current landlord is needed because update/remove take a dorm ID without a landlord argument.

```java
List<Dorm> getMyDorms(Landlord landlord)
    throws SQLException, InvalidInputException, UnauthorizedActionException;

Dorm postDorm(Landlord landlord, DormDetails details)
    throws SQLException, InvalidInputException, UnauthorizedActionException;

Dorm updateDorm(int dormId, DormDetails details)
    throws SQLException, InvalidInputException, UnauthorizedActionException;

void removeDorm(int dormId)
    throws SQLException, InvalidInputException, UnauthorizedActionException;
```

`getMyDorms` returns only the current owner's listings. `postDorm` validates and saves a new listing, including amenities/photos. `updateDorm` reloads the owned listing and its actual residents before applying details; total slots cannot become smaller than the resident count. `removeDorm` rejects occupied listings.

## Dashboard

```java
DashboardSummary DashboardService.getSummary(Landlord landlord)
    throws SQLException, InvalidInputException, UnauthorizedActionException;
```

Create the service with `new DashboardService()`. The result contains dorm count, total slots, occupied slots, available slots, and pending application count for that owner only.

## Announcements

```java
Announcement AnnouncementService.publish(Landlord landlord, Dorm dorm, String message)
    throws SQLException, InvalidInputException, UnauthorizedActionException;
```

Create the service with `new AnnouncementService()`. It reloads the owned dorm and uses the Announcement model's message rules: required, at most 1000 characters. The returned saved object contains its generated ID and posting date.

## Landlord complaint viewing

```java
List<ComplaintView> LandlordComplaintService.getComplaints(Landlord landlord, Dorm dorm)
    throws SQLException, InvalidInputException, UnauthorizedActionException;
```

Create the service with `new LandlordComplaintService()`. Only the owner may read the dorm's complaints. `ComplaintView` contains complaint ID, message, and posting date. It has no sender, account, or dorm reference. The query does not select the sender's identity, even for nonanonymous complaints. The returned list is read-only; this service has no edit, reply, or delete operation.

## Application review

```java
List<Application> ApplicationReviewService.reviewApplications(Landlord landlord, Dorm dorm)
    throws SQLException, InvalidInputException, UnauthorizedActionException;

Application ApplicationReviewService.acceptApplication(Landlord landlord, int applicationId)
    throws SQLException, InvalidInputException, UnauthorizedActionException, NoAvailableSlotsException;

Application ApplicationReviewService.rejectApplication(Landlord landlord, int applicationId)
    throws SQLException, InvalidInputException, UnauthorizedActionException;
```

Create the service with `new ApplicationReviewService()`. Review lists contain pending applications for owned dorms only. Acceptance needs a free slot and uses `Application.accept`, which calls `Dorm.addTenant` and assigns the current dorm. A move from another dorm requires transfer confirmation. Residency, both affected slot counts, interest removal, and the decision are saved in one transaction. Rejection saves the decision/date without moving the tenant. Already-decided applications cannot be reviewed again.

## Errors and ownership

| Exception | Meaning for the screen |
|---|---|
| InvalidInputException | Show the validation/business-rule message |
| UnauthorizedActionException | The account or requested dorm/application cannot be used for this operation |
| NoAvailableSlotsException | Acceptance failed because the target dorm is full |
| SQLException | A database read/write failed; do not report the change as saved |

Owner checks use SQL filtered by both dorm ID and owner ID. Missing/nonowned management IDs produce an access error. A Dorm received from a screen is used to identify the requested record; the service reloads the owned data instead of trusting its contents. Returned lists protect list membership; Dorm/Application objects themselves remain model objects. Refresh the screen after a successful write.

Interest notifications are not implemented because approval in [01] has not been supplied.

## OpenStreetMap nearby search

`src/main/service/NearbyEstablishmentService.java` is separate from database saving. It needs internet access but no Google Cloud account, billing setup, or API key.

```java
List<Establishment> findNearby(Dorm dorm)
    throws InvalidInputException, IOException, InterruptedException;

List<Establishment> findNearby(String fullPlusCode)
    throws InvalidInputException, IOException, InterruptedException;

double Establishment.distanceFrom(Dorm dorm) throws InvalidInputException;
double Establishment.distanceFrom(PlusCode location) throws InvalidInputException;
```

Create the service with `new NearbyEstablishmentService()`. The landlord enters a full Plus Code. The service decodes it locally, searches supported OSM establishments within 1000 meters, and returns nearby results. The existing haversine calculation supplies approximate center-to-center straight-line meters. Roads, walking routes, and travel times are not calculated.

```java
String code = "7Q63J3JQ+22"; // Replace with the dorm's actual full Plus Code.
PlusCode location = new PlusCode(code);
List<Establishment> places = new NearbyEstablishmentService().findNearby(code);
for (Establishment place : places) {
    System.out.printf("%s | %s | %.0f m straight line%n",
        place.getName(), place.getType(), place.distanceFrom(location));
}
```

These results are transient Establishment objects with ID 0. They are not inserted into the database. Supported categories are convenience stores, groceries/supermarkets, laundries, pharmacies, and schools/colleges/universities.

Missing/invalid location produces `InvalidInputException`. HTTP failures, malformed/incomplete responses, and timeouts produce `IOException`; a successful empty list means no supported mapped establishments were found. `InterruptedException` means the waiting operation was interrupted. If a UI catches it, restore the interrupt flag with `Thread.currentThread().interrupt()`.

The lookup has timeouts and no automatic repeated retries. The frontend should run the HTTP call off the UI thread, keep the results while the screen is open, and provide an explicit refresh. A failed nearby lookup must not prevent posting a dorm.

Display `© OpenStreetMap contributors` beside live results and link it to [OpenStreetMap copyright](https://www.openstreetmap.org/copyright).

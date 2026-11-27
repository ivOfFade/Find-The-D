# Find The D — Presentation Script

**Target runtime: about 9 minutes 40 seconds, including prepared demo actions.** This is under the 10-minute limit. Code citations, presenter notes, and the verification appendices are not spoken. Prepare the application and data before presenting; do not pause to type credentials or create records live.

## 1. Project introduction
**0:00–0:30 | Dylan**

**Show:** Project title and program-flow diagram.

“Good day, everyone. We are Dylan, Arriane, Rizialyn, and Neil, and our project is Find The D, a dorm-finding and management system.

Tenants can browse dorms and apply. Landlords can review applications, manage tenants, and post announcements. We will explain how we designed the project, then demonstrate the main tenant and landlord workflows.”

**Show:** Choose role → Sign in → Browse and apply → Landlord reviews → Tenant accesses My Dorm

## 2. Program structure
**0:30–0:40 | Arriane**

**Show code:** [`FindTheDUI.java`](../src/ui/FindTheDUI.java#L22) — folder `src/ui`; the `CardLayout` field is at line 22 and the root panel is at line 24.

```java
private final CardLayout screens = new CardLayout();
private final JPanel root = new JPanel(screens);
```

“The main window uses `CardLayout` to switch screens. The interface calls services for application actions; services coordinate business rules and database access.”

# Four object-oriented programming principles

## 3. Encapsulation
**0:40–1:30 | Dylan**

**Plain-language definition:** Encapsulation protects an object’s state and controls how other code can change it.

**Show code:** [`Person.java`](../src/model/Person.java#L15) — folder `src/model`, field at line 15; setter starts at line 133.

```java
private int age;

public final void setAge(int age) throws InvalidInputException {
    this.age = Validator.age(age);
}
```

**Optional second example:** [`Dorm.java`](../src/model/Dorm.java#L278) — folder `src/model`, `getAmenities` starts at line 278.

```java
public List<String> getAmenities() {
    return Collections.unmodifiableList(new ArrayList<>(amenities));
}
```

“`Person.age` is private, and `Person.setAge` validates an age before storing it. `Dorm.getAmenities` returns a defensive copy that callers cannot modify. The model, not the screen, controls these rules.

Without encapsulation, a screen could bypass validation or alter a dorm’s internal collection, causing invalid or inconsistent state. We will show the age-validation edge case in section 17 and complaint behavior in section 16.”

## 4. Inheritance
**1:30–2:10 | Rizialyn**

**Plain-language definition:** Inheritance lets a specialized class reuse common state and behavior from a more general class.

**Show code sources:**
- [`Tenant.java`](../src/model/Tenant.java#L12) — folder `src/model`, class begins at line 12.
- [`Landlord.java`](../src/model/Landlord.java#L12) — folder `src/model`, class begins at line 12.
- Shared parent: [`Person.java`](../src/model/Person.java#L10) — folder `src/model`, class begins at line 10.

```java
public class Tenant extends Person {
  private Dorm currentDorm;
  private final ArrayList<Application> applications = new ArrayList<>();
}
```

```java
public class Landlord extends Person {
  private String otherContact;
  private final ArrayList<Dorm> handledDorms = new ArrayList<>();
}
```

“Both roles inherit shared account features from `Person`, but each adds its own data. A `Tenant` tracks a current dorm and applications; a `Landlord` tracks landlord contact information and managed dorms.

Without inheritance, we would duplicate common account fields and validation in both classes, making fixes easier to miss or implement differently. You will see the two roles in the sign-in and landlord-dashboard demonstrations.”

## 5. Abstraction
**2:10–2:55 | Arriane**

**Plain-language definition:** Abstraction exposes the common idea a caller needs while leaving implementation details to the appropriate class.

**Show code:** [`Person.java`](../src/model/Person.java#L10) — folder `src/model`, abstract class starts at line 10; abstract method is at line 84.

```java
public abstract class Person {
```

```java
public abstract DashboardType getDashboardType();
```

“`Person` is abstract because it represents what tenants and landlords share, not a complete account role by itself. Its `getDashboardType` declaration requires each concrete role to provide a result.

The authentication service also stores the signed-in account using the common `Person` type. Without this shared abstraction, code handling accounts would need to branch on concrete role classes everywhere. We will see both roles sign in and use their separate workflows in sections 9 and 13.”

**Presenter note:** The `Tenant` and `Landlord` implementations of `getDashboardType` exist, but there are no call sites in the current source. Do not say this method performs the application’s screen navigation.

**Presenter note:** The repositories implement `RepositoryInterface`, but services currently instantiate concrete repository classes. Describe it as a shared contract, not as proof that the running application can switch database implementations without code changes.

## 6. Polymorphism
**2:55–3:40 | Neil**

**Plain-language definition:** Polymorphism lets the same method call use the implementation that belongs to the actual object.

**Show code:** [`FlatButton.java`](../src/ui/components/FlatButton.java#L5) — folder `src/ui/components`; class extends `JButton` at line 5 and overrides `paintComponent` beginning at line 14.

```java
@Override
protected void paintComponent(Graphics g) {
  g.setColor(getBackground());
  g.fillRect(0, 0, getWidth(), getHeight());
  super.paintComponent(g);
}
```

“`FlatButton` is still a Swing button, but it overrides `paintComponent`. When Swing paints a button whose actual object is a `FlatButton`, Java calls this version to paint the flat background before the normal button painting.

Without that override, the custom background behavior in `FlatButton` would be missing and the button would use the inherited painting behavior alone. The same kind of Swing painting override is visible in the custom panels and cards throughout the demonstrations.”

**Additional model evidence:** `Tenant.getDashboardType` starts at line 135 in [`Tenant.java`](../src/model/Tenant.java#L135); `Landlord.getDashboardType` starts at line 69 in [`Landlord.java`](../src/model/Landlord.java#L69). These are role-specific overrides, but the current user interface routes screens separately by the selected role.

## 7. How the four principles work together
**3:40–3:55 | Dylan**

**Show:** [`RoundedPanel.java`](../src/ui/components/RoundedPanel.java#L6) — folder `src/ui/components`; class starts at line 6, `setOutline` at line 19, and `paintComponent` at line 25.

“One component brings the principles together: `RoundedPanel` inherits from `JPanel`; it encapsulates its private outline; other screens use it without drawing the shape themselves; and Swing calls its overridden painting method. That combination gives us reusable panels without spreading drawing code across the application.”

## 8. External libraries and components
**3:55–4:15 | Neil**

**Show:** The four jar files in the `lib` folder and the Geoapify name separately.

“The project uses Gson to read nearby-place responses, jBCrypt to hash and check passwords, Open Location Code to validate and decode Plus Codes, and the PostgreSQL driver to connect to the database. Geoapify is an external places service, not a library. Swing and Java’s database classes are part of Java.”

**Sources to show if asked:**
- `lib/gson-2.11.0.jar`; `JsonParser` is used in [`NearbyEstablishmentService.java`](../src/service/NearbyEstablishmentService.java#L105), folder `src/service`, line 105.
- `lib/jbcrypt-0.4.jar`; `BCrypt` is used in [`PasswordHasher.java`](../src/util/PasswordHasher.java#L3), folder `src/util`, line 3.
- `lib/openlocationcode-1.0.4.jar`; `OpenLocationCode` is used in [`PlusCode.java`](../src/model/PlusCode.java#L3), folder `src/model`, line 3.
- `lib/postgresql-42.7.14.jar`; the driver is loaded in [`DatabaseConnection.java`](../src/util/DatabaseConnection.java#L11), folder `src/util`, line 11.
- Geoapify requests are sent in [`NearbyEstablishmentService.java`](../src/service/NearbyEstablishmentService.java#L79), folder `src/service`, line 79.

# Demonstration

## 9. Tenant sign-in: wrong password and happy case
**4:15–4:50 | Rizialyn and Neil**

**Action:** Enter an incorrect password once, show the error, then sign in with the prepared tenant account.

### Rizialyn

“First, I’ll try an incorrect password. The application rejects it. Now I’ll sign in with our prepared tenant account.”

### Neil — show backend code

**Code source:** [`Person.java`](../src/model/Person.java#L80) — folder `src/model`, `checkPassword` starts at line 80.

```java
public boolean checkPassword(String rawPassword) {
    return PasswordHasher.matches(rawPassword, passwordHash);
}
```

“`AuthService.login` checks the selected role and account. `Person.checkPassword` delegates the comparison to `PasswordHasher.matches`, so the application checks the entered password against its stored hash. Wrong credentials produce the same generic authentication error.”

## 10. Browse, filters, and empty results
**4:50–5:30 | Rizialyn and Arriane**

### Rizialyn

**Action:** Select the `Gym` and `Laundry` amenity filters together, show the empty result, clear the filters, and open the prepared target dorm.

“I’ll select two amenities that no seeded dorm has together. The feed shows that there are no matching dorms. I’ll clear the filters and open our target listing.”

### Arriane — show backend/UI filtering code

**Code source:** [`TenantDashboard.java`](../src/ui/tenant/TenantDashboard.java#L593) — folder `src/ui/tenant`, `matchesFilters` starts at line 593.

```java
return dorm.getAmenities().containsAll(wanted)
    && (budget < 0
        || (dorm.getRate() >= BUDGET_MIN[budget]
            && dorm.getRate() <= BUDGET_MAX[budget]));
```

“The filter requires the dorm to have every selected amenity, and its rate must fit the selected budget range. When nothing matches, the interface displays an empty-results message.”

**Presenter note:** This `Gym` plus `Laundry` example assumes the supplied seed data is unchanged. Check the filter result before presenting.

## 11. Plus Code and nearby places
**5:30–5:50 | Dylan**

**Action:** Point to the prepared nearby-place results on the selected listing.

**Show code:** [`NearbyEstablishmentService.java`](../src/service/NearbyEstablishmentService.java#L79) — folder `src/service`, request starts at line 79; Gson parsing is at line 105.

```java
HttpResponse<String> response = client.send(request(location), HttpResponse.BodyHandlers.ofString());
```

```java
JsonArray features = JsonParser.parseString(json).getAsJsonObject().getAsJsonArray("features");
```

“The service requests nearby places using the dorm’s location, then Gson reads the returned data. We prepared these results before the presentation.”

**Presenter note:** The seed inserts general establishments but does not insert dorm-to-nearby-establishment rows. Do not assume seeded dorm cards have nearby results. Prepare a listing through a successful Geoapify lookup or use an accurately labelled prepared result. Do not claim the application displays an embedded map.

## 12. Interest and application
**5:50–6:35 | Rizialyn and Neil**

### Rizialyn

**Action:** Mark interest, submit an application, and show the `Applied` button.

“I’ll mark the dorm as interesting, then submit an application. Interest and applying are separate actions: applying sends a request for the landlord to review.”

### Neil — show backend code

**Code source:** [`DormService.java`](../src/service/DormService.java#L154) — folder `src/service`, application method starts at line 154.

```java
Tenant savedTenant = TENANTS.findById(tenant.getPersonId())
    .orElseThrow(() -> new SQLException("Tenant account no longer exists."));
Dorm savedDorm = DORMS.findById(dorm.getDormId())
    .orElseThrow(() -> new SQLException("Dorm listing no longer exists."));
Application application = savedTenant.applyToDorm(savedDorm, confirmTransfer);
APPLICATION_REPO.save(application);
return application;
```

“The service reloads the tenant and dorm from storage, creates the application, and saves it. It does not reserve a slot at this point; that happens only if the landlord accepts.”

**Action:** Note the available-slot count for comparison after acceptance.

## 13. Landlord dashboard and ownership
**6:35–7:05 | Arriane and Dylan**

### Arriane

**Action:** Log out, sign in as the owner of the selected dorm, and open its pending applications.

“I’ll sign in as the landlord who owns this dorm. The landlord can review applicants and manage their own dorms.”

### Dylan — show backend code

**Code source:** [`ApplicationReviewService.java`](../src/service/ApplicationReviewService.java#L35) — folder `src/service`, ownership check begins at line 35.

```java
if (!landlord.ownsDorm(dorm)) {
    throw new UnauthorizedActionException(
        "Dorm is not owned by this landlord."
    );
}
```

“Before returning pending applications, the service checks ownership. A landlord cannot use this operation to view another landlord’s applicants.”

**Presenter note:** The normal dashboard lists the signed-in landlord’s dorms. Demonstrate the rejected cross-owner request with a prepared test result or code, not by implying there is a normal screen for selecting another owner’s dorm.

## 14. Accept an application and update capacity
**7:05–7:55 | Arriane and Neil**

### Arriane

**Action:** Accept the new application and show the updated available-slot count.

“I’ll accept the application. Before acceptance, there were **[read the prepared number]** available slots. After acceptance, there should be one fewer.”

### Neil — show backend code

**Code source:** [`ApplicationReviewService.java`](../src/service/ApplicationReviewService.java#L73) — folder `src/service`, transaction begins at line 73.

```java
conn.setAutoCommit(false);

application.accept(landlord);
applicationRepository.reserveSlot(conn, dormId);
applicationRepository.moveTenant(conn, tenantId, dormId);
applicationRepository.update(conn, application);

conn.commit();
```

“The service checks the application, reserves a slot, moves the tenant, and updates the application in one transaction. If an operation fails, the transaction rolls back so partial updates are not kept.”

**Rollback source:** [`ApplicationReviewService.java`](../src/service/ApplicationReviewService.java#L112) — folder `src/service`, rollback starts at line 112.

## 15. Tenant sees the result
**7:55–8:15 | Rizialyn**

**Action:** Sign back in as the tenant, open My Dorm, and point to its announcement.

“I’ll sign back in as the tenant. After the application is accepted, the tenant has a current dorm and can open My Dorm to see its information and announcements.”

## 16. Complaint submission and privacy
**8:15–8:55 | Rizialyn and Neil**

### Rizialyn

**Action:** Submit the prepared complaint with `Post anonymously` selected.

“A resident can submit a concern about their current dorm. I’ll enter our prepared message, select `Post anonymously`, and submit it.”

### Neil — show backend code

**Code source:** [`Complaint.java`](../src/model/Complaint.java#L35) — folder `src/model`, anonymous sender handling starts at line 35.

```java
if (anonymous) {
    this.sender = null;
} else {
    this.sender = sender;
}
```

**Show the landlord query:** [`ComplaintService.java`](../src/service/ComplaintService.java#L53) — folder `src/service`, query starts at line 53.

```java
String sql = "SELECT c.complaint_id, c.message, c.date_posted FROM complaints c "
  + "JOIN dorms d ON c.dorm_id = d.dorm_id WHERE d.dorm_id = ? AND d.owner_id = ? "
  + "ORDER BY c.date_posted DESC, c.complaint_id DESC";
```

“For an anonymous complaint, the model does not keep the sender. The landlord’s query also does not select sender information.”

**Presenter note:** The current landlord view labels every complaint `Anonymous`, including complaints submitted as non-anonymous. Do not claim it distinguishes them. Use a prepared screenshot unless the just-submitted complaint has been refreshed into the landlord view.

## 17. Edge and failure cases
**8:55–9:20 | Dylan**

**Show:** Prepared examples or the relevant code; do not disconnect the live database.

“We also checked important failure cases. The application rejects invalid registration values, an empty filter result is shown clearly, and a full dorm cannot accept another tenant. The owner check rejects a landlord who does not own the dorm.

If a database operation fails during acceptance, the transaction rolls back. If Geoapify is unavailable while posting a dorm with a Plus Code, the listing is not saved.”

**Presenter note:** The current seed has no full dorm. Prepare a separate full-dorm case if demonstrating zero capacity. The user interface blocks approval when it sees zero slots; the service and database also check capacity during acceptance. Do not simulate a database outage on the presentation database.

## 18. Closing
**9:20–9:40 | Dylan**

**Show:** Project name and “Thank you.”

“Find The D brings dorm listings and management into one application. Its object-oriented design supports the tenant and landlord workflows we demonstrated.

Thank you for listening. We are ready for your questions.”

---

# Presenter Verification Notes — Not Spoken

## Current-script audit

The supplied script was checked against the current workspace:

- The old source links pointed to another computer’s `C:/Users/Arrianne/...` directory. They have been replaced with links to the current project files and their current line numbers.
- `Tenant.getDashboardType` and `Landlord.getDashboardType` exist, but a project-wide search found no call sites. The script presents them as role-specific overrides, not as the mechanism that routes the live interface.
- `ComplaintService` deliberately omits sender identity from the landlord query, and `LandlordDashboard` labels every returned complaint `Anonymous`. This applies to non-anonymous complaints too; the script does not claim the landlord screen distinguishes them.
- Nearby-place support is in the source, but `database/seed.sql` inserts general `establishments` and does not insert rows into `dorm_nearby_establishments`. Seeded dorms are not guaranteed to have nearby results. A successful Geoapify lookup or a clearly labelled prepared result is needed.
- The seed contains no dorm-photo assets. Do not promise that sample listings have real photos.
- The application interface disables `Apply` when it sees a pending application for that dorm, but the application table has no unique constraint on tenant and dorm, and `ApplicationRepository.save` has no duplicate-pair check. Describe this as a user-interface guard, not universal backend duplicate prevention.
- The application-level age validator requires age 18 or older, while the database schema permits ages from 16 through 120. For a live invalid-input example, say the application rejects age 17; do not say the database has the same minimum.
- The project has no Maven or Gradle build file. Its external jar dependencies are in `lib`. A full `javac -encoding UTF-8 -cp "lib/*" -d out ...` compile completed successfully during this review.

## Confirmed OOP evidence shortlist

These are the strongest concise examples found in the source:

| Principle | Source evidence |
|---|---|
| Encapsulation | `Person.age` is private and `Person.setAge` validates it ([`Person.java`](../src/model/Person.java#L15)); `DormDetails` uses private final fields and validated construction ([`DormDetails.java`](../src/model/DormDetails.java#L10)); `Dorm.getAmenities` returns a defensive, unmodifiable copy ([`Dorm.java`](../src/model/Dorm.java#L278)). |
| Inheritance | `Tenant` extends `Person` ([`Tenant.java`](../src/model/Tenant.java#L12)); `Landlord` extends `Person` ([`Landlord.java`](../src/model/Landlord.java#L12)); `RoundedPanel` extends `JPanel` ([`RoundedPanel.java`](../src/ui/components/RoundedPanel.java#L6)). |
| Abstraction | `Person` is abstract and declares `getDashboardType` ([`Person.java`](../src/model/Person.java#L10)); `RepositoryInterface<T, ID>` defines a shared repository contract ([`RepositoryInterface.java`](../src/repository/RepositoryInterface.java#L7)); `RoundedPanel` lets screens use a panel without implementing its drawing details ([`RoundedPanel.java`](../src/ui/components/RoundedPanel.java#L25)). |
| Polymorphism | `FlatButton` overrides Swing’s `paintComponent` ([`FlatButton.java`](../src/ui/components/FlatButton.java#L14)); `RoundedPanel` overrides `paintComponent` ([`RoundedPanel.java`](../src/ui/components/RoundedPanel.java#L25)); tenant and landlord classes override `getDashboardType` ([`Tenant.java`](../src/model/Tenant.java#L135), [`Landlord.java`](../src/model/Landlord.java#L69)). The role overrides have no current call sites. |

## Actual behavior by scenario

| Scenario | Behavior supported by source | Demonstration guidance |
|---|---|---|
| Happy path | `AuthService.login` checks the requested role, loads the account, and calls `Person.login`. `DormService.apply` reloads the tenant and dorm, creates and saves a pending application. `ApplicationReviewService.acceptApplication` checks and locks records, verifies ownership/status/capacity, updates the application and tenant, reserves a slot, and commits. The tenant can then see My Dorm. | Use a tenant with no current dorm and no pending application for the target dorm, plus the landlord who owns that dorm. Ensure the dorm has at least one free slot and an announcement before starting. |
| Invalid registration | The registration screen rejects a non-numeric age. `Validator.age` rejects values below 18. Email, contact number, password, and required fields are also validated. | Use age 17 or a deliberately malformed email, then stop before creating an account. |
| Empty browse results | `TenantDashboard.applyFeedFilters` displays `No dorms match your filters.` when no listing matches. | With the unchanged seed, `Gym` and `Laundry` together should produce no results because no seeded dorm has both. Verify before presenting. |
| Full dorm | The landlord interface stops approval and displays that the dorm is full. The model and repository also check capacity; the database reservation updates only when `available_slots > 0`. | The seed does not contain a full dorm. Prepare a separate full dorm and pending application if showing this case. |
| Duplicate application | The tenant interface disables `Apply` for an existing pending application. `Application` rejects applying to the tenant’s current dorm and requires transfer confirmation for another dorm. There is no database uniqueness rule preventing duplicate application rows for a tenant/dorm pair. | Use a pair with no existing pending application for the happy case. Do not claim the backend prevents every possible duplicate submission. |
| Transfer | A tenant with a current dorm is asked to confirm applying to a different dorm. `Application` rejects an unconfirmed transfer. | If demonstrating, prepare a tenant who already belongs to a dorm and a different target dorm. |
| Anonymous complaint | The model sets the sender to `null` for anonymous complaints, and the schema requires anonymous rows to have no sender. The landlord query excludes sender data. | Use a resident account and a non-empty prepared message. The landlord view currently labels all complaints anonymous, so do not compare anonymous and non-anonymous display. |
| Wrong password or role | Authentication fails with the generic `Incorrect email or password.` message. | Show one wrong attempt, then sign in with the prepared correct role and password. |
| Unauthorized landlord | `ApplicationReviewService.viewPendingApplications` verifies ownership and rejects a dorm not owned by that landlord. | Use a prepared test result or screenshot; the ordinary landlord dashboard only lists that landlord’s dorms. |
| Failure during acceptance | Acceptance is transactional. Exceptions, including no available slots and database errors, cause a rollback. A repeated review is rejected because only pending applications are reviewable. | Show the code or a prepared full-dorm result. Do not intentionally interrupt the live database during the presentation. |
| Database unavailable | `DatabaseConnection` obtains the configured values and opens a PostgreSQL connection. A failed connection returns a database error through the service; login displays service errors. | Verify connectivity before presenting. If discussing this worst case, show a prepared error/result rather than stopping PostgreSQL live. |
| Geoapify unavailable | A missing key or failed nearby request raises an error. `ListingService.postDorm` performs the nearby lookup before inserting the new dorm, so a failed lookup prevents the listing from being saved. | Keep a valid local key in `.env`, test the request in advance, and do not show or commit credentials. |

## External dependencies actually used

There is no Maven or Gradle manifest. These jars are present in `lib` and have corresponding source usages:

| Dependency | Use in source |
|---|---|
| Gson (`lib/gson-2.11.0.jar`) | Parses Geoapify responses in `src/service/NearbyEstablishmentService.java`, including `JsonParser` at line 105. |
| jBCrypt (`lib/jbcrypt-0.4.jar`) | Hashes and checks passwords in `src/util/PasswordHasher.java`, including `BCrypt.hashpw` at line 10 and `BCrypt.checkpw` at line 15. |
| Open Location Code (`lib/openlocationcode-1.0.4.jar`) | Validates and decodes `PlusCode` in `src/model/PlusCode.java`; `src/model/Establishment.java` also encodes coordinates. |
| PostgreSQL JDBC driver (`lib/postgresql-42.7.14.jar`) | Loads the driver and opens connections in `src/util/DatabaseConnection.java`. |

Geoapify is a hosted external places service, not a jar dependency. OpenStreetMap is the data source attribution, not a library. Java Swing, `java.sql`, and `java.net.http.HttpClient` are part of the Java platform.

## Demonstration data checklist

- [ ] Start PostgreSQL and verify the project database is reachable.
- [ ] Ensure `database/schema.sql` and `database/seed.sql` have been applied to a fresh or intentionally prepared demo database. The seed script is for setup; do not rerun it on a database with presentation state you need to keep.
- [ ] Keep local database settings and `GEOAPIFY_API_KEY` in `.env`; do not display or commit the file. The setup instructions are in [`database/README.md`](../database/README.md).
- [ ] Confirm the four jars listed above are present under `lib`.
- [ ] Prepare a tenant account with no current dorm and no pending application for the target dorm.
- [ ] Prepare the landlord account that owns that target dorm. Check both credentials and selected roles before presentation; use the sample-account instructions in `database/README.md` without putting passwords on slides.
- [ ] Prepare a target dorm with at least one available slot, a valid Plus Code, amenities, a budget within a filter range, and an announcement. Record its available-slot count before acceptance.
- [ ] Ensure the target tenant/dorm pair has no pre-existing pending application; the seeded database already includes pending, accepted, and rejected applications.
- [ ] If showing nearby results, create and verify them before the presentation through a successful Geoapify lookup. The seed does not attach its general establishment rows to dorms.
- [ ] If showing photos, add and verify image files; the seed includes no dorm-photo assets.
- [ ] Prepare the no-results filter combination (`Gym` plus `Laundry`) and verify it against the current database.
- [ ] For the zero-slot edge case, prepare a separate full dorm and a pending applicant. The seed does not include a full dorm.
- [ ] Prepare a resident tenant, a short non-empty complaint, and the anonymous checkbox state. Remember that the landlord interface currently labels all complaints anonymous.
- [ ] Prepare an invalid registration value such as age 17, a wrong-password attempt, and—if demonstrating transfer—a tenant who already belongs to another dorm.
- [ ] For unauthorized-owner and database-outage worst cases, prepare a screenshot or test result rather than altering the live demonstration database.

## Timing and cut plan

The spoken schedule ends at **9:40**, leaving about 20 seconds under the hard limit. Rehearse the actions, especially switching accounts. If the run is late, skip the optional second encapsulation example, shorten the nearby-place explanation, and summarize the worst-case notes. Do not cut the four principle explanations.

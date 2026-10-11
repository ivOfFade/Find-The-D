# Find The D: Class Design

## 1. Domain classes

### Person (abstract)

Purpose: the data and behavior shared by every user. Parent of Tenant and Landlord.

**Attributes**

- `personId : int`
- `name : String` (required)
- `age : int` (16 to 120)
- `contactNum : String` (Philippine mobile format: 09XXXXXXXXX or +639XXXXXXXXX)
- `email : String` (valid format, unique, stored in lowercase)
- `passwordHash : String` (never returned, printed, or logged; called "password" in the proposal)
- `role : Role` (set by the subclass, read-only)
- `loggedIn : boolean`

**Constructors**

- `protected Person(name, age, contactNum, email, rawPassword, role)`: validates, then hashes the password
- `protected Person(personId, name, age, contactNum, email, passwordHash, role)`: used when loading from the database

**Methods**

- `displayInfo() : String`: summary without the password; subclasses override and add their own details
- `login(rawPassword) : void`: throws `AuthenticationException` if the password is wrong, otherwise sets `loggedIn`
- `logout() : void`
- `checkPassword(rawPassword) : boolean`
- `abstract getDashboardType() : DashboardType`
- getters and validated setters (no getter for `passwordHash` outside the repository layer)
- `equals()` and `hashCode()` based on `personId`; `toString()` never includes the password

### Tenant (extends Person)

Purpose: a user who is looking for a dorm or already lives in one.

**Attributes**

- `currentDorm : Dorm` (`null` until an application is accepted)
- `applications : ArrayList<Application>` (the tenant's submitted applications)
- `interestedDorms : ArrayList<Dorm>` (no duplicates)

**Constructors**

- `Tenant(name, age, contactNum, email, rawPassword)`
- `Tenant(personId, name, age, contactNum, email, passwordHash)`: database loading

**Methods**

- `lookForDorm(listings, filter) : ArrayList<Dorm>`: returns the listings that match the `DormFilter`
- `applyToDorm(dorm, confirmTransfer) : Application`: submits a pending application; does not make the tenant a resident or reserve a slot. If the tenant already has a different current dorm, `confirmTransfer` must be true; the existing dorm remains current unless the new dorm's owner accepts.
- `viewMyDorm() : Dorm`: throws `DormNotFoundException` if the tenant has no dorm
- `markInterested(dorm) : void` and `removeInterest(dorm) : void`
- `viewAnnouncements() : ArrayList<Announcement>`: only the current dorm's announcements
- `complain(message, anonymous) : Complaint`: throws `DormNotFoundException` if the tenant has no dorm and `InvalidInputException` if the message is empty
- `hasDorm() : boolean`
- `getDashboardType() : DashboardType`: `TENANT_DORM_HOME` if the tenant has a dorm, otherwise `TENANT_LISTINGS`
- `displayInfo()` override: adds current dorm name

### Landlord (extends Person)

Purpose: a dorm owner who posts and manages listings.

**Attributes**

- `handledDorms : ArrayList<Dorm>`
- `otherContact : String` (optional, for example a Messenger or Facebook link shown to tenants)

**Constructors**

- `Landlord(name, age, contactNum, email, rawPassword)`
- `Landlord(personId, name, age, contactNum, email, passwordHash, otherContact)`: database loading

**Methods**

- `postDorm(details) : Dorm`: creates a `Dorm` from `DormDetails`, adds it to `handledDorms`
- `getMyDorms() : ArrayList<Dorm>`: returns only dorms owned by this landlord; landlords cannot browse or open other landlords' dorm listings
- `manageDorm(dormId, details) : void`: edits only a dorm returned by this landlord's `getMyDorms()`; the service loads it using an owner-scoped lookup and throws `UnauthorizedActionException` if the dorm ID is not owned by this landlord
- `removeDorm(dorm) : void`: throws `UnauthorizedActionException` if not the owner, and `InvalidInputException` if the dorm still has tenants
- `makeAnnouncement(dorm, message) : Announcement`: owner only; message must not be empty
- `viewComplaints(dorm) : ArrayList<Complaint>`: owner only, read-only
- `reviewApplications(dorm) : ArrayList<Application>`: returns pending applications for an owned dorm only
- `acceptApplication(application) : void` and `rejectApplication(application) : void`: owner only; acceptance adds the tenant only if a slot is still available, while rejection leaves the tenant's current dorm unchanged
- `ownsDorm(dorm) : boolean`
- `getDashboardType() : DashboardType`: always `LANDLORD_DASHBOARD`
- `displayInfo()` override: adds the number of dorms

### Dorm

Purpose: a listing posted by a landlord that tenants can live in.

**Attributes**

- `dormId : int`
- `dormName : String` (required)
- `location : String` (address or area description)
- `plusCode : PlusCode` (a validated full Google Open Location Code for the dorm location; decoded for map coordinates and distance calculations)
- `description : String`
- `owner : Landlord`
- `tenants : ArrayList<Tenant>` (current residents)
- `totalSlots : int` (at least 1)
- `availableSlots : int` (kept in sync by `addTenant` and `removeTenant`)
- `rate : double` (monthly, greater than 0)
- `amenities : ArrayList<String>`
- `rules : String`
- `photos : ArrayList<String>` (file paths)
- `interestedTenants : ArrayList<Tenant>`
- `announcements : ArrayList<Announcement>`
- `complaints : ArrayList<Complaint>`
- `datePosted : LocalDateTime`

**Constructors**

- `Dorm(owner, details)`: validates the details, including the Plus Code; `availableSlots` starts equal to `totalSlots`
- `Dorm(dormId, owner, details, datePosted)`: database loading

**Methods**

- `addTenant(tenant) : void`: called only when an application is accepted; throws `NoAvailableSlotsException` when `availableSlots` is 0 and ignores a tenant who is already inside
- `removeTenant(tenant) : void`
- `updateDetails(details) : void`: throws `InvalidInputException` if the new `totalSlots` is smaller than the current number of tenants; adjusts `availableSlots`
- `displayInfo() : String`
- `hasAvailableSlots() : boolean`
- `hasTenants() : boolean`
- `getInterestedCount() : int`
- `addInterest(tenant)`, `removeInterest(tenant)`, `addAnnouncement(announcement)`, `addComplaint(complaint)`: called by Tenant and Landlord methods, not by the UI
- getters that return read-only copies of the lists, so outside code cannot change them directly

### Announcement

Purpose: a message from a landlord to the tenants of one dorm.

**Attributes**

- `announcementId : int`
- `dorm : Dorm`
- `author : Landlord`
- `message : String` (required, up to 1000 characters)
- `datePosted : LocalDateTime` (set when created)

**Constructors**

- `Announcement(dorm, author, message)`: throws `UnauthorizedActionException` if the author does not own the dorm
- `Announcement(announcementId, dorm, author, message, datePosted)`: database loading

**Methods**

- `isVisibleTo(tenant) : boolean`: true only if the tenant's current dorm is this announcement's dorm
- `displayInfo() : String`
- getters

### Complaint

Purpose: a tenant's concern about a dorm, readable only by that dorm's landlord.

**Attributes**

- `complaintId : int`
- `dorm : Dorm`
- `message : String` (required, up to 1000 characters)
- `datePosted : LocalDateTime`
- `anonymous : boolean`
- `sender : Tenant` (private; stays `null` forever when anonymous)

**Constructors**

- `Complaint(dorm, sender, message, anonymous)`: if `anonymous` is true the sender is never stored, so the identity does not exist in memory or in the database
- `Complaint(complaintId, dorm, sender, message, anonymous, datePosted)`: database loading (`sender` is `null` when anonymous)

**Methods**

- `isAnonymous() : boolean`
- `getSenderName() : String`: "Anonymous" when anonymous, otherwise the sender's name
- `displayInfo() : String`
- getters; there is no public `getSender()`

### Establishment

Purpose: a nearby place (convenience store, landmark) shown on a dorm's map.

**Attributes**

- `establishmentId : int`
- `name : String`
- `type : EstablishmentType` (for example convenience store, laundry, grocery, school, or pharmacy)
- `plusCode : PlusCode` (a validated full Google Open Location Code for this establishment)
- `NEARBY_RADIUS_METERS : int` (constant, 1000)

**Methods**

- `distanceFrom(dorm) : double`: approximate straight-line distance in meters, calculated with the haversine formula using the decoded center coordinates of the dorm and establishment Plus Codes
- `displayInfo() : String`
- getters

### Application

Purpose: a tenant's request to live in a dorm, which must be decided by that dorm's owner.

**Attributes**

- `applicationId : int`
- `tenant : Tenant`
- `dorm : Dorm`
- `status : ApplicationStatus` (`PENDING`, `ACCEPTED`, or `REJECTED`)
- `transferConfirmed : boolean` (true when an already-housed tenant confirmed moving if accepted)
- `dateSubmitted : LocalDateTime`
- `dateReviewed : LocalDateTime` (null while pending)

**Methods**

- `accept(landlord) : void`: only the dorm's owner may accept a pending application; acceptance is rejected if the dorm is full
- `reject(landlord) : void`: only the dorm's owner may reject a pending application
- `displayInfo() : String`
- getters; status changes only through the owner-authorized review operation

## 2. Relationships

| From             | To                  | Type                                        | Java field                                         |
| ---------------- | ------------------- | ------------------------------------------- | -------------------------------------------------- |
| Tenant, Landlord | Person              | Inheritance                                 | `extends Person`                                   |
| Landlord         | Dorm                | owns (association)                          | `Landlord.handledDorms`, `Dorm.owner`              |
| Dorm             | Tenant              | current residents (association)             | `Dorm.tenants`, `Tenant.currentDorm`               |
| Tenant           | Application         | submits                                     | `Tenant.applications`, `Application.tenant`        |
| Dorm             | Application         | receives                                    | `Application.dorm`                                 |
| Tenant           | Dorm                | interested in (association)                 | `Tenant.interestedDorms`, `Dorm.interestedTenants` |
| Dorm             | Announcement        | has (composition)                           | `Dorm.announcements`, `Announcement.dorm`          |
| Landlord         | Announcement        | writes (association)                        | `Announcement.author`                              |
| Dorm             | Complaint           | receives (composition)                      | `Dorm.complaints`, `Complaint.dorm`                |
| Tenant           | Complaint           | submits (association, none if anonymous)    | `Complaint.sender` (private)                       |
| Dorm             | Establishment       | near (dependency, computed from Plus Codes) | none stored; `Establishment.isNear(dorm)`          |
| Person           | Role, DashboardType | uses                                        | `role`, `getDashboardType()`                       |

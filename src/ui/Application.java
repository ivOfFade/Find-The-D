import java.time.LocalDateTime;

/** A tenant's request to live in a dorm, decided by that dorm's owner. */
public class Application {
    private final int applicationId;
    private final Tenant tenant;
    private final Dorm dorm;
    private ApplicationStatus status = ApplicationStatus.PENDING;
    private final boolean transferConfirmed;
    private final LocalDateTime dateSubmitted = LocalDateTime.now();
    private LocalDateTime dateReviewed;

    /** Public so Tenant.applyToDorm can create it from its own file. */
    public Application(Tenant tenant, Dorm dorm, boolean transferConfirmed) {
        this.applicationId = IdGenerator.next();
        this.tenant = tenant;
        this.dorm = dorm;
        this.transferConfirmed = transferConfirmed;
    }

    public void accept(Landlord landlord) {
        requireReviewable(landlord);
        if (!dorm.hasAvailableSlots()) {
            throw new NoAvailableSlotsException(dorm.getDormName() + " is full.");
        }
        Dorm old = tenant.getCurrentDorm();
        dorm.addTenant(tenant);
        if (old != null && !old.equals(dorm)) {
            old.removeTenant(tenant);
            tenant.setCurrentDorm(dorm); // removeTenant clears it only if still pointing at old
        }
        status = ApplicationStatus.ACCEPTED;
        dateReviewed = LocalDateTime.now();
    }

    public void reject(Landlord landlord) {
        requireReviewable(landlord);
        status = ApplicationStatus.REJECTED;
        dateReviewed = LocalDateTime.now();
    }

    private void requireReviewable(Landlord landlord) {
        if (!landlord.ownsDorm(dorm)) {
            throw new UnauthorizedActionException("Only the dorm's owner can review this application.");
        }
        if (status != ApplicationStatus.PENDING) {
            throw new InvalidInputException("This application was already reviewed.");
        }
    }

    public String displayInfo() { return tenant.getName() + " -> " + dorm.getDormName() + " [" + status + "]"; }

    public int getApplicationId() { return applicationId; }
    public Tenant getTenant() { return tenant; }
    public Dorm getDorm() { return dorm; }
    public ApplicationStatus getStatus() { return status; }
    public boolean isTransferConfirmed() { return transferConfirmed; }
    public LocalDateTime getDateSubmitted() { return dateSubmitted; }
    public LocalDateTime getDateReviewed() { return dateReviewed; }
}

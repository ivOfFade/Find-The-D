import java.util.ArrayList;

/** A user who is looking for a dorm or already lives in one. */
public class Tenant extends Person {
    private Dorm currentDorm;
    private final ArrayList<Application> applications = new ArrayList<>();
    private final ArrayList<Dorm> interestedDorms = new ArrayList<>();

    public Tenant(String name, int age, String contactNum, String email, String rawPassword) {
        super(name, age, contactNum, email, rawPassword, Role.TENANT);
    }

    public Tenant(int personId, String name, int age, String contactNum, String email, String passwordHash) {
        super(personId, name, age, contactNum, email, passwordHash, Role.TENANT);
    }

    public ArrayList<Dorm> lookForDorm(ArrayList<Dorm> listings, DormFilter filter) {
        ArrayList<Dorm> result = new ArrayList<>();
        for (Dorm d : listings) {
            if (filter == null || filter.matches(d)) {
                result.add(d);
            }
        }
        return result;
    }

    /** Submits a pending application only; no slot is reserved and the tenant is not moved. */
    public Application applyToDorm(Dorm dorm, boolean confirmTransfer) {
        if (dorm == null) {
            throw new DormNotFoundException("Dorm not found.");
        }
        if (dorm.equals(currentDorm)) {
            throw new InvalidInputException("You already live in this dorm.");
        }
        if (currentDorm != null && !confirmTransfer) {
            throw new InvalidInputException("Confirm the transfer to apply to a different dorm.");
        }
        for (Application a : applications) {
            if (a.getDorm().equals(dorm) && a.getStatus() == ApplicationStatus.PENDING) {
                throw new InvalidInputException("You already have a pending application for this dorm.");
            }
        }
        Application app = new Application(this, dorm, currentDorm != null);
        applications.add(app);
        dorm.addApplication(app);
        return app;
    }

    public Dorm viewMyDorm() {
        if (currentDorm == null) {
            throw new DormNotFoundException("You do not have a dorm yet.");
        }
        return currentDorm;
    }

    public void markInterested(Dorm dorm) {
        if (!interestedDorms.contains(dorm)) {
            interestedDorms.add(dorm);
            dorm.addInterest(this);
        }
    }

    public void removeInterest(Dorm dorm) {
        if (interestedDorms.remove(dorm)) {
            dorm.removeInterest(this);
        }
    }

    public ArrayList<Announcement> viewAnnouncements() {
        ArrayList<Announcement> result = new ArrayList<>();
        if (currentDorm != null) {
            for (Announcement a : currentDorm.getAnnouncements()) {
                if (a.isVisibleTo(this)) {
                    result.add(a);
                }
            }
        }
        return result;
    }

    public Complaint complain(String message, boolean anonymous) {
        if (currentDorm == null) {
            throw new DormNotFoundException("You must live in a dorm to file a complaint.");
        }
        Complaint c = new Complaint(currentDorm, this, message, anonymous);
        currentDorm.addComplaint(c);
        return c;
    }

    public boolean hasDorm() { return currentDorm != null; }

    @Override
    public DashboardType getDashboardType() {
        return hasDorm() ? DashboardType.TENANT_DORM_HOME : DashboardType.TENANT_LISTINGS;
    }

    @Override
    public String displayInfo() {
        return super.displayInfo() + ", dorm: " + (currentDorm == null ? "none" : currentDorm.getDormName());
    }

    public Dorm getCurrentDorm() { return currentDorm; }
    public ArrayList<Application> getApplications() { return new ArrayList<>(applications); }
    public ArrayList<Dorm> getInterestedDorms() { return new ArrayList<>(interestedDorms); }

    /** Set only by Dorm/Application when an owner accepts or removes a tenant. */
    void setCurrentDorm(Dorm dorm) { this.currentDorm = dorm; }
}

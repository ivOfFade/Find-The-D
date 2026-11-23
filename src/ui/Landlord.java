import java.util.ArrayList;

/** A dorm owner who posts and manages listings. */
public class Landlord extends Person {
    private static final ArrayList<Dorm> ALL_DORMS = new ArrayList<>();
    private final ArrayList<Dorm> handledDorms = new ArrayList<>();
    private String otherContact;

    public Landlord(String name, int age, String contactNum, String email, String rawPassword) {
        super(name, age, contactNum, email, rawPassword, Role.LANDLORD);
    }

    public Landlord(int personId, String name, int age, String contactNum, String email,
                    String passwordHash, String otherContact) {
        super(personId, name, age, contactNum, email, passwordHash, Role.LANDLORD);
        this.otherContact = otherContact;
    }

    public Dorm postDorm(DormDetails details) {
        Dorm dorm = new Dorm(this, details);
        handledDorms.add(dorm);
        ALL_DORMS.add(dorm);
        return dorm;
    }

    public ArrayList<Dorm> getMyDorms() { return new ArrayList<>(handledDorms); }

    /** The dorms a tenant can browse: everything every landlord has posted. */
    public static ArrayList<Dorm> listings() { return new ArrayList<>(ALL_DORMS); }

    public void manageDorm(int dormId, DormDetails details) {
        findOwned(dormId).updateDetails(details);
    }

    public void removeDorm(Dorm dorm) {
        requireOwner(dorm);
        if (dorm.hasTenants()) {
            throw new InvalidInputException("Cannot remove a dorm that still has tenants.");
        }
        handledDorms.remove(dorm);
        ALL_DORMS.remove(dorm);
    }

    public Announcement makeAnnouncement(Dorm dorm, String message) {
        requireOwner(dorm);
        Announcement a = new Announcement(dorm, this, message);
        dorm.addAnnouncement(a);
        return a;
    }

        public void removeAnnouncement(Dorm dorm, Announcement announcement) {
        requireOwner(dorm);
        dorm.removeAnnouncement(announcement);
    }

    public void removeComplaint(Dorm dorm, Complaint complaint) {
        requireOwner(dorm);
        dorm.removeComplaint(complaint);
    }

    public ArrayList<Complaint> viewComplaints(Dorm dorm) {
        requireOwner(dorm);
        return dorm.getComplaints();
    }

    public ArrayList<Application> reviewApplications(Dorm dorm) {
        requireOwner(dorm);
        ArrayList<Application> pending = new ArrayList<>();
        for (Application a : dorm.getApplications()) {
            if (a.getStatus() == ApplicationStatus.PENDING) {
                pending.add(a);
            }
        }
        return pending;
    }

    public void acceptApplication(Application application) { application.accept(this); }
    public void rejectApplication(Application application) { application.reject(this); }

    public boolean ownsDorm(Dorm dorm) { return dorm != null && handledDorms.contains(dorm); }

    @Override public DashboardType getDashboardType() { return DashboardType.LANDLORD_DASHBOARD; }

    @Override
    public String displayInfo() {
        return super.displayInfo() + ", dorms: " + handledDorms.size();
    }

    public String getOtherContact() { return otherContact; }
    public void setOtherContact(String otherContact) { this.otherContact = otherContact; }

    private void requireOwner(Dorm dorm) {
        if (!ownsDorm(dorm)) {
            throw new UnauthorizedActionException("You do not own this dorm.");
        }
    }

    private Dorm findOwned(int dormId) {
        for (Dorm d : handledDorms) {
            if (d.getDormId() == dormId) {
                return d;
            }
        }
        throw new UnauthorizedActionException("You do not own a dorm with id " + dormId + ".");
    }
}

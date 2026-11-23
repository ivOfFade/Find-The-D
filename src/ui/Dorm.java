import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A listing posted by a landlord that tenants can live in. */
public class Dorm {
    private final int dormId;
    private String dormName;
    private String location;
    private PlusCode plusCode;
    private String description;
    private final Landlord owner;
    private final ArrayList<Tenant> tenants = new ArrayList<>();
    private int totalSlots;
    private int availableSlots;
    private double rate;
    private ArrayList<String> amenities = new ArrayList<>();
    private String rules;
    private ArrayList<String> photos = new ArrayList<>();
    private final ArrayList<Tenant> interestedTenants = new ArrayList<>();
    private final ArrayList<Announcement> announcements = new ArrayList<>();
    private final ArrayList<Complaint> complaints = new ArrayList<>();
    private final ArrayList<Application> applications = new ArrayList<>();
    private final ArrayList<Establishment> establishments = new ArrayList<>();
    private final LocalDateTime datePosted;

    public Dorm(Landlord owner, DormDetails details) {
        if (owner == null) {
            throw new InvalidInputException("A dorm needs an owner.");
        }
        this.dormId = IdGenerator.next();
        this.owner = owner;
        this.datePosted = LocalDateTime.now();
        apply(details);
        this.availableSlots = totalSlots;
    }

    public Dorm(int dormId, Landlord owner, DormDetails details, LocalDateTime datePosted) {
        this.dormId = dormId;
        this.owner = owner;
        this.datePosted = datePosted;
        apply(details);
        this.availableSlots = totalSlots;
    }

    private void apply(DormDetails d) {
        this.dormName = Validation.required(d.dormName, "Dorm name");
        this.location = d.location == null ? "" : d.location.trim();
        this.plusCode = new PlusCode(d.plusCode);
        if (d.totalSlots < 1) {
            throw new InvalidInputException("A dorm needs at least 1 slot.");
        }
        if (d.rate <= 0) {
            throw new InvalidInputException("Rate must be greater than 0.");
        }
        this.totalSlots = d.totalSlots;
        this.rate = d.rate;
        this.description = d.description == null ? "" : d.description;
        this.rules = d.rules == null ? "" : d.rules;
        this.amenities = new ArrayList<>(d.amenities);
        this.photos = new ArrayList<>(d.photos);
    }

    public void addTenant(Tenant tenant) {
        if (tenants.contains(tenant)) {
            return;
        }
        if (availableSlots <= 0) {
            throw new NoAvailableSlotsException(dormName + " has no available slots.");
        }
        tenants.add(tenant);
        availableSlots--;
        tenant.setCurrentDorm(this);
    }

    public void removeTenant(Tenant tenant) {
        if (tenants.remove(tenant)) {
            availableSlots++;
            if (this.equals(tenant.getCurrentDorm())) {
                tenant.setCurrentDorm(null);
            }
        }
    }

    public void updateDetails(DormDetails d) {
        if (d.totalSlots < tenants.size()) {
            throw new InvalidInputException("Total slots cannot be fewer than the current " + tenants.size() + " tenants.");
        }
        apply(d);
        availableSlots = totalSlots - tenants.size();
    }

    public String displayInfo() {
        return dormName + " - " + location + " (" + plusCode + "), Php " + String.format("%.2f", rate)
            + "/mo, " + availableSlots + "/" + totalSlots + " slots free";
    }

    public boolean hasAvailableSlots() { return availableSlots > 0; }
    public boolean hasTenants() { return !tenants.isEmpty(); }
    public int getInterestedCount() { return interestedTenants.size(); }

    void addInterest(Tenant t) { if (!interestedTenants.contains(t)) interestedTenants.add(t); }
    void removeInterest(Tenant t) { interestedTenants.remove(t); }
    void addAnnouncement(Announcement a) { announcements.add(a); }
    void addComplaint(Complaint c) { complaints.add(c); }
    void addApplication(Application a) { applications.add(a); }
    void removeAnnouncement(Announcement a) { announcements.remove(a); }
    void removeComplaint(Complaint c) { complaints.remove(c); }
    void addEstablishment(Establishment e) { establishments.add(e); }

    public int getDormId() { return dormId; }
    public String getDormName() { return dormName; }
    public String getLocation() { return location; }
    public PlusCode getPlusCode() { return plusCode; }
    public String getDescription() { return description; }
    public Landlord getOwner() { return owner; }
    public int getTotalSlots() { return totalSlots; }
    public int getAvailableSlots() { return availableSlots; }
    public double getRate() { return rate; }
    public String getRules() { return rules; }
    public LocalDateTime getDatePosted() { return datePosted; }
    public List<String> getAmenities() { return Collections.unmodifiableList(amenities); }
    public List<String> getPhotos() { return Collections.unmodifiableList(photos); }
    public ArrayList<Tenant> getTenants() { return new ArrayList<>(tenants); }
    public ArrayList<Tenant> getInterestedTenants() { return new ArrayList<>(interestedTenants); }
    public ArrayList<Announcement> getAnnouncements() { return new ArrayList<>(announcements); }
    public ArrayList<Establishment> getEstablishments() { return new ArrayList<>(establishments); }
    public ArrayList<Complaint> getComplaints() { return new ArrayList<>(complaints); }
    ArrayList<Application> getApplications() { return new ArrayList<>(applications); }

    @Override public boolean equals(Object o) { return o instanceof Dorm && ((Dorm) o).dormId == dormId; }
    @Override public int hashCode() { return Integer.hashCode(dormId); }
    @Override public String toString() { return dormName; }
}

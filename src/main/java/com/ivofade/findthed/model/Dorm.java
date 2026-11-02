package com.ivofade.findthed.model;

import com.ivofade.findthed.exception.InvalidInputException;
import com.ivofade.findthed.exception.NoAvailableSlotsException;
import com.ivofade.findthed.util.Validator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Dorm {

  private int dormId;
  private Landlord owner;
  private String dormName;
  private String location;
  private PlusCode plusCode;
  private String description;
  private int totalSlots;
  private int availableSlots;
  private double rate;
  private String rules;
  private LocalDateTime datePosted;

  private final ArrayList<Tenant> tenants = new ArrayList<>();
  private final ArrayList<String> amenities = new ArrayList<>();
  private final ArrayList<String> photos = new ArrayList<>();
  private final ArrayList<Tenant> interestedTenants = new ArrayList<>();
  private final ArrayList<Announcement> announcements = new ArrayList<>();
  private final ArrayList<Complaint> complaints = new ArrayList<>();

  public Dorm(Landlord owner, DormDetails details)
    throws InvalidInputException {
    this.owner = Validator.requiredReference(owner, "Owner");
    Validator.requiredReference(details, "Dorm details").validate();
    this.dormId = 0;
    this.dormName = details.getDormName();
    this.location = details.getLocation();
    this.plusCode = details.getPlusCode();
    this.description = details.getDescription();
    this.totalSlots = details.getTotalSlots();
    this.availableSlots = totalSlots;
    this.rate = details.getRate();
    this.rules = details.getRules();
    this.datePosted = LocalDateTime.now();
    this.amenities.addAll(details.getAmenities());
    this.photos.addAll(details.getPhotos());
  }

  // This overload represents an empty saved listing. Use the resident overload
  // for occupied listings so availability is restored without double counting.
  public Dorm(
    int dormId,
    Landlord owner,
    DormDetails details,
    LocalDateTime datePosted
  ) throws InvalidInputException {
    this(owner, details);
    this.dormId = Validator.savedId(dormId);
    this.datePosted = Validator.requiredReference(datePosted, "Posting date");
  }

  public Dorm(
    int dormId,
    Landlord owner,
    DormDetails details,
    LocalDateTime datePosted,
    List<Tenant> residents
  ) throws InvalidInputException {
    this(dormId, owner, details, datePosted);
    Validator.requiredReference(residents, "Residents");
    ArrayList<Tenant> checked = new ArrayList<>();
    for (Tenant resident : residents) {
      Validator.requiredReference(resident, "Resident");
      Validator.condition(
        checked.stream().noneMatch(t -> Person.sameIdentity(t, resident)),
        "Duplicate resident."
      );
      Validator.condition(
        resident.getCurrentDorm() == null ||
          Dorm.sameIdentity(resident.getCurrentDorm(), this),
        "Resident already belongs to another dorm."
      );
      checked.add(resident);
    }
    Validator.capacity(totalSlots, checked.size());
    tenants.addAll(checked);
    availableSlots = totalSlots - tenants.size();
    for (Tenant resident : tenants) resident.setCurrentDormInternal(this);
  }

  public void assignDormId(int generatedId) throws InvalidInputException {
    Validator.condition(dormId == 0, "Dorm ID is already assigned.");
    dormId = Validator.savedId(generatedId);
  }

  public void updateDetails(DormDetails details) throws InvalidInputException {
    Validator.requiredReference(details, "Dorm details").validate();
    Validator.capacity(details.getTotalSlots(), tenants.size());
    dormName = details.getDormName();
    location = details.getLocation();
    plusCode = details.getPlusCode();
    description = details.getDescription();
    totalSlots = details.getTotalSlots();
    availableSlots = totalSlots - tenants.size();
    rate = details.getRate();
    rules = details.getRules();
    amenities.clear();
    amenities.addAll(details.getAmenities());
    photos.clear();
    photos.addAll(details.getPhotos());
  }

  public void addTenant(Tenant tenant)
    throws InvalidInputException, NoAvailableSlotsException {
    Validator.requiredReference(tenant, "Tenant");
    if (tenants.stream().anyMatch(t -> Person.sameIdentity(t, tenant))) return;
    Validator.condition(
      tenant.getCurrentDorm() == null ||
        Dorm.sameIdentity(tenant.getCurrentDorm(), this),
      "Tenant must leave the current dorm before admission."
    );
    if (!hasAvailableSlots()) throw new NoAvailableSlotsException(
      "Dorm is full."
    );
    tenants.add(tenant);
    tenant.setCurrentDormInternal(this);
    availableSlots--;
    removeInterest(tenant);
  }

  public void removeTenant(Tenant tenant) {
    if (tenant == null) return;
    for (int i = 0; i < tenants.size(); i++) {
      Tenant resident = tenants.get(i);
      if (Person.sameIdentity(resident, tenant)) {
        tenants.remove(i);
        availableSlots++;
        if (Dorm.sameIdentity(resident.getCurrentDorm(), this)) {
          resident.setCurrentDormInternal(null);
        }
        if (Dorm.sameIdentity(tenant.getCurrentDorm(), this)) {
          tenant.setCurrentDormInternal(null);
        }
        return;
      }
    }
  }

  public void addInterest(Tenant tenant) throws InvalidInputException {
    Validator.requiredReference(tenant, "Tenant");
    Validator.condition(
      !Dorm.sameIdentity(tenant.getCurrentDorm(), this),
      "A resident cannot mark their current dorm as interested."
    );
    if (
      interestedTenants.stream().noneMatch(t -> Person.sameIdentity(t, tenant))
    ) {
      interestedTenants.add(tenant);
      tenant.addInterestInternal(this);
    }
  }

  public void removeInterest(Tenant tenant) {
    if (tenant == null) return;
    interestedTenants.removeIf(t -> {
      if (!Person.sameIdentity(t, tenant)) return false;
      t.removeInterestInternal(this);
      return true;
    });
    tenant.removeInterestInternal(this);
  }

  public void addAnnouncement(Announcement announcement)
    throws InvalidInputException {
    Validator.requiredReference(announcement, "Announcement");
    Validator.condition(
      Dorm.sameIdentity(announcement.getDorm(), this),
      "Announcement belongs to another dorm."
    );
    if (
      announcements
        .stream()
        .noneMatch(
          a ->
            a == announcement ||
            (a.getAnnouncementId() > 0 &&
              a.getAnnouncementId() == announcement.getAnnouncementId())
        )
    ) {
      announcements.add(announcement);
    }
  }

  public void addComplaint(Complaint complaint) throws InvalidInputException {
    Validator.requiredReference(complaint, "Complaint");
    Validator.condition(
      Dorm.sameIdentity(complaint.getDorm(), this),
      "Complaint belongs to another dorm."
    );
    if (!complaints.contains(complaint)) complaints.add(complaint);
  }

  public String displayInfo() {
    return (
      dormName +
      " | Rate: " +
      rate +
      " | Available: " +
      availableSlots +
      "/" +
      totalSlots
    );
  }

  public boolean hasAvailableSlots() {
    return availableSlots > 0;
  }

  public boolean hasTenants() {
    return !tenants.isEmpty();
  }

  public int getInterestedCount() {
    return interestedTenants.size();
  }

  public int getDormId() {
    return dormId;
  }

  public Landlord getOwner() {
    return owner;
  }

  public String getDormName() {
    return dormName;
  }

  public String getLocation() {
    return location;
  }

  public PlusCode getPlusCode() {
    return plusCode;
  }

  public String getDescription() {
    return description;
  }

  public int getTotalSlots() {
    return totalSlots;
  }

  public int getAvailableSlots() {
    return availableSlots;
  }

  public double getRate() {
    return rate;
  }

  public String getRules() {
    return rules;
  }

  public LocalDateTime getDatePosted() {
    return datePosted;
  }

  public List<Tenant> getTenants() {
    return Collections.unmodifiableList(new ArrayList<>(tenants));
  }

  public List<String> getAmenities() {
    return Collections.unmodifiableList(new ArrayList<>(amenities));
  }

  public List<String> getPhotos() {
    return Collections.unmodifiableList(new ArrayList<>(photos));
  }

  public List<Tenant> getInterestedTenants() {
    return Collections.unmodifiableList(new ArrayList<>(interestedTenants));
  }

  public List<Announcement> getAnnouncements() {
    return Collections.unmodifiableList(new ArrayList<>(announcements));
  }

  public List<Complaint> getComplaints() {
    return Collections.unmodifiableList(new ArrayList<>(complaints));
  }

  static boolean sameIdentity(Dorm first, Dorm second) {
    return (
      first == second ||
      (first != null &&
        second != null &&
        first.dormId > 0 &&
        first.dormId == second.dormId)
    );
  }
}

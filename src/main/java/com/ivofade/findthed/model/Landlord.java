package com.ivofade.findthed.model;

import com.ivofade.findthed.enums.DashboardType;
import com.ivofade.findthed.enums.Role;
import com.ivofade.findthed.exception.InvalidInputException;
import com.ivofade.findthed.exception.UnauthorizedActionException;
import com.ivofade.findthed.util.Validator;
import java.util.ArrayList;
import java.util.List;

public class Landlord extends Person {

  private String otherContact;
  private final ArrayList<Dorm> handledDorms = new ArrayList<>();

  // Constructor for registration
  public Landlord(
    String name,
    int age,
    String contactNum,
    String email,
    String rawPassword,
    String otherContact
  ) throws InvalidInputException {
    super(name, age, contactNum, email, rawPassword, Role.LANDLORD);
    setOtherContact(otherContact);
  }

  // Constructor for database
  public Landlord(
    int personId,
    String name,
    int age,
    String contactNum,
    String email,
    String passwordHash,
    String otherContact
  ) throws InvalidInputException {
    super(personId, name, age, contactNum, email, passwordHash, Role.LANDLORD);
    setOtherContact(otherContact);
  }

  public String getOtherContact() {
    return otherContact;
  }

  public final void setOtherContact(String otherContact)
    throws InvalidInputException {
    this.otherContact =
      otherContact == null ? null : Validator.otherContact(otherContact);
  }

  public ArrayList<Dorm> getMyDorms() {
    return new ArrayList<>(handledDorms);
  }

  @Override
  public DashboardType getDashboardType() {
    return DashboardType.LANDLORD_DASHBOARD;
  }

  public Landlord(
    String name,
    int age,
    String contactNum,
    String email,
    String rawPassword
  ) throws InvalidInputException {
    this(name, age, contactNum, email, rawPassword, null);
  }

  public boolean ownsDorm(Dorm dorm) {
    return dorm != null && Person.sameIdentity(this, dorm.getOwner());
  }

  private void requireOwnership(Dorm dorm) throws UnauthorizedActionException {
    if (!ownsDorm(dorm)) {
      throw new UnauthorizedActionException(
        "Dorm is not owned by this landlord."
      );
    }
  }

  public Dorm postDorm(DormDetails details) throws InvalidInputException {
    Dorm dorm = new Dorm(this, details);
    handledDorms.add(dorm);
    return dorm;
  }

  // Repository/service calls this after fully loading a listing.
  public void attachOwnedDorm(Dorm dorm) throws UnauthorizedActionException {
    requireOwnership(dorm);
    if (handledDorms.stream().noneMatch(d -> Dorm.sameIdentity(d, dorm))) {
      handledDorms.add(dorm);
    }
  }

  public void manageDorm(int dormId, DormDetails details)
    throws UnauthorizedActionException, InvalidInputException {
    Validator.savedId(dormId);
    for (Dorm dorm : handledDorms) {
      if (dorm.getDormId() == dormId) {
        requireOwnership(dorm);
        dorm.updateDetails(details);
        return;
      }
    }
    throw new UnauthorizedActionException(
      "Dorm is not in this landlord's listings."
    );
  }

  public void removeDorm(Dorm dorm)
    throws UnauthorizedActionException, InvalidInputException {
    requireOwnership(dorm);
    // Validate the tracked object as well as the caller's reference.
    for (Dorm tracked : handledDorms) {
      if (Dorm.sameIdentity(tracked, dorm)) {
        Validator.condition(
          !tracked.hasTenants() && !dorm.hasTenants(),
          "Cannot remove an occupied dorm."
        );
      }
    }
    Validator.condition(!dorm.hasTenants(), "Cannot remove an occupied dorm.");
    handledDorms.removeIf(d -> Dorm.sameIdentity(d, dorm));
  }

  public Announcement makeAnnouncement(Dorm dorm, String message)
    throws UnauthorizedActionException, InvalidInputException {
    requireOwnership(dorm);
    Announcement announcement = new Announcement(dorm, this, message);
    dorm.addAnnouncement(announcement);
    return announcement;
  }

  public List<Complaint> viewComplaints(Dorm dorm)
    throws UnauthorizedActionException {
    requireOwnership(dorm);
    return dorm.getComplaints();
  }

  public ArrayList<Application> reviewApplications(Dorm dorm)
    throws UnauthorizedActionException {
    requireOwnership(dorm);
    throw new UnsupportedOperationException(
      "Pending application loading belongs to issue [06]."
    );
  }

  public void acceptApplication(Application application)
    throws InvalidInputException, UnauthorizedActionException {
    Validator.requiredReference(application, "Application");
    requireOwnership(application.getDorm());
    application.accept(this);
  }

  public void rejectApplication(Application application)
    throws InvalidInputException, UnauthorizedActionException {
    Validator.requiredReference(application, "Application");
    requireOwnership(application.getDorm());
    application.reject(this);
  }

  @Override
  public String displayInfo() {
    return super.displayInfo() + " | Dorms: " + handledDorms.size();
  }
}

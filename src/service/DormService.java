package service;

import enums.Role;
import exception.AuthenticationException;
import exception.DormNotFoundException;
import exception.InvalidInputException;
import exception.NoAvailableSlotsException;
import exception.ServiceOperationException;
import exception.UnauthorizedActionException;
import model.Announcement;
import model.Application;
import model.Dorm;
import model.DormDetails;
import model.Landlord;
import model.Person;
import model.Tenant;
import repository.AnnouncementRepository;
import repository.ApplicationRepository;
import repository.DormRepository;
import repository.TenantRepository;
import util.Validator;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class DormService {

  private static final AuthService AUTH = new AuthService();
  private static final ApplicationReviewService APPLICATIONS = new ApplicationReviewService();
  private static final AnnouncementService ANNOUNCEMENTS = new AnnouncementService();
  private static final ComplaintService COMPLAINTS = new ComplaintService();
  private static final DashboardService DASHBOARD = new DashboardService();
  private static final DormRepository DORMS = new DormRepository();
  private static final TenantRepository TENANTS = new TenantRepository();
  private static final ApplicationRepository APPLICATION_REPO = new ApplicationRepository();
  private static final AnnouncementRepository ANNOUNCEMENT_REPO = new AnnouncementRepository();

  private DormService() {}

  public static void register(
    String role,
    String name,
    int age,
    String contactNum,
    String email,
    String password,
    String otherContact
  ) {
    Role parsedRole = parseRole(role);
    try {
      AUTH.register(parsedRole, name, age, contactNum, email, password, otherContact);
      refreshCurrentLandlord();
    } catch (SQLException | InvalidInputException | AuthenticationException
             | UnauthorizedActionException exception) {
      throw failure(exception);
    }
  }

  public static void login(String email, String password, String role) {
    Role parsedRole = parseRole(role);
    try {
      AUTH.login(email, password, parsedRole);
      refreshCurrentLandlord();
    } catch (SQLException | AuthenticationException | InvalidInputException
             | UnauthorizedActionException exception) {
      throw failure(exception);
    }
  }

  public static Person currentUser() {
    Person user = AUTH.currentUser();
    if (user == null || !user.isLoggedIn()) {
      throw new ServiceOperationException("Please sign in first.", null);
    }
    return user;
  }

  public static Tenant currentTenant() {
    Person user = currentUser();
    if (!(user instanceof Tenant)) {
      throw new ServiceOperationException("Sign in with a tenant account.", null);
    }
    return (Tenant) user;
  }

  public static Landlord currentLandlord() {
    Person user = currentUser();
    if (!(user instanceof Landlord)) {
      throw new ServiceOperationException("Sign in with a landlord account.", null);
    }
    refreshLandlord((Landlord) user);
    return (Landlord) user;
  }

  public static void logout() {
    try {
      AUTH.logout();
    } catch (SQLException exception) {
      throw failure(exception);
    }
  }

  public static List<Dorm> listings() {
    try {
      return DORMS.findAll();
    } catch (SQLException exception) {
      throw failure(exception);
    }
  }

  public static List<Application> getApplications(Tenant tenant) {
    try {
      requireSavedTenant(tenant);
      return APPLICATION_REPO.findByTenantId(tenant.getPersonId());
    } catch (SQLException | InvalidInputException exception) {
      throw failure(exception);
    }
  }

  public static Set<Integer> getInterestedDormIds(Tenant tenant) {
    try {
      requireSavedTenant(tenant);
      return TENANTS.findInterestedDormIds(tenant.getPersonId());
    } catch (SQLException | InvalidInputException exception) {
      throw failure(exception);
    }
  }

  public static void setInterested(Tenant tenant, Dorm dorm, boolean interested) {
    try {
      requireSavedTenant(tenant);
      Validator.requiredReference(dorm, "Dorm");
      Validator.savedId(dorm.getDormId());
      Tenant savedTenant = TENANTS.findById(tenant.getPersonId())
        .orElseThrow(() -> new SQLException("Tenant account no longer exists."));
      Dorm currentDorm = savedTenant.getCurrentDorm();
      Validator.condition(
        !interested || currentDorm == null || currentDorm.getDormId() != dorm.getDormId(),
        "A resident cannot mark their current dorm as interested."
      );
      TENANTS.setInterested(savedTenant.getPersonId(), dorm.getDormId(), interested);
    } catch (SQLException | InvalidInputException exception) {
      throw failure(exception);
    }
  }

  public static Application apply(Tenant tenant, Dorm dorm, boolean confirmTransfer) {
    try {
      requireSavedTenant(tenant);
      Validator.requiredReference(dorm, "Dorm");
      Validator.savedId(dorm.getDormId());
      Tenant savedTenant = TENANTS.findById(tenant.getPersonId())
        .orElseThrow(() -> new SQLException("Tenant account no longer exists."));
      Dorm savedDorm = DORMS.findById(dorm.getDormId())
        .orElseThrow(() -> new SQLException("Dorm listing no longer exists."));
      Application application = savedTenant.applyToDorm(savedDorm, confirmTransfer);
      APPLICATION_REPO.save(application);
      return application;
    } catch (SQLException | InvalidInputException exception) {
      throw failure(exception);
    }
  }

  public static List<Announcement> tenantAnnouncements(Tenant tenant) {
    try {
      requireSavedTenant(tenant);
      Tenant savedTenant = TENANTS.findById(tenant.getPersonId())
        .orElseThrow(() -> new SQLException("Tenant account no longer exists."));
      Dorm currentDorm = savedTenant.getCurrentDorm();
      return currentDorm == null ? List.of() : ANNOUNCEMENT_REPO.findByDormId(currentDorm.getDormId());
    } catch (SQLException | InvalidInputException exception) {
      throw failure(exception);
    }
  }

  public static void submitComplaint(Tenant tenant, String message, boolean anonymous) {
    try {
      COMPLAINTS.submitComplaint(tenant, message, anonymous);
    } catch (SQLException | InvalidInputException | DormNotFoundException exception) {
      throw failure(exception);
    }
  }

  public static List<Dorm> myDorms(Landlord landlord) {
    try {
      List<Dorm> dorms = new ListingService(landlord).getMyDorms(landlord);
      landlord.replaceOwnedDorms(dorms);
      return dorms;
    } catch (SQLException | InvalidInputException | UnauthorizedActionException exception) {
      throw failure(exception);
    }
  }

  public static List<Application> pending(Dorm dorm) {
    try {
      return APPLICATIONS.viewPendingApplications(dorm.getOwner(), dorm.getDormId());
    } catch (SQLException | InvalidInputException | UnauthorizedActionException exception) {
      throw failure(exception);
    }
  }

  public static void approve(Application application) {
    try {
      APPLICATIONS.acceptApplication(application.getDorm().getOwner(), application.getApplicationId());
      refreshLandlord(application.getDorm().getOwner());
    } catch (SQLException | InvalidInputException | UnauthorizedActionException
             | NoAvailableSlotsException exception) {
      throw failure(exception);
    }
  }

  public static void decline(Application application) {
    try {
      APPLICATIONS.rejectApplication(application.getDorm().getOwner(), application.getApplicationId());
    } catch (SQLException | InvalidInputException | UnauthorizedActionException exception) {
      throw failure(exception);
    }
  }

  public static void removeTenant(Dorm dorm, Tenant tenant) {
    try {
      ListingService.requireLandlord(dorm.getOwner());
      Validator.requiredReference(tenant, "Tenant");
      Validator.savedId(tenant.getPersonId());
      Validator.requiredReference(dorm, "Dorm");
      Validator.savedId(dorm.getDormId());
      DORMS.removeTenantFromDorm(dorm.getOwner(), dorm.getDormId(), tenant.getPersonId());
      refreshLandlord(dorm.getOwner());
    } catch (SQLException | InvalidInputException | UnauthorizedActionException exception) {
      throw failure(exception);
    }
  }

  public static void postDorm(Landlord landlord, DormDetails details) {
    try {
      new ListingService(landlord).postDorm(landlord, details);
      refreshLandlord(landlord);
    } catch (SQLException | InvalidInputException | UnauthorizedActionException
             | java.io.IOException exception) {
      throw failure(exception);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw failure(exception);
    }
  }

  public static void createListing(
    Landlord landlord,
    String name,
    String location,
    String plusCode,
    String description,
    int totalSlots,
    double rate,
    List<String> amenities,
    String rules,
    List<String> photos
  ) {
    try {
      postDorm(landlord, new DormDetails(
        name, location, plusCode, description, totalSlots, rate, amenities, rules, photos));
    } catch (InvalidInputException exception) {
      throw failure(exception);
    }
  }

  public static void removeDorm(Landlord landlord, Dorm dorm) {
    try {
      new ListingService(landlord).removeDorm(dorm.getDormId());
      refreshLandlord(landlord);
    } catch (SQLException | InvalidInputException | UnauthorizedActionException exception) {
      throw failure(exception);
    }
  }

  public static void publishAnnouncement(Landlord landlord, Dorm dorm, String message) {
    try {
      ANNOUNCEMENTS.postAnnouncement(landlord, dorm.getDormId(), message);
    } catch (SQLException | InvalidInputException | UnauthorizedActionException exception) {
      throw failure(exception);
    }
  }

  public static List<Map<String, String>> complaints(Landlord landlord, Dorm dorm) {
    try {
      return COMPLAINTS.getComplaints(landlord, dorm);
    } catch (SQLException | InvalidInputException | UnauthorizedActionException exception) {
      throw failure(exception);
    }
  }

  public static List<Announcement> landlordAnnouncements(Landlord landlord) {
    List<Announcement> result = new ArrayList<>();
    for (Dorm dorm : myDorms(landlord)) {
      try {
        result.addAll(ANNOUNCEMENT_REPO.findByDormId(dorm.getDormId()));
      } catch (SQLException exception) {
        throw failure(exception);
      }
    }
    result.sort((first, second) -> second.getDatePosted().compareTo(first.getDatePosted()));
    return result;
  }

  public static int totalTenants(Landlord landlord) {
    try {
      return DASHBOARD.getSummary(landlord).getOccupiedSlots();
    } catch (SQLException | InvalidInputException exception) {
      throw failure(exception);
    }
  }

  public static int totalComplaints(Landlord landlord) {
    int total = 0;
    for (Dorm dorm : myDorms(landlord)) {
      total += complaints(landlord, dorm).size();
    }
    return total;
  }

  private static void refreshCurrentLandlord() throws SQLException, InvalidInputException,
    UnauthorizedActionException {
    Person user = AUTH.currentUser();
    if (user instanceof Landlord) {
      refreshLandlord((Landlord) user);
    }
  }

  private static Role parseRole(String role) {
    try {
      if (role == null) {
        throw new IllegalArgumentException("Role is required.");
      }
      return Role.valueOf(role.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException exception) {
      throw new ServiceOperationException("Choose a valid account type.", exception);
    }
  }

  private static void refreshLandlord(Landlord landlord) {
    myDorms(landlord);
  }

  private static void requireSavedTenant(Tenant tenant) throws InvalidInputException {
    Validator.requiredReference(tenant, "Tenant");
    Validator.savedId(tenant.getPersonId());
  }

  private static ServiceOperationException failure(Exception exception) {
    String message = exception.getMessage();
    return new ServiceOperationException(
      message == null || message.isBlank() ? "The operation could not be completed." : message,
      exception
    );
  }
}

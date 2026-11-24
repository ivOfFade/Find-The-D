package service;

import exception.DormNotFoundException;
import exception.InvalidInputException;
import exception.UnauthorizedActionException;
import model.Announcement;
import model.Application;
import model.Complaint;
import model.Dorm;
import model.Tenant;
import model.Landlord;
import repository.AnnouncementRepository;
import repository.ApplicationRepository;
import repository.ComplaintRepository;
import repository.DormRepository;
import repository.TenantRepository;
import util.DatabaseConnection;
import util.Validator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TenantService {

  private final TenantRepository tenants = new TenantRepository();
  private final DormRepository dorms = new DormRepository();
  private final ApplicationRepository applications = new ApplicationRepository();
  private final AnnouncementRepository announcements = new AnnouncementRepository();
  private final ComplaintRepository complaints = new ComplaintRepository();

  public List<Dorm> getListings() throws SQLException {
    return dorms.findAll();
  }

  public List<Application> getApplications(Tenant tenant) throws SQLException, InvalidInputException {
    requireSavedTenant(tenant);
    return applications.findByTenantId(tenant.getPersonId());
  }

  public Set<Integer> getInterestedDormIds(Tenant tenant) throws SQLException, InvalidInputException {
    requireSavedTenant(tenant);
    Set<Integer> result = new HashSet<>();
    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(
           "SELECT dorm_id FROM interested_dorms WHERE tenant_id = ? ORDER BY dorm_id")) {
      statement.setInt(1, tenant.getPersonId());
      try (var rows = statement.executeQuery()) {
        while (rows.next()) {
          result.add(rows.getInt("dorm_id"));
        }
      }
    }
    return Set.copyOf(result);
  }

  public void setInterested(Tenant tenant, Dorm dorm, boolean interested)
    throws SQLException, InvalidInputException {
    requireSavedTenant(tenant);
    Validator.requiredReference(dorm, "Dorm");
    Validator.savedId(dorm.getDormId());
    Tenant savedTenant = tenants.findById(tenant.getPersonId())
      .orElseThrow(() -> new SQLException("Tenant account no longer exists."));
    Dorm currentDorm = savedTenant.getCurrentDorm();
    Validator.condition(
      !interested || currentDorm == null || currentDorm.getDormId() != dorm.getDormId(),
      "A resident cannot mark their current dorm as interested."
    );
    String sql = interested
      ? "INSERT INTO interested_dorms (tenant_id, dorm_id) VALUES (?, ?) ON CONFLICT DO NOTHING"
      : "DELETE FROM interested_dorms WHERE tenant_id = ? AND dorm_id = ?";
    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setInt(1, savedTenant.getPersonId());
      statement.setInt(2, dorm.getDormId());
      statement.executeUpdate();
    }
  }

  public Application apply(Tenant tenant, Dorm dorm, boolean confirmTransfer)
    throws SQLException, InvalidInputException {
    requireSavedTenant(tenant);
    Validator.requiredReference(dorm, "Dorm");
    Validator.savedId(dorm.getDormId());
    Tenant savedTenant = tenants.findById(tenant.getPersonId())
      .orElseThrow(() -> new SQLException("Tenant account no longer exists."));
    Dorm savedDorm = dorms.findById(dorm.getDormId())
      .orElseThrow(() -> new SQLException("Dorm listing no longer exists."));
    Application application = savedTenant.applyToDorm(savedDorm, confirmTransfer);
    applications.save(application);
    return application;
  }

  public List<Announcement> getAnnouncements(Tenant tenant) throws SQLException, InvalidInputException {
    requireSavedTenant(tenant);
    Tenant savedTenant = tenants.findById(tenant.getPersonId())
      .orElseThrow(() -> new SQLException("Tenant account no longer exists."));
    Dorm currentDorm = savedTenant.getCurrentDorm();
    if (currentDorm == null) {
      return List.of();
    }
    return announcements.findByDormId(currentDorm.getDormId());
  }

  public Complaint submitComplaint(Tenant tenant, String message, boolean anonymous)
    throws SQLException, InvalidInputException, DormNotFoundException {
    requireSavedTenant(tenant);
    Tenant savedTenant = tenants.findById(tenant.getPersonId())
      .orElseThrow(() -> new SQLException("Tenant account no longer exists."));
    Complaint complaint = savedTenant.complain(message, anonymous);
    complaints.save(complaint);
    return complaint;
  }

  public void removeTenant(Landlord landlord, Dorm dorm, Tenant tenant)
    throws SQLException, InvalidInputException, UnauthorizedActionException {
    ListingService.requireLandlord(landlord);
    Validator.requiredReference(tenant, "Tenant");
    Validator.savedId(tenant.getPersonId());
    Validator.requiredReference(dorm, "Dorm");
    Validator.savedId(dorm.getDormId());
    try (Connection connection = DatabaseConnection.getConnection()) {
      connection.setAutoCommit(false);
      try {
        new ListingService(landlord).findOwnedDorm(connection, dorm.getDormId(), landlord, true);
        try (PreparedStatement statement = connection.prepareStatement(
          "UPDATE tenants SET current_dorm_id = NULL WHERE person_id = ? AND current_dorm_id = ?")) {
          statement.setInt(1, tenant.getPersonId());
          statement.setInt(2, dorm.getDormId());
          if (statement.executeUpdate() != 1) {
            throw new InvalidInputException("Tenant is no longer assigned to this dorm.");
          }
        }
        try (PreparedStatement statement = connection.prepareStatement(
          "UPDATE dorms SET available_slots = total_slots - "
            + "(SELECT COUNT(*) FROM tenants WHERE current_dorm_id = ?) WHERE dorm_id = ?")) {
          statement.setInt(1, dorm.getDormId());
          statement.setInt(2, dorm.getDormId());
          if (statement.executeUpdate() != 1) {
            throw new SQLException("Dorm listing no longer exists.");
          }
        }
        connection.commit();
      } catch (SQLException | InvalidInputException | UnauthorizedActionException | RuntimeException exception) {
        try {
          connection.rollback();
        } catch (SQLException rollbackError) {
          exception.addSuppressed(rollbackError);
        }
        throw exception;
      }
    }
  }

  private static void requireSavedTenant(Tenant tenant) throws InvalidInputException {
    Validator.requiredReference(tenant, "Tenant");
    Validator.savedId(tenant.getPersonId());
  }
}

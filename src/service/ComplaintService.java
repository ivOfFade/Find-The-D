package service;

import exception.DormNotFoundException;
import exception.InvalidInputException;
import exception.UnauthorizedActionException;
import model.Complaint;
import model.Dorm;
import model.Landlord;
import model.Tenant;
import repository.ComplaintRepository;
import repository.DormRepository;
import repository.TenantRepository;
import util.DatabaseConnection;
import util.Validator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ComplaintService {

  private final ComplaintRepository complaints = new ComplaintRepository();
  private final TenantRepository tenants = new TenantRepository();

  public Complaint submitComplaint(Tenant tenant, String message, boolean anonymous)
      throws SQLException, InvalidInputException, DormNotFoundException {
    Validator.requiredReference(tenant, "Tenant");
    Validator.savedId(tenant.getPersonId());
    Tenant savedTenant = tenants.findById(tenant.getPersonId())
      .orElseThrow(() -> new SQLException("Tenant account no longer exists."));
    Complaint complaint = savedTenant.complain(message, anonymous);
    complaints.save(complaint);
    return complaint;
  }

  public List<Map<String, String>> getComplaints(Landlord landlord, Dorm dorm)
      throws SQLException, InvalidInputException, UnauthorizedActionException {
    ListingService.requireLandlord(landlord);
    Validator.requiredReference(dorm, "Dorm");
    Validator.savedId(dorm.getDormId());
    try (Connection connection = DatabaseConnection.getConnection()) {
      new DormRepository().findOwnedDorm(connection, dorm.getDormId(), landlord, false);
      return findComplaints(connection, dorm.getDormId(), landlord.getPersonId());
    }
  }

  private List<Map<String, String>> findComplaints(Connection connection, int dormId, int ownerId)
      throws SQLException {
    // Never SELECT sender_id or join persons here, even for nonanonymous messages.
    String sql = "SELECT c.complaint_id, c.message, c.date_posted FROM complaints c "
      + "JOIN dorms d ON c.dorm_id = d.dorm_id WHERE d.dorm_id = ? AND d.owner_id = ? "
      + "ORDER BY c.date_posted DESC, c.complaint_id DESC";
    List<Map<String, String>> result = new ArrayList<>();
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setInt(1, dormId);
      statement.setInt(2, ownerId);
      try (ResultSet rows = statement.executeQuery()) {
        while (rows.next()) {
          result.add(Map.of(
            "complaintId", Integer.toString(rows.getInt(1)),
            "message", rows.getString(2),
            "datePosted", rows.getTimestamp(3).toLocalDateTime().toString()));
        }
      }
    }
    return List.copyOf(result);
  }
}
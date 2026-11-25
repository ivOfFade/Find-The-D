package service;

import exception.DormNotFoundException;
import exception.InvalidInputException;
import exception.UnauthorizedActionException;
<<<<<<< HEAD
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
=======
>>>>>>> 5bb4896 (refactor: consolidate services and update repositories)
import model.Complaint;
import model.Dorm;
import model.Landlord;
import model.Tenant;
import repository.ComplaintRepository;
import repository.DormRepository;
import repository.TenantRepository;
<<<<<<< HEAD
import util.Validator;

public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final DormRepository dormRepository;
    private final TenantRepository tenantRepository;

    public ComplaintService() {
        complaintRepository = new ComplaintRepository();
        dormRepository = new DormRepository();
        tenantRepository = new TenantRepository();
    }

    public Complaint submitComplaint(
        Tenant tenant,
        String message,
        boolean anonymous
    ) throws InvalidInputException, DormNotFoundException, SQLException {

        Validator.requiredReference(tenant, "Tenant");
        Validator.savedId(tenant.getPersonId());

        Tenant currentTenant = tenantRepository.findById(
            tenant.getPersonId()
        ).orElseThrow(
            () -> new InvalidInputException("Tenant was not found.")
        );

        Dorm dorm = currentTenant.viewMyDorm();

        Complaint complaint = new Complaint(
            dorm,
            currentTenant,
            message,
            anonymous
        );

        complaintRepository.save(complaint);
        dorm.addComplaint(complaint);

        return complaint;
    }

    public List<Map<String, String>> viewComplaints(
        Landlord landlord,
        int dormId
    ) throws InvalidInputException, UnauthorizedActionException, SQLException {

        Validator.requiredReference(landlord, "Landlord");
        Validator.savedId(landlord.getPersonId());
        Validator.savedId(dormId);

        Dorm dorm = dormRepository.findById(dormId).orElseThrow(
            () -> new UnauthorizedActionException(
                "Dorm does not exist or is not owned by this landlord."
            )
        );

        if (!landlord.ownsDorm(dorm)) {
            throw new UnauthorizedActionException(
                "Dorm is not owned by this landlord."
            );
        }

        return complaintRepository.findForOwner(
            dormId,
            landlord.getPersonId()
        );
    }
}
=======
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
>>>>>>> 5bb4896 (refactor: consolidate services and update repositories)

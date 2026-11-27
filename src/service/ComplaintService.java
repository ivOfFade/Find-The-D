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
import java.sql.SQLException;
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
      return complaints.findForOwner(dorm.getDormId(), landlord.getPersonId());
    }
  }
}
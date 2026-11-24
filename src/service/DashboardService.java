package service;

import exception.InvalidInputException;
import exception.UnauthorizedActionException;
import java.util.Map;
import model.Landlord;
import util.DatabaseConnection;
import java.sql.*;

public class DashboardService {

    public Map<String, Integer> getSummary(Landlord landlord)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        ListingService.requireLandlord(landlord);
        try (Connection connection = DatabaseConnection.getConnection()) {
            return readSummary(connection, landlord.getPersonId());
        }
    }

    private Map<String, Integer> readSummary(Connection connection, int ownerId) throws SQLException {
        String sql = "SELECT COUNT(*) AS dorm_count, COALESCE(SUM(total_slots), 0) AS total, "
            + "(SELECT COUNT(*) FROM tenants t JOIN dorms d ON t.current_dorm_id = d.dorm_id "
            + "WHERE d.owner_id = ?) AS occupied, "
            + "(SELECT COUNT(*) FROM applications a JOIN dorms d ON a.dorm_id = d.dorm_id "
            + "WHERE d.owner_id = ? AND a.status = 'PENDING') AS pending, "
            + "(SELECT COUNT(*) FROM complaints c JOIN dorms d ON c.dorm_id = d.dorm_id "
            + "WHERE d.owner_id = ?) AS complaints FROM dorms WHERE owner_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ownerId);
            statement.setInt(2, ownerId);
            statement.setInt(3, ownerId);
            statement.setInt(4, ownerId);
            try (ResultSet row = statement.executeQuery()) {
                row.next();
                int total = row.getInt("total");
                int occupied = row.getInt("occupied");
                return Map.of("dormCount", row.getInt("dorm_count"), "totalSlots", total,
                    "occupiedSlots", occupied, "availableSlots", total - occupied,
                    "pendingApplications", row.getInt("pending"),
                    "complaintCount", row.getInt("complaints"));
            }
        }
    }
}

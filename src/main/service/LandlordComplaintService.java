package service;

import exception.InvalidInputException;
import exception.UnauthorizedActionException;
import java.util.Map;
import java.util.ArrayList;
import model.Dorm;
import model.Landlord;
import util.DatabaseConnection;
import util.Validator;
import java.sql.*;
import java.util.List;

public class LandlordComplaintService {

    public List<Map<String, String>> getComplaints(Landlord landlord, Dorm dorm)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        ListingService.requireLandlord(landlord);
        Validator.requiredReference(dorm, "Dorm");
        Validator.savedId(dorm.getDormId());
        try (Connection connection = DatabaseConnection.getConnection()) {
            new ListingService(landlord).findOwnedDorm(connection, dorm.getDormId(), landlord, false);
            return findComplaints(connection, dorm.getDormId(), landlord.getPersonId());
        }
    }

    private List<Map<String, String>> findComplaints(Connection connection, int dormId, int ownerId)
        throws SQLException {
        // Never SELECT sender_id or join persons here, even for nonanonymous messages.
        String sql = "SELECT c.complaint_id, c.message, c.date_posted FROM complaints c "
            + "JOIN dorms d ON c.dorm_id = d.dorm_id WHERE d.dorm_id = ? AND d.owner_id = ? "
            + "ORDER BY c.date_posted DESC, c.complaint_id DESC";
        List<Map<String, String>> complaints = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, dormId);
            statement.setInt(2, ownerId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) complaints.add(Map.of(
                    "complaintId", Integer.toString(rows.getInt(1)),
                    "message", rows.getString(2),
                    "datePosted", rows.getTimestamp(3).toLocalDateTime().toString()));
            }
        }
        return List.copyOf(complaints);
    }
}

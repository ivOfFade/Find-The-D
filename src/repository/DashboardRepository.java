package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import exception.InvalidInputException;
import model.DashboardSummary;
import util.DatabaseConnection;

public class DashboardRepository {

    public DashboardSummary getSummary(int landlordId) throws SQLException {
        String sql =
            "SELECT " +
            "  (SELECT COUNT(*) FROM dorms d " +
            "   WHERE d.owner_id = ?) AS listing_count, " +

            "  (SELECT COUNT(*) FROM tenants t " +
            "   JOIN dorms d ON t.current_dorm_id = d.dorm_id " +
            "   WHERE d.owner_id = ?) AS occupied_slots, " +

            "  (SELECT COALESCE(SUM(d.total_slots), 0) FROM dorms d " +
            "   WHERE d.owner_id = ?) AS total_slots, " +

            "  (SELECT COUNT(*) FROM applications a " +
            "   JOIN dorms d ON a.dorm_id = d.dorm_id " +
            "   WHERE d.owner_id = ? AND a.status = 'PENDING') " +
            "   AS pending_applications";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, landlordId);
            ps.setInt(2, landlordId);
            ps.setInt(3, landlordId);
            ps.setInt(4, landlordId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Could not load dashboard summary.");
                }

                try {
                    return new DashboardSummary(
                        rs.getInt("listing_count"),
                        rs.getInt("occupied_slots"),
                        rs.getInt("total_slots"),
                        rs.getInt("pending_applications")
                    );
                } catch (InvalidInputException e) {
                    throw new SQLException("Invalid dashboard data.", e);
                }
            }
        }
    }
}
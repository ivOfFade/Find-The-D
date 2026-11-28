package repository;

import exception.InvalidInputException;
import model.Complaint;
import model.Dorm;
import model.Tenant;
import util.DatabaseConnection;
import java.sql.*;
import java.util.*;

public class ComplaintRepository implements RepositoryInterface<Complaint, Integer> {

    private static final String SELECT = "SELECT c.* FROM complaints c";

    private Complaint map(ResultSet rs) throws SQLException {

        int dormId = rs.getInt("dorm_id");
        Dorm dorm = new DormRepository().findById(dormId).orElseThrow(() -> new SQLException("Dorm not found: " + dormId));

        int senderId = rs.getInt("sender_id");
        boolean senderMissing = rs.wasNull();
        Tenant sender = senderMissing ? null : new TenantRepository().findById(senderId)
            .orElseThrow(() -> new SQLException("Sender not found: " + senderId));

        try {
            return new Complaint(
                rs.getInt("complaint_id"),
                dorm,
                sender,
                rs.getString("message"),
                rs.getBoolean("anonymous"),
                rs.getTimestamp("date_posted").toLocalDateTime(),
                (Integer) rs.getObject("reporter_id")
            );
        } catch (InvalidInputException e) {
            throw new SQLException(
                "Invalid data in a complaint row: " + e.getMessage(), e
            );
        }
    }

    @Override
    public void save(Complaint c) throws SQLException {
        if (c.getComplaintId() != 0) {
            throw new IllegalArgumentException(
                "This complaint already has an ID. Use update() instead."
            );
        }

        String sql = "INSERT INTO complaints (dorm_id, sender_id, reporter_id, message, anonymous) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            int complaintId;

            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        sql, new String[] {"complaint_id"})) {

                    ps.setInt(1, c.getDorm().getDormId());

                    setNullableInt(ps, 2, c.isAnonymous() ? null : c.getSender().getPersonId());
                    setNullableInt(ps, 3, c.getReporterId());
                    ps.setString(4, c.getMessage());
                    ps.setBoolean(5, c.isAnonymous());

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Complaint was not saved.");
                    }

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                "No complaint ID was returned."
                            );
                        }

                        complaintId = keys.getInt(1);
                    }
                }

                conn.commit();
            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;
            }

            // Assign the ID only after the transaction succeeds.
            try {
                c.assignComplaintId(complaintId);
            } catch (InvalidInputException e) {
                throw new SQLException(
                    "Could not assign the generated complaint ID.",
                    e
                );
            }
        }
    }

    @Override
    public Optional<Complaint> findById(Integer id) throws SQLException {
        String sql = SELECT + " WHERE c.complaint_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                    ? Optional.of(map(rs))
                    : Optional.empty();
            }
        }
    }

    @Override
    public List<Complaint> findAll() throws SQLException {
        List<Complaint> complaints = new ArrayList<>();
        String sql = SELECT + " ORDER BY c.date_posted DESC, c.complaint_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) complaints.add(map(rs));

        }

        return complaints;
    }

    // Load the complaints belonging to one dorm.
    public List<Complaint> findByDormId(int dormId) throws SQLException {
        List<Complaint> complaints = new ArrayList<>();
        String sql = SELECT + " WHERE c.dorm_id = ? ORDER BY c.date_posted DESC, c.complaint_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dormId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) complaints.add(map(rs));
            }
        }

        return complaints;
    }

    public List<Map<String, String>> findForOwner(int dormId, int ownerId)
        throws SQLException {

        String sql =
            "SELECT c.complaint_id, c.sender_id, c.message, c.date_posted, " +
            "CASE " +
            "  WHEN c.anonymous THEN 'Anonymous' " +
            "  WHEN p.person_id IS NULL THEN 'Deleted tenant' " +
            "  ELSE p.name " +
            "END AS sender_name " +
            "FROM complaints c " +
            "JOIN dorms d ON c.dorm_id = d.dorm_id " +
            "LEFT JOIN persons p ON c.sender_id = p.person_id " +
            "WHERE d.dorm_id = ? AND d.owner_id = ? AND c.resolved = false " +
            "ORDER BY c.date_posted DESC, c.complaint_id DESC";

        List<Map<String, String>> complaints = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dormId);
            ps.setInt(2, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    complaints.add(Map.of(
                        "complaintId", Integer.toString(
                            rs.getInt("complaint_id")
                        ),
                        "senderName", rs.getString("sender_name"),
                        "message", rs.getString("message"),
                        "datePosted", rs.getTimestamp("date_posted")
                            .toLocalDateTime().toString(),
                        "senderId", rs.getString("sender_id") == null ? "" : rs.getString("sender_id")
                    ));
                }
            }
        }

        return List.copyOf(complaints);
    }

    @Override
    public void update(Complaint c) throws SQLException {
        if (c.getComplaintId() <= 0) {
            throw new IllegalArgumentException(
                "Save the complaint before updating it."
            );
        }

        String sql = "UPDATE complaints SET message = ? WHERE complaint_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getMessage());
            ps.setInt(2, c.getComplaintId());

            if (ps.executeUpdate() != 1) {
                throw new SQLException(
                    "Complaint not found: " + c.getComplaintId()
                );
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM complaints WHERE complaint_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void resolve(int complaintId, int ownerId, Integer tenantId, String dormName) throws SQLException {
        String sql = "UPDATE complaints c SET resolved = true, resolved_at = now() "
            + "FROM dorms d WHERE c.complaint_id = ? AND c.dorm_id = d.dorm_id "
            + "AND d.owner_id = ? AND c.resolved = false";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, complaintId);
                    ps.setInt(2, ownerId);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Complaint not found or already resolved.");
                    }
                }
                if (tenantId != null) {
                    try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO complaint_notifications (complaint_id, tenant_id, message) VALUES (?, ?, ?)")) {
                        ps.setInt(1, complaintId);
                        ps.setInt(2, tenantId);
                        ps.setString(3, "Your complaint about " + dormName + " has been resolved.");
                        ps.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    public List<Map<String, String>> findNotificationsForTenant(int tenantId)
        throws SQLException {
        String sql = "SELECT notification_id, message, date_posted FROM complaint_notifications "
            + "WHERE tenant_id = ? ORDER BY date_posted DESC, notification_id DESC";
        List<Map<String, String>> notifications = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notifications.add(Map.of(
                        "notificationId", Integer.toString(rs.getInt("notification_id")),
                        "message", rs.getString("message"),
                        "datePosted", rs.getTimestamp("date_posted").toLocalDateTime().toString()
                    ));
                }
            }
        }
        return List.copyOf(notifications);
    }


    private static void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

}

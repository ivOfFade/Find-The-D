package repository;

import enums.ApplicationStatus;
import exception.InvalidInputException;
import exception.UnauthorizedActionException;
import model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// SQL for Dylan's landlord services. Every operation uses the caller's connection.
public class LandlordServiceRepository {
    // Listings and dashboard.

    public List<Dorm> findMyDorms(Connection connection, Landlord owner)
        throws SQLException, InvalidInputException {
        List<Dorm> dorms = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM dorms WHERE owner_id = ? ORDER BY dorm_id")) {
            statement.setInt(1, owner.getPersonId());
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) dorms.add(readDorm(connection, rows, owner));
            }
        }
        return List.copyOf(dorms);
    }

    public Dorm findOwnedDorm(Connection connection, int dormId, Landlord owner, boolean lock)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        String sql = "SELECT * FROM dorms WHERE dorm_id = ? AND owner_id = ?"
            + (lock ? " FOR UPDATE" : "");
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, dormId);
            statement.setInt(2, owner.getPersonId());
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) throw new UnauthorizedActionException("Dorm is not in your listings.");
                return readDorm(connection, row, owner);
            }
        }
    }

    // Used for browsing and for restoring a tenant's existing dorm during transfer.
    // Landlord access to a target dorm must always use findOwnedDorm instead.
    public Dorm findPreviousDorm(Connection connection, int dormId)
        throws SQLException, InvalidInputException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM dorms WHERE dorm_id = ?")) {
            statement.setInt(1, dormId);
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) return null;
                Landlord owner = readLandlord(connection, row.getInt("owner_id"));
                return readDorm(connection, row, owner);
            }
        }
    }

    private Dorm readDorm(Connection connection, ResultSet row, Landlord owner)
        throws SQLException, InvalidInputException {
        int id = row.getInt("dorm_id");
        DormDetails details = new DormDetails(row.getString("dorm_name"), row.getString("location"),
            row.getString("plus_code"), row.getString("description"), row.getInt("total_slots"),
            row.getDouble("rate"), readStrings(connection, "dorm_amenities", "amenity", id),
            row.getString("rules"), readStrings(connection, "dorm_photos", "photo_url", id));
        return new Dorm(id, owner, details, row.getTimestamp("date_posted").toLocalDateTime(),
            readResidents(connection, id));
    }

    private List<String> readStrings(Connection connection, String table, String column, int dormId)
        throws SQLException {
        // Table/column names are private constants at the call sites, never user input.
        List<String> values = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT " + column + " FROM " + table + " WHERE dorm_id = ? ORDER BY " + column)) {
            statement.setInt(1, dormId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) values.add(rows.getString(1));
            }
        }
        return values;
    }

    public Dorm insertDorm(Connection connection, Dorm dorm) throws SQLException, InvalidInputException {
        String sql = "INSERT INTO dorms (owner_id, dorm_name, location, plus_code, description, "
            + "total_slots, available_slots, rate, rules, date_posted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
            + "RETURNING dorm_id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, dorm.getOwner().getPersonId());
            setDetails(statement, dorm, 2);
            statement.setTimestamp(10, Timestamp.valueOf(dorm.getDatePosted()));
            try (ResultSet row = statement.executeQuery()) {
                row.next();
                dorm.assignDormId(row.getInt(1));
            }
        }
        replaceLists(connection, dorm);
        return dorm;
    }

    public void updateDorm(Connection connection, Dorm dorm) throws SQLException {
        String sql = "UPDATE dorms SET dorm_name = ?, location = ?, plus_code = ?, description = ?, "
            + "total_slots = ?, available_slots = ?, rate = ?, rules = ? WHERE dorm_id = ? AND owner_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            setDetails(statement, dorm, 1);
            statement.setInt(9, dorm.getDormId());
            statement.setInt(10, dorm.getOwner().getPersonId());
            if (statement.executeUpdate() != 1) throw new SQLException("Listing changed while saving.");
        }
        replaceLists(connection, dorm);
    }

    private void setDetails(PreparedStatement statement, Dorm dorm, int start) throws SQLException {
        statement.setString(start, dorm.getDormName());
        statement.setString(start + 1, dorm.getLocation());
        statement.setString(start + 2, dorm.getPlusCode() == null ? null : dorm.getPlusCode().getCode());
        statement.setString(start + 3, dorm.getDescription());
        statement.setInt(start + 4, dorm.getTotalSlots());
        statement.setInt(start + 5, dorm.getAvailableSlots());
        statement.setDouble(start + 6, dorm.getRate());
        statement.setString(start + 7, dorm.getRules());
    }

    private void replaceLists(Connection connection, Dorm dorm) throws SQLException {
        replaceStrings(connection, "dorm_amenities", "amenity", dorm.getDormId(), dorm.getAmenities());
        replaceStrings(connection, "dorm_photos", "photo_url", dorm.getDormId(), dorm.getPhotos());
    }

    private void replaceStrings(Connection connection, String table, String column, int dormId,
                                List<String> values) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM " + table + " WHERE dorm_id = ?")) {
            statement.setInt(1, dormId);
            statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO " + table + " (dorm_id, " + column + ") VALUES (?, ?)")) {
            for (String value : values) {
                statement.setInt(1, dormId);
                statement.setString(2, value);
                statement.executeUpdate();
            }
        }
    }

    public void deleteDorm(Connection connection, int dormId, int ownerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM dorms WHERE dorm_id = ? AND owner_id = ? "
                + "AND NOT EXISTS (SELECT 1 FROM tenants WHERE current_dorm_id = dorms.dorm_id)")) {
            statement.setInt(1, dormId);
            statement.setInt(2, ownerId);
            if (statement.executeUpdate() != 1) throw new SQLException("Listing changed while removing.");
        }
    }

    public DashboardSummary getSummary(Connection connection, int ownerId) throws SQLException {
        String sql = "SELECT COUNT(*) AS dorm_count, COALESCE(SUM(total_slots), 0) AS total, "
            + "(SELECT COUNT(*) FROM tenants t JOIN dorms d ON t.current_dorm_id = d.dorm_id "
            + "WHERE d.owner_id = ?) AS occupied, "
            + "(SELECT COUNT(*) FROM applications a JOIN dorms d ON a.dorm_id = d.dorm_id "
            + "WHERE d.owner_id = ? AND a.status = 'PENDING') AS pending FROM dorms WHERE owner_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ownerId);
            statement.setInt(2, ownerId);
            statement.setInt(3, ownerId);
            try (ResultSet row = statement.executeQuery()) {
                row.next();
                int total = row.getInt("total");
                int occupied = row.getInt("occupied");
                return new DashboardSummary(row.getInt("dorm_count"), total, occupied,
                    total - occupied, row.getInt("pending"));
            }
        }
    }

    public void lockDorms(Connection connection, int targetId, int previousId) throws SQLException {
        // Two landlords accepting opposite transfers must lock dorms in the same order.
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT dorm_id FROM dorms WHERE dorm_id IN (?, ?) ORDER BY dorm_id FOR UPDATE")) {
            statement.setInt(1, targetId);
            statement.setInt(2, previousId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) { /* Reading acquires each row lock. */ }
            }
        }
    }

    public void refreshAvailability(Connection connection, int dormId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE dorms SET available_slots = total_slots - "
                + "(SELECT COUNT(*) FROM tenants WHERE current_dorm_id = ?) WHERE dorm_id = ?")) {
            statement.setInt(1, dormId);
            statement.setInt(2, dormId);
            if (statement.executeUpdate() != 1) throw new SQLException("Dorm no longer exists.");
        }
    }

    // Account loading and residency updates.

    private Landlord readLandlord(Connection connection, int id)
        throws SQLException, InvalidInputException {
        String sql = "SELECT p.*, l.other_contact FROM persons p "
            + "JOIN landlords l ON p.person_id = l.person_id WHERE p.person_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) return null;
                return new Landlord(id, row.getString("name"), row.getInt("age"),
                    row.getString("contact_num"), row.getString("email"),
                    row.getString("password_hash"), row.getString("other_contact"));
            }
        }
    }

    private List<Tenant> readResidents(Connection connection, int dormId)
        throws SQLException, InvalidInputException {
        String sql = "SELECT p.* FROM persons p JOIN tenants t ON p.person_id = t.person_id "
            + "WHERE t.current_dorm_id = ? ORDER BY p.person_id";
        List<Tenant> tenants = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, dormId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) tenants.add(readTenant(rows));
            }
        }
        return tenants;
    }

    private Tenant readTenant(ResultSet row) throws SQLException, InvalidInputException {
        return new Tenant(row.getInt("person_id"), row.getString("name"), row.getInt("age"),
            row.getString("contact_num"), row.getString("email"), row.getString("password_hash"));
    }

    public int lockTenantCurrentDorm(Connection connection, int tenantId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT current_dorm_id FROM tenants WHERE person_id = ? FOR UPDATE")) {
            statement.setInt(1, tenantId);
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) throw new SQLException("Tenant no longer exists.");
                return row.getInt(1); // SQL NULL becomes 0: the tenant has no dorm.
            }
        }
    }

    public void updateCurrentDorm(Connection connection, Tenant tenant) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE tenants SET current_dorm_id = ? WHERE person_id = ?")) {
            statement.setInt(1, tenant.getCurrentDorm().getDormId());
            statement.setInt(2, tenant.getPersonId());
            if (statement.executeUpdate() != 1) throw new SQLException("Tenant no longer exists.");
        }
    }

    // Applications and decisions.

    public List<Application> findPendingApplications(Connection connection, Landlord landlord, Dorm dorm)
        throws SQLException, InvalidInputException {
        String sql = "SELECT a.*, p.* FROM applications a JOIN persons p ON a.tenant_id = p.person_id "
            + "JOIN dorms d ON a.dorm_id = d.dorm_id WHERE d.owner_id = ? AND d.dorm_id = ? "
            + "AND a.status = 'PENDING' ORDER BY a.date_submitted, a.application_id";
        List<Application> pending = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, landlord.getPersonId());
            statement.setInt(2, dorm.getDormId());
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) pending.add(readApplication(rows, dorm, readTenant(rows)));
            }
        }
        return List.copyOf(pending);
    }

    public Application findOwnedApplication(Connection connection, int id, Landlord landlord,
                                 boolean lock, Tenant restoredTenant)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        String sql = "SELECT a.*, p.* FROM applications a JOIN persons p ON a.tenant_id = p.person_id "
            + "JOIN dorms d ON a.dorm_id = d.dorm_id WHERE a.application_id = ? AND d.owner_id = ?"
            + (lock ? " FOR UPDATE OF a" : "");
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.setInt(2, landlord.getPersonId());
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) throw new UnauthorizedActionException("Application is not in your listings.");
                Dorm dorm = findOwnedDorm(connection, row.getInt("dorm_id"), landlord, false);
                Tenant tenant = restoredTenant == null ? readTenant(row) : restoredTenant;
                if (tenant.getPersonId() != row.getInt("tenant_id")) {
                    throw new SQLException("Application tenant changed while reviewing.");
                }
                return readApplication(row, dorm, tenant);
            }
        }
    }

    private Application readApplication(ResultSet row, Dorm dorm, Tenant tenant)
        throws SQLException, InvalidInputException {
        Timestamp reviewed = row.getTimestamp("date_reviewed");
        return new Application(row.getInt("application_id"), tenant, dorm,
            ApplicationStatus.valueOf(row.getString("status")), row.getBoolean("transfer_confirmed"),
            row.getTimestamp("date_submitted").toLocalDateTime(),
            reviewed == null ? null : reviewed.toLocalDateTime());
    }

    public void saveDecision(Connection connection, Landlord landlord, Application application)
        throws SQLException {
        String sql = "UPDATE applications SET status = ?, date_reviewed = ? "
            + "WHERE application_id = ? AND status = 'PENDING' "
            + "AND dorm_id IN (SELECT dorm_id FROM dorms WHERE owner_id = ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, application.getStatus().name());
            statement.setTimestamp(2, Timestamp.valueOf(application.getDateReviewed()));
            statement.setInt(3, application.getApplicationId());
            statement.setInt(4, landlord.getPersonId());
            if (statement.executeUpdate() != 1) throw new SQLException("Application changed while reviewing.");
        }
    }

    public void removeAcceptedInterest(Connection connection, Application application) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM interested_dorms WHERE tenant_id = ? AND dorm_id = ?")) {
            statement.setInt(1, application.getTenant().getPersonId());
            statement.setInt(2, application.getDorm().getDormId());
            statement.executeUpdate();
        }
    }

    // Announcements.

    public Announcement insertAnnouncement(Connection connection, Announcement announcement)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        String sql = "INSERT INTO announcements (dorm_id, author_id, message) "
            + "SELECT dorm_id, owner_id, ? FROM dorms WHERE dorm_id = ? AND owner_id = ? "
            + "RETURNING announcement_id, date_posted";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, announcement.getMessage());
            statement.setInt(2, announcement.getDorm().getDormId());
            statement.setInt(3, announcement.getAuthor().getPersonId());
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) throw new UnauthorizedActionException("Dorm is not in your listings.");
                return new Announcement(row.getInt(1), announcement.getDorm(), announcement.getAuthor(),
                    announcement.getMessage(), row.getTimestamp(2).toLocalDateTime());
            }
        }
    }

    // Sender-free complaints.

    public List<ComplaintView> findComplaints(Connection connection, int dormId, int ownerId)
        throws SQLException {
        // Never SELECT sender_id or join persons here, even for nonanonymous messages.
        String sql = "SELECT c.complaint_id, c.message, c.date_posted FROM complaints c "
            + "JOIN dorms d ON c.dorm_id = d.dorm_id WHERE d.dorm_id = ? AND d.owner_id = ? "
            + "ORDER BY c.date_posted DESC, c.complaint_id DESC";
        List<ComplaintView> complaints = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, dormId);
            statement.setInt(2, ownerId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) complaints.add(new ComplaintView(rows.getInt(1), rows.getString(2),
                    rows.getTimestamp(3).toLocalDateTime()));
            }
        }
        return List.copyOf(complaints);
    }

}

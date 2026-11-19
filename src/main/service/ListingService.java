package service;

import exception.InvalidInputException;
import exception.UnauthorizedActionException;
import model.Dorm;
import model.DormDetails;
import model.Landlord;
import model.Tenant;
import util.DatabaseConnection;
import util.Validator;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class ListingService {
    private final Landlord currentLandlord;

    public ListingService(Landlord currentLandlord) throws UnauthorizedActionException {
        requireLandlord(currentLandlord);
        this.currentLandlord = currentLandlord;
    }

    public List<Dorm> getMyDorms(Landlord landlord)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        requireCurrentLandlord(landlord);
        try (Connection connection = DatabaseConnection.getConnection()) {
            return findMyDorms(connection, currentLandlord);
        }
    }

    public Dorm postDorm(Landlord landlord, DormDetails details)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        requireCurrentLandlord(landlord);
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Dorm dorm = new Dorm(currentLandlord, details);
                Dorm saved = insertDorm(connection, dorm);
                connection.commit();
                return saved;
            } catch (SQLException | InvalidInputException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public Dorm updateDorm(int dormId, DormDetails details)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        Validator.savedId(dormId);
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Dorm dorm = findOwnedDorm(connection, dormId, currentLandlord, true);
                dorm.updateDetails(details);
                updateDorm(connection, dorm);
                connection.commit();
                return dorm;
            } catch (SQLException | InvalidInputException | UnauthorizedActionException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public void removeDorm(int dormId)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        Validator.savedId(dormId);
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Dorm dorm = findOwnedDorm(connection, dormId, currentLandlord, true);
                Validator.condition(!dorm.hasTenants(), "Cannot remove an occupied dorm.");
                deleteDorm(connection, dormId, currentLandlord.getPersonId());
                connection.commit();
            } catch (SQLException | InvalidInputException | UnauthorizedActionException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    static void requireLandlord(Landlord landlord) throws UnauthorizedActionException {
        if (landlord == null || landlord.getPersonId() <= 0) {
            throw new UnauthorizedActionException("A saved landlord account is required.");
        }
    }

    private void requireCurrentLandlord(Landlord landlord) throws UnauthorizedActionException {
        requireLandlord(landlord);
        if (landlord.getPersonId() != currentLandlord.getPersonId()) {
            throw new UnauthorizedActionException("Use the landlord who logged in.");
        }
    }

    private List<Dorm> findMyDorms(Connection connection, Landlord owner)
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

    Dorm findOwnedDorm(Connection connection, int dormId, Landlord owner, boolean lock)
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

    // Restore the applicant's existing dorm during a confirmed transfer.
    // Landlord access to a target dorm must always use findOwnedDorm instead.

    Dorm findPreviousDorm(Connection connection, int dormId)
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

    private Dorm insertDorm(Connection connection, Dorm dorm) throws SQLException, InvalidInputException {
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

    private void updateDorm(Connection connection, Dorm dorm) throws SQLException {
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

    private void deleteDorm(Connection connection, int dormId, int ownerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM dorms WHERE dorm_id = ? AND owner_id = ? "
                + "AND NOT EXISTS (SELECT 1 FROM tenants WHERE current_dorm_id = dorms.dorm_id)")) {
            statement.setInt(1, dormId);
            statement.setInt(2, ownerId);
            if (statement.executeUpdate() != 1) throw new SQLException("Listing changed while removing.");
        }
    }

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

    static Tenant readTenant(ResultSet row) throws SQLException, InvalidInputException {
        return new Tenant(row.getInt("person_id"), row.getString("name"), row.getInt("age"),
            row.getString("contact_num"), row.getString("email"), row.getString("password_hash"));
    }

}

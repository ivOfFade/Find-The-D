package repository;

import enums.EstablishmentType;
import exception.InvalidInputException;
import exception.UnauthorizedActionException;
import model.Dorm;
import model.DormDetails;
import model.Establishment;
import model.Landlord;
import model.PlusCode;
import model.Tenant;
import util.DatabaseConnection;
import util.Validator;
import java.sql.*;
import java.util.*;

public class DormRepository implements RepositoryInterface<Dorm, Integer> {

    private static final String SELECT = "SELECT d.* FROM dorms d";

    private List<String> loadAmenities(Connection conn, int dormId) throws SQLException {

        List<String> amenities = new ArrayList<>();
        String sql = "SELECT amenity FROM dorm_amenities WHERE dorm_id = ? ORDER BY amenity";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dormId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    amenities.add(rs.getString("amenity"));
                }
            }
        }

        return amenities;
    }

    private List<String> loadPhotos(Connection conn, int dormId) throws SQLException {

        List<String> photos = new ArrayList<>();
        String sql = "SELECT photo_url FROM dorm_photos WHERE dorm_id = ? ORDER BY photo_url";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dormId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    photos.add(rs.getString("photo_url"));
                }
            }
        }

        return photos;
    }

    private List<Establishment> loadNearbyEstablishments(Connection conn, int dormId)
        throws SQLException, InvalidInputException {
        List<Establishment> establishments = new ArrayList<>();
        String sql = "SELECT establishment_name, type, plus_code, distance_meters "
            + "FROM dorm_nearby_establishments WHERE dorm_id = ? "
            + "ORDER BY distance_meters, establishment_name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dormId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    establishments.add(new Establishment(
                        rs.getString("establishment_name"),
                        EstablishmentType.valueOf(rs.getString("type")),
                        new PlusCode(rs.getString("plus_code")),
                        rs.getInt("distance_meters")
                    ));
                }
            }
        }
        return establishments;
    }

    private Dorm map(Connection conn, ResultSet rs) throws SQLException {

        int dormId = rs.getInt("dorm_id");
        int ownerId = rs.getInt("owner_id");

        LandlordRepository landlordRepository = new LandlordRepository();
        Optional<Landlord> result = landlordRepository.findById(ownerId);

        if (result.isEmpty()) {
            throw new SQLException("Landlord not found: " + ownerId);
        }

        Landlord owner = result.get();

        List<String> amenities = loadAmenities(conn, dormId);
        List<String> photos = loadPhotos(conn, dormId);

        List<Tenant> residents = new TenantRepository().loadResidents(conn, dormId);

        try {
            DormDetails details = new DormDetails(
                rs.getString("dorm_name"),
                rs.getString("location"),
                rs.getString("plus_code"),
                rs.getString("description"),
                rs.getInt("total_slots"),
                rs.getDouble("rate"),
                amenities,
                rs.getString("rules"),
                photos
            );

            Dorm dorm = new Dorm(
                dormId,
                owner,
                details,
                rs.getTimestamp("date_posted").toLocalDateTime(),
                residents
            );
            dorm.replaceNearbyEstablishments(loadNearbyEstablishments(conn, dormId));
            if (rs.getBoolean("nearby_search_completed")) {
                dorm.markNearbySearchCompleted();
            }
            for (Tenant tenant : new TenantRepository().loadInterested(conn, dormId)) {
                dorm.addInterest(tenant);
            }
            return dorm;
        } catch (InvalidInputException e) {
            throw new SQLException("Invalid dorm data.", e);
        }
    }

    @Override
    public void save(Dorm d) throws SQLException {
        if (d.getDormId() != 0) {
            throw new IllegalArgumentException("This dorm already has an ID. Use update() instead.");
        }

        String dormSql =
            " INSERT INTO dorms (owner_id, dorm_name, location, plus_code, description, total_slots, available_slots, rate, rules) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String amenitySql =
            " INSERT INTO dorm_amenities (dorm_id, amenity) VALUES (?, ?)";

        String photoSql =
            " INSERT INTO dorm_photos (dorm_id, photo_url) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            int dormId;

            try {
                // 1. Save the dorm and retrieve its generated ID.
                try (PreparedStatement ps = conn.prepareStatement(
                        dormSql, new String[] {"dorm_id"})) {

                    ps.setInt(1, d.getOwner().getPersonId());
                    ps.setString(2, d.getDormName());
                    ps.setString(3, d.getLocation());
                    ps.setString(4, d.getPlusCode() == null ? null : d.getPlusCode().getCode());
                    ps.setString(5, d.getDescription());
                    ps.setInt(6, d.getTotalSlots());
                    ps.setInt(7, d.getAvailableSlots());
                    ps.setDouble(8, d.getRate());
                    ps.setString(9, d.getRules());

                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("No dorm ID was returned.");
                        }

                        dormId = keys.getInt(1);
                    }
                }

                // 2. Save the amenities using the new dorm ID.
                try (PreparedStatement ps =
                        conn.prepareStatement(amenitySql)) {

                    for (String amenity : d.getAmenities()) {
                        ps.setInt(1, dormId);
                        ps.setString(2, amenity);
                        ps.executeUpdate();
                    }
                }

                // 3. Save the photo URLs using the same dorm ID.
                try (PreparedStatement ps =
                        conn.prepareStatement(photoSql)) {

                    for (String photoUrl : d.getPhotos()) {
                        ps.setInt(1, dormId);
                        ps.setString(2, photoUrl);
                        ps.executeUpdate();
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

            // Keep the object unchanged if any database operation was rolled back.
            d.assignDormId(dormId);
        } catch (InvalidInputException e) {
            throw new SQLException("Could not assign the generated dorm ID.", e);
        }
    }

    //might get updated because of how the amenities and photos will be stored
    @Override
    public Optional<Dorm> findById(Integer id) throws SQLException {
        String sql = SELECT + " WHERE dorm_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(conn, rs));
                }

                return Optional.empty();
            }
        }
    }

    //might get changed same as findbyId
    @Override
    public List<Dorm> findAll() throws SQLException {
        List<Dorm> dorms = new ArrayList<>();
        String sql = SELECT + " ORDER BY date_posted DESC";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                dorms.add(map(conn, rs));
            }
        }

        return dorms;
    }

    @Override
    public void update(Dorm d) throws SQLException {
        if (d.getDormId() <= 0) {
            throw new IllegalArgumentException("Save the dorm before updating it.");
        }

        String dormSql = "UPDATE dorms SET dorm_name=?, location=?, plus_code=?, description=?, total_slots=?, available_slots=?, rate=?, rules=? WHERE dorm_id=?";
        String deleteAmenitiesSql = "DELETE FROM dorm_amenities WHERE dorm_id=?";
        String insertAmenitySql = "INSERT INTO dorm_amenities (dorm_id, amenity) VALUES (?, ?)";
        String deletePhotosSql = "DELETE FROM dorm_photos WHERE dorm_id=?";
        String insertPhotoSql = "INSERT INTO dorm_photos (dorm_id, photo_url) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement ps = conn.prepareStatement(dormSql)) {
                    ps.setString(1, d.getDormName());
                    ps.setString(2, d.getLocation());
                    ps.setString(3, d.getPlusCode() == null ? null : d.getPlusCode().getCode());
                    ps.setString(4, d.getDescription());
                    ps.setInt(5, d.getTotalSlots());
                    ps.setInt(6, d.getAvailableSlots());
                    ps.setDouble(7, d.getRate());
                    ps.setString(8, d.getRules());
                    ps.setInt(9, d.getDormId());

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Dorm not found: " + d.getDormId());
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(deleteAmenitiesSql)) {
                    ps.setInt(1, d.getDormId());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(insertAmenitySql)) {
                    for (String amenity : d.getAmenities()) {
                        ps.setInt(1, d.getDormId());
                        ps.setString(2, amenity);
                        ps.executeUpdate();
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(deletePhotosSql)) {
                    ps.setInt(1, d.getDormId());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(insertPhotoSql)) {
                    for (String photoUrl : d.getPhotos()) {
                        ps.setInt(1, d.getDormId());
                        ps.setString(2, photoUrl);
                        ps.executeUpdate();
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
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM dorms WHERE dorm_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Dorm> findByOwner(Landlord owner)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        requireLandlord(owner);
        try (Connection conn = DatabaseConnection.getConnection()) {
            return findByOwner(conn, owner);
        }
    }

    public List<Dorm> findByOwner(Connection conn, Landlord owner)
        throws SQLException, InvalidInputException {
        List<Dorm> dorms = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM dorms WHERE owner_id = ? ORDER BY dorm_id")) {
            ps.setInt(1, owner.getPersonId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) dorms.add(map(conn, rs));
            }
        }
        return List.copyOf(dorms);
    }

    public Dorm findOwnedDorm(Connection connection, int dormId, Landlord owner, boolean lock)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        requireLandlord(owner);
        Validator.savedId(dormId);
        String sql = "SELECT * FROM dorms WHERE dorm_id = ? AND owner_id = ?"
            + (lock ? " FOR UPDATE" : "");
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, dormId);
            ps.setInt(2, owner.getPersonId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new UnauthorizedActionException("Dorm is not in your listings.");
                }
                return map(connection, rs);
            }
        }
    }

    public Dorm findPreviousDorm(Connection connection, int dormId)
        throws SQLException, InvalidInputException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM dorms WHERE dorm_id = ?")) {
            ps.setInt(1, dormId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Landlord owner = readLandlord(connection, rs.getInt("owner_id"));
                return map(connection, rs, owner);
            }
        }
    }

    public Dorm insertNewDorm(Dorm dorm) throws SQLException, InvalidInputException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Dorm saved = insertDorm(conn, dorm);
                conn.commit();
                return saved;
            } catch (SQLException | InvalidInputException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public Dorm updateDormDetails(int dormId, DormDetails details, Landlord owner)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        Validator.savedId(dormId);
        Validator.requiredReference(details, "Dorm details");
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Dorm dorm = findOwnedDorm(conn, dormId, owner, true);
                dorm.updateDetails(details);
                updateDorm(conn, dorm);
                conn.commit();
                return dorm;
            } catch (SQLException | InvalidInputException | UnauthorizedActionException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void deleteOwnedDorm(int dormId, Landlord owner)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        Validator.savedId(dormId);
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Dorm dorm = findOwnedDorm(conn, dormId, owner, true);
                Validator.condition(!dorm.hasTenants(), "Cannot remove an occupied dorm.");
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM dorms WHERE dorm_id = ? AND owner_id = ? "
                        + "AND NOT EXISTS (SELECT 1 FROM tenants WHERE current_dorm_id = dorms.dorm_id)")) {
                    ps.setInt(1, dormId);
                    ps.setInt(2, owner.getPersonId());
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Listing changed while removing.");
                    }
                }
                conn.commit();
            } catch (SQLException | InvalidInputException | UnauthorizedActionException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private Dorm map(Connection conn, ResultSet rs, Landlord owner)
        throws SQLException, InvalidInputException {
        int dormId = rs.getInt("dorm_id");
        List<String> amenities = loadAmenities(conn, dormId);
        List<String> photos = loadPhotos(conn, dormId);
        List<Tenant> residents = new TenantRepository().loadResidents(conn, dormId);
        DormDetails details = new DormDetails(
            rs.getString("dorm_name"),
            rs.getString("location"),
            rs.getString("plus_code"),
            rs.getString("description"),
            rs.getInt("total_slots"),
            rs.getDouble("rate"),
            amenities,
            rs.getString("rules"),
            photos
        );
        Dorm dorm = new Dorm(
            dormId,
            owner,
            details,
            rs.getTimestamp("date_posted").toLocalDateTime(),
            residents
        );
        dorm.replaceNearbyEstablishments(loadNearbyEstablishments(conn, dormId));
        if (rs.getBoolean("nearby_search_completed")) {
            dorm.markNearbySearchCompleted();
        }
        for (Tenant tenant : new TenantRepository().loadInterested(conn, dormId)) {
            dorm.addInterest(tenant);
        }
        return dorm;
    }

    private Dorm insertDorm(Connection conn, Dorm dorm) throws SQLException, InvalidInputException {
        String sql = "INSERT INTO dorms (owner_id, dorm_name, location, plus_code, description, "
            + "total_slots, available_slots, rate, rules, date_posted, nearby_search_completed) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
            + "RETURNING dorm_id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dorm.getOwner().getPersonId());
            setDetails(ps, dorm, 2);
            ps.setTimestamp(10, Timestamp.valueOf(dorm.getDatePosted()));
            ps.setBoolean(11, dorm.isNearbySearchCompleted());
            try (ResultSet row = ps.executeQuery()) {
                row.next();
                dorm.assignDormId(row.getInt(1));
            }
        }
        replaceLists(conn, dorm);
        replaceNearbyEstablishments(conn, dorm);
        return dorm;
    }

    private void updateDorm(Connection conn, Dorm dorm) throws SQLException {
        String sql = "UPDATE dorms SET dorm_name = ?, location = ?, plus_code = ?, description = ?, "
            + "total_slots = ?, available_slots = ?, rate = ?, rules = ?, nearby_search_completed = ? "
            + "WHERE dorm_id = ? AND owner_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setDetails(ps, dorm, 1);
            ps.setBoolean(9, dorm.isNearbySearchCompleted());
            ps.setInt(10, dorm.getDormId());
            ps.setInt(11, dorm.getOwner().getPersonId());
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Listing changed while saving.");
            }
        }
        replaceLists(conn, dorm);
        replaceNearbyEstablishments(conn, dorm);
    }

    private void setDetails(PreparedStatement ps, Dorm dorm, int start) throws SQLException {
        ps.setString(start, dorm.getDormName());
        ps.setString(start + 1, dorm.getLocation());
        ps.setString(start + 2, dorm.getPlusCode() == null ? null : dorm.getPlusCode().getCode());
        ps.setString(start + 3, dorm.getDescription());
        ps.setInt(start + 4, dorm.getTotalSlots());
        ps.setInt(start + 5, dorm.getAvailableSlots());
        ps.setDouble(start + 6, dorm.getRate());
        ps.setString(start + 7, dorm.getRules());
    }

    private void replaceLists(Connection conn, Dorm dorm) throws SQLException {
        replaceStrings(conn, "dorm_amenities", "amenity", dorm.getDormId(), dorm.getAmenities());
        replaceStrings(conn, "dorm_photos", "photo_url", dorm.getDormId(), dorm.getPhotos());
    }

    private void replaceNearbyEstablishments(Connection conn, Dorm dorm) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM dorm_nearby_establishments WHERE dorm_id = ?")) {
            ps.setInt(1, dorm.getDormId());
            ps.executeUpdate();
        }
        String sql = "INSERT INTO dorm_nearby_establishments "
            + "(dorm_id, establishment_name, type, plus_code, distance_meters) "
            + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Establishment establishment : dorm.getNearbyEstablishments()) {
                ps.setInt(1, dorm.getDormId());
                ps.setString(2, establishment.getName());
                ps.setString(3, establishment.getType().name());
                ps.setString(4, establishment.getPlusCode().getCode());
                ps.setInt(5, establishment.getDistanceMeters());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void replaceStrings(Connection conn, String table, String column, int dormId,
                                List<String> values) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM " + table + " WHERE dorm_id = ?")) {
            ps.setInt(1, dormId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + table + " (dorm_id, " + column + ") VALUES (?, ?)")) {
            for (String value : values) {
                ps.setInt(1, dormId);
                ps.setString(2, value);
                ps.executeUpdate();
            }
        }
    }

    private Landlord readLandlord(Connection connection, int id)
        throws SQLException, InvalidInputException {
        String sql = "SELECT p.*, l.other_contact FROM persons p "
            + "JOIN landlords l ON p.person_id = l.person_id WHERE p.person_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Landlord(id, rs.getString("name"), rs.getInt("age"),
                    rs.getString("contact_num"), rs.getString("email"),
                    rs.getString("password_hash"), rs.getString("other_contact"));
            }
        }
    }

    private static void requireLandlord(Landlord landlord) throws UnauthorizedActionException {
        if (landlord == null || landlord.getPersonId() <= 0) {
            throw new UnauthorizedActionException("A saved landlord account is required.");
        }
    }

}

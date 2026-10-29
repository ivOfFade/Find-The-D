package com.ivofade.findthed.repository;

import com.ivofade.findthed.exception.InvalidInputException;
import com.ivofade.findthed.model.Dorm;
import com.ivofade.findthed.util.DatabaseConnection;
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

    private Dorm map(Connection conn, ResultSet rs) throws SQLException {
        int dormId = rs.getInt("dorm_id");

        List<String> amenities = loadAmenities(conn, dormId);
        List<String> photos = loadPhotos(conn, dormId);

        try {
            return new Dorm (
                dormId,
                rs.getInt("owner_id"),
                rs.getString("dorm_name"),
                rs.getString("location"),
                rs.getString("plus_code"),
                rs.getString("description"),
                rs.getInt("total_slots"),
                rs.getInt("available_slots"),
                rs.getBigDecimal("rate"),
                rs.getString("rules"),
                rs.getTimestamp("date_posted").toLocalDateTime(),
                amenities,
                photos
            );
        } catch (InvalidInputException e) {
            throw new SQLException("Invalid data in a row: " + e.getMessage(), e);
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

                    ps.setInt(1, d.getOwnerId());
                    ps.setString(2, d.getDormName());
                    ps.setString(3, d.getLocation());
                    ps.setString(4, d.getPlusCode());
                    ps.setString(5, d.getDescription());
                    ps.setInt(6, d.getTotalSlots());
                    ps.setInt(7, d.getAvailableSlots());
                    ps.setBigDecimal(8, d.getRate());
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
                    ps.setString(3, d.getPlusCode());
                    ps.setString(4, d.getDescription());
                    ps.setInt(5, d.getTotalSlots());
                    ps.setInt(6, d.getAvailableSlots());
                    ps.setBigDecimal(7, d.getRate());
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

}

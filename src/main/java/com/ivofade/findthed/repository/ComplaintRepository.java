package com.ivofade.findthed.repository;

import com.ivofade.findthed.exception.InvalidInputException;
import com.ivofade.findthed.model.Complaint;
import com.ivofade.findthed.util.DatabaseConnection;
import java.sql.*;
import java.util.*;

public class ComplaintRepository implements RepositoryInterface<Complaint, Integer> {

    private static final String SELECT = "SELECT c.* FROM complaints c";

    private Complaint map(ResultSet rs) throws SQLException {
        int sender = rs.getInt("sender_id");
        Integer senderId = rs.wasNull() ? null : sender;

        try {
            return new Complaint(
                rs.getInt("complaint_id"),
                rs.getInt("dorm_id"),
                senderId,
                rs.getString("message"),
                rs.getBoolean("anonymous"),
                rs.getTimestamp("date_posted").toLocalDateTime()
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

        String sql = "INSERT INTO complaints (dorm_id, sender_id, message, anonymous) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            int complaintId;

            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        sql, new String[] {"complaint_id"})) {

                    ps.setInt(1, c.getDormId());

                    setNullableInt(ps, 2, c.isAnonymous() ? null : c.getSenderId());

                    ps.setString(3, c.getMessage());
                    ps.setBoolean(4, c.isAnonymous());

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
            c.assignComplaintId(complaintId);
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

    private static void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

}
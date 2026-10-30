package com.ivofade.findthed.repository;

import com.ivofade.findthed.enums.ApplicationStatus;
import com.ivofade.findthed.exception.InvalidInputException;
import com.ivofade.findthed.model.Application;
import com.ivofade.findthed.util.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class ApplicationRepository implements RepositoryInterface<Application, Integer> {

    private static final String SELECT = "SELECT a.* FROM applications a";

    private static final String ORDER_BY = " ORDER BY a.date_submitted DESC, a.application_id DESC";

    private Application map(ResultSet rs) throws SQLException {
        Timestamp reviewed = rs.getTimestamp("date_reviewed");

        try {
            return new Application(
                rs.getInt("application_id"),
                rs.getInt("tenant_id"),
                rs.getInt("dorm_id"),
                ApplicationStatus.valueOf(rs.getString("status")),
                rs.getBoolean("transfer_confirmed"),
                rs.getTimestamp("date_submitted").toLocalDateTime(),
                reviewed == null ? null : reviewed.toLocalDateTime()
            );
        } catch (InvalidInputException | IllegalArgumentException e) {
            throw new SQLException(
                "Invalid data in an application row: " + e.getMessage(), e
            );
        }
    }

    @Override
    public void save(Application a) throws SQLException {
        if (a.getApplicationId() != 0) {
            throw new IllegalArgumentException(
                "This application already has an ID. Use update() instead."
            );
        }

        if (a.getStatus() != ApplicationStatus.PENDING
                || a.getDateReviewed() != null) {
            throw new IllegalArgumentException(
                "New applications must be pending and not yet reviewed."
            );
        }

        String sql = "INSERT INTO applications (tenant_id, dorm_id, transfer_confirmed) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            int applicationId;

            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        sql, new String[] {"application_id"})) {

                    ps.setInt(1, a.getTenantId());
                    ps.setInt(2, a.getDormId());
                    ps.setBoolean(3, a.isTransferConfirmed());

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Application was not saved.");
                    }

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                "No application ID was returned."
                            );
                        }

                        applicationId = keys.getInt(1);
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

            a.assignApplicationId(applicationId);
        }
    }

    @Override
    public Optional<Application> findById(Integer id) throws SQLException {
        String sql = SELECT + " WHERE a.application_id = ?";

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
    public List<Application> findAll() throws SQLException {
        List<Application> applications = new ArrayList<>();
        String sql = SELECT + ORDER_BY;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                applications.add(map(rs));
            }
        }

        return applications;
    }

    // Load all applications submitted by one tenant.
    public List<Application> findByTenantId(int tenantId) throws SQLException {
        List<Application> applications = new ArrayList<>();
        String sql = SELECT + " WHERE a.tenant_id = ?" + ORDER_BY;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tenantId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    applications.add(map(rs));
                }
            }
        }

        return applications;
    }

    // Load all applications for one dorm.
    public List<Application> findByDormId(int dormId) throws SQLException {
        List<Application> applications = new ArrayList<>();
        String sql = SELECT + " WHERE a.dorm_id = ?" + ORDER_BY;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dormId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    applications.add(map(rs));
                }
            }
        }

        return applications;
    }

    // Load only applications waiting for review.
    public List<Application> findPendingByDormId(int dormId)
            throws SQLException {

        List<Application> applications = new ArrayList<>();
        String sql = SELECT + " WHERE a.dorm_id = ? AND a.status = ?" + ORDER_BY;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dormId);
            ps.setString(2, ApplicationStatus.PENDING.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    applications.add(map(rs));
                }
            }
        }

        return applications;
    }

    @Override
    public void update(Application a) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            update(conn, a);
        }
    }

    // Allows a service to include this update in a larger transaction.
    // The caller owns the connection and handles commit/rollback.
    public void update(Connection conn, Application a) throws SQLException {
        if (a.getApplicationId() <= 0) {
            throw new IllegalArgumentException(
                "Save the application before updating it."
            );
        }

        if (a.getStatus() == null) {
            throw new IllegalArgumentException(
                "Application status is required."
            );
        }

        if (a.getStatus() == ApplicationStatus.PENDING) {
            if (a.getDateReviewed() != null) {
                throw new IllegalArgumentException(
                    "Pending applications cannot have a review date."
                );
            }
        } else if (a.getDateReviewed() == null) {
            throw new IllegalArgumentException(
                "Accepted or rejected applications need a review date."
            );
        }

        // Only applications that are still pending can be changed.
        String sql =
            "UPDATE applications " +
            "SET status = ?, transfer_confirmed = ?, date_reviewed = ? " +
            "WHERE application_id = ? AND status = 'PENDING'";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getStatus().name());
            ps.setBoolean(2, a.isTransferConfirmed());
            setNullableTimestamp(ps, 3, a.getDateReviewed());
            ps.setInt(4, a.getApplicationId());

            if (ps.executeUpdate() != 1) {
                throw new SQLException(
                    "Application not found or already reviewed: " +
                    a.getApplicationId()
                );
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql =
            "DELETE FROM applications WHERE application_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static void setNullableTimestamp(
            PreparedStatement ps, int index, LocalDateTime value)
            throws SQLException {

        if (value == null) {
            ps.setNull(index, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(index, Timestamp.valueOf(value));
        }
    }
}
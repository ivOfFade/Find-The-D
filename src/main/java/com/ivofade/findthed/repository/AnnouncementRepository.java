package com.ivofade.findthed.repository;

import com.ivofade.findthed.exception.InvalidInputException;
import com.ivofade.findthed.model.Announcement;
import com.ivofade.findthed.util.DatabaseConnection;
import java.sql.*;
import java.util.*;

public class AnnouncementRepository implements RepositoryInterface<Announcement, Integer> {

    private static final String SELECT = "SELECT a.* FROM announcements a";

    private Announcement map(ResultSet rs) throws SQLException {
        try {
            return new Announcement(
                rs.getInt("announcement_id"),
                rs.getInt("dorm_id"),
                rs.getInt("author_id"),
                rs.getString("message"),
                rs.getTimestamp("date_posted").toLocalDateTime()
            );
        } catch (InvalidInputException e) {
            throw new SQLException(
                "Invalid data in an announcement row: " + e.getMessage(), e
            );
        }
    }

    @Override
    public void save(Announcement a) throws SQLException {
        if (a.getAnnouncementId() != 0) {
            throw new IllegalArgumentException(
                "This announcement already has an ID. Use update() instead."
            );
        }

        String sql = "INSERT INTO announcements (dorm_id, author_id, message) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            int announcementId;

            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        sql, new String[] {"announcement_id"})) {

                    ps.setInt(1, a.getDormId());
                    ps.setInt(2, a.getAuthorId());
                    ps.setString(3, a.getMessage());

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                            "Announcement was not saved."
                        );
                    }

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                "No announcement ID was returned."
                            );
                        }

                        announcementId = keys.getInt(1);
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

            a.assignAnnouncementId(announcementId);
        }
    }

    @Override
    public Optional<Announcement> findById(Integer id) throws SQLException {
        String sql = SELECT + " WHERE a.announcement_id = ?";

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
    public List<Announcement> findAll() throws SQLException {
        List<Announcement> announcements = new ArrayList<>();
        String sql = SELECT + " ORDER BY a.date_posted DESC, a.announcement_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) announcements.add(map(rs));
        }

        return announcements;
    }

    public List<Announcement> findByDormId(int dormId) throws SQLException {
        List<Announcement> announcements = new ArrayList<>();
        String sql = SELECT + " WHERE a.dorm_id = ? ORDER BY a.date_posted DESC, a.announcement_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dormId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) announcements.add(map(rs));
            }
        }

        return announcements;
    }

    @Override
    public void update(Announcement a) throws SQLException {
        if (a.getAnnouncementId() <= 0) {
            throw new IllegalArgumentException(
                "Save the announcement before updating it."
            );
        }

        String sql = "UPDATE announcements SET message = ? WHERE announcement_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, a.getMessage());
            ps.setInt(2, a.getAnnouncementId());

            if (ps.executeUpdate() != 1) {
                throw new SQLException(
                    "Announcement not found: " + a.getAnnouncementId()
                );
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM announcements WHERE announcement_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }



}

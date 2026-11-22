package service;

import exception.InvalidInputException;
import exception.UnauthorizedActionException;
import model.Announcement;
import model.Dorm;
import model.Landlord;
import util.DatabaseConnection;
import util.Validator;
import java.sql.*;

public class AnnouncementService {

    public Announcement publish(Landlord landlord, Dorm dorm, String message)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        ListingService.requireLandlord(landlord);
        Validator.requiredReference(dorm, "Dorm");
        Validator.savedId(dorm.getDormId());
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Dorm owned = new ListingService(landlord).findOwnedDorm(connection, dorm.getDormId(), landlord, true);
                Announcement announcement = new Announcement(owned, landlord, message);
                Announcement saved = insertAnnouncement(connection, announcement);
                owned.addAnnouncement(saved);
                connection.commit();
                return saved;
            } catch (SQLException | InvalidInputException | UnauthorizedActionException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private Announcement insertAnnouncement(Connection connection, Announcement announcement)
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
}

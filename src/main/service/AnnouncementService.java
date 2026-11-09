package service;

import exception.InvalidInputException;
import exception.UnauthorizedActionException;
import model.Announcement;
import model.Dorm;
import model.Landlord;
import repository.LandlordServiceRepository;
import util.DatabaseConnection;
import util.Validator;
import java.sql.Connection;
import java.sql.SQLException;

public class AnnouncementService {
    private final LandlordServiceRepository repository = new LandlordServiceRepository();

    public Announcement publish(Landlord landlord, Dorm dorm, String message)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        LandlordAccess.requireSavedLandlord(landlord);
        Validator.requiredReference(dorm, "Dorm");
        Validator.savedId(dorm.getDormId());
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Dorm owned = repository.findOwnedDorm(connection, dorm.getDormId(), landlord, true);
                Announcement announcement = new Announcement(owned, landlord, message);
                Announcement saved = repository.insertAnnouncement(connection, announcement);
                owned.addAnnouncement(saved);
                connection.commit();
                return saved;
            } catch (SQLException | InvalidInputException | UnauthorizedActionException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }
}

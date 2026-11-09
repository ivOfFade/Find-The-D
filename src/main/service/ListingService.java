package service;

import exception.InvalidInputException;
import exception.UnauthorizedActionException;
import model.Dorm;
import model.DormDetails;
import model.Landlord;
import repository.LandlordServiceRepository;
import util.DatabaseConnection;
import util.Validator;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ListingService {
    private final Landlord currentLandlord;
    private final LandlordServiceRepository repository = new LandlordServiceRepository();

    public ListingService(Landlord currentLandlord) throws UnauthorizedActionException {
        LandlordAccess.requireSavedLandlord(currentLandlord);
        this.currentLandlord = currentLandlord;
    }

    public List<Dorm> getMyDorms(Landlord landlord)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        LandlordAccess.requireSameLandlord(currentLandlord, landlord);
        try (Connection connection = DatabaseConnection.getConnection()) {
            return repository.findMyDorms(connection, currentLandlord);
        }
    }

    public Dorm postDorm(Landlord landlord, DormDetails details)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        LandlordAccess.requireSameLandlord(currentLandlord, landlord);
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Dorm dorm = new Dorm(currentLandlord, details);
                Dorm saved = repository.insertDorm(connection, dorm);
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
                Dorm dorm = repository.findOwnedDorm(connection, dormId, currentLandlord, true);
                dorm.updateDetails(details);
                repository.updateDorm(connection, dorm);
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
                Dorm dorm = repository.findOwnedDorm(connection, dormId, currentLandlord, true);
                Validator.condition(!dorm.hasTenants(), "Cannot remove an occupied dorm.");
                repository.deleteDorm(connection, dormId, currentLandlord.getPersonId());
                connection.commit();
            } catch (SQLException | InvalidInputException | UnauthorizedActionException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }
}

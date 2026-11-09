package service;

import exception.InvalidInputException;
import exception.UnauthorizedActionException;
import model.ComplaintView;
import model.Dorm;
import model.Landlord;
import repository.LandlordServiceRepository;
import util.DatabaseConnection;
import util.Validator;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class LandlordComplaintService {
    private final LandlordServiceRepository repository = new LandlordServiceRepository();

    public List<ComplaintView> getComplaints(Landlord landlord, Dorm dorm)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        LandlordAccess.requireSavedLandlord(landlord);
        Validator.requiredReference(dorm, "Dorm");
        Validator.savedId(dorm.getDormId());
        try (Connection connection = DatabaseConnection.getConnection()) {
            repository.findOwnedDorm(connection, dorm.getDormId(), landlord, false);
            return repository.findComplaints(connection, dorm.getDormId(), landlord.getPersonId());
        }
    }
}

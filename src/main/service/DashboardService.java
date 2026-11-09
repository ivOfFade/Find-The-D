package service;

import exception.InvalidInputException;
import exception.UnauthorizedActionException;
import model.DashboardSummary;
import model.Landlord;
import repository.LandlordServiceRepository;
import util.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class DashboardService {
    private final LandlordServiceRepository repository = new LandlordServiceRepository();

    public DashboardSummary getSummary(Landlord landlord)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        LandlordAccess.requireSavedLandlord(landlord);
        try (Connection connection = DatabaseConnection.getConnection()) {
            return repository.getSummary(connection, landlord.getPersonId());
        }
    }
}

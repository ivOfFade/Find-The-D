package service;

import exception.InvalidInputException;
import java.sql.SQLException;
import model.DashboardSummary;
import model.Landlord;
import repository.DashboardRepository;
import util.Validator;

public class DashboardService {

    private final DashboardRepository dashboardRepository;

    public DashboardService() {
        dashboardRepository = new DashboardRepository();
    }

    public DashboardSummary getSummary(Landlord landlord)
            throws InvalidInputException, SQLException {

        Validator.requiredReference(landlord, "Landlord");
        Validator.savedId(landlord.getPersonId());

        return dashboardRepository.getSummary(landlord.getPersonId());
    }
}
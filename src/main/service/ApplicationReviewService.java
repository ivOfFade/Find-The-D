package service;

import exception.InvalidInputException;
import exception.NoAvailableSlotsException;
import exception.UnauthorizedActionException;
import model.Application;
import model.Dorm;
import model.Landlord;
import model.Tenant;
import repository.LandlordServiceRepository;
import util.DatabaseConnection;
import util.Validator;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ApplicationReviewService {
    private final LandlordServiceRepository repository = new LandlordServiceRepository();

    public List<Application> reviewApplications(Landlord landlord, Dorm dorm)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        LandlordAccess.requireSavedLandlord(landlord);
        Validator.requiredReference(dorm, "Dorm");
        Validator.savedId(dorm.getDormId());
        try (Connection connection = DatabaseConnection.getConnection()) {
            Dorm owned = repository.findOwnedDorm(connection, dorm.getDormId(), landlord, false);
            return repository.findPendingApplications(connection, landlord, owned);
        }
    }

    public Application acceptApplication(Landlord landlord, int applicationId)
        throws SQLException, InvalidInputException, UnauthorizedActionException, NoAvailableSlotsException {
        LandlordAccess.requireSavedLandlord(landlord);
        Validator.savedId(applicationId);
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Application application = loadForReview(connection, landlord, applicationId);
                Dorm previous = application.getTenant().getCurrentDorm();
                application.accept(landlord); // The model calls Dorm.addTenant and handles a confirmed move.
                repository.updateCurrentDorm(connection, application.getTenant());
                repository.refreshAvailability(connection, application.getDorm().getDormId());
                if (previous != null) {
                    repository.refreshAvailability(connection, previous.getDormId());
                }
                repository.removeAcceptedInterest(connection, application);
                repository.saveDecision(connection, landlord, application);
                connection.commit();
                return application;
            } catch (SQLException | InvalidInputException | UnauthorizedActionException
                     | NoAvailableSlotsException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public Application rejectApplication(Landlord landlord, int applicationId)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        LandlordAccess.requireSavedLandlord(landlord);
        Validator.savedId(applicationId);
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Application application = loadForReview(connection, landlord, applicationId);
                application.reject(landlord);
                repository.saveDecision(connection, landlord, application);
                connection.commit();
                return application;
            } catch (SQLException | InvalidInputException | UnauthorizedActionException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private Application loadForReview(Connection connection, Landlord landlord, int applicationId)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        Application initial = repository.findOwnedApplication(connection, applicationId, landlord, false, null);
        Tenant tenant = initial.getTenant();
        int previousId = repository.lockTenantCurrentDorm(connection, tenant.getPersonId());
        repository.lockDorms(connection, initial.getDorm().getDormId(), previousId);

        if (previousId != 0) {
            Dorm previous = repository.findPreviousDorm(connection, previousId);
            if (previous == null) {
                throw new SQLException("Tenant's current dorm no longer exists.");
            }
            // Dorm's resident-loading constructor restores each resident's currentDorm.
            for (Tenant resident : previous.getTenants()) {
                if (resident.getPersonId() == tenant.getPersonId()) {
                    tenant = resident;
                    break;
                }
            }
            if (tenant.getCurrentDorm() == null) {
                throw new SQLException("Could not restore the tenant's current dorm.");
            }
        }

        // Reload after waiting for locks, so pending status and capacity are current.
        return repository.findOwnedApplication(connection, applicationId, landlord, true, tenant);
    }
}

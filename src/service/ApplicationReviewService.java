package service;

import exception.InvalidInputException;
import exception.NoAvailableSlotsException;
import exception.UnauthorizedActionException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import model.Application;
import model.Dorm;
import model.Landlord;
import repository.ApplicationRepository;
import repository.DormRepository;
import util.DatabaseConnection;
import util.Validator;

public class ApplicationReviewService {

    private final ApplicationRepository applicationRepository;
    private final DormRepository dormRepository;

    public ApplicationReviewService() {
        applicationRepository = new ApplicationRepository();
        dormRepository = new DormRepository();
    }

    public List<Application> viewPendingApplications(
        Landlord landlord,
        int dormId
    ) throws InvalidInputException, UnauthorizedActionException, SQLException {
        Validator.requiredReference(landlord, "Landlord");
        Validator.savedId(landlord.getPersonId());
        Validator.savedId(dormId);

        Dorm dorm = dormRepository.findById(dormId).orElseThrow(
            () -> new UnauthorizedActionException(
                "Dorm does not exist or is not owned by this landlord."
            )
        );

        if (!landlord.ownsDorm(dorm)) {
        throw new UnauthorizedActionException(
            "Dorm is not owned by this landlord."
        );
        }

        return applicationRepository.findPendingByDormId(dormId);
    }

    public void rejectApplication(Landlord landlord, int applicationId)
        throws InvalidInputException, UnauthorizedActionException, SQLException {
        Validator.requiredReference(landlord, "Landlord");
        Validator.savedId(landlord.getPersonId());
        Validator.savedId(applicationId);

        Application application = findApplication(applicationId);

        application.reject(landlord);

        applicationRepository.update(application);
    }

    public void acceptApplication(Landlord landlord, int applicationId)
        throws InvalidInputException,
               UnauthorizedActionException,
               NoAvailableSlotsException,
               SQLException {

        Validator.requiredReference(landlord, "Landlord");
        Validator.savedId(landlord.getPersonId());
        Validator.savedId(applicationId);

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int[] applicationInfo =
                    applicationRepository.lockApplication(conn, applicationId);

                int tenantId = applicationInfo[0];
                int dormId = applicationInfo[1];

                Integer previousDormId =
                    applicationRepository.lockTenant(conn, tenantId);

                applicationRepository.lockDorms(conn, dormId, previousDormId);

                Application application = findApplication(applicationId);

                // Checks ownership, pending status, transfer and capacity.
                application.accept(landlord);

                applicationRepository.reserveSlot(conn, dormId);

                if (previousDormId != null) {
                    applicationRepository.releaseSlot(conn, previousDormId);
                }

                applicationRepository.moveTenant(conn, tenantId, dormId);
                applicationRepository.removeInterest(conn, tenantId, dormId);
                applicationRepository.update(conn, application);

                conn.commit();
            } catch (
                InvalidInputException |
                UnauthorizedActionException |
                NoAvailableSlotsException |
                SQLException |
                RuntimeException e
            ) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;
            }
        }
    }

    private Application findApplication(int applicationId)
        throws InvalidInputException, SQLException {
        return applicationRepository.findById(applicationId).orElseThrow(
        () -> new InvalidInputException("Application was not found.")
        );
    }

    
}
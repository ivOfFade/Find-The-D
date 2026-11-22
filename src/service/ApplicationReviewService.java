package service;

import exception.InvalidInputException;
import exception.NoAvailableSlotsException;
import exception.UnauthorizedActionException;
import model.Application;
import model.Dorm;
import model.Landlord;
import model.Tenant;
import util.DatabaseConnection;
import util.Validator;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import enums.ApplicationStatus;

public class ApplicationReviewService {

    public List<Application> reviewApplications(Landlord landlord, Dorm dorm)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        ListingService.requireLandlord(landlord);
        Validator.requiredReference(dorm, "Dorm");
        Validator.savedId(dorm.getDormId());
        try (Connection connection = DatabaseConnection.getConnection()) {
            Dorm owned = new ListingService(landlord).findOwnedDorm(connection, dorm.getDormId(), landlord, false);
            return findPendingApplications(connection, landlord, owned);
        }
    }

    public Application acceptApplication(Landlord landlord, int applicationId)
        throws SQLException, InvalidInputException, UnauthorizedActionException, NoAvailableSlotsException {
        ListingService.requireLandlord(landlord);
        Validator.savedId(applicationId);
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Application application = loadForReview(connection, landlord, applicationId);
                Dorm previous = application.getTenant().getCurrentDorm();
                application.accept(landlord); // The model calls Dorm.addTenant and handles a confirmed move.
                updateCurrentDorm(connection, application.getTenant());
                refreshAvailability(connection, application.getDorm().getDormId());
                if (previous != null) {
                    refreshAvailability(connection, previous.getDormId());
                }
                removeAcceptedInterest(connection, application);
                saveDecision(connection, landlord, application);
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
        ListingService.requireLandlord(landlord);
        Validator.savedId(applicationId);
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Application application = loadForReview(connection, landlord, applicationId);
                application.reject(landlord);
                saveDecision(connection, landlord, application);
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
        Application initial = findOwnedApplication(connection, applicationId, landlord, false, null);
        Tenant tenant = initial.getTenant();
        int previousId = lockTenantCurrentDorm(connection, tenant.getPersonId());
        lockDorms(connection, initial.getDorm().getDormId(), previousId);

        if (previousId != 0) {
            Dorm previous = new ListingService(landlord).findPreviousDorm(connection, previousId);
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
        return findOwnedApplication(connection, applicationId, landlord, true, tenant);
    }

    private List<Application> findPendingApplications(Connection connection, Landlord landlord, Dorm dorm)
        throws SQLException, InvalidInputException {
        String sql = "SELECT a.*, p.* FROM applications a JOIN persons p ON a.tenant_id = p.person_id "
            + "JOIN dorms d ON a.dorm_id = d.dorm_id WHERE d.owner_id = ? AND d.dorm_id = ? "
            + "AND a.status = 'PENDING' ORDER BY a.date_submitted, a.application_id";
        List<Application> pending = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, landlord.getPersonId());
            statement.setInt(2, dorm.getDormId());
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) pending.add(readApplication(rows, dorm, ListingService.readTenant(rows)));
            }
        }
        return List.copyOf(pending);
    }

    private Application findOwnedApplication(Connection connection, int id, Landlord landlord,
                                 boolean lock, Tenant restoredTenant)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        String sql = "SELECT a.*, p.* FROM applications a JOIN persons p ON a.tenant_id = p.person_id "
            + "JOIN dorms d ON a.dorm_id = d.dorm_id WHERE a.application_id = ? AND d.owner_id = ?"
            + (lock ? " FOR UPDATE OF a" : "");
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.setInt(2, landlord.getPersonId());
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) throw new UnauthorizedActionException("Application is not in your listings.");
                Dorm dorm = new ListingService(landlord).findOwnedDorm(connection, row.getInt("dorm_id"), landlord, false);
                Tenant tenant = restoredTenant == null ? ListingService.readTenant(row) : restoredTenant;
                if (tenant.getPersonId() != row.getInt("tenant_id")) {
                    throw new SQLException("Application tenant changed while reviewing.");
                }
                return readApplication(row, dorm, tenant);
            }
        }
    }

    private Application readApplication(ResultSet row, Dorm dorm, Tenant tenant)
        throws SQLException, InvalidInputException {
        Timestamp reviewed = row.getTimestamp("date_reviewed");
        return new Application(row.getInt("application_id"), tenant, dorm,
            ApplicationStatus.valueOf(row.getString("status")), row.getBoolean("transfer_confirmed"),
            row.getTimestamp("date_submitted").toLocalDateTime(),
            reviewed == null ? null : reviewed.toLocalDateTime());
    }

    private void saveDecision(Connection connection, Landlord landlord, Application application)
        throws SQLException {
        String sql = "UPDATE applications SET status = ?, date_reviewed = ? "
            + "WHERE application_id = ? AND status = 'PENDING' "
            + "AND dorm_id IN (SELECT dorm_id FROM dorms WHERE owner_id = ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, application.getStatus().name());
            statement.setTimestamp(2, Timestamp.valueOf(application.getDateReviewed()));
            statement.setInt(3, application.getApplicationId());
            statement.setInt(4, landlord.getPersonId());
            if (statement.executeUpdate() != 1) throw new SQLException("Application changed while reviewing.");
        }
    }

    private void removeAcceptedInterest(Connection connection, Application application) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM interested_dorms WHERE tenant_id = ? AND dorm_id = ?")) {
            statement.setInt(1, application.getTenant().getPersonId());
            statement.setInt(2, application.getDorm().getDormId());
            statement.executeUpdate();
        }
    }

    // Announcements.

    private void lockDorms(Connection connection, int targetId, int previousId) throws SQLException {
        // Two landlords accepting opposite transfers must lock dorms in the same order.
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT dorm_id FROM dorms WHERE dorm_id IN (?, ?) ORDER BY dorm_id FOR UPDATE")) {
            statement.setInt(1, targetId);
            statement.setInt(2, previousId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) { /* Reading acquires each row lock. */ }
            }
        }
    }

    private int lockTenantCurrentDorm(Connection connection, int tenantId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT current_dorm_id FROM tenants WHERE person_id = ? FOR UPDATE")) {
            statement.setInt(1, tenantId);
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) throw new SQLException("Tenant no longer exists.");
                return row.getInt(1); // SQL NULL becomes 0: the tenant has no dorm.
            }
        }
    }

    private void updateCurrentDorm(Connection connection, Tenant tenant) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE tenants SET current_dorm_id = ? WHERE person_id = ?")) {
            statement.setInt(1, tenant.getCurrentDorm().getDormId());
            statement.setInt(2, tenant.getPersonId());
            if (statement.executeUpdate() != 1) throw new SQLException("Tenant no longer exists.");
        }
    }

    // Applications and decisions.

    private void refreshAvailability(Connection connection, int dormId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE dorms SET available_slots = total_slots - "
                + "(SELECT COUNT(*) FROM tenants WHERE current_dorm_id = ?) WHERE dorm_id = ?")) {
            statement.setInt(1, dormId);
            statement.setInt(2, dormId);
            if (statement.executeUpdate() != 1) throw new SQLException("Dorm no longer exists.");
        }
    }

    // Account loading and residency updates.

}

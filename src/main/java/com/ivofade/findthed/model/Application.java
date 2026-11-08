package com.ivofade.findthed.model;

import com.ivofade.findthed.enums.ApplicationStatus;
import com.ivofade.findthed.exception.InvalidInputException;
import com.ivofade.findthed.exception.NoAvailableSlotsException;
import com.ivofade.findthed.exception.UnauthorizedActionException;
import com.ivofade.findthed.util.Validator;
import java.time.LocalDateTime;

public class Application {

    private final int applicationId;
    private final Tenant tenant;
    private final Dorm dorm;
    private ApplicationStatus status;
    private final boolean transferConfirmed;
    private final LocalDateTime dateSubmitted;
    private LocalDateTime dateReviewed;

    // new application
    public Application(
        Tenant tenant,
        Dorm dorm,
        boolean confirmTransfer
    ) throws InvalidInputException {
        this.applicationId = 0;
        this.tenant = Validator.requiredReference(tenant, "Tenant");
        this.dorm = Validator.requiredReference(dorm, "Dorm");

        Dorm currentDorm = tenant.getCurrentDorm();

        Validator.condition(
            !Dorm.sameIdentity(currentDorm, dorm),
            "You already live in this dorm."
        );

        boolean transferring = currentDorm != null;

        Validator.condition(
            !transferring || confirmTransfer,
            "Confirm the transfer before applying to another dorm."
        );

        this.transferConfirmed = transferring && confirmTransfer;
        this.status = ApplicationStatus.PENDING;
        this.dateSubmitted = LocalDateTime.now();
        this.dateReviewed = null;
    }

    // loading application from db
    public Application(
        int applicationId,
        Tenant tenant,
        Dorm dorm,
        ApplicationStatus status,
        boolean transferConfirmed,
        LocalDateTime dateSubmitted,
        LocalDateTime dateReviewed
    ) throws InvalidInputException {
        this.applicationId = Validator.savedId(applicationId);
        this.tenant = Validator.requiredReference(tenant, "Tenant");
        this.dorm = Validator.requiredReference(dorm, "Dorm");
        this.status = Validator.requiredReference(status, "Status");
        this.transferConfirmed = transferConfirmed;
        this.dateSubmitted =
            Validator.requiredReference(dateSubmitted, "Submission date");

        if (status == ApplicationStatus.PENDING) {
            Validator.condition(
                dateReviewed == null,
                "A pending application cannot have a review date."
            );
        } else {
            Validator.requiredReference(dateReviewed, "Review date");

            Validator.condition(
                !dateReviewed.isBefore(dateSubmitted),
                "Review date cannot be before submission date."
            );
        }

        this.dateReviewed = dateReviewed;
    }

    public void accept(Landlord landlord)
        throws InvalidInputException,
               UnauthorizedActionException,
               NoAvailableSlotsException {

        requireReviewable(landlord);

        Dorm previousDorm = tenant.getCurrentDorm();

        Validator.condition(
            !Dorm.sameIdentity(previousDorm, dorm),
            "The tenant already lives in this dorm."
        );

        // Check again because the tenant's situation may have changed.
        Validator.condition(
            previousDorm == null || transferConfirmed,
            "The tenant has not confirmed a transfer."
        );

        for (Tenant resident : dorm.getTenants()) {
            Validator.condition(
                !Person.sameIdentity(resident, tenant),
                "The tenant is already listed in this dorm."
            );
        }

        if (!dorm.hasAvailableSlots()) {
            throw new NoAvailableSlotsException("Dorm is full.");
        }

        // Keep the old dorm's resident list unchanged until admission succeeds.
        // Dorm.addTenant requires the tenant's current dorm to be cleared first.
        tenant.setCurrentDormInternal(null);

        try {
            dorm.addTenant(tenant);
        } catch (InvalidInputException | NoAvailableSlotsException exception) {
            tenant.setCurrentDormInternal(previousDorm);
            throw exception;
        }

        if (previousDorm != null) {
            previousDorm.removeTenant(tenant);
        }

        status = ApplicationStatus.ACCEPTED;
        dateReviewed = LocalDateTime.now();
    }

    public void reject(Landlord landlord)
        throws InvalidInputException, UnauthorizedActionException {

        requireReviewable(landlord);

        status = ApplicationStatus.REJECTED;
        dateReviewed = LocalDateTime.now();
    }

    // Shared checks used by both accept() and reject().
    private void requireReviewable(Landlord landlord)
        throws InvalidInputException, UnauthorizedActionException {

        if (landlord == null || !landlord.ownsDorm(dorm)) {
            throw new UnauthorizedActionException(
                "Only the dorm owner may review this application."
            );
        }

        Validator.condition(
            status == ApplicationStatus.PENDING,
            "This application has already been reviewed."
        );
    }

    public String displayInfo() {
        return tenant.getName()
            + " | Dorm: " + dorm.getDormName()
            + " | Status: " + status
            + " | Submitted: " + dateSubmitted;
    }

    // Getters
    public int getApplicationId() {
        return applicationId;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public Dorm getDorm() {
        return dorm;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public boolean isTransferConfirmed() {
        return transferConfirmed;
    }

    public LocalDateTime getDateSubmitted() {
        return dateSubmitted;
    }

    public LocalDateTime getDateReviewed() {
        return dateReviewed;
    }
}
package com.ivofade.findthed.model;

import com.ivofade.findthed.enums.ApplicationStatus;
import com.ivofade.findthed.enums.DashboardType;
import com.ivofade.findthed.enums.Role;
import com.ivofade.findthed.exception.DormNotFoundException;
import com.ivofade.findthed.exception.InvalidInputException;
import com.ivofade.findthed.util.Validator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tenant extends Person {
    
    private Dorm currentDorm;
    private final ArrayList<Application> applications = new ArrayList<>();
    private final ArrayList<Dorm> interestedDorms = new ArrayList<>();

    // for registering
    public Tenant (
        String name,
        int age,
        String contactNum,
        String email,
        String rawPassword
    ) throws InvalidInputException {
        super(name, age, contactNum, email, rawPassword, Role.TENANT);
    }

    // for loading data from database 
    public Tenant (
        int personId,
        String name,
        int age,
        String contactNum,
        String email,
        String passwordHash
    ) throws InvalidInputException {
        super(Validator.savedId(personId), name, age, contactNum, email, passwordHash, Role.TENANT);
    }

    public ArrayList<Dorm> lookForDorm(List<Dorm> listings, DormFilter filter) throws InvalidInputException {
        Validator.requiredReference(listings, "Listings");
        Validator.requiredReference(filter, "Dorm Filter");

        ArrayList<Dorm> results = new ArrayList<>();

        for(Dorm dorm : listings){
            Validator.requiredReference(dorm, "Dorm");

            if (filter.matches(dorm)) {
                results.add(dorm);
            }
        }

        return results;
    }

    public Application applyToDorm(Dorm dorm, boolean confirmTransfer) throws InvalidInputException {
        Validator.requiredReference(dorm, "Dorm");

        boolean transferring = hasDorm() && !Dorm.sameIdentity(currentDorm, dorm);

        Validator.condition(!transferring || confirmTransfer,"Confirm the transfer before applying to another dorm.");

        Application application = new Application(this, dorm, confirmTransfer);

        applications.add(application);

        // Current dorm and available slots remain unchanged.
        return application;
    }

    public Dorm viewMyDorm() throws DormNotFoundException {
        if (!hasDorm()) {
            throw new DormNotFoundException(
                "You do not currently belong to a dorm."
            );
        }

        return currentDorm;
    }

    public void markInterested(Dorm dorm) throws InvalidInputException {
        Validator.requiredReference(dorm, "Dorm");
        dorm.addInterest(this);
    }

    public void removeInterest(Dorm dorm) {
        if (dorm != null) {
            dorm.removeInterest(this);
        }
    }

    public ArrayList<Announcement> viewAnnouncements() {
        if(!hasDorm()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(currentDorm.getAnnouncements());
    }

    public Complaint complain(String message, boolean anonymous) throws DormNotFoundException, InvalidInputException {

        Dorm dorm = viewMyDorm();
        String validatedMessage = Validator.message(message);

        Complaint complaint = new Complaint(dorm, this, validatedMessage, anonymous);

        dorm.addComplaint(complaint);

        return complaint;
    }

    public boolean hasDorm() {
        return currentDorm != null;
    }

    @Override
    public DashboardType getDashboardType() {
        return hasDorm() ? DashboardType.TENANT_DORM_HOME : DashboardType.TENANT_LISTINGS;
    }

    @Override 
    public String displayInfo() {
        String dormName = hasDorm() ? currentDorm.getDormName() : "None";

        return super.displayInfo() + " | Current Dorm: " + dormName;
    }

    // Getters
    public Dorm getCurrentDorm() {
        return currentDorm;
    }

    public List<Application> getApplications() {
        return Collections.unmodifiableList(new ArrayList<>(applications));
    }

    public List<Dorm> getInterestedDorms() {
        return Collections.unmodifiableList(new ArrayList<>(interestedDorms));
    }

    void setCurrentDormInternal(Dorm dorm) {
        currentDorm = dorm;
    }

    void addInterestInternal(Dorm dorm) {
    for (Dorm existing : interestedDorms) {
        if (Dorm.sameIdentity(existing, dorm)) {
            return;
        }
    }

    interestedDorms.add(dorm);
}

void removeInterestInternal(Dorm dorm) {
    Dorm dormToRemove = null;

    for (Dorm existing : interestedDorms) {
        if (Dorm.sameIdentity(existing, dorm)) {
            dormToRemove = existing;
            break;
        }
    }

    if (dormToRemove != null) {
        interestedDorms.remove(dormToRemove);
    }
}
}

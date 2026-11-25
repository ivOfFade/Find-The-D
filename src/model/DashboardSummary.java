package model;

import exception.InvalidInputException;
import util.Validator;

public final class DashboardSummary {

    private final int listingCount;
    private final int occupiedSlots;
    private final int totalSlots;
    private final int pendingApplications;

    public DashboardSummary(
        int listingCount,
        int occupiedSlots,
        int totalSlots,
        int pendingApplications
    ) throws InvalidInputException {
        Validator.condition(listingCount >= 0, "Listing count cannot be negative.");
        Validator.condition(occupiedSlots >= 0, "Occupied slots cannot be negative.");
        Validator.condition(totalSlots >= 0, "Total slots cannot be negative.");
        Validator.condition(
        occupiedSlots <= totalSlots,
        "Occupied slots cannot exceed total slots."
        );
        Validator.condition(
        pendingApplications >= 0,
        "Pending applications cannot be negative."
        );

        this.listingCount = listingCount;
        this.occupiedSlots = occupiedSlots;
        this.totalSlots = totalSlots;
        this.pendingApplications = pendingApplications;
    }

    public int getListingCount() {
        return listingCount;
    }

    public int getOccupiedSlots() {
        return occupiedSlots;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public int getPendingApplications() {
        return pendingApplications;
    }
}
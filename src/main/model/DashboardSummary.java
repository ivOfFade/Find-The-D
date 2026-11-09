package model;

public final class DashboardSummary {
    private final int dormCount;
    private final int totalSlots;
    private final int occupiedSlots;
    private final int availableSlots;
    private final int pendingApplications;

    public DashboardSummary(int dormCount, int totalSlots, int occupiedSlots,
                            int availableSlots, int pendingApplications) {
        this.dormCount = dormCount;
        this.totalSlots = totalSlots;
        this.occupiedSlots = occupiedSlots;
        this.availableSlots = availableSlots;
        this.pendingApplications = pendingApplications;
    }

    public int getDormCount() { return dormCount; }
    public int getTotalSlots() { return totalSlots; }
    public int getOccupiedSlots() { return occupiedSlots; }
    public int getAvailableSlots() { return availableSlots; }
    public int getPendingApplications() { return pendingApplications; }
}

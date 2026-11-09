package model;

import java.time.LocalDateTime;

// Landlord screens receive only these fields, never a sender/account reference.
public final class ComplaintView {
    private final int complaintId;
    private final String message;
    private final LocalDateTime datePosted;

    public ComplaintView(int complaintId, String message, LocalDateTime datePosted) {
        this.complaintId = complaintId;
        this.message = message;
        this.datePosted = datePosted;
    }

    public int getComplaintId() { return complaintId; }
    public String getMessage() { return message; }
    public LocalDateTime getDatePosted() { return datePosted; }
}

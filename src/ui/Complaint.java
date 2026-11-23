import java.time.LocalDateTime;

/** A tenant's concern about a dorm, readable only by that dorm's landlord. */
public class Complaint {
    private final int complaintId;
    private final Dorm dorm;
    private final String message;
    private final LocalDateTime datePosted;
    private final boolean anonymous;
    private final Tenant sender; // null forever when anonymous

    public Complaint(Dorm dorm, Tenant sender, String message, boolean anonymous) {
        this.complaintId = IdGenerator.next();
        this.dorm = dorm;
        this.message = Validation.message(message);
        this.anonymous = anonymous;
        this.sender = anonymous ? null : sender;
        this.datePosted = LocalDateTime.now();
    }

    public Complaint(int complaintId, Dorm dorm, Tenant sender, String message, boolean anonymous, LocalDateTime datePosted) {
        this.complaintId = complaintId;
        this.dorm = dorm;
        this.message = message;
        this.anonymous = anonymous;
        this.sender = anonymous ? null : sender;
        this.datePosted = datePosted;
    }

    public boolean isAnonymous() { return anonymous; }
    public String getSenderName() { return anonymous || sender == null ? "Anonymous" : sender.getName(); }
    public String displayInfo() { return getSenderName() + ": " + message; }

    public int getComplaintId() { return complaintId; }
    public Dorm getDorm() { return dorm; }
    public String getMessage() { return message; }
    public LocalDateTime getDatePosted() { return datePosted; }
}

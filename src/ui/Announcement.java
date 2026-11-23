import java.time.LocalDateTime;

/** A message from a landlord to the tenants of one dorm. */
public class Announcement {
    private final int announcementId;
    private final Dorm dorm;
    private final Landlord author;
    private final String message;
    private final LocalDateTime datePosted;

    public Announcement(Dorm dorm, Landlord author, String message) {
        if (!author.ownsDorm(dorm)) {
            throw new UnauthorizedActionException("Only the dorm's owner can post announcements.");
        }
        this.announcementId = IdGenerator.next();
        this.dorm = dorm;
        this.author = author;
        this.message = Validation.message(message);
        this.datePosted = LocalDateTime.now();
    }

    public Announcement(int announcementId, Dorm dorm, Landlord author, String message, LocalDateTime datePosted) {
        this.announcementId = announcementId;
        this.dorm = dorm;
        this.author = author;
        this.message = message;
        this.datePosted = datePosted;
    }

    public boolean isVisibleTo(Tenant tenant) { return dorm.equals(tenant.getCurrentDorm()); }
    public String displayInfo() { return "[" + dorm.getDormName() + "] " + message; }

    public int getAnnouncementId() { return announcementId; }
    public Dorm getDorm() { return dorm; }
    public Landlord getAuthor() { return author; }
    public String getMessage() { return message; }
    public LocalDateTime getDatePosted() { return datePosted; }
}

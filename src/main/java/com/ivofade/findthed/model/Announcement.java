package com.ivofade.findthed.model;

import com.ivofade.findthed.exception.InvalidInputException;
import com.ivofade.findthed.exception.UnauthorizedActionException;
import com.ivofade.findthed.util.Validator;
import java.time.LocalDateTime;

public class Announcement {

  private final int announcementId;
  private final Dorm dorm;
  private final Landlord author;
  private final String message;
  private final LocalDateTime datePosted;

  public Announcement(Dorm dorm, Landlord author, String message)
    throws InvalidInputException, UnauthorizedActionException {
    this(0, dorm, author, message, LocalDateTime.now(), false);
  }

  public Announcement(
    int announcementId,
    Dorm dorm,
    Landlord author,
    String message,
    LocalDateTime datePosted
  ) throws InvalidInputException, UnauthorizedActionException {
    this(announcementId, dorm, author, message, datePosted, true);
  }

  private Announcement(
    int id,
    Dorm dorm,
    Landlord author,
    String message,
    LocalDateTime datePosted,
    boolean saved
  ) throws InvalidInputException, UnauthorizedActionException {
    this.dorm = Validator.requiredReference(dorm, "Dorm");
    this.author = Validator.requiredReference(author, "Author");
    if (!author.ownsDorm(dorm)) {
      throw new UnauthorizedActionException(
        "Only the dorm owner may announce."
      );
    }
    this.announcementId = saved ? Validator.savedId(id) : 0;
    this.message = Validator.message(message);
    this.datePosted = Validator.requiredReference(datePosted, "Posting date");
  }

  public boolean isVisibleTo(Tenant tenant) {
    return (tenant != null && Dorm.sameIdentity(tenant.getCurrentDorm(), dorm));
  }

  public int getAnnouncementId() {
    return announcementId;
  }

  public Dorm getDorm() {
    return dorm;
  }

  public Landlord getAuthor() {
    return author;
  }

  public String getMessage() {
    return message;
  }

  public LocalDateTime getDatePosted() {
    return datePosted;
  }

  public String displayInfo() {
    return datePosted + " | " + message;
  }
}

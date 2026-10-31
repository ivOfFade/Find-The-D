package com.ivofade.findthed.model;

import com.ivofade.findthed.exception.InvalidInputException;
import com.ivofade.findthed.util.Validator;
import java.time.LocalDateTime;

public class Complaint {

    private final int complaintId;
    private final Dorm dorm;
    private final String message;
    private final LocalDateTime datePosted;
    private final boolean anonymous;
    private final Tenant sender;

    // new complaint
    public Complaint (
        Dorm dorm,
        Tenant sender,
        String message,
        boolean anonymous
     ) throws InvalidInputException {
        this.complaintId = 0;
        this.dorm = Validator.requiredReference(dorm, "Dorm");
        this.message = Validator.message(message);
        this.datePosted = LocalDateTime.now();
        this.anonymous = anonymous;

        Validator.requiredReference(sender, "Sender");
        Validator.condition(
            Dorm.sameIdentity(sender.getCurrentDorm(), this.dorm),
            "You can only submit a complaint about your current dorm."
        );

        if (anonymous) {
            this.sender = null;
        } else {
            this.sender = sender;
        }
    }

    //loading from database
    public Complaint(
        int complaintId,
        Dorm dorm,
        Tenant sender,
        String message,
        boolean anonymous,
        LocalDateTime datePosted
    ) throws InvalidInputException {
        this.complaintId = Validator.savedId(complaintId);
        this.dorm = Validator.requiredReference(dorm, "Dorm");
        this.message = Validator.message(message);
        this.datePosted = Validator.requiredReference(datePosted, "Posting date");
        this.anonymous = anonymous;

        if (anonymous) {
            this.sender = null;
        } else {
            // Can be null if the tenant's account was deleted.
            this.sender = sender;
        }
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public String getSenderName() {
        if(anonymous) {
            return "Anonymous";
        }

        if(sender == null) {
            return "Deleted tenant";
        }

        return sender.getName();
    }

    public String displayInfo() {
        return datePosted + " | " + getSenderName() + " | " + message;
    }

    public int getComplaintId() {
        return complaintId;
    }

    public Dorm getDorm() {
        return dorm;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getDatePosted() {
        return datePosted;
    }
}
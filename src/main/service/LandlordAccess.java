package service;

import exception.UnauthorizedActionException;
import model.Landlord;

public final class LandlordAccess {
    private LandlordAccess() { }

    public static void requireSavedLandlord(Landlord landlord) throws UnauthorizedActionException {
        if (landlord == null || landlord.getPersonId() <= 0) {
            throw new UnauthorizedActionException("A saved landlord account is required.");
        }
    }

    public static void requireSameLandlord(Landlord current, Landlord supplied)
        throws UnauthorizedActionException {
        requireSavedLandlord(current);
        requireSavedLandlord(supplied);
        if (current.getPersonId() != supplied.getPersonId()) {
            throw new UnauthorizedActionException("Use the landlord who logged in.");
        }
    }
}

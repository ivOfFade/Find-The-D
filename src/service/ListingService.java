package service;

import exception.InvalidInputException;
import exception.UnauthorizedActionException;
import model.Dorm;
import model.DormDetails;
import model.Establishment;
import model.Landlord;
import repository.DormRepository;
import util.Validator;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ListingService {
    private final Landlord currentLandlord;
    private final DormRepository dorms = new DormRepository();

    public ListingService(Landlord currentLandlord) throws UnauthorizedActionException {
        requireLandlord(currentLandlord);
        this.currentLandlord = currentLandlord;
    }

    public List<Dorm> getMyDorms(Landlord landlord)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        requireCurrentLandlord(landlord);
        return dorms.findByOwner(currentLandlord);
    }

    public Dorm postDorm(Landlord landlord, DormDetails details)
        throws SQLException, InvalidInputException, UnauthorizedActionException,
               IOException, InterruptedException {
        requireCurrentLandlord(landlord);
        Dorm dorm = new Dorm(currentLandlord, details);
        List<Establishment> nearby = dorm.getPlusCode() == null
            ? List.of()
            : new NearbyEstablishmentService().findNearby(dorm);
        dorm.replaceNearbyEstablishments(nearby);
        if (dorm.getPlusCode() != null) {
            dorm.markNearbySearchCompleted();
        }
        return dorms.insertNewDorm(dorm);
    }

    public Dorm updateDorm(int dormId, DormDetails details)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        Validator.savedId(dormId);
        return dorms.updateDormDetails(dormId, details, currentLandlord);
    }

    public void removeDorm(int dormId)
        throws SQLException, InvalidInputException, UnauthorizedActionException {
        Validator.savedId(dormId);
        dorms.deleteOwnedDorm(dormId, currentLandlord);
    }

    static void requireLandlord(Landlord landlord) throws UnauthorizedActionException {
        if (landlord == null || landlord.getPersonId() <= 0) {
            throw new UnauthorizedActionException("A saved landlord account is required.");
        }
    }

    private void requireCurrentLandlord(Landlord landlord) throws UnauthorizedActionException {
        requireLandlord(landlord);
        if (landlord.getPersonId() != currentLandlord.getPersonId()) {
            throw new UnauthorizedActionException("Use the landlord who logged in.");
        }
    }

}

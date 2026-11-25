package service;

import exception.InvalidInputException;
import exception.UnauthorizedActionException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import model.Announcement;
import model.Dorm;
import model.Landlord;
import model.Tenant;
import repository.AnnouncementRepository;
import repository.DormRepository;
import repository.TenantRepository;
import util.Validator;

public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final DormRepository dormRepository;
    private final TenantRepository tenantRepository;

    public AnnouncementService() {
        announcementRepository = new AnnouncementRepository();
        dormRepository = new DormRepository();
        tenantRepository = new TenantRepository();
    }

    public Announcement postAnnouncement(
        Landlord landlord,
        int dormId,
        String message
    ) throws
        InvalidInputException,
        UnauthorizedActionException,
        SQLException {
        Validator.requiredReference(landlord, "Landlord");
        Validator.savedId(landlord.getPersonId());
        Validator.savedId(dormId);

        Dorm dorm = dormRepository.findById(dormId).orElseThrow(
        () -> new UnauthorizedActionException(
            "Dorm does not exist or is not owned by this landlord."
        )
        );

        Announcement announcement = new Announcement(dorm, landlord, message);
        announcementRepository.save(announcement);

        dorm.addAnnouncement(announcement);
        return announcement;
    }

    public List<Announcement> viewAnnouncements(Tenant tenant)
        throws InvalidInputException, SQLException {
        Validator.requiredReference(tenant, "Tenant");
        Validator.savedId(tenant.getPersonId());

        Tenant currentTenant = tenantRepository.findById(
        tenant.getPersonId()
        ).orElseThrow(
        () -> new InvalidInputException("Tenant was not found.")
        );

        Dorm currentDorm = currentTenant.getCurrentDorm();
        if (currentDorm == null) {
        return Collections.emptyList();
        }

        return announcementRepository.findByDormId(currentDorm.getDormId());
    }

    public List<Announcement> viewAnnouncements(
        Landlord landlord,
        int dormId
        ) throws InvalidInputException, UnauthorizedActionException, SQLException {
        Validator.requiredReference(landlord, "Landlord");
        Validator.savedId(landlord.getPersonId());
        Validator.savedId(dormId);

        Dorm dorm = dormRepository.findById(dormId).orElseThrow(
            () -> new UnauthorizedActionException(
            "Dorm does not exist or is not owned by this landlord."
            )
        );

        if (!landlord.ownsDorm(dorm)) {
            throw new UnauthorizedActionException(
            "Dorm is not owned by this landlord."
            );
        }

        return announcementRepository.findByDormId(dormId);
    }
}
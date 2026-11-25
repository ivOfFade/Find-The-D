package service;

import exception.DormNotFoundException;
import exception.InvalidInputException;
import exception.UnauthorizedActionException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import model.Complaint;
import model.Dorm;
import model.Landlord;
import model.Tenant;
import repository.ComplaintRepository;
import repository.DormRepository;
import repository.TenantRepository;
import util.Validator;

public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final DormRepository dormRepository;
    private final TenantRepository tenantRepository;

    public ComplaintService() {
        complaintRepository = new ComplaintRepository();
        dormRepository = new DormRepository();
        tenantRepository = new TenantRepository();
    }

    public Complaint submitComplaint(
        Tenant tenant,
        String message,
        boolean anonymous
    ) throws InvalidInputException, DormNotFoundException, SQLException {

        Validator.requiredReference(tenant, "Tenant");
        Validator.savedId(tenant.getPersonId());

        Tenant currentTenant = tenantRepository.findById(
            tenant.getPersonId()
        ).orElseThrow(
            () -> new InvalidInputException("Tenant was not found.")
        );

        Dorm dorm = currentTenant.viewMyDorm();

        Complaint complaint = new Complaint(
            dorm,
            currentTenant,
            message,
            anonymous
        );

        complaintRepository.save(complaint);
        dorm.addComplaint(complaint);

        return complaint;
    }

    public List<Map<String, String>> viewComplaints(
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

        return complaintRepository.findForOwner(
            dormId,
            landlord.getPersonId()
        );
    }
}
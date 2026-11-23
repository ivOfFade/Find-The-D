import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** What the screens talk to: accounts, the dorm listings, and display formatting. Holds no sample data. */
final class DormService {
    private static final List<Person> ACCOUNTS = new ArrayList<>();
    private static Person current;

    private DormService() { }

    // ---------- accounts ----------
    static Person register(String role, String name, int age, String contactNum, String email,
                           String rawPassword, String otherContact) {
        String e = Validation.email(email);
        for (Person p : ACCOUNTS) {
            if (p.getEmail().equals(e)) {
                throw new InvalidInputException("That email is already registered.");
            }
        }
        Person p;
        if (role.equalsIgnoreCase("Landlord")) {
            Landlord l = new Landlord(name, age, contactNum, e, rawPassword);
            if (otherContact != null && !otherContact.trim().isEmpty()) {
                l.setOtherContact(otherContact.trim());
            }
            p = l;
        } else {
            p = new Tenant(name, age, contactNum, e, rawPassword);
        }
        ACCOUNTS.add(p);
        p.login(rawPassword);
        current = p;
        return p;
    }

    static Person login(String email, String rawPassword, String role) {
        String e = email == null ? "" : email.trim().toLowerCase();
        for (Person p : ACCOUNTS) {
            if (p.getEmail().equals(e) && p.getRole() == roleOf(role)) {
                p.login(rawPassword);
                current = p;
                return p;
            }
        }
        throw new AuthenticationException("Incorrect email or password.");
    }

    static void logout() {
        if (current != null) {
            current.logout();
        }
        current = null;
    }

    private static Role roleOf(String role) {
        return role.equalsIgnoreCase("Landlord") ? Role.LANDLORD : Role.TENANT;
    }

    static Person currentUser() { return current; }
    static Landlord currentLandlord() { return (Landlord) current; }
    static Tenant currentTenant() { return (Tenant) current; }

    // ---------- dorms ----------
    static Dorm postDorm(Landlord landlord, DormDetails d, String nearbyName, String nearbyType) {
        requireText(d.location, "Location");
        requireText(d.description, "Description");
        requireText(d.rules, "Dorm rules");
        if (d.amenities.isEmpty()) {
            throw new InvalidInputException("Pick at least one amenity.");
        }
        Dorm dorm = landlord.postDorm(d);
        if (nearbyName != null && !nearbyName.trim().isEmpty()) {
            // The form has no separate Plus Code for the nearby place, so it uses the dorm's.
            dorm.addEstablishment(new Establishment(nearbyName, EstablishmentType.fromText(nearbyType),
                dorm.getPlusCode(), nearbyType));
        }
        return dorm;
    }

    private static void requireText(String v, String field) {
        if (v == null || v.trim().isEmpty()) {
            throw new InvalidInputException(field + " is required.");
        }
    }

    static int totalTenants(Landlord l) {
        int total = 0;
        for (Dorm d : l.getMyDorms()) {
            total += d.getTenants().size();
        }
        return total;
    }

    static int totalComplaints(Landlord l) {
        int total = 0;
        for (Dorm d : l.getMyDorms()) {
            total += d.getComplaints().size();
        }
        return total;
    }

    static ArrayList<Application> pending(Dorm dorm) { return dorm.getOwner().reviewApplications(dorm); }
    static void apply(Dorm d) { Tenant t = currentTenant(); t.applyToDorm(d, t.hasDorm()); }
    static void approve(Application a) { a.getDorm().getOwner().acceptApplication(a); }
    static void decline(Application a) { a.getDorm().getOwner().rejectApplication(a); }
    static void removeTenant(Dorm d, Tenant t) { d.removeTenant(t); }

    // ---------- parsing form input ----------
    static int parseWhole(String text, String field) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException ex) {
            throw new InvalidInputException(field + " must be a whole number.");
        }
    }

    static double parseMoney(String text) {
        try {
            return Double.parseDouble(text.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException ex) {
            throw new InvalidInputException("Monthly rate must be a number.");
        }
    }

    // ---------- display text (same formats the screens always used) ----------
    static String slotsText(Dorm d) {
        int free = d.getAvailableSlots();
        return free <= 0 ? "Full" : free + (free == 1 ? " slot" : " slots");
    }

    static String money(double rate) {
        return "\u20B1" + (rate == Math.rint(rate) ? String.format("%,.0f", rate) : String.format("%,.2f", rate));
    }

    static String dateText(LocalDateTime t) {
        return t.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH));
    }

    static String timeText(LocalDateTime t) {
        return t.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)).toLowerCase();
    }

    static boolean isToday(LocalDateTime t) { return t.toLocalDate().equals(LocalDate.now()); }

    /** "11:11 pm" for today, then "2d ago", then "1w ago". */
    static String stamp(LocalDateTime t) {
        long days = ChronoUnit.DAYS.between(t.toLocalDate(), LocalDate.now());
        if (days <= 0) return timeText(t);
        if (days < 7) return days + "d ago";
        return (days / 7) + "w ago";
    }

    static String nearbyName(Dorm d) {
        return d.getEstablishments().isEmpty() ? "-" : d.getEstablishments().get(0).getName();
    }

    static String nearbyType(Dorm d) {
        return d.getEstablishments().isEmpty() ? "-" : d.getEstablishments().get(0).getTypeLabel();
    }

    static String facebook(Landlord l) {
        return (l.getOtherContact() == null || l.getOtherContact().isEmpty()) ? "-" : l.getOtherContact();
    }

    static String twoDigits(int n) { return String.format("%02d", n); }
}

/** A nearby place (store, landmark) shown on a dorm's map. */
public class Establishment {
    public static final int NEARBY_RADIUS_METERS = 1000;
    private final int establishmentId;
    private final String name;
    private final EstablishmentType type;
    private final PlusCode plusCode;
    private final String typeLabel;

    public Establishment(String name, EstablishmentType type, PlusCode plusCode) {
        this(name, type, plusCode, type.getLabel());
    }

    /** typeLabel keeps the wording the landlord typed (for example "Library"). */
    public Establishment(String name, EstablishmentType type, PlusCode plusCode, String typeLabel) {
        this.typeLabel = (typeLabel == null || typeLabel.trim().isEmpty()) ? type.getLabel() : typeLabel.trim();
        this.establishmentId = IdGenerator.next();
        this.name = Validation.required(name, "Name");
        this.type = type;
        this.plusCode = plusCode;
    }

    /** Haversine straight-line distance in meters between the decoded Plus Code centers. */
    public double distanceFrom(Dorm dorm) {
        double r = 6371000;
        double p1 = Math.toRadians(plusCode.getLatitude());
        double p2 = Math.toRadians(dorm.getPlusCode().getLatitude());
        double dp = p2 - p1;
        double dl = Math.toRadians(dorm.getPlusCode().getLongitude() - plusCode.getLongitude());
        double a = Math.sin(dp / 2) * Math.sin(dp / 2) + Math.cos(p1) * Math.cos(p2) * Math.sin(dl / 2) * Math.sin(dl / 2);
        return 2 * r * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public boolean isNearby(Dorm dorm) { return distanceFrom(dorm) <= NEARBY_RADIUS_METERS; }
    public String displayInfo() { return name + " (" + type + ")"; }

    public int getEstablishmentId() { return establishmentId; }
    public String getName() { return name; }
    public EstablishmentType getType() { return type; }
    public String getTypeLabel() { return typeLabel; }
    public PlusCode getPlusCode() { return plusCode; }
}

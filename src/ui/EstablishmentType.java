public enum EstablishmentType {
    CONVENIENCE_STORE("Convenience Store"), LAUNDRY("Laundry"), GROCERY("Grocery"),
    SCHOOL("School"), PHARMACY("Pharmacy"), LANDMARK("Landmark");

    /** Amenities a dorm can offer; the Post Dorm form and the tenant filter both read this. */
    public static final String[] AMENITIES = {"Wi-Fi", "Air Conditioning", "Laundry", "Kitchen", "Gym"};

    private final String label;
    EstablishmentType(String label) { this.label = label; }
    public String getLabel() { return label; }

    /** Best match for typed text; anything unknown is a LANDMARK. */
    public static EstablishmentType fromText(String text) {
        String t = text == null ? "" : text.trim().replace('_', ' ');
        for (EstablishmentType e : values()) {
            if (e.label.equalsIgnoreCase(t)) {
                return e;
            }
        }
        return LANDMARK;
    }
}

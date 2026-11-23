/** A validated full Open Location Code (e.g. 7Q5RJ2C2+XX). Decodes to center coordinates. */
public final class PlusCode {
    private static final String ALPHABET = "23456789CFGHJMPQRVWX";
    private final String code;
    private final double latitude;
    private final double longitude;

    public PlusCode(String raw) {
        String c = Validation.required(raw, "Plus Code").toUpperCase();
        int plus = c.indexOf('+');
        if (plus != 8 || c.indexOf('+', plus + 1) >= 0 || c.length() < 11) {
            throw new InvalidInputException("Plus Code must be a full code like 7Q5RJ2C2+XX.");
        }
        for (int i = 0; i < c.length(); i++) {
            if (i != plus && ALPHABET.indexOf(c.charAt(i)) < 0) {
                throw new InvalidInputException("Plus Code contains an invalid character.");
            }
        }
        this.code = c;
        String digits = c.replace("+", "");
        double lat = -90, lng = -180, res = 20;
        int pairLen = Math.min(digits.length(), 10);
        for (int i = 0; i < pairLen; i += 2) {
            lat += ALPHABET.indexOf(digits.charAt(i)) * res;
            lng += ALPHABET.indexOf(digits.charAt(i + 1)) * res;
            if (i + 2 < pairLen) {
                res /= 20;
            }
        }
        double latSize = res, lngSize = res;
        for (int i = 10; i < digits.length(); i++) {
            int d = ALPHABET.indexOf(digits.charAt(i));
            latSize /= 5;
            lngSize /= 4;
            lat += (d / 4) * latSize;
            lng += (d % 4) * lngSize;
        }
        this.latitude = lat + latSize / 2;
        this.longitude = lng + lngSize / 2;
    }

    public String getCode() { return code; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    @Override public String toString() { return code; }
    @Override public boolean equals(Object o) { return o instanceof PlusCode && ((PlusCode) o).code.equals(code); }
    @Override public int hashCode() { return code.hashCode(); }
}

import java.util.ArrayList;
import java.util.List;

/** Search criteria a tenant applies to the listings. Unset criteria match everything. */
public class DormFilter {
    private String keyword = "";
    private double minRate = 0;
    private double maxRate = Double.MAX_VALUE;
    private boolean onlyAvailable = false;
    private final List<String> requiredAmenities = new ArrayList<>();

    public DormFilter keyword(String k) { keyword = k == null ? "" : k.trim().toLowerCase(); return this; }
    public DormFilter rateBetween(double min, double max) { minRate = min; maxRate = max; return this; }
    public DormFilter onlyAvailable(boolean v) { onlyAvailable = v; return this; }
    public DormFilter requireAmenity(String a) { requiredAmenities.add(a); return this; }

    public boolean matches(Dorm d) {
        if (!keyword.isEmpty() && !(d.getDormName() + " " + d.getLocation()).toLowerCase().contains(keyword)) {
            return false;
        }
        if (d.getRate() < minRate || d.getRate() > maxRate) {
            return false;
        }
        if (onlyAvailable && !d.hasAvailableSlots()) {
            return false;
        }
        return d.getAmenities().containsAll(requiredAmenities);
    }
}

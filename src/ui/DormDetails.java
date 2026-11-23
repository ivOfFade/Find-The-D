import java.util.ArrayList;
import java.util.List;

/** The editable facts of a dorm listing, passed to Landlord.postDorm / manageDorm. */
public class DormDetails {
    public String dormName;
    public String location;
    public String plusCode;
    public String description = "";
    public int totalSlots;
    public double rate;
    public List<String> amenities = new ArrayList<>();
    public String rules = "";
    public List<String> photos = new ArrayList<>();

    public DormDetails() { }

    public DormDetails(String dormName, String location, String plusCode, int totalSlots, double rate) {
        this.dormName = dormName;
        this.location = location;
        this.plusCode = plusCode;
        this.totalSlots = totalSlots;
        this.rate = rate;
    }
}

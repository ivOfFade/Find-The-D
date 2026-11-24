package ui.components;

import model.Dorm;
import model.Landlord;
import exception.ServiceOperationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class UiFormat {

  private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
  private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

  private UiFormat() {}

  public static String twoDigits(int value) {
    return String.format("%02d", value);
  }

  public static String money(double value) {
    return String.format(Locale.US, "Php %,.2f", value);
  }

  public static String slotsText(Dorm dorm) {
    return twoDigits(dorm.getAvailableSlots()) + "/" + twoDigits(dorm.getTotalSlots());
  }

  public static String dateText(LocalDateTime value) {
    return value.format(DATE);
  }

  public static String timeText(LocalDateTime value) {
    return value.format(TIME);
  }

  public static String stamp(LocalDateTime value) {
    return dateText(value) + " at " + timeText(value);
  }

  public static boolean isToday(LocalDateTime value) {
    return value.toLocalDate().equals(LocalDate.now());
  }

  public static String facebook(Landlord landlord) {
    String contact = landlord.getOtherContact();
    return contact == null || contact.isBlank() ? "Not provided" : contact;
  }

  public static int parseWhole(String value, String fieldName) {
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException exception) {
      throw new ServiceOperationException(fieldName + " must be a whole number.", exception);
    }
  }

  public static double parseMoney(String value) {
    String normalized = value.replace("₱", "").replace("Php", "").replace(",", "").trim();
    try {
      return Double.parseDouble(normalized);
    } catch (NumberFormatException exception) {
      throw new ServiceOperationException("Monthly rate must be a valid amount.", exception);
    }
  }
}

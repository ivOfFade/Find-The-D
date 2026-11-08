package com.ivofade.findthed.util;

import com.ivofade.findthed.enums.Role;
import com.ivofade.findthed.exception.InvalidInputException;
import com.ivofade.findthed.model.PlusCode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class Validator {

  private Validator() {}

  // ---------------------------------------------------------------------------
  // Shared helpers - used throughout the model classes
  // ---------------------------------------------------------------------------

  public static String required(String value, String fieldName)
    throws InvalidInputException {
    if (value == null || value.trim().isEmpty()) {
      throw new InvalidInputException(fieldName + " is required.");
    }
    return value.trim();
  }

  public static Role required(Role value, String fieldName)
    throws InvalidInputException {
    if (value == null) {
      throw new InvalidInputException(fieldName + " is required.");
    }
    return value;
  }

  public static <T> T requiredReference(T value, String fieldName)
    throws InvalidInputException {
    if (value == null) {
      throw new InvalidInputException(fieldName + " is required.");
    }
    return value;
  }

  // ---------------------------------------------------------------------------
  // Person - inherited account validation for Landlord and Tenant
  // ---------------------------------------------------------------------------

  public static String name(String value) throws InvalidInputException {
    String name = required(value, "Name");
    if (name.length() > 100) {
      throw new InvalidInputException("Name must not exceed 100 characters.");
    }
    return name;
  }

  public static int age(int value) throws InvalidInputException {
    if (value < 18) {
      throw new InvalidInputException("Age must be atleast 18 years old.");
    }
    return value;
  }

  public static String contactNum(String value) throws InvalidInputException {
    String contactNumber = required(value, "Contact Number");
    if (!contactNumber.matches("^(09|\\+639)\\d{9}$")) {
      throw new InvalidInputException(
        "Contact number must be 09XXXXXXXXX or +639XXXXXXXXX."
      );
    }
    return contactNumber;
  }

  public static String email(String value) throws InvalidInputException {
    String email = required(value, "Email");
    if (
      !email.matches(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$"
      )
    ) {
      throw new InvalidInputException("Please provide a valid email.");
    }
    return email;
  }

  public static String password(String value) throws InvalidInputException {
    if (value == null || value.trim().isEmpty()) {
      throw new InvalidInputException("Password is required.");
    }
    if (value.length() < 8) {
      throw new InvalidInputException(
        "Password must contain at least 8 characters."
      );
    }
    if (value.getBytes(StandardCharsets.UTF_8).length > 72) {
      throw new InvalidInputException("Password is too long.");
    }
    return value;
  }

  public static Role role(Role value) throws InvalidInputException {
    Role role = required(value, "Role");
    return role;
  }

  // ---------------------------------------------------------------------------
  // Plus Code conversion - shared location validation
  // ---------------------------------------------------------------------------

  public static PlusCode plusCode(String value) throws InvalidInputException {
    return new PlusCode(value);
  }

  // ---------------------------------------------------------------------------
  // Announcement - message validation (reusable by Complaint)
  // ---------------------------------------------------------------------------

  public static String message(String value) throws InvalidInputException {
    String message = required(value, "Message");
    if (message.length() > 1000) {
      throw new InvalidInputException(
        "Message must not exceed 1000 characters."
      );
    }
    return message;
  }

  // ---------------------------------------------------------------------------
  // Landlord - additional contact information
  // ---------------------------------------------------------------------------

  public static String otherContact(String value) throws InvalidInputException {
    String otherContact = value;
    if (otherContact.length() > 100) {
      throw new InvalidInputException("Must not exceed 1000 characters.");
    }
    return otherContact;
  }

  // ---------------------------------------------------------------------------
  // DormDetails - listing inputs used to construct and update Dorm
  // ---------------------------------------------------------------------------

  public static String dormName(String value) throws InvalidInputException {
    String name = required(value, "Dorm name");
    if (name.length() > 150) {
      throw new InvalidInputException(
        "Dorm name must not exceed 150 characters."
      );
    }
    return name;
  }

  public static String location(String value) throws InvalidInputException {
    String location = value == null ? null : value.trim();
    if (location != null && location.length() > 255) {
      throw new InvalidInputException(
        "Location must not exceed 255 characters."
      );
    }
    return location;
  }

  public static PlusCode optionalPlusCode(String value)
    throws InvalidInputException {
    return value == null || value.trim().isEmpty() ? null : plusCode(value);
  }

  public static int totalSlots(int value) throws InvalidInputException {
    if (value < 1) {
      throw new InvalidInputException("Total slots must be at least 1.");
    }
    return value;
  }

  public static double rate(double value) throws InvalidInputException {
    if (!Double.isFinite(value) || value <= 0) {
      throw new InvalidInputException(
        "Rate must be a positive, finite number."
      );
    }
    return value;
  }

  public static ArrayList<String> amenities(List<String> values)
    throws InvalidInputException {
    return copyEntries(values, "Amenity", 100);
  }

  public static ArrayList<String> photos(List<String> values)
    throws InvalidInputException {
    return copyEntries(values, "Photo", 255);
  }

  // ---------------------------------------------------------------------------
  // DormDetails - internal helper for amenities and photos
  // ---------------------------------------------------------------------------

  private static ArrayList<String> copyEntries(
    List<String> values,
    String fieldName,
    int maxLength
  ) throws InvalidInputException {
    if (values == null) {
      throw new InvalidInputException(
        fieldName + " list must not be null. Use an empty list."
      );
    }

    ArrayList<String> result = new ArrayList<>();
    for (String value : values) {
      String cleaned = required(value, fieldName);
      if (cleaned.length() > maxLength) {
        throw new InvalidInputException(
          fieldName + " must not exceed " + maxLength + " characters."
        );
      }
      if (!result.contains(cleaned)) {
        result.add(cleaned);
      }
    }
    return result;
  }

  // ---------------------------------------------------------------------------
  // Dorm - capacity and occupancy validation
  // ---------------------------------------------------------------------------

  public static int capacity(int totalSlots, int occupants)
    throws InvalidInputException {
    totalSlots(totalSlots);
    if (occupants < 0 || totalSlots < occupants) {
      throw new InvalidInputException(
        "Capacity cannot be below current occupancy."
      );
    }
    return totalSlots;
  }

  // ---------------------------------------------------------------------------
  // Shared saved IDs - Dorm, Landlord, Announcement and Establishment
  // ---------------------------------------------------------------------------

  public static int savedId(int value) throws InvalidInputException {
    if (value <= 0) throw new InvalidInputException(
      "Saved ID must be positive."
    );
    return value;
  }

  // ---------------------------------------------------------------------------
  // Establishment - place name validation
  // ---------------------------------------------------------------------------

  public static String establishmentName(String value)
    throws InvalidInputException {
    String name = required(value, "Establishment name");
    if (name.length() > 150) {
      throw new InvalidInputException(
        "Establishment name must not exceed 150 characters."
      );
    }
    return name;
  }

  // ---------------------------------------------------------------------------
  // Shared condition checks - Dorm and Landlord
  // ---------------------------------------------------------------------------

  public static void condition(boolean valid, String message)
    throws InvalidInputException {
    if (!valid) throw new InvalidInputException(message);
  }
}

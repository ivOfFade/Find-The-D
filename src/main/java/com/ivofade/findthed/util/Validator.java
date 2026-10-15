package com.ivofade.findthed.util;

import com.ivofade.findthed.enums.Role;
import com.ivofade.findthed.exception.InvalidInputException;
import com.ivofade.findthed.model.PlusCode;
import java.nio.charset.StandardCharsets;

public final class Validator {

  private Validator() {}

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

  public static PlusCode plusCode(String value) throws InvalidInputException {
    return new PlusCode(value);
  }

  public static String message(String value) throws InvalidInputException {
    String message = required(value, "Message");
    if (message.length() > 1000) {
      throw new InvalidInputException(
        "Message must not exceed 1000 characters."
      );
    }
    return message;
  }
}

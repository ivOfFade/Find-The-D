package com.ivofade.findthed.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordHasher {

  private PasswordHasher() {}

  public static String hash(String rawPassword) {
    return BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
  }

  public static boolean matches(String rawPassword, String storedHash) {
    try {
      return BCrypt.checkpw(rawPassword, storedHash);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}

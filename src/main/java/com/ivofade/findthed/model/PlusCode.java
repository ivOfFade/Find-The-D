package com.ivofade.findthed.model;

import com.google.openlocationcode.OpenLocationCode;
import com.ivofade.findthed.exception.InvalidInputException;

public final class PlusCode {

  private final String code;
  private final double latitude;
  private final double longitude;

  public PlusCode(String code) throws InvalidInputException {
    if (code == null || code.trim().isEmpty()) {
      throw new InvalidInputException("Plus Code is required.");
    }

    String normalized = code.trim().toUpperCase();

    if (!OpenLocationCode.isFullCode(normalized)) {
      throw new InvalidInputException("Enter a valid full Plus Code.");
    }

    OpenLocationCode.CodeArea area = new OpenLocationCode(normalized).decode();

    this.code = normalized;
    this.latitude = area.getCenterLatitude();
    this.longitude = area.getCenterLongitude();
  }

  public String getCode() {
    return code;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }
}

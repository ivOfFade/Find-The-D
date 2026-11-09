package com.ivofade.findthed.model;

import com.google.openlocationcode.OpenLocationCode;
import com.ivofade.findthed.enums.EstablishmentType;
import com.ivofade.findthed.exception.InvalidInputException;
import com.ivofade.findthed.util.Validator;

public class Establishment {

  public static final int NEARBY_RADIUS_METERS = 1000;
  private static final double EARTH_RADIUS_METERS = 6371000.0;
  private final int establishmentId;
  private final String name;
  private final EstablishmentType type;
  private final PlusCode plusCode;

  public Establishment(String name, EstablishmentType type, PlusCode plusCode)
    throws InvalidInputException {
    this(0, name, type, plusCode, false);
  }

  // OpenStreetMap returns coordinates. Encode them locally to reuse our Plus Code model.
  public Establishment(String name, EstablishmentType type, double latitude, double longitude)
    throws InvalidInputException {
    this(name, type, codeFromCoordinates(latitude, longitude));
  }

  private static PlusCode codeFromCoordinates(double latitude, double longitude)
    throws InvalidInputException {
    if (!Double.isFinite(latitude) || !Double.isFinite(longitude)
        || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
      throw new InvalidInputException("Establishment coordinates are invalid.");
    }
    return new PlusCode(OpenLocationCode.encode(latitude, longitude, 15));
  }

  public Establishment(
    int establishmentId,
    String name,
    EstablishmentType type,
    PlusCode plusCode
  ) throws InvalidInputException {
    this(establishmentId, name, type, plusCode, true);
  }

  private Establishment(
    int id,
    String name,
    EstablishmentType type,
    PlusCode plusCode,
    boolean saved
  ) throws InvalidInputException {
    this.establishmentId = saved ? Validator.savedId(id) : 0;
    this.name = Validator.establishmentName(name);
    this.type = Validator.requiredReference(type, "Establishment type");
    this.plusCode = Validator.requiredReference(plusCode, "Plus Code");
  }

  public double distanceFrom(Dorm dorm) throws InvalidInputException {
    Validator.requiredReference(dorm, "Dorm");
    return distanceFrom(dorm.getPlusCode());
  }

  public double distanceFrom(PlusCode destination) throws InvalidInputException {
    Validator.requiredReference(destination, "Dorm Plus Code");
    double lat1 = Math.toRadians(plusCode.getLatitude());
    double lat2 = Math.toRadians(destination.getLatitude());
    double dLat = lat2 - lat1;
    double dLon = Math.toRadians(
      destination.getLongitude() - plusCode.getLongitude()
    );
    double a =
      Math.pow(Math.sin(dLat / 2), 2) +
      Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLon / 2), 2);
    a = Math.max(0, Math.min(1, a));
    return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  public boolean isNear(Dorm dorm) throws InvalidInputException {
    return distanceFrom(dorm) <= NEARBY_RADIUS_METERS;
  }

  public int getEstablishmentId() {
    return establishmentId;
  }

  public String getName() {
    return name;
  }

  public EstablishmentType getType() {
    return type;
  }

  public PlusCode getPlusCode() {
    return plusCode;
  }

  public String displayInfo() {
    return name + " | " + type + " | " + plusCode.getCode();
  }
}

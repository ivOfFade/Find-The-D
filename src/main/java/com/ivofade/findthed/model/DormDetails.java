package com.ivofade.findthed.model;

import com.ivofade.findthed.exception.InvalidInputException;
import com.ivofade.findthed.util.Validator;
import java.util.ArrayList;
import java.util.List;

public final class DormDetails {

  private final String dormName;
  private final String location;
  private final PlusCode plusCode;
  private final String description;
  private final int totalSlots;
  private final double rate;
  private final ArrayList<String> amenities;
  private final String rules;
  private final ArrayList<String> photos;

  public DormDetails(
    String dormName,
    String location,
    String plusCode,
    String description,
    int totalSlots,
    double rate,
    List<String> amenities,
    String rules,
    List<String> photos
  ) throws InvalidInputException {
    this.dormName = Validator.dormName(dormName);
    this.location = Validator.location(location);
    this.plusCode = Validator.optionalPlusCode(plusCode);
    this.description = description;
    this.totalSlots = Validator.totalSlots(totalSlots);
    this.rate = Validator.rate(rate);
    this.amenities = Validator.amenities(amenities);
    this.rules = rules;
    this.photos = Validator.photos(photos);
  }

  public void validate() throws InvalidInputException {
    Validator.dormName(dormName);
    Validator.location(location);
    Validator.totalSlots(totalSlots);
    Validator.rate(rate);
    Validator.amenities(amenities);
    Validator.photos(photos);
  }

  public String getDormName() {
    return dormName;
  }

  public String getLocation() {
    return location;
  }

  public PlusCode getPlusCode() {
    return plusCode;
  }

  public String getDescription() {
    return description;
  }

  public int getTotalSlots() {
    return totalSlots;
  }

  public double getRate() {
    return rate;
  }

  public ArrayList<String> getAmenities() {
    return new ArrayList<>(amenities);
  }

  public String getRules() {
    return rules;
  }

  public ArrayList<String> getPhotos() {
    return new ArrayList<>(photos);
  }
}

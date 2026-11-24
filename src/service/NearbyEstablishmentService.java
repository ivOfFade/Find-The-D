package service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import enums.EstablishmentType;
import exception.InvalidInputException;
import model.Dorm;
import model.Establishment;
import model.PlusCode;
import util.Env;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

// Geoapify provides a hosted Places API backed by OpenStreetMap data.
public class NearbyEstablishmentService {

  private static final URI ENDPOINT = URI.create(
    "https://api.geoapify.com/v2/places"
  );
  private static final String CATEGORIES = String.join(
    ",",
    "commercial.supermarket",
    "commercial.convenience",
    "commercial.health_and_beauty.pharmacy",
    "education.school",
    "education.college",
    "education.university",
    "service.cleaning.laundry"
  );

  private final String apiKey;
  private final HttpClient client;

  public NearbyEstablishmentService() {
    this(Env.require("GEOAPIFY_API_KEY"));
  }

  public NearbyEstablishmentService(String apiKey) {
    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalStateException(
        "GEOAPIFY_API_KEY is required. Add your Geoapify key to .env."
      );
    }
    this.apiKey = apiKey.trim();
    this.client = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(10))
      .build();
  }

  public List<Establishment> findNearby(Dorm dorm)
    throws InvalidInputException, IOException, InterruptedException {
    if (dorm == null) throw new InvalidInputException("Dorm is required.");
    return findNearby(dorm.getPlusCode());
  }

  public List<Establishment> findNearby(String code)
    throws InvalidInputException, IOException, InterruptedException {
    return findNearby(new PlusCode(code));
  }

  private List<Establishment> findNearby(PlusCode location)
    throws InvalidInputException, IOException, InterruptedException {
    requireLocation(location);

    String filter = String.format(
      Locale.ROOT,
      "circle:%.8f,%.8f,%d",
      location.getLongitude(),
      location.getLatitude(),
      Establishment.NEARBY_RADIUS_METERS
    );
    String query = "categories=" + encode(CATEGORIES)
      + "&filter=" + encode(filter)
      + "&limit=100&lang=en&apiKey=" + encode(apiKey);
    HttpRequest request = HttpRequest.newBuilder(
        URI.create(ENDPOINT + "?" + query)
      )
      .timeout(Duration.ofSeconds(20))
      .header("Accept", "application/json")
      .header("User-Agent", "FindTheD-coursework/1.0")
      .GET()
      .build();
    HttpResponse<String> response = client.send(
      request,
      HttpResponse.BodyHandlers.ofString()
    );
    if (response.statusCode() != 200) {
      throw new IOException(errorMessage(response.statusCode()));
    }
    return List.copyOf(readResults(response.body(), location));
  }

  private static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private static String errorMessage(int status) {
    if (status == 401 || status == 403) {
      return "Geoapify rejected the API key. Check GEOAPIFY_API_KEY and Places API access.";
    }
    if (status == 429) {
      return "Geoapify request limit reached. Try again later.";
    }
    return "Geoapify nearby search failed (HTTP " + status + ").";
  }

  private static ArrayList<Establishment> readResults(
    String json,
    PlusCode location
  ) throws IOException, InvalidInputException {
    ArrayList<Establishment> results = new ArrayList<>();
    ArrayList<String> seen = new ArrayList<>();
    try {
      JsonObject root = JsonParser.parseString(json).getAsJsonObject();
      JsonArray features = root.getAsJsonArray("features");
      if (features == null) throw new IOException("Missing Geoapify results.");
      for (JsonElement featureElement : features) {
        JsonObject feature = featureElement.getAsJsonObject();
        if (
          !feature.has("properties") ||
          !feature.get("properties").isJsonObject() ||
          !feature.has("geometry") ||
          !feature.get("geometry").isJsonObject()
        ) continue;
        JsonObject properties = feature.getAsJsonObject("properties");
        JsonArray coordinates = feature.getAsJsonObject("geometry")
          .getAsJsonArray("coordinates");
        if (properties == null || coordinates == null || coordinates.size() < 2) {
          continue;
        }

        EstablishmentType type = category(properties.getAsJsonArray("categories"));
        if (type == null) continue;

        double longitude = coordinates.get(0).getAsDouble();
        double latitude = coordinates.get(1).getAsDouble();
        Establishment locationPoint = new Establishment(
          "Location",
          type,
          latitude,
          longitude
        );
        double distance = locationPoint.distanceFrom(location);
        if (distance > Establishment.NEARBY_RADIUS_METERS) continue;

        String name = text(properties, "name");
        if (name.isBlank()) {
          name = "Unnamed " + type.toString().toLowerCase(Locale.ROOT);
        }
        if (name.length() > 100) name = name.substring(0, 100);

        String key = text(properties, "place_id");
        if (key.isBlank()) key = name + "/" + latitude + "/" + longitude;
        if (seen.contains(key)) continue;
        seen.add(key);

        results.add(new Establishment(
          name,
          type,
          latitude,
          longitude,
          (int) Math.round(distance)
        ));
      }
      results.sort(Comparator.comparingInt(Establishment::getDistanceMeters)
        .thenComparing(Establishment::getName));
      return results;
    } catch (
      com.google.gson.JsonParseException
      | IllegalStateException
      | NumberFormatException
      | InvalidInputException exception
    ) {
      throw new IOException("Invalid Geoapify nearby-search response.", exception);
    }
  }

  private static EstablishmentType category(JsonArray categories) {
    if (categories == null) return null;
    for (JsonElement category : categories) {
      String value = category.getAsString();
      if (value.equals("commercial.supermarket")) return EstablishmentType.GROCERY;
      if (value.equals("commercial.convenience")) return EstablishmentType.STORE;
      if (value.startsWith("service.cleaning.laundry")) return EstablishmentType.LAUNDRY;
      if (value.endsWith(".pharmacy")) return EstablishmentType.PHARMACY;
      if (
        value.equals("education.school") ||
        value.equals("education.college") ||
        value.equals("education.university")
      ) {
        return EstablishmentType.SCHOOL;
      }
    }
    return null;
  }

  private static String text(JsonObject object, String field) {
    if (!object.has(field) || object.get(field).isJsonNull()) return "";
    return object.get(field).getAsString();
  }

  private static void requireLocation(PlusCode location)
    throws InvalidInputException {
    if (location == null) {
      throw new InvalidInputException(
        "A dorm Plus Code is required for nearby search."
      );
    }
  }

  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Usage: java service.NearbyEstablishmentService FULL_PLUS_CODE");
      return;
    }
    try {
      PlusCode location = new PlusCode(args[0]);
      List<Establishment> places = new NearbyEstablishmentService().findNearby(args[0]);
      if (places.isEmpty()) System.out.println("No supported places within 1000 m.");
      for (Establishment place : places) {
        System.out.printf(
          Locale.ROOT,
          "%s | %s | %d m straight line%n",
          place.getName(),
          place.getType(),
          place.getDistanceMeters()
        );
      }
      System.out.println(
        "Data: Geoapify and OpenStreetMap contributors — https://www.openstreetmap.org/copyright"
      );
    } catch (InvalidInputException | IOException exception) {
      System.err.println(exception.getMessage());
      System.exit(1);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      System.err.println("Nearby search was interrupted.");
      System.exit(1);
    }
  }
}

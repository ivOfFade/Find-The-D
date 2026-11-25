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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// Geoapify provides a hosted Places API backed by OpenStreetMap data.
public class NearbyEstablishmentService {

  private static final URI ENDPOINT = URI.create("https://api.geoapify.com/v2/places");
  private static final String CATEGORIES = String.join(",",
    "commercial.supermarket",
    "commercial.convenience",
    "commercial.health_and_beauty.pharmacy",
    "education.school",
    "education.college",
    "education.university",
    "service.cleaning.laundry");
  private static final Map<String, EstablishmentType> CATEGORY_TYPES = Map.of(
    "commercial.supermarket", EstablishmentType.GROCERY,
    "commercial.convenience", EstablishmentType.STORE,
    "education.school", EstablishmentType.SCHOOL,
    "education.college", EstablishmentType.SCHOOL,
    "education.university", EstablishmentType.SCHOOL);

  private final String apiKey;
  private final HttpClient client;

  public NearbyEstablishmentService() {
    this(Env.require("GEOAPIFY_API_KEY"));
  }

  public NearbyEstablishmentService(String apiKey) {
    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalStateException("GEOAPIFY_API_KEY is required. Add your Geoapify key to .env.");
    }
    this.apiKey = apiKey.trim();
    this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
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
    if (location == null) {
      throw new InvalidInputException("A dorm Plus Code is required for nearby search.");
    }
    HttpResponse<String> response = client.send(request(location), HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() != 200) {
      throw new IOException(errorMessage(response.statusCode()));
    }
    return readResults(response.body(), location);
  }

  private HttpRequest request(PlusCode location) {
    String filter = String.format(Locale.ROOT, "circle:%.8f,%.8f,%d",
      location.getLongitude(), location.getLatitude(), Establishment.NEARBY_RADIUS_METERS);
    String query = "categories=" + encode(CATEGORIES)
      + "&filter=" + encode(filter)
      + "&limit=100&lang=en&apiKey=" + encode(apiKey);
    return HttpRequest.newBuilder(URI.create(ENDPOINT + "?" + query))
      .timeout(Duration.ofSeconds(20))
      .header("Accept", "application/json")
      .header("User-Agent", "FindTheD-coursework/1.0")
      .GET()
      .build();
  }

  private static List<Establishment> readResults(String json, PlusCode location)
    throws IOException, InvalidInputException {
    List<Establishment> results = new ArrayList<>();
    Set<String> seen = new HashSet<>();
    try {
      JsonArray features = JsonParser.parseString(json).getAsJsonObject().getAsJsonArray("features");
      if (features == null) throw new IOException("Missing Geoapify results.");
      for (JsonElement featureElement : features) {
        addFeature(results, seen, featureElement.getAsJsonObject(), location);
      }
      results.sort(Comparator.comparingInt(Establishment::getDistanceMeters).thenComparing(Establishment::getName));
      return List.copyOf(results);
    } catch (RuntimeException exception) {
      throw new IOException("Invalid Geoapify nearby-search response.", exception);
    }
  }

  private static void addFeature(List<Establishment> results, Set<String> seen,
      JsonObject feature, PlusCode location) throws InvalidInputException {
    JsonObject properties = object(feature, "properties");
    JsonObject geometry = object(feature, "geometry");
    if (properties == null || geometry == null) return;
    JsonArray coordinates = geometry.getAsJsonArray("coordinates");
    if (coordinates == null || coordinates.size() < 2) return;

    EstablishmentType type = category(properties.getAsJsonArray("categories"));
    if (type == null) return;

    double longitude = coordinates.get(0).getAsDouble();
    double latitude = coordinates.get(1).getAsDouble();
    double distance = new Establishment("Location", type, latitude, longitude).distanceFrom(location);
    if (distance > Establishment.NEARBY_RADIUS_METERS) return;

    String name = text(properties, "name");
    if (name.isBlank()) name = "Unnamed " + type.toString().toLowerCase(Locale.ROOT);
    if (name.length() > 100) name = name.substring(0, 100);

    String key = text(properties, "place_id");
    if (key.isBlank()) key = name + "/" + latitude + "/" + longitude;
    if (seen.add(key)) {
      results.add(new Establishment(name, type, latitude, longitude, (int) Math.round(distance)));
    }
  }

  private static EstablishmentType category(JsonArray categories) {
    if (categories == null) return null;
    for (JsonElement category : categories) {
      String value = category.getAsString();
      EstablishmentType type = CATEGORY_TYPES.get(value);
      if (type != null) return type;
      if (value.startsWith("service.cleaning.laundry")) return EstablishmentType.LAUNDRY;
      if (value.endsWith(".pharmacy")) return EstablishmentType.PHARMACY;
    }
    return null;
  }

  private static JsonObject object(JsonObject parent, String field) {
    return parent.has(field) && parent.get(field).isJsonObject() ? parent.getAsJsonObject(field) : null;
  }

  private static String text(JsonObject object, String field) {
    return object.has(field) && !object.get(field).isJsonNull() ? object.get(field).getAsString() : "";
  }

  private static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private static String errorMessage(int status) {
    if (status == 401 || status == 403) {
      return "Geoapify rejected the API key. Check GEOAPIFY_API_KEY and Places API access.";
    }
    if (status == 429) return "Geoapify request limit reached. Try again later.";
    return "Geoapify nearby search failed (HTTP " + status + ").";
  }

  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Usage: java service.NearbyEstablishmentService FULL_PLUS_CODE");
      return;
    }
    try {
      List<Establishment> places = new NearbyEstablishmentService().findNearby(args[0]);
      if (places.isEmpty()) System.out.println("No supported places within 1000 m.");
      for (Establishment place : places) {
        System.out.printf(Locale.ROOT, "%s | %s | %d m straight line%n",
          place.getName(), place.getType(), place.getDistanceMeters());
      }
      System.out.println("Data: Geoapify and OpenStreetMap contributors — https://www.openstreetmap.org/copyright");
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

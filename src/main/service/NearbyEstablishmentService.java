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
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.List;

// Overpass searches OpenStreetMap data. It is separate from drawing a map.
public class NearbyEstablishmentService {

  private final URI endpoint;
  private final HttpClient client;

  public NearbyEstablishmentService() {
    this(URI.create("https://overpass-api.de/api/interpreter"));
  }

  public NearbyEstablishmentService(URI endpoint) {
    this.endpoint = endpoint;
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

    // 1. Prepare the query and form data.
    String query = buildQuery(location);
    String formData =
      "data=" + URLEncoder.encode(query, StandardCharsets.UTF_8);

    // 2. Send one request. Do not keep retrying a busy public server.
    HttpRequest request = HttpRequest.newBuilder(endpoint)
      .timeout(Duration.ofSeconds(35))
      .header("User-Agent", "FindTheD-coursework/1.0")
      .header(
        "Content-Type",
        "application/x-www-form-urlencoded; charset=UTF-8"
      )
      .POST(HttpRequest.BodyPublishers.ofString(formData))
      .build();
    HttpResponse<String> response = client.send(
      request,
      HttpResponse.BodyHandlers.ofString()
    );
    if (response.statusCode() == 429) {
      throw new IOException("OpenStreetMap search is busy. Try again later.");
    }
    if (response.statusCode() != 200) {
      throw new IOException(
        "Nearby search failed (HTTP " + response.statusCode() + ")."
      );
    }
    // 3. Convert the response into our Establishment objects.
    return List.copyOf(readResults(response.body(), location));
  }

  private static String buildQuery(PlusCode location) throws InvalidInputException {
    requireLocation(location);
    String area = String.format(
      Locale.ROOT,
      "(around:%d,%.8f,%.8f)",
      Establishment.NEARBY_RADIUS_METERS,
      location.getLatitude(),
      location.getLongitude()
    );
    // nwr includes points, building outlines, and relations.
    return "[out:json][timeout:25];("
      + "nwr[shop=supermarket]" + area + ";"
      + "nwr[shop=convenience]" + area + ";"
      + "nwr[shop=laundry]" + area + ";"
      + "nwr[amenity=pharmacy]" + area + ";"
      + "nwr[amenity=school]" + area + ";"
      + "nwr[amenity=college]" + area + ";"
      + "nwr[amenity=university]" + area + ";"
      + ");out center tags;";
  }

  private static ArrayList<Establishment> readResults(String json, PlusCode location)
    throws IOException, InvalidInputException {
    requireLocation(location);
    ArrayList<Establishment> results = new ArrayList<>();
    ArrayList<String> seen = new ArrayList<>();
    try {
      JsonObject root = JsonParser.parseString(json).getAsJsonObject();
      if (root.has("remark")) {
        throw new IOException("Nearby search was incomplete. Try again later.");
      }
      JsonArray elements = root.getAsJsonArray("elements");
      if (elements == null) throw new IOException("Missing search results.");
      for (JsonElement element : elements) {
        JsonObject item = element.getAsJsonObject();
        String key =
          item.get("type").getAsString() + "/" + item.get("id").getAsString();
        if (seen.contains(key)) continue;
        seen.add(key);

        Establishment place = readPlace(item);
        if (place == null) continue;
        // A building can intersect the query circle while its center is outside.
        if (place.distanceFrom(location) <= Establishment.NEARBY_RADIUS_METERS) results.add(place);
      }
      sortNearestFirst(results, location);
      return results;
    } catch (RuntimeException | InvalidInputException e) {
      throw new IOException("Invalid nearby-search response.", e);
    }
  }

  // Read one place. Return null when it has no supported category or location.
  private static Establishment readPlace(JsonObject item)
    throws InvalidInputException {
    JsonObject tags = item.getAsJsonObject("tags");
    if (tags == null) return null;
    EstablishmentType type = category(tags);
    if (type == null) return null;

    JsonObject location;
    if (item.has("lat")) {
      location = item; // A point has its own coordinates.
    } else {
      location = item.getAsJsonObject("center"); // A building has a center.
    }
    if (
      location == null || !location.has("lat") || !location.has("lon")
    ) return null;

    String name = text(tags, "name");
    if (name.isBlank()) name =
      "Unnamed " + type.toString().toLowerCase(Locale.ROOT);
    if (name.length() > 100) name = name.substring(0, 100);

    double latitude = location.get("lat").getAsDouble();
    double longitude = location.get("lon").getAsDouble();
    return new Establishment(name, type, latitude, longitude);
  }

  private static void sortNearestFirst(
    ArrayList<Establishment> places,
    PlusCode location
  ) throws InvalidInputException {
    // Compare adjacent entries and swap any pair in the wrong order.
    for (int end = places.size() - 1; end > 0; end--) {
      boolean swapped = false;
      for (int i = 0; i < end; i++) {
        double first = places.get(i).distanceFrom(location);
        double second = places.get(i + 1).distanceFrom(location);
        if (first > second) {
          Collections.swap(places, i, i + 1);
          swapped = true;
        }
      }
      if (!swapped) return;
    }
  }

  private static EstablishmentType category(JsonObject tags) {
    String shop = text(tags, "shop");
    String amenity = text(tags, "amenity");
    if (shop.equals("supermarket")) return EstablishmentType.GROCERY;
    if (shop.equals("convenience")) return EstablishmentType.STORE;
    if (shop.equals("laundry")) return EstablishmentType.LAUNDRY;
    if (amenity.equals("pharmacy")) return EstablishmentType.PHARMACY;
    if (
      amenity.equals("school") ||
      amenity.equals("college") ||
      amenity.equals("university")
    ) {
      return EstablishmentType.SCHOOL;
    }
    return null;
  }

  private static String text(JsonObject object, String field) {
    if (!object.has(field) || object.get(field).isJsonNull()) return "";
    return object.get(field).getAsString();
  }

  private static void requireLocation(PlusCode location) throws InvalidInputException {
    if (location == null) {
      throw new InvalidInputException(
        "A dorm Plus Code is required for nearby search."
      );
    }
  }

  // Run directly when you only want to enter a full Plus Code and see places.
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Usage: java service.NearbyEstablishmentService FULL_PLUS_CODE");
      return;
    }
    try {
      PlusCode location = new PlusCode(args[0]);
      List<Establishment> places = new NearbyEstablishmentService().findNearby(args[0]);
      if (places.isEmpty()) System.out.println("No supported mapped establishments within 1000 m.");
      for (Establishment place : places) {
        System.out.printf(Locale.ROOT, "%s | %s | %.0f m straight line%n",
          place.getName(), place.getType(), place.distanceFrom(location));
      }
      System.out.println("Data: OpenStreetMap contributors — https://www.openstreetmap.org/copyright");
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

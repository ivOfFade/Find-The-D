# Nearby establishments

The landlord enters the dorm's full Plus Code. No establishment input, Google
account, API key, or billing setup is needed.

`NearbyEstablishmentService.findNearby(dorm)` or `findNearby(fullPlusCode)` decodes
the location and sends one Overpass request for OpenStreetMap places within 1000
meters. It supports groceries, convenience stores, pharmacies, laundries, and
schools/colleges/universities. Gson reads the JSON response; all required JARs
are in `lib`.

The existing Establishment model calculates an approximate straight-line
distance using haversine. Its new `distanceFrom(PlusCode)` overload also works
without constructing a Dorm. Coordinates from OSM are encoded locally into a
high precision Plus Code. Results are sorted nearest first and their list is
read-only. This does not calculate walking/driving routes.

Points use their coordinates. Building outlines and relations use their
bounding-box centers. Only supported places mapped in OSM can be returned; an
empty result does not prove there are no real establishments in the area.
Timeouts, server errors, and malformed/incomplete responses produce an
IOException instead of being reported as an empty result. No automatic retries
are made against the public server.

After compilation, from the project root:

```powershell
java -cp 'out;lib/*' service.NearbyEstablishmentService '7Q63J3JQ+22'
```

If Java on Windows reports a PKIX certificate-chain error, this run configuration
uses Windows' trusted certificate store while keeping TLS verification enabled:

```powershell
java '-Djavax.net.ssl.trustStoreType=Windows-ROOT' '-Djavax.net.ssl.trustStore=NONE' -cp 'out;lib/*' service.NearbyEstablishmentService '7Q63J3JQ+22'
```

Show `© OpenStreetMap contributors` and link to
[OpenStreetMap copyright](https://www.openstreetmap.org/copyright) beside live
results. Fetch on a background UI task when the frontend is connected. Retain
results while the screen is open and refresh on request. Saving a dorm is a
separate operation, so an internet failure does not prevent listing changes.

Reference: [Overpass query language](https://wiki.openstreetmap.org/wiki/Overpass_API/Overpass_QL).

The project has no tests or sample-data stubs, following the team's decision.

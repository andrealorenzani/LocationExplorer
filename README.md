![Upgraded with Claude](https://img.shields.io/badge/Upgraded%20with-Claude-D97757)

# LocationExplorer

A REST API created with **Spring Boot** that recommends the venues near a location, expressed **by name** (for example
`London` or `Sarzana`). It asks the [Foursquare explore API](https://developer.foursquare.com/docs/venues/explore)
and merges the answers (it can ask for several pages in parallel) into one simple reply.

It started in 2017 as a technical test (hence the old name `whitbread-test`).

## Upgraded after 9 years

The project was written in 2017 on Spring Boot 1.5 and Java 8 and was upgraded in 2026 with Claude.

| Area | Before (2017) | Now |
|------|---------------|-----|
| Java | 8 | 21 |
| Spring Boot | 1.5.7 | 4.1.2 (Spring Framework 7, Jackson 3) |
| HTTP client | `RestTemplate` | `RestClient` |
| Concurrency | `CompletableFuture` chains in the controller, on the common pool | plain blocking code; pages are fetched in parallel on virtual threads |
| Model | 9 mutable classes with getters and setters | records |
| Tests | JUnit 4, `SpringRunner`, `@MockBean`, 12 tests | JUnit 5, AssertJ, 32 tests (see below) |
| Maven wrapper | Maven 3.5.0 with a binary jar in git | Maven 3.9.11, script-only wrapper |
| Project name | `whitbread-test`, package `whitbreadtest` | `location-explorer`, package `locationexplorer` |

What changed in the code:

* **Layers**: `VenueController` only maps URLs, `VenueService` decides what to ask and merges the answers,
  `FoursquareClient` talks to Foursquare, `FoursquareExplore` is the wire format. The configuration is a typed
  `FoursquareProperties` record instead of a dozen `@Value`s.
* **Secrets are no longer in the repository.** The Foursquare client id and secret were committed in
  `application.properties`; they now come from the environment (see *Run*). If those keys are still valid, revoke
  them: they remain in the git history.
* **`/query/{location}` hides the secret.** It used to print the full Foursquare URL, secret included, to anybody who
  could call it.
* **Pagination really works.** The old code asked for the following pages with `start = totalResults` and
  `end = maxToRetrieve`, so `start > end` and no page after the first was ever requested. The next pages now start
  where the previous one ended, and the number of requests is capped by `foursquare.max-requests-per-search`.
* **Locations are URL encoded** (`New York&Co` used to break the query string) and the sections are sent with the
  spelling Foursquare expects (`nextVenues`, `topPicks`), not lower case.
* **Read-only endpoints only answer GET.** Before, any HTTP method was accepted.
* **Errors**: the handler no longer fails with a `NullPointerException` when the exception has no cause, and a
  Foursquare failure is logged once with its request id (also returned to the caller).
* The `CommandLineRunner` that only logged the bean names and the commented code were removed.

### Heads up: the Foursquare API

This service speaks the **Foursquare v2 `venues/explore` API with `client_id`/`client_secret`**, as it did in 2017.
Foursquare has since moved to a new Places API with different authentication and response format, and the v2
endpoint may not work with your credentials any more. The Foursquare-facing code is isolated in the
`foursquare` package (`FoursquareClient`, `FoursquareExplore`) so it can be ported without touching the rest; the
tests run against a fake Foursquare server.

## Build and run

Requires JDK 21+.

```bash
./mvnw verify
export FOURSQUARE_CLIENT_ID=<your id>
export FOURSQUARE_CLIENT_SECRET=<your secret>
java -jar target/location-explorer-1.0.0.jar
```

The settings are in `src/main/resources/application.properties` (page size, maximum number of requests per search,
base URL of Foursquare...) and can be overridden with environment variables or `--foursquare.page-size=50`.

## API

| Request | Result |
|---------|--------|
| `GET /?loc=<location>` | venues near the location |
| `GET /search/<location>` | the same, with the location in the path |
| `GET /search/<location>/<limit>` | at most `limit` venues |
| `GET /search/<location>/<query>/<limit>` | if `query` is one of `food`, `drinks`, `coffee`, `shops`, `arts`, `outdoors`, `sights`, `trending`, `specials`, `nextVenues`, `topPicks` (case insensitive) it selects that section, otherwise it is a free text query |
| `GET /query/<location>` | the Foursquare URL that would be invoked (secret hidden), mainly for debugging |
| `GET /status/` | health check |

```bash
curl localhost:8080/?loc=sarzana
curl localhost:8080/search/london/1
curl localhost:8080/search/london/food/3       # food is used as a section
curl localhost:8080/search/sarzana/pizza/3     # pizza is used as a generic query
curl localhost:8080/status/
```

A reply looks like:

```json
{
  "requestedLocation": "sarzana",
  "location": "sarzana",
  "headerFullLocation": "Sarzana",
  "displayLocation": "Sarzana, Liguria, Italy",
  "granularity": "city",
  "totalRes": 1,
  "recommendedVenues": [
    {"venueType": "Recommended Places", "name": "Gemmi", "url": null, "address": "Via Giuseppe Mazzini, 21",
     "city": "Sarzana", "country": "Italia", "state": "Liguria", "postalCode": "19038", "rating": 8.5}
  ]
}
```

If Foursquare fails the API answers `500` with `{"errorCode": "1", "requestId": ..., "message": ..., "cause": ...}`.

## Tests

`./mvnw test` runs 32 tests: mapping of a real Foursquare answer, the client against a mocked server (parameters,
encoding, errors, secret hiding), the paging logic (offsets, caps, limits, failures), the controllers, and the
whole application against a fake Foursquare server that knows 250 venues.

## License

[Apache License 2.0](LICENSE), Copyright 2017-2026 Andrea Lorenzani.

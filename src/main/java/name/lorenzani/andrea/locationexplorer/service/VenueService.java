package name.lorenzani.andrea.locationexplorer.service;

import name.lorenzani.andrea.locationexplorer.config.FoursquareProperties;
import name.lorenzani.andrea.locationexplorer.exception.FoursquareException;
import name.lorenzani.andrea.locationexplorer.foursquare.FoursquareClient;
import name.lorenzani.andrea.locationexplorer.foursquare.FoursquareExplore;
import name.lorenzani.andrea.locationexplorer.model.RecommendedVenue;
import name.lorenzani.andrea.locationexplorer.model.VenueByLocationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class VenueService {

    /** The names Foursquare accepts as {@code section}, by their lower case version. Anything else is free text. */
    private static final Map<String, String> SECTIONS = Stream.of("food", "drinks", "coffee", "shops", "arts",
                    "outdoors", "sights", "trending", "specials", "nextVenues", "topPicks")
            .collect(Collectors.toUnmodifiableMap(section -> section.toLowerCase(Locale.ROOT), section -> section));

    private static final Logger log = LoggerFactory.getLogger(VenueService.class);

    private final FoursquareClient client;
    private final FoursquareProperties properties;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public VenueService(FoursquareClient client, FoursquareProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    /**
     * @param query a section ("food", "coffee"...) or any word to search for; may be null
     * @param limit the maximum number of venues to return; null or zero means as many as allowed
     */
    public VenueByLocationResponse search(String location, String query, Integer limit) {
        String requestId = UUID.randomUUID().toString();
        try {
            return doSearch(location, filterFor(query), limit == null ? 0 : limit);
        } catch (RuntimeException e) {
            log.error("[{}] Error calling Foursquare API: {}", requestId, e.getMessage());
            throw new FoursquareException(requestId, "Error invoking foursquare API: " + e.getMessage(), e);
        }
    }

    static Map<String, String> filterFor(String query) {
        if (query == null || query.isBlank()) {
            return Map.of();
        }
        String normalised = query.trim().toLowerCase(Locale.ROOT);
        String section = SECTIONS.get(normalised);
        return section != null ? Map.of("section", section) : Map.of("query", normalised);
    }

    private VenueByLocationResponse doSearch(String location, Map<String, String> filter, int limit) {
        int pageSize = properties.pageSize();
        FoursquareExplore firstPage = client.explore(location, filter, 0, limit > 0 ? Math.min(limit, pageSize) : pageSize);
        List<RecommendedVenue> venues = new ArrayList<>(firstPage.venues());

        long wanted = Math.min(firstPage.totalResults(), properties.maxVenuesPerSearch());
        if (limit > 0) {
            wanted = Math.min(wanted, limit);
        }
        // The next pages start where the first one ended and are requested in parallel.
        var nextPages = new ArrayList<CompletableFuture<FoursquareExplore>>();
        if (!venues.isEmpty()) {
            for (long offset = venues.size(); offset < wanted; offset += pageSize) {
                long pageOffset = offset;
                long pageLimit = Math.min(pageSize, wanted - offset);
                nextPages.add(CompletableFuture.supplyAsync(
                        () -> client.explore(location, filter, pageOffset, pageLimit), executor));
            }
        }
        nextPages.forEach(page -> venues.addAll(page.join().venues()));
        return VenueByLocationResponse.of(location, firstPage, venues);
    }
}

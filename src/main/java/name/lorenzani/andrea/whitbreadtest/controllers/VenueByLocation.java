package name.lorenzani.andrea.whitbreadtest.controllers;

import name.lorenzani.andrea.whitbreadtest.exception.FoursquareException;
import name.lorenzani.andrea.whitbreadtest.model.VenueByLocationResponse;
import name.lorenzani.andrea.whitbreadtest.model.VenueResponse;
import name.lorenzani.andrea.whitbreadtest.utils.FoursquareInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.CompletableFuture;

// https://developer.foursquare.com/docs/venues/explore

@RestController
public class VenueByLocation {

    private static final Set<String> sections = new HashSet<>(Arrays.asList("food", "drinks", "coffee", "shops", "arts", "outdoors", "sights", "trending", "specials", "nextVenues", "topPicks"));
    private static Logger logger = LoggerFactory.getLogger(VenueByLocation.class);
    private final FoursquareInvoker invoker;
    private final int maxRetrievedPerRequest;

    @Autowired
    public VenueByLocation(@Value("${app.maxApiRequestPerSearch}") Integer maxApiRequest,
                           @Value("${foursquare.maxApiResults}") Integer maxApiResults,
                           FoursquareInvoker invoker) {
        this.maxRetrievedPerRequest = maxApiRequest * maxApiResults;
        this.invoker = invoker;
    }

    @RequestMapping("/")
    public CompletableFuture<VenueByLocationResponse> index(@RequestParam("loc") String loc) {
        return locationAndTypeLimited(loc, null, null);
    }

    @RequestMapping("/search/{location}")
    public CompletableFuture<VenueByLocationResponse> locationInPath(@PathVariable String location) {
        return locationAndTypeLimited(location, null, null);
    }

    @RequestMapping("/search/{location}/{limit}")
    public CompletableFuture<VenueByLocationResponse> locationLimited(@PathVariable String location, @PathVariable Integer limit) {
        return locationAndTypeLimited(location, null, limit);
    }

    @RequestMapping("/search/{location}/{query}/{limit}")
    public CompletableFuture<VenueByLocationResponse> locationAndTypeLimited(@PathVariable String location, @PathVariable String query, @PathVariable Integer limit) {
        String reqId = UUID.randomUUID().toString();
        String params = "";
        if (Optional.ofNullable(query).isPresent() && !query.isEmpty()) {
            if (sections.contains(query.trim().toLowerCase()))
                params = String.format("&section=%s", query.trim().toLowerCase());
            else params = String.format("&query=%s", query.trim().toLowerCase());
        }
        return callFoursquareApi(location, params, Optional.ofNullable(limit).orElse(0))
                .exceptionally(ex -> {
                    logger.error(String.format("[%s] Error calling Foursquare API: %s", reqId, ex.getMessage()));
                    throw new FoursquareException(reqId, "Error invoking foursquare API: "+ex.getMessage(), ex);
                });
    }

    @RequestMapping("/query/{location}")
    public String basicPath(@PathVariable String location) {
        return invoker.getExploreUrl(location);
    }

    private CompletableFuture<VenueByLocationResponse> callFoursquareApi(String location, String params, int limit) {
        // Please note that on the website limit is max 50 but then the api true limit is 100
        // Foursquare invocation errors are handled by FoursquareExceptionHandler
        CompletableFuture<VenueResponse> futFoursquareRes = invoker.invokeExplore(location, limit, params);
        CompletableFuture<VenueByLocationResponse> res =
                futFoursquareRes.thenApplyAsync(foursquareRes -> new VenueByLocationResponse(location, foursquareRes));
        CompletableFuture<List<VenueResponse>> multipleVenues =
                futFoursquareRes.thenComposeAsync(foursquareRes -> {
            long maxToRetrieve = Math.min(foursquareRes.getResponse().getTotalResults(), maxRetrievedPerRequest); // TotalResult is a required field
            if (limit > 0 && limit < maxToRetrieve) maxToRetrieve = limit;
            long totalres = foursquareRes.getResponse().getTotalResults();
            // Foursquare invocation errors are handled by FoursquareExceptionHandler
            return invoker.invokeMultipleExplore(location, totalres, maxToRetrieve, params);
        });
        return res.thenCombine(multipleVenues, (result, otherres) ->{
            otherres.forEach(venueResponse -> result.getRecommendedVenues().addAll(new VenueByLocationResponse("", venueResponse).getRecommendedVenues()));
            result.setTotalRes(result.getRecommendedVenues().size());
            return result;
        });
    }


}

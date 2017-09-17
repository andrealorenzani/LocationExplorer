package name.lorenzani.andrea.whitbreadtest.restclient;

import name.lorenzani.andrea.whitbreadtest.utils.FoursquareInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// https://developer.foursquare.com/docs/venues/explore

@RestController
public class VenueByLocation {

    private final FoursquareInvoker invoker;
    private final int maxRetrievedPerRequest;
    private static final Set<String> sections = new HashSet<>(Arrays.asList("food", "drinks", "coffee", "shops", "arts", "outdoors", "sights", "trending", "specials", "nextVenues", "topPicks"));

    @Autowired
    public VenueByLocation(@Value("${app.maxApiRequestPerSearch}") Integer maxApiRequest,
                           @Value("${foursquare.maxApiResults}") Integer maxApiResults,
                           FoursquareInvoker invoker) {
        this.maxRetrievedPerRequest = maxApiRequest*maxApiResults;
        this.invoker = invoker;
    }

    @RequestMapping("/")
    public VenueByLocationResponse index(@RequestParam("loc") String loc) {
        return locationAndTypeLimited(loc, null, null);
    }

    @RequestMapping("/search/{location}")
    public VenueByLocationResponse locationInPath(@PathVariable String location) {
        return locationAndTypeLimited(location, null, null);
    }

    @RequestMapping("/search/{location}/{limit}")
    public VenueByLocationResponse locationLimited(@PathVariable String location, @PathVariable Integer limit) {
        return locationAndTypeLimited(location, null, limit);
    }

    @RequestMapping("/search/{location}/{query}/{limit}")
    public VenueByLocationResponse locationAndTypeLimited(@PathVariable String location, @PathVariable String query, @PathVariable Integer limit){
        String params = "";
        if(Optional.ofNullable(query).isPresent() && !query.isEmpty()) {
            if(sections.contains(query.trim().toLowerCase())) params = String.format("&section=%s", query.trim().toLowerCase());
            else params = String.format("&query=%s", query.trim().toLowerCase());
        }
        return callFoursquareApi(location, params, Optional.ofNullable(limit).orElse(0));
    }

    @RequestMapping("/query/{location}")
    public String basicPath(@PathVariable String location) {
        return invoker.getExploreUrl(location);
    }

    private VenueByLocationResponse callFoursquareApi(String location, String params, int limit){
        // Please note that on the website limit is max 50 but then the api true limit is 100
        VenueResponse foursquareRes = new VenueResponse();
        try{
            foursquareRes = invoker.invokeExplore(location, limit, params).get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        long maxToRetrieve = Math.min(foursquareRes.getResponse().getTotalResults(), maxRetrievedPerRequest); // TotalResult is a required field
        if(limit > 0 && limit < maxToRetrieve) maxToRetrieve = limit;
        VenueByLocationResponse res = new VenueByLocationResponse(location, foursquareRes);
        try {
            invoker.invokeMultipleExplore(location, res.getTotalRes(), maxToRetrieve, params)
                   .get()
                   .forEach(venueResponse -> res.getRecommendedVenues().addAll(new VenueByLocationResponse("", venueResponse).getRecommendedVenues()));
        }
        catch (Exception e){
            e.printStackTrace();
        }
        res.setTotalRes(res.getRecommendedVenues().size());
        return res;
    }


}

package name.lorenzani.andrea.whitbreadtest.restclient;

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

    private final String url;
    private final String authParams;
    private final int maxApiRequest;
    private final int max4SResults;
    private static final Set<String> sections = new HashSet<>(Arrays.asList("food", "drinks", "coffee", "shops", "arts", "outdoors", "sights", "trending", "specials", "nextVenues", "topPicks"));

    @Autowired
    public VenueByLocation(@Value("${foursquare.baseuri}") String baseUri,
                           @Value("${foursquare.explore}") String explore,
                           @Value("${foursquare.version.accepted}") String version,
                           @Value("${foursquare.client}") String clientKey,
                           @Value("${foursquare.secret}") String secretKey,
                           @Value("${app.maxApiRequestPerSearch}") Integer maxApiRequest,
                           @Value("${foursquare.maxApiResults}") Integer maxApiResults) {
        this.url = baseUri + explore;
        this.authParams = String.format("&client_id=%s&client_secret=%s&v=%s&m=foursquare", clientKey, secretKey,version);
        this.maxApiRequest = maxApiRequest;
        this.max4SResults = maxApiResults;
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
        String path = basicPath(location);
        if(Optional.ofNullable(query).isPresent() && !query.isEmpty()) {
            if(sections.contains(query.trim().toLowerCase())) path+=String.format("&section=%s", query.trim().toLowerCase());
            else path+=String.format("&query=%s", query.trim().toLowerCase());
        }
        return callFoursquareApi(location, path, Optional.ofNullable(limit).orElse(0));
    }

    @RequestMapping("/query/{location}")
    public String basicPath(@PathVariable String location) {
        return url+location+authParams;
    }

    private VenueByLocationResponse callFoursquareApi(String location, String path, int limit){
        // Please note that on the website limit is max 50 but then the api true limit is 100
        RestTemplate restTemplate = new RestTemplate();
        String limitedPath = path + String.format("&limit=%d", (limit>0)?limit:100);
        VenueResponse foursquareRes = restTemplate.getForObject(limitedPath, VenueResponse.class);
        long maxToRetrieve = Math.min(foursquareRes.getResponse().getTotalResults(), max4SResults*maxApiRequest); // TotalResult is a required field
        if(limit > 0 && limit < maxToRetrieve) maxToRetrieve = limit;
        System.out.println(String.format("Requested query for %s and retrieved %d venues", location, foursquareRes.getResponse().getTotalResults()));
        System.out.println(path);
        VenueByLocationResponse res = new VenueByLocationResponse(location, foursquareRes);
        res.getRecommendedVenues().addAll(invoke4SquareInParallel(path, res.getTotalRes(), maxToRetrieve));
        res.setTotalRes(res.getRecommendedVenues().size());
        return res;
    }

    private List<RecommendedVenue> invoke4SquareInParallel(String path, long start, long end) {
        if(start >= end) return Collections.emptyList();
        List<Future<VenueResponse>> allSearches = new ArrayList<>();
        for(long s=start;s<end;s=s+100){
            allSearches.add(processingSingleRequest(path, s));
            if(!allSearches.stream().allMatch(Future::isDone)) {
                System.out.print("Requesting in parallel...");
                while(!allSearches.stream().allMatch(Future::isDone)){
                    try{
                        Thread.sleep(500);
                        System.out.print(".");
                    } catch(Exception e) {
                        System.out.print("x");
                    }
                    allSearches.removeIf(Future::isCancelled);
                }
                System.out.println();
            }
        }
        List<RecommendedVenue> recommendedVenues = new ArrayList<>();
        allSearches.forEach(fut -> {
            try{ recommendedVenues.addAll(new VenueByLocationResponse("", fut.get()).getRecommendedVenues()); }
            catch(Exception e){
                System.out.println("Error executing the search: "+e.getMessage());
            }
        });
        return recommendedVenues;
    }

    private Future<VenueResponse> processingSingleRequest(String path, long start){
        final ExecutorService threadpool = Executors.newCachedThreadPool();
        String limitedPath = path+String.format("&limit=100&offset=%d", start);
        return threadpool.submit(() -> new RestTemplate().getForObject(limitedPath, VenueResponse.class));
    }

}

package name.lorenzani.andrea.whitbreadtest.utils;

import name.lorenzani.andrea.whitbreadtest.model.VenueResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;

@Component
public class FoursquareInvoker {

    private final ExecutorService threadpool = Executors.newCachedThreadPool();
    private static final Set<String> sections = new HashSet<>(Arrays.asList("food", "drinks", "coffee", "shops", "arts", "outdoors", "sights", "trending", "specials", "nextVenues", "topPicks"));

    private final String url;
    private final String authParams;
    private final String explore;
    private final int max4SResults;

    @Autowired
    public FoursquareInvoker(@Value("${foursquare.baseuri}") String baseUri,
                             @Value("${foursquare.explore}") String explore,
                             @Value("${foursquare.version.accepted}") String version,
                             @Value("${foursquare.client}") String clientKey,
                             @Value("${foursquare.secret}") String secretKey,
                             @Value("${foursquare.maxApiResults}") Integer maxApiResults) {
        this.url = baseUri;
        this.authParams = String.format("&client_id=%s&client_secret=%s&v=%s&m=foursquare", clientKey, secretKey,version);
        this.max4SResults = maxApiResults;
        this.explore = explore;
    }

    public String getExploreUrl(String location) {
        return url+explore+location+authParams;
    }

    public Future<VenueResponse> invokeExplore(String location, long limit, String otherParams) {
        return invokeApi(getExploreUrl(location)+otherParams, limit, VenueResponse.class);
    }

    public Future<List<VenueResponse>> invokeMultipleExplore(String location, long start, long end, String otherParams) {
        return invokeMultipleApi(getExploreUrl(location)+otherParams, start, end, VenueResponse.class);
    }

    private <T> Future<T> invokeApi(String path, long limit, Class<T> clazz) {
        String limitedPath = path + ((limit>0)?String.format("&limit=%d", limit):"");
        return threadpool.submit(() -> new RestTemplate().getForObject(limitedPath, clazz));
    }

    private <T> Future<List<T>> invokeMultipleApi(String path, long start, long end, Class<T> clazz){
        CompletionService cs = new ExecutorCompletionService(threadpool);
        List<Future<T>> allSearches = new ArrayList<>();
        for(long s=start;s<end;s=s+max4SResults) {
            allSearches.add(invokeApi(path+String.format("&offset=%d", s), max4SResults, clazz));
        }
        return threadpool.submit(() -> {
            if(!allSearches.stream().allMatch(Future::isDone)) {
                while(!allSearches.stream().allMatch(Future::isDone)){
                    try{
                        Thread.sleep(10);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    allSearches.removeIf(Future::isCancelled);
                }
            }
            List<T> returnList = new ArrayList<>();
            allSearches.forEach(fut -> {
                try{ returnList.add(fut.get()); }
                catch(Exception e){
                    System.out.println("Error executing the search: "+e.getMessage());
                }
            });
            return returnList;
        });
    }
}

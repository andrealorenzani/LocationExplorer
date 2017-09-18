package name.lorenzani.andrea.whitbreadtest.utils;

import name.lorenzani.andrea.whitbreadtest.exception.FoursquareException;
import name.lorenzani.andrea.whitbreadtest.model.VenueResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class FoursquareInvoker {

    // Define the logger object for this class
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final String url;
    private final String authParams;
    private final String explore;
    private final int max4SResults;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    public FoursquareInvoker(@Value("${foursquare.baseuri}") String baseUri,
                             @Value("${foursquare.explore}") String explore,
                             @Value("${foursquare.version.accepted}") String version,
                             @Value("${foursquare.client}") String clientKey,
                             @Value("${foursquare.secret}") String secretKey,
                             @Value("${foursquare.maxApiResults}") Integer maxApiResults) {
        this.url = baseUri;
        this.authParams = String.format("&client_id=%s&client_secret=%s&v=%s&m=foursquare", clientKey, secretKey, version);
        this.max4SResults = maxApiResults;
        this.explore = explore;
    }

    public String getExploreUrl(String location) {
        return url + explore + location + authParams;
    }

    public VenueResponse invokeExplore(String location, long limit, String otherParams) {
        CompletableFuture<VenueResponse> res = invokeApi(getExploreUrl(location) + otherParams, limit, VenueResponse.class);
        try {
            return res.get();
        } catch (Throwable e) {
            String requestId = UUID.randomUUID().toString();
            log.error(String.format("ReqId [%s] Error invoking %s", requestId, getExploreUrl(location) + otherParams), e);
            throw new FoursquareException(requestId, "Error invoking the foursquare explore api for location " + location + ": " + e.getMessage(), e);
        }
    }

    public List<VenueResponse> invokeMultipleExplore(String location, long start, long end, String otherParams) {
        CompletableFuture<List<VenueResponse>> res = invokeMultipleApi(getExploreUrl(location) + otherParams, start, end, VenueResponse.class);
        try {
            return res.get();
        } catch (Throwable e) {
            String requestId = UUID.randomUUID().toString();
            log.error(String.format("ReqId [%s] Error invoking multiple [%d,%d] %s", requestId, start, end, getExploreUrl(location) + otherParams), e);
            throw new FoursquareException(requestId, "Error invoking the foursquare explore api for location " + location + ": " + e.getMessage(), e);
        }
    }

    private <T> CompletableFuture<T> invokeApi(String path, long limit, Class<T> clazz) {
        String limitedPath = path + ((limit > 0) ? String.format("&limit=%d", limit) : "");
        return CompletableFuture.supplyAsync(() ->
                restTemplate.getForObject(limitedPath, clazz)
        );
    }

    private <T> CompletableFuture<List<T>> invokeMultipleApi(String path, long start, long end, Class<T> clazz) {
        if (start > end) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        List<CompletableFuture<T>> allSearches = new ArrayList<>();
        for (long s = start; s < end; s = s + max4SResults) {
            allSearches.add(invokeApi(path + String.format("&offset=%d", s), Math.min(max4SResults, end - s), clazz));
        }
        return CompletableFuture.supplyAsync(() -> allSearches.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
    }
}

package name.lorenzani.andrea.whitbreadtest.restclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
public class VenueByLocation {

    private final String url;
    private final String authParams;

    @Autowired
    public VenueByLocation(@Value("${foursquare.baseuri}") String baseUri,
                           @Value("${foursquare.explore}") String explore,
                           @Value("${foursquare.version.accepted}") String version,
                           @Value("${foursquare.client}") String clientKey,
                           @Value("${foursquare.secret}") String secretKey) {
        this.url = baseUri + explore;
        this.authParams = String.format("&client_id=%s&client_secret=%s&v=%s&m=foursquare", clientKey, secretKey,version);
    }

    @RequestMapping("/")
    public String index(@RequestParam("loc") String loc) {
        RestTemplate restTemplate = new RestTemplate();
        String res = restTemplate.getForObject(url+loc+authParams, String.class);
        return res;
    }

    @RequestMapping("/str")
    public String str(@RequestParam("loc") String loc) {
        return url+loc+authParams;
    }

    }

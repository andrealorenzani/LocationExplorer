package name.lorenzani.andrea.whitbreadtest.controllers;

import name.lorenzani.andrea.whitbreadtest.exception.FoursquareException;
import name.lorenzani.andrea.whitbreadtest.model.VenueResponse;
import name.lorenzani.andrea.whitbreadtest.utils.FoursquareInvoker;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.List;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

//https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html

@RunWith(SpringRunner.class)
@RestClientTest(FoursquareInvoker.class)
public class RestServiceTest {

    @Autowired
    private FoursquareInvoker invoker;

    @Autowired
    private MockRestServiceServer server;

    @Test
    public void testBasicInteraction()
            throws Exception {
        this.server.expect(requestTo("https://api.foursquare.com/v2/venues/explore?near=sarzana&client_id=FYWEJAKHBGWP432GHW212YWH2KI2UAUVJD3WSPIQVEJ442OW&client_secret=O2PFMVK1G43JCJRTKWSZRSXVDSAMRL5FUTN4BYM22JHQNFU4&v=20170916&m=foursquare&limit=1"))
                .andRespond(withSuccess("{\"response\":{\"geocode\": {}, \"headerLocation\": null, \"headerFullLocation\": \"test\", \"headerLocationGranularity\": null, \"totalResults\": 0, \"groups\": []}}", MediaType.APPLICATION_JSON));
        VenueResponse response = this.invoker.invokeExplore("sarzana", 1, "");
        Assert.assertTrue(response.getResponse().getHeaderFullLocation().equals("test"));
    }

    @Test
    public void testMultipleInteraction()
            throws Exception {
        this.server.expect(ExpectedCount.max(10), anything())
                .andRespond(withSuccess("{\"response\":{\"geocode\": {}, \"headerLocation\": null, \"headerFullLocation\": \"test\", \"headerLocationGranularity\": null, \"totalResults\": 0, \"groups\": []}}", MediaType.APPLICATION_JSON));
        List<VenueResponse> response = this.invoker.invokeMultipleExplore("sarzana", 0, 1000, "");
        Assert.assertEquals(10, response.size());
    }

    @Test(expected = FoursquareException.class)
    public void testErrorInteraction()
            throws Exception {
        this.server.expect(ExpectedCount.max(10), anything())
                .andRespond(withBadRequest());
        List<VenueResponse> response = this.invoker.invokeMultipleExplore("sarzana", 0, 1000, "");
    }
}

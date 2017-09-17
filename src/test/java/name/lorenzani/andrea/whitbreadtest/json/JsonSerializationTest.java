package name.lorenzani.andrea.whitbreadtest.json;

import name.lorenzani.andrea.whitbreadtest.model.RecommendedVenue;
import name.lorenzani.andrea.whitbreadtest.controllers.VenueByLocationResponse;
import name.lorenzani.andrea.whitbreadtest.model.VenueResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@JsonTest
public class JsonSerializationTest {

    private String uniqueResult = "{\"meta\":{\"code\":200,\"requestId\":\"59bdc6596a60717bd9b02a4b\"},\"response\":{\"suggestedFilters\":{\"header\":\"Tap to show:\",\"filters\":[{\"name\":\"Open now\",\"key\":\"openNow\"}]},\"geocode\":{\"what\":\"\",\"where\":\"sarzana\",\"center\":{\"lat\":44.11045,\"lng\":9.96541},\"displayString\":\"Sarzana, Liguria, Italy\",\"cc\":\"IT\",\"geometry\":{\"bounds\":{\"ne\":{\"lat\":44.12596798401478,\"lng\":9.994769913656626},\"sw\":{\"lat\":44.09801112310214,\"lng\":9.937864078935945}}},\"slug\":\"sarzana-italy\",\"longId\":\"72057594041095040\"},\"warning\":{\"text\":\"There aren't a lot of results near you. Try something more general, reset your filters, or expand the search area.\"},\"headerLocation\":\"Sarzana\",\"headerFullLocation\":\"Sarzana\",\"headerLocationGranularity\":\"city\",\"totalResults\":53,\"suggestedBounds\":{\"ne\":{\"lat\":44.11412449113423,\"lng\":9.96207609389055},\"sw\":{\"lat\":44.11142483134878,\"lng\":9.959353289941928}},\"groups\":[{\"type\":\"Recommended Places\",\"name\":\"recommended\",\"items\":[{\"reasons\":{\"count\":0,\"items\":[{\"summary\":\"This spot is popular\",\"type\":\"general\",\"reasonName\":\"globalInteractionReason\"}]},\"venue\":{\"id\":\"4bc19b792a89ef3be091f288\",\"name\":\"Gemmi\",\"location\":{\"address\":\"Via Giuseppe Mazzini, 21\",\"lat\":44.1127746612415,\"lng\":9.96071469191624,\"labeledLatLngs\":[{\"label\":\"display\",\"lat\":44.1127746612415,\"lng\":9.96071469191624}],\"postalCode\":\"19038\",\"cc\":\"IT\",\"city\":\"Sarzana\",\"state\":\"Liguria\",\"country\":\"Italia\",\"formattedAddress\":[\"Via Giuseppe Mazzini, 21\",\"19038 Sarzana Liguria\",\"Italia\"]},\"categories\":[{\"id\":\"4bf58dd8d48988d1d0941735\",\"name\":\"Dessert Shop\",\"pluralName\":\"Dessert Shops\",\"shortName\":\"Desserts\",\"icon\":{\"prefix\":\"https:\\/\\/ss3.4sqi.net\\/img\\/categories_v2\\/food\\/dessert_\",\"suffix\":\".png\"},\"primary\":true}],\"verified\":false,\"stats\":{\"checkinsCount\":149,\"usersCount\":104,\"tipCount\":5},\"price\":{\"tier\":2,\"message\":\"Moderate\",\"currency\":\"\\u20ac\"},\"rating\":8.5,\"ratingColor\":\"73CF42\",\"ratingSignals\":23,\"allowMenuUrlEdit\":true,\"beenHere\":{\"count\":0,\"marked\":false,\"lastCheckinExpiredAt\":0},\"photos\":{\"count\":0,\"groups\":[]},\"hereNow\":{\"count\":0,\"summary\":\"Nobody here\",\"groups\":[]}},\"tips\":[{\"id\":\"4e41a111ae60f5540b37a9b1\",\"createdAt\":1312923921,\"text\":\"Cannoncini caldi\",\"type\":\"user\",\"canonicalUrl\":\"https:\\/\\/foursquare.com\\/item\\/4e41a111ae60f5540b37a9b1\",\"likes\":{\"count\":2,\"groups\":[],\"summary\":\"2 Likes\"},\"logView\":true,\"agreeCount\":0,\"disagreeCount\":0,\"user\":{\"id\":\"10999990\",\"firstName\":\"Vitaliano\",\"lastName\":\"Vitale\",\"gender\":\"male\",\"photo\":{\"prefix\":\"https:\\/\\/igx.4sqi.net\\/img\\/user\\/\",\"suffix\":\"\\/JEXBQKOBIRO4HZFG.jpg\"}}}],\"referralId\":\"e-0-4bc19b792a89ef3be091f288-0\"}]}]}}";
    private String uniqueExpected = "{\"requestedLocation\":\"\",\"location\":\"sarzana\",\"headerFullLocation\":\"Sarzana\",\"displayLocation\":\"Sarzana, Liguria, Italy\",\"granularity\":\"city\",\"totalRes\":1,\"recommendedVenues\":[{\"venueType\":\"Recommended Places\",\"name\":\"Gemmi\",\"url\":null,\"address\":\"Via Giuseppe Mazzini, 21\",\"city\":\"Sarzana\",\"country\":\"Italia\",\"state\":\"Liguria\",\"postalCode\":\"19038\",\"rating\":8.5}]}";

    @Autowired
    private JacksonTester<VenueResponse> jsonReader;

    @Autowired
    private JacksonTester<VenueByLocationResponse> jsonWriter;

    @Autowired
    private JacksonTester<RecommendedVenue> json2Writer;

    @Test
    public void testSerialize() throws Exception {

    }

    @Test
    public void testEmptyDeserialize() throws Exception {
        VenueResponse res = this.jsonReader.parse("{\"response\":{\"geocode\": {}, \"headerLocation\": null, \"headerFullLocation\": null, \"headerLocationGranularity\": null, \"totalResults\": 0, \"groups\": []}}").getObject();
        Assert.assertTrue(res.getResponse() != null);
        Assert.assertTrue(res.getResponse().getTotalResults()==0);
        Assert.assertTrue(res.getResponse().getHeaderFullLocation()==null);
        Assert.assertTrue(res.getResponse().getGeocode() != null);
    }

    @Test
    public void testExampleSerialization() throws Exception {
        VenueByLocationResponse enclosing = new VenueByLocationResponse();
        RecommendedVenue rvenue = new RecommendedVenue();
        rvenue.setAddress("addr");
        rvenue.setName("name");
        String rvenueRepp = "{\"venueType\":null,\"name\":\"name\",\"url\":null,\"address\":\"addr\",\"city\":null,\"country\":null,\"state\":null,\"postalCode\":null,\"rating\":null}";
        enclosing.getRecommendedVenues().add(rvenue);
        enclosing.setTotalRes(10);
        enclosing.setLocation("loc");
        enclosing.setGranularity("grano");
        String enclosingRepp = String.format("{\"requestedLocation\":null,\"location\":\"loc\",\"headerFullLocation\":null,\"displayLocation\":null,\"granularity\":\"grano\",\"totalRes\":10,\"recommendedVenues\":[%s]}", rvenueRepp);
        Assert.assertEquals(enclosingRepp, jsonWriter.write(enclosing).getJson());
    }

    @Test
    public void testUniqueDeserialize() throws Exception {
        VenueResponse res = this.jsonReader.parse(uniqueResult).getObject();
        Assert.assertTrue(res.getResponse() != null);
        Assert.assertTrue(res.getResponse().getTotalResults()==53);
        Assert.assertEquals("Sarzana", res.getResponse().getHeaderFullLocation());
        Assert.assertEquals("Gemmi",res.getResponse().getGroups().get(0).getItems().get(0).getVenue().getName());
    }

    @Test
    public void testUniqueSerializer() throws Exception {
        VenueResponse foursquareres = this.jsonReader.parse(uniqueResult).getObject();
        VenueByLocationResponse res = new VenueByLocationResponse("", foursquareres);
        String resStr = jsonWriter.write(res).getJson();
        Assert.assertEquals(uniqueExpected, resStr);
    }
}

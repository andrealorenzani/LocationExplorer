package name.lorenzani.andrea.whitbreadtest.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.assertTrue;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HealthcheckControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Value(value = "${app.name}")
    private String value;

    @Test
    public void itShouldGiveBackHealthyStatusWhenRequested() {
        Map body = this.restTemplate.getForObject("/status/", Map.class);
        assertTrue(body.get("status").equals(new Integer(200)));
        assertTrue(body.get("message").equals("I am alive, " + value));
    }

    @Test
    public void itShouldFailIfNotAGetRequest() {
        ResponseEntity<Map> body = this.restTemplate.postForEntity("/status/", "", Map.class);
        assertTrue(body.getStatusCode().equals(HttpStatus.METHOD_NOT_ALLOWED));
    }

}


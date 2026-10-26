package name.lorenzani.andrea.locationexplorer.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthcheckController {

    private final String appName;

    public HealthcheckController(@Value("${app.name}") String appName) {
        this.appName = appName;
    }

    @GetMapping({"/status", "/status/"})
    public Map<String, Object> status() {
        return Map.of("status", 200, "message", "I am alive, " + appName);
    }
}

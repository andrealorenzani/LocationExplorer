package name.lorenzani.andrea.locationexplorer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LocationExplorerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocationExplorerApplication.class, args);
    }
}

package tech.sangdang.lmscoreapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LmsCoreApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(LmsCoreApiApplication.class, args);
  }
}

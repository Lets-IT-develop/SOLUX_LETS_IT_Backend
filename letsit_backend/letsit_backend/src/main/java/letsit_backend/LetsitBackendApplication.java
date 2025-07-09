package letsit_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class LetsitBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LetsitBackendApplication.class, args);
    }

}

package org.eldir;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EldirServerApplication {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "server");

        SpringApplication.run(EldirServerApplication.class, args);
    }
}
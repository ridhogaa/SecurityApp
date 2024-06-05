package org.ergea.securityapp;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition
public class SecurityAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityAppApplication.class, args);
    }

}

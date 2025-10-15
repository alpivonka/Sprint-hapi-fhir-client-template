package com.spring.hapi.fhirclient.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Run the verifier with the "verify" profile:
 *   mvn spring-boot:run -Dspring-boot.run.profiles=verify
 *   mvn --% spring-boot:run -Dspring-boot.run.profiles=verify
 */

@Component
@ConditionalOnProperty(name = "app.verify.enabled", havingValue = "true")
public class VerifyRunner implements CommandLineRunner {

    private final FhirSearchYamlVerifier verifier;

    public VerifyRunner(FhirSearchYamlVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public void run(String... args) {
        verifier.verifyAll();
    }
}

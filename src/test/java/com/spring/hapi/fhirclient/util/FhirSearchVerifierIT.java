package com.spring.hapi.fhirclient.util;

import com.spring.hapi.fhirclient.util.FhirSearchYamlVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
@ActiveProfiles("verify")
class FhirSearchVerifierIT {

    @Autowired
    FhirSearchYamlVerifier verifier;

    @Test
    void verifySearchYamls() {
        verifier.verifyAll();
    }
}

package com.spring.hapi.fhirclient;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureWireMock(port = 8089)
@ActiveProfiles("test")
public class FhirSearchIT {

  @BeforeAll
  static void setupStubs() {
    WireMock.configureFor("localhost", 8089);
    // Minimal /metadata for R4
    stubFor(get(urlEqualTo("/fhir/metadata"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
                .withBody("{\"resourceType\":\"CapabilityStatement\",\"fhirVersion\":\"4.0.1\"}")));
    // Simple Observation search
    stubFor(get(urlPathEqualTo("/fhir/Observation"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/fhir+json")
            .withBody("{\"resourceType\":\"Bundle\",\"type\":\"searchset\",\"total\":0}")));
  }

  @Test
  void wiremockBoots() {
    assertTrue(true);
  }
}

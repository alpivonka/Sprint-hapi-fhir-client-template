// src/main/java/com/spring/hapi/fhirclient/config/FhirClientProperties.java
package com.spring.hapi.fhirclient.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * FHIR client settings (bound from prefix {@code fhir-server}).
 */
@Validated
@ConfigurationProperties(prefix = "fhir-server")
public record FhirClientProperties(
        @NotBlank String baseUrl,
        @Positive int connectTimeoutMs,
        @Positive int socketTimeoutMs
) {
  // Defaults & guardrails
  public FhirClientProperties {
    if (connectTimeoutMs <= 0) connectTimeoutMs = 5_000;
    if (socketTimeoutMs  <= 0) socketTimeoutMs  = 15_000;
  }
}

package com.spring.hapi.fhirclient.config;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Retry settings bound from application properties with prefix {@code FHIR-request-retry}.
 *
 * Example (application.yml):
 * FHIR-request-retry:
 *   enabled: true
 *   max-attempts: 3
 *   backoff-ms: 200
 */
@Validated
@ConfigurationProperties(prefix = "fhir-request-retry")
public record RetryProperties (boolean enabled, @Positive int maxAttempts, @Positive long backoffMs ){}

// src/main/java/com/spring/hapi/fhirclient/config/RetryProperties.java
package com.spring.hapi.fhirclient.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Retry settings (bound from prefix {@code retry}).
 */
@Validated
@ConfigurationProperties(prefix = "retry")
public record RetryProperties(
        boolean enabled,
        @Positive int maxAttempts,
        @Positive long backoffMs
) {
  // defaults
  public RetryProperties {
    if (maxAttempts <= 0) maxAttempts = 3;
    if (backoffMs  <= 0)  backoffMs  = 200;
  }
}

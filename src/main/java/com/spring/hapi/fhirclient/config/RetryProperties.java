package com.spring.hapi.fhirclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
/**
 * Retry settings bound from application properties with prefix {@code FHIR-request-retry}.
 *
 * Example (application.yml):
 * FHIR-request-retry:
 *   enabled: true
 *   max-attempts: 3
 *   backoff-ms: 200
 */
@Configuration
@ConfigurationProperties(prefix = "FHIR-request-retry")
public class RetryProperties {
  private boolean enabled = true;
  private int maxAttempts = 3;
  private long backoffMs = 200;

  public boolean isEnabled() { return enabled; }
  public void setEnabled(boolean enabled) { this.enabled = enabled; }
  public int getMaxAttempts() { return maxAttempts; }
  public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
  public long getBackoffMs() { return backoffMs; }
  public void setBackoffMs(long backoffMs) { this.backoffMs = backoffMs; }
}

package com.spring.hapi.fhirclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
/**
 * FHIR client settings bound from application properties with prefix {@code fhir-server}.
 *
 *Example (application.yml):
 *
 * fhir-server:
 *   base-url: https://hapi.fhir.org/baseR4
 *   connect-timeout-ms: 5000
 *   socket-timeout-ms: 15000

 */
@Configuration
@ConfigurationProperties(prefix = "fhir-server")
public class FhirClientProperties {
  private String baseUrl;
  private int connectTimeoutMs = 5000;
  private int socketTimeoutMs = 15000;

  public String getBaseUrl() { return baseUrl; }
  public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
  public int getConnectTimeoutMs() { return connectTimeoutMs; }
  public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
  public int getSocketTimeoutMs() { return socketTimeoutMs; }
  public void setSocketTimeoutMs(int socketTimeoutMs) { this.socketTimeoutMs = socketTimeoutMs; }
}

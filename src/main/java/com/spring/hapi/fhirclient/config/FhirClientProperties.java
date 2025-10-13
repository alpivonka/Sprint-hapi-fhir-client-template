package com.spring.hapi.fhirclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "fhir")
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

package com.spring.hapi.fhirclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "security.oauth2")
public class SecurityProperties {
  private boolean enabled = false;
  private String provider;
  private String tokenUri;
  private String clientId;
  private String clientSecret;
  private String scope;

  public boolean isEnabled() { return enabled; }
  public void setEnabled(boolean enabled) { this.enabled = enabled; }
  public String getProvider() { return provider; }
  public void setProvider(String provider) { this.provider = provider; }
  public String getTokenUri() { return tokenUri; }
  public void setTokenUri(String tokenUri) { this.tokenUri = tokenUri; }
  public String getClientId() { return clientId; }
  public void setClientId(String clientId) { this.clientId = clientId; }
  public String getClientSecret() { return clientSecret; }
  public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
  public String getScope() { return scope; }
  public void setScope(String scope) { this.scope = scope; }
}

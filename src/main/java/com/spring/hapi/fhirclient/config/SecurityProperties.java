package com.spring.hapi.fhirclient.config;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * OAuth2/OpenID Connect client settings bound from properties with prefix {@code security.oauth2}.
 *
 * Example (application.yml):
 * security:
 *   oauth2:
 *     enabled: true
 *     provider: my-idp
 *     token-uri: https://idp.example.com/oauth2/token
 *     client-id: my-client
 *     client-secret: ${OAUTH_CLIENT_SECRET}
 *     scope: "system/*.read"
 */
@Validated
@ConfigurationProperties(prefix = "security.oauth2")
public record SecurityProperties (
  boolean enabled,
  String provider,
  @NotBlank String tokenUri,
  @NotBlank   String clientId,
  @NotBlank  String clientSecret,
  String scope
){}


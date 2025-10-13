package com.spring.hapi.fhirclient.security;

import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import com.spring.hapi.fhirclient.config.SecurityProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Component
public class OAuth2AccessTokenProvider {
  private final SecurityProperties props;
  private final RestTemplate rest = new RestTemplate();
  private volatile String tokenValue;
  private volatile Instant expiresAt = Instant.EPOCH;

  public OAuth2AccessTokenProvider(SecurityProperties props) {
    this.props = props;
  }

  public Optional<String> getAccessToken() {
    if (!props.isEnabled()) return Optional.empty();
    if (tokenValue == null || Instant.now().isAfter(expiresAt.minusSeconds(60))) {
      fetchToken();
    }
    return Optional.ofNullable(tokenValue);
  }

  private synchronized void fetchToken() {
    if (!props.isEnabled()) return;
    if (tokenValue != null && Instant.now().isBefore(expiresAt.minusSeconds(60))) return;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", "client_credentials");
    if (props.getScope()!=null && !props.getScope().isBlank()) form.add("scope", props.getScope());
    form.add("client_id", props.getClientId());
    form.add("client_secret", props.getClientSecret());

    HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(form, headers);
    ResponseEntity<Map> resp = rest.postForEntity(props.getTokenUri(), req, Map.class);
    Map body = resp.getBody();
    if (body == null) throw new IllegalStateException("Empty token response");
    Object at = body.get("access_token");
    Object exp = body.get("expires_in");
    if (at == null) throw new IllegalStateException("No access_token in response");
    tokenValue = String.valueOf(at);
    long ttl = 3600;
    if (exp != null) {
      try { ttl = Long.parseLong(String.valueOf(exp)); } catch (Exception ignored) {}
    }
    expiresAt = Instant.now().plusSeconds(ttl);
  }

  public void install(ca.uhn.fhir.rest.client.api.IGenericClient client) {
    if (!props.isEnabled()) return;
    client.registerInterceptor(new IClientInterceptor() {
      @Override public void interceptRequest(IHttpRequest theRequest) {
        getAccessToken().ifPresent(tok -> theRequest.addHeader("Authorization", "Bearer " + tok));
      }
      @Override public void interceptResponse(IHttpResponse theResponse) throws IOException { }
    });
  }
}

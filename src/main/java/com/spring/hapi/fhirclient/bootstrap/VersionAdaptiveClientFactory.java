package com.spring.hapi.fhirclient.bootstrap;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.spring.hapi.fhirclient.client.FhirRelease;
import com.spring.hapi.fhirclient.client.VersionedClient;
import com.spring.hapi.fhirclient.config.FhirClientProperties;
import com.spring.hapi.fhirclient.interceptors.InterceptorRegistryFactory;
import com.spring.hapi.fhirclient.security.OAuth2AccessTokenProvider;
import com.spring.hapi.fhirclient.util.ResilientExecutor;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Builds a HAPI FHIR client configured for the server's FHIR release (R4/R4B/R5).
 * <p>Detects release via GET {baseUrl}/metadata, then creates a matching FhirContext/client,
 * installs OAuth/interceptors, and fetches the CapabilityStatement.</p>
 */
@Component
public class VersionAdaptiveClientFactory {
  private final RestTemplate restTemplate = new RestTemplate();
  private final FhirClientProperties props;
  private final OAuth2AccessTokenProvider tokenProvider;
  private final InterceptorRegistryFactory interceptors;
  private final ResilientExecutor resilient;

  public VersionAdaptiveClientFactory(FhirClientProperties props,
                                      OAuth2AccessTokenProvider tokenProvider,
                                      InterceptorRegistryFactory interceptors,
                                      ResilientExecutor resilient) {
    this.props = props;
    this.tokenProvider = tokenProvider;
    this.interceptors = interceptors;
    this.resilient = resilient;
  }
  /**
   * Create a client adapted to the server's FHIR version.
   *
   * <p>Steps:</p>
   * <ol>
   *   <li>GET {baseUrl}/metadata (JSON)</li>
   *   <li>Map fhirVersion → {@link FhirRelease}</li>
   *   <li>Build {@link FhirContext} and {@link IGenericClient}</li>
   *   <li>Install OAuth and custom interceptors</li>
   *   <li>Fetch {@link CapabilityStatement}</li>
   * </ol>
   *
   * @return versioned client (context, generic client, capability statement, release)
   * @throws org.springframework.web.client.RestClientException on /metadata failure
   */
  public VersionedClient create() {
    String base = props.baseUrl().replaceAll("/+$","");
    String metaUrl = base + "/metadata";

    HttpHeaders headers = new HttpHeaders();
    tokenProvider.getAccessToken().ifPresent(tok -> headers.setBearerAuth(tok));
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    ResponseEntity<Map> resp = restTemplate.exchange(metaUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

    String fhirVersion = null;
    if (resp.getBody()!=null) {
      Object fv = resp.getBody().get("fhirVersion");
      if (fv instanceof String s) fhirVersion = s;
    }

    FhirRelease release = normalize(fhirVersion);
    FhirContext ctx = switch (release) {
      case R4 -> FhirContext.forR4();
      case R4B -> FhirContext.forR4B();
      case R5 -> FhirContext.forR5();
    };
    ctx.getRestfulClientFactory().setConnectTimeout(props.connectTimeoutMs());
    ctx.getRestfulClientFactory().setSocketTimeout(props.socketTimeoutMs());

    IGenericClient client = ctx.newRestfulGenericClient(base);
    tokenProvider.install(client);
    interceptors.createAll().forEach(client::registerInterceptor);

    CapabilityStatement capability = resilient.run("capabilities", () -> client.capabilities().ofType(CapabilityStatement.class).execute());
    return new VersionedClient(ctx, client, capability, release);
  }
  /**
   * Map raw version (e.g. "4.0.1", "4.3.x", "5.0.x") to {@link FhirRelease}.
   * Defaults to R4 for unknown/blank.
   *
   * @param raw raw fhirVersion from CapabilityStatement (may be null)
   * @return release enum
   */
  private FhirRelease normalize(String raw) {
    if (raw == null) return FhirRelease.R4;
    String s = raw.toLowerCase();
    if (s.startsWith("4.3")) return FhirRelease.R4B;
    if (s.startsWith("4.")) return FhirRelease.R4;
    if (s.startsWith("5.")) return FhirRelease.R5;
    return FhirRelease.R4;
  }
}

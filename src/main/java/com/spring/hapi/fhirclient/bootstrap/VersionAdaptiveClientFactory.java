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
 * This factory will produce FHIR versioned resources based on what is found out
 * when the /metadata call returns for the FHIR server in which the application is
 * exeucting searches against.
 *
 * This is dependent upon Srping RestTesmplate and not any specific version of FHIR to obtain the /metadata response.
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

  public VersionedClient create() {
    String base = props.getBaseUrl().replaceAll("/+$","");
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
    ctx.getRestfulClientFactory().setConnectTimeout(props.getConnectTimeoutMs());
    ctx.getRestfulClientFactory().setSocketTimeout(props.getSocketTimeoutMs());

    IGenericClient client = ctx.newRestfulGenericClient(base);
    tokenProvider.install(client);
    interceptors.createAll().forEach(client::registerInterceptor);

    CapabilityStatement capability = resilient.run("capabilities", () -> client.capabilities().ofType(CapabilityStatement.class).execute());
    return new VersionedClient(ctx, client, capability, release);
  }

  private FhirRelease normalize(String raw) {
    if (raw == null) return FhirRelease.R4;
    String s = raw.toLowerCase();
    if (s.startsWith("4.3")) return FhirRelease.R4B;
    if (s.startsWith("4.")) return FhirRelease.R4;
    if (s.startsWith("5.")) return FhirRelease.R5;
    return FhirRelease.R4;
  }
}

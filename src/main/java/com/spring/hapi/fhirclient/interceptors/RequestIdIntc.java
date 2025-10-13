package com.spring.hapi.fhirclient.interceptors;

import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix="interceptors.request-id", name="enabled", havingValue="true")
public class RequestIdIntc implements IClientInterceptor {
  @Value("${interceptors.request-id.header-name:X-Request-Id}")
  private String headerName;

  @Override public void interceptRequest(IHttpRequest request) {
    request.addHeader(headerName, UUID.randomUUID().toString());
  }
  @Override public void interceptResponse(IHttpResponse response) throws IOException { }
}

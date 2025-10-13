package com.spring.hapi.fhirclient.interceptors;

import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix="interceptors.logging", name="enabled", havingValue="true")
public class LoggingIntc extends LoggingInterceptor {
  public LoggingIntc(
    @Value("${interceptors.logging.log-request-body:false}") boolean logReq,
    @Value("${interceptors.logging.log-response-body:false}") boolean logResp
  ) {
    setLogRequestBody(logReq);
    setLogResponseBody(logResp);
  }
}

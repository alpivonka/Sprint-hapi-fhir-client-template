package com.spring.hapi.fhirclient.interceptors;

import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Hapi FHIR Interceptor that captures the start and stop time of a url call.
 */
@Component
@ConditionalOnProperty(prefix="interceptors.timing", name="enabled", havingValue="true")
public class TimingIntc implements IClientInterceptor {
  private static final Logger LOG = LoggerFactory.getLogger(TimingIntc.class);

  private static class ReqInfo {
    final String method;
    final String uri;
    final long startMs;
    ReqInfo(String method, String uri, long startMs) { this.method = method; this.uri = uri; this.startMs = startMs; }
  }

  private final ThreadLocal<ReqInfo> ctx = new ThreadLocal<>();

  @Override public void interceptRequest(IHttpRequest request) {
    String method = request != null && request.getHttpVerbName()!=null ? request.getHttpVerbName() : "HTTP";
    String uri = request != null ? request.getUri() : "<unknown>";
    ctx.set(new ReqInfo(method, uri, System.currentTimeMillis()));
  }

  @Override public void interceptResponse(IHttpResponse response) throws IOException {
    ReqInfo info = ctx.get();
    long dur = System.currentTimeMillis() - (info != null ? info.startMs : System.currentTimeMillis());
    if (info != null) {
      LOG.info("[TIMING] {} {} -> {} ms", info.method, info.uri, dur);
    } else {
      LOG.info("[TIMING] request completed in {} ms", dur);
    }
    ctx.remove();
  }
}

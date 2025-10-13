package com.spring.hapi.fhirclient.interceptors;

import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Interceptor to log the raw, ascii and decoded URL for a FHIR search
 * for debugging.
 */
@Component
@ConditionalOnProperty(prefix="interceptors.raw-url", name="enabled", havingValue="true")
public class RawUrlLoggerIntc implements IClientInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(RawUrlLoggerIntc.class);

    @Override
    public void interceptRequest(IHttpRequest request) {
        String raw = request.getUri(); // what HAPI will call
        String ascii = raw;
        try { ascii = URI.create(raw).toASCIIString(); } catch (Exception ignored) {}
        String decoded = raw;
        try { decoded = URLDecoder.decode(raw, StandardCharsets.UTF_8); } catch (Exception ignored) {}

        LOG.info("[FHIR-URL] method={} raw={} ascii={} decoded={}", request.getHttpVerbName(), raw, ascii, decoded);

        List<String> auth = request.getAllHeaders().get("Authorization");
        if (auth != null && !auth.isEmpty()) {
            LOG.info("[FHIR-URL] Authorization: Bearer ***** ({} chars)", auth.get(0).length());
        }
    }

    @Override
    public void interceptResponse(IHttpResponse response) throws IOException {
        // no-op
    }
}

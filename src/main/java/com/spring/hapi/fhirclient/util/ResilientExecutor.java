package com.spring.hapi.fhirclient.util;

import com.spring.hapi.fhirclient.config.RetryProperties;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Supplier;

@Component
public class ResilientExecutor {
  private final RetryProperties props;
  private final RetryRegistry registry;

  public ResilientExecutor(RetryProperties props) {
    this.props = props;
    RetryConfig config = RetryConfig.custom()
        .maxAttempts(props.maxAttempts())
        .waitDuration(Duration.ofMillis(props.backoffMs()))
        .build();
    this.registry = RetryRegistry.of(config);
  }

  public <T> T run(String name, Supplier<T> supplier) {
    if (!props.enabled()) return supplier.get();
    Retry retry = registry.retry(name);
    Supplier<T> decorated = Retry.decorateSupplier(retry, supplier);
    return decorated.get();
  }
}

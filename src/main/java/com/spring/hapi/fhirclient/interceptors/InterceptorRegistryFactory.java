package com.spring.hapi.fhirclient.interceptors;

import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InterceptorRegistryFactory {
  private final List<IClientInterceptor> all;

  public InterceptorRegistryFactory(List<IClientInterceptor> all) {
    this.all = all;
  }

  public List<IClientInterceptor> createAll() {
    return new ArrayList<>(all);
  }
}

package com.spring.hapi.fhirclient.search;

import ca.uhn.fhir.rest.gclient.IQuery;
import com.spring.hapi.fhirclient.bootstrap.VersionAdaptiveClientFactory;
import com.spring.hapi.fhirclient.client.VersionedClient;
import com.spring.hapi.fhirclient.metadata.CapabilitySupport;
import com.spring.hapi.fhirclient.config.SearchProperties;
import com.spring.hapi.fhirclient.util.ResilientExecutor;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Service
public class FhirSearchService {
  private final VersionAdaptiveClientFactory factory;
  private final YamlSearchRepository repo;
  private final QueryBuilder builder;
  private final ResilientExecutor resilient;

  @Value("#{'${search.packs}'.split(',')}")
  private java.util.List<String> locations;

  private volatile Map<String,SearchDef> cache;

  private final SearchProperties searchProps;

  public FhirSearchService(VersionAdaptiveClientFactory factory,
                           YamlSearchRepository repo,
                           QueryBuilder builder,
                           ResilientExecutor resilient,
                           SearchProperties searchProps) {
    this.factory = factory;
    this.repo = repo;
    this.builder = builder;
    this.resilient = resilient;
    this.searchProps = searchProps;
  }

  @PostConstruct
  void init() {
    java.util.List<String> locs = searchProps.getPacks();
    if (locs == null || locs.isEmpty()) {
      locs = java.util.List.of("classpath:searches/core.searches.yml");
    }
    cache = repo.loadAll(locs);
  }
  public Bundle run(String searchName, Map<String,Object> variables) {
    SearchDef def = cache.get(searchName);
    if (def == null) throw new IllegalArgumentException("Unknown search: " + searchName);

    VersionedClient vc = factory.create();
    CapabilitySupport.assertSupported(vc.capability(), def);

    Map<String,Object> vars = (variables == null) ? java.util.Collections.emptyMap() : variables;
    IQuery<Bundle> q = builder.build(vc.client(), vars, def);
    return (Bundle) resilient.run("search-exec", () -> (Bundle) q.execute());
  }

  /**
   * Expose the cached list of search names obtained from the yml file.
   */
  public Set<String> listSearchNames(){
    return Collections.unmodifiableSet(cache.keySet());
  }
}

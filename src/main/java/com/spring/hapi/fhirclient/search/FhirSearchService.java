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
/**
 * Executes named FHIR searches defined in YAML packs.
 *
 * <p>On startup this service loads all configured YAML "search packs" into an in-memory cache
 * (name → {@link SearchDef}). At runtime it:
 * <ol>
 *   <li>Creates a version-adaptive HAPI client via {@link VersionAdaptiveClientFactory}.</li>
 *   <li>Builds a search query from the cached {@link SearchDef} using {@link QueryBuilder}
 *       (templating variables, params, elements, includes, sorting, etc.).</li>
 *   <li>Executes the query with {@link ResilientExecutor} and returns a {@link org.hl7.fhir.r4.model.Bundle}.</li>
 * </ol>
 *
 * <p>Thread-safety: the cache is populated once at init and then treated as read-only.</p>
 */
@Service
public class FhirSearchService {
  private final VersionAdaptiveClientFactory factory;
  private final YamlSearchRepository repo;
  private final QueryBuilder builder;
  private final ResilientExecutor resilient;

  @Value("#{'${fhir-search-ymls.packs}'.split(',')}")
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
    java.util.List<String> locs = searchProps.packs();
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

package com.spring.hapi.fhirclient.search;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class YamlSearchRepository {
  private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

  /**
   * lod the spcificed FHIR search yml into a Map.
   * @param locations
   * @return
   */
  public Map<String,SearchDef> loadAll(List<String> locations) {
    Map<String,SearchDef> all = new HashMap<>();
    Yaml yaml = new Yaml();
    try {
      for (String loc : locations) {
        for (Resource r : resolver.getResources(loc)) {
          try (InputStream in = r.getInputStream()) {
            SearchPack pack = yaml.loadAs(in, SearchPack.class);
            if (pack!=null && pack.getSearches()!=null) {
              all.putAll(pack.getSearches());
            }
          }
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return all;
  }
}

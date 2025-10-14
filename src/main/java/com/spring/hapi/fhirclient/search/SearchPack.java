package com.spring.hapi.fhirclient.search;

import java.util.HashMap;
import java.util.Map;
/**
 * Container for a YAML search pack (one {@code *.searches.yml} file).
 */
public class SearchPack {
  private String pkg;
  private Map<String, SearchDef> searches = new HashMap<>();

  public String getPkg() { return pkg; }
  public void setPkg(String pkg) { this.pkg = pkg; }
  public Map<String, SearchDef> getSearches() { return searches; }
  public void setSearches(Map<String, SearchDef> searches) { this.searches = searches; }
}

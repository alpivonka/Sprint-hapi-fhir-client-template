package com.spring.hapi.fhirclient.search;

import java.util.List;
import java.util.Map;
/**
 * POJO backing a YAML-defined FHIR search (one entry under {@code searches:}).
 *
 * Maps directly to fields in <code>*.searches.yml</code>. Used by {@link QueryBuilder}
 * to construct a HAPI search query and by {@link FhirSearchService} to execute it.
 *
 * <p>Typical YAML:</p>
 * <pre>{@code
 * searches:
 *   patient-cohort-loinc-72166-2:
 *     resource: "Patient"
 *     path: "/Patient"
 *     params:
 *       _has:Observation:subject:code: "http://loinc.org|72166-2"
 *     elements: ["id","address"]
 *     includes: ["Patient:general-practitioner"]
 *     revIncludes: ["Provenance:target"]
 *     sort: ["-id"]
 *     count: 200
 *     summary: count
 *     notes: "Example cohort"
 * }</pre>
 *
 * All fields are optional except {@link #resource & path}.
 */
public class SearchDef {
  private String resource;
  private String path;
  private Map<String,String> params;
  private Integer count;
  private List<String> sort;
  private Object summary;
  private List<String> elements;
  private List<String> includes;
  private List<String> revIncludes;

  public String getResource() { return resource; }
  public void setResource(String resource) { this.resource = resource; }
  public String getPath() { return path; }
  public void setPath(String path) { this.path = path; }
  public Map<String, String> getParams() { return params; }
  public void setParams(Map<String, String> params) { this.params = params; }
  public Integer getCount() { return count; }
  public void setCount(Integer count) { this.count = count; }
  public List<String> getSort() { return sort; }
  public void setSort(List<String> sort) { this.sort = sort; }
  public Object getSummary() { return summary; }
  public void setSummary(Object summary) { this.summary = summary; }
  public List<String> getElements() { return elements; }
  public void setElements(List<String> elements) { this.elements = elements; }
  public List<String> getIncludes() { return includes; }
  public void setIncludes(List<String> includes) { this.includes = includes; }
  public List<String> getRevIncludes() { return revIncludes; }
  public void setRevIncludes(List<String> revIncludes) { this.revIncludes = revIncludes; }
}

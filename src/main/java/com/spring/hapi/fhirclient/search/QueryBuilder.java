package com.spring.hapi.fhirclient.search;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class QueryBuilder {

  public IQuery<Bundle> build(IGenericClient client, Map<String, Object> vars, SearchDef def) {
    IQuery<IBaseBundle> q = client.search().forResource(def.getResource());

    // --- params (with sanitize + token handling) ---
    if (def.getParams() != null) {
      for (Map.Entry<String, String> e : def.getParams().entrySet()) {
        String name = e.getKey();
        String value = applyTemplate(e.getValue(), vars);

        // 1) sanitize: undo accidental property-style escapes (e.g., '\|' or '\,')
        if (value != null) {
          value = value.replace("\\|", "|").replace("\\,", ",");
        }

        // 2) token heuristic: if value looks like system|code (single '|' and no commas),
        //    build it as a token param so HAPI formats it correctly
        if (value != null && value.indexOf('|') > 0 && value.indexOf(',') < 0) {
          String[] parts = value.split("\\|", 2);
          if (parts.length == 2) {
            String system = parts[0].trim();
            String code   = parts[1].trim();
            q = q.where(new TokenClientParam(name).exactly().systemAndCode(system, code));
            continue;
          }
        }

        // 3) fallback: plain string param
        q = q.where(new StringClientParam(name).matches().value(value));
      }
    }

    // --- _include / _revinclude ---
    if (def.getIncludes() != null) {
      for (String inc : def.getIncludes()) {
        q = q.include(new Include(inc));
      }
    }
    if (def.getRevIncludes() != null) {
      for (String inc : def.getRevIncludes()) {
        q = q.revInclude(new Include(inc));
      }
    }

    // --- _elements ---
    if (def.getElements() != null && !def.getElements().isEmpty()) {
      q = q.elementsSubset(def.getElements().toArray(new String[0]));
    }

    // --- _summary ---
    if (def.getSummary() != null) {
      Object s = def.getSummary();
      if (s instanceof Boolean b && b)        q = q.summaryMode(SummaryEnum.TRUE);
      else if ("count".equals(s))             q = q.summaryMode(SummaryEnum.COUNT);
      else if ("text".equals(s))              q = q.summaryMode(SummaryEnum.TEXT);
      else if ("data".equals(s))              q = q.summaryMode(SummaryEnum.DATA);
    }

    // --- _sort ---
    if (def.getSort() != null) {
      for (String s : def.getSort()) {
        boolean desc = s.startsWith("-");
        String field = desc ? s.substring(1) : s;
        q = desc ? q.sort().descending(field) : q.sort().ascending(field);
      }
    }

    // --- _count ---
    if (def.getCount() != null) {
      q = q.count(def.getCount());
    }

    return q.returnBundle(Bundle.class);
  }

  // very simple ${var} replacement; null-safe
  private String applyTemplate(String template, Map<String, Object> vars) {
    if (template == null) return null;
    if (vars == null || vars.isEmpty()) return template;
    String out = template;
    for (var e : vars.entrySet()) {
      out = out.replace("${" + e.getKey() + "}", String.valueOf(e.getValue()));
    }
    return out;
  }
}

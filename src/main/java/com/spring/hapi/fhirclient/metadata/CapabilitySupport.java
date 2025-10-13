package com.spring.hapi.fhirclient.metadata;

import com.spring.hapi.fhirclient.search.SearchDef;
import org.hl7.fhir.r4.model.CapabilityStatement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CapabilitySupport {
  public static void assertSupported(CapabilityStatement cs, SearchDef def) {
    if (def.getParams()==null || def.getParams().isEmpty()) return;
    List<CapabilityStatement.CapabilityStatementRestComponent> rests = cs.getRest();
    Set<String> supported = new HashSet<>();
    rests.stream()
      .flatMap(r -> r.getResource().stream())
      .filter(rr -> def.getResource().equals(rr.getType()))
      .flatMap(rr -> rr.getSearchParam().stream())
      .forEach(sp -> supported.add(sp.getName()));

    for (String p : def.getParams().keySet()) {
      if (!supported.contains(p) && !p.startsWith("_")) {
        throw new IllegalArgumentException("Search param not supported by server for "
          + def.getResource() + ": " + p);
      }
    }
  }
}

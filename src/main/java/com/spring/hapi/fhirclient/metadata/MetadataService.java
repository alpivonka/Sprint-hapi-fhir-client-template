package com.spring.hapi.fhirclient.metadata;

import org.hl7.fhir.r4.model.CapabilityStatement;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MetadataService {
  public String getFhirVersion(CapabilityStatement cs) {
    return cs.getFhirVersionElement()!=null ? cs.getFhirVersionElement().getValueAsString() : "unknown";
  }

  public List<String> supportedSearchParams(CapabilityStatement cs, String resourceType) {
    return cs.getRest().stream()
      .flatMap(r -> r.getResource().stream())
      .filter(rr -> resourceType.equals(rr.getType()))
      .flatMap(rr -> rr.getSearchParam().stream())
      .map(sp -> sp.getName())
      .distinct()
      .collect(Collectors.toList());
  }
}

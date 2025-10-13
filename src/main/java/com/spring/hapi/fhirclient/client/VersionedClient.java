package com.spring.hapi.fhirclient.client;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.CapabilityStatement;

public record VersionedClient(
  FhirContext context,
  IGenericClient client,
  CapabilityStatement capability,
  FhirRelease release
) {}

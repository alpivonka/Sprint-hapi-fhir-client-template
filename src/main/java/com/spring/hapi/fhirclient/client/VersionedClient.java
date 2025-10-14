package com.spring.hapi.fhirclient.client;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.CapabilityStatement;
/**
 * Holder Class for a fully-initialized, version-aware HAPI FHIR client.
 *
 * Produced by the factory after probing {@code /metadata} to determine the
 * server's FHIR release (R4/R4B/R5). Use {@link #client()} to execute requests,
 * {@link #context()} for configuration, and {@link #capability()} for capability checks.
 */
public record VersionedClient(
  FhirContext context,
  IGenericClient client,
  CapabilityStatement capability,
  FhirRelease release
) {}

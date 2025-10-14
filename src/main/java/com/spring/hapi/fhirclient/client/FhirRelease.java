package com.spring.hapi.fhirclient.client;
/**
 * Supported FHIR releases used to select the appropriate HAPI {@code FhirContext}.
 *
 * <p>Values map to:
 * <ul>
 *   <li>{@link #R4}  → FHIR R4 (e.g., 4.0.x)</li>
 *   <li>{@link #R4B} → FHIR R4B (4.3.x)</li>
 *   <li>{@link #R5}  → FHIR R5 (5.x)</li>
 * </ul>
 *
 * @see <a href="https://hl7.org/fhir/versions.html">FHIR Versions</a>
 */
public enum FhirRelease { R4, R4B, R5 }

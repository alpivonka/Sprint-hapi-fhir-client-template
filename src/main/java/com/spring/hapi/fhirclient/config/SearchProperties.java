package com.spring.hapi.fhirclient.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
/**
 * Search YAML pack locations bound from properties with prefix {@code fhir-search-ymls}.
 *
 * <p>Each entry is a Spring resource location (e.g., {@code classpath:searches/core.searches.yml}
 * or a filesystem path). The application loads all packs at startup.</p>
 *
 * Example (application.yml):
 * fhir-search-ymls:
 *   packs: classpath:searches/core.searches.yml, classpath:searches/custom.searches.yml #if multiple, use comma delimited list
 */
@Validated
@ConfigurationProperties(prefix = "fhir-search-ymls")
public record SearchProperties(@NotNull List<String> packs) {}

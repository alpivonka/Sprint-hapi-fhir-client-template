package com.spring.hapi.fhirclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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
@Configuration
@ConfigurationProperties(prefix = "fhir-search-ymls")
public class SearchProperties {
    private List<String> packs = new ArrayList<>();

    public List<String> getPacks() { return packs; }
    public void setPacks(List<String> packs) { this.packs = packs; }
}

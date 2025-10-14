package com.spring.hapi.fhirclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "fhir-search-ymls")
public class SearchProperties {
    private List<String> packs = new ArrayList<>();

    public List<String> getPacks() { return packs; }
    public void setPacks(List<String> packs) { this.packs = packs; }
}

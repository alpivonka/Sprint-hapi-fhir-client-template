package com.spring.hapi.fhirclient;

import com.spring.hapi.fhirclient.search.FhirSearchService;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;  // ← add
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class DemoRunner {

    @Bean
    @ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")   // ← add
    CommandLineRunner runDemo(FhirSearchService svc) {
        return args -> {
            try {
                Map<String,Object> vars = Map.of(
                        "patientId","123",
                        "codes","http://loinc.org|8480-6,http://loinc.org|8462-4",
                        "since","2024-01-01"
                );
                Bundle b = (Bundle) svc.run("observations-by-codes-since", vars);
                System.out.println("Bundle total: " + (b.getTotalElement()!=null? b.getTotalElement().getValue() : "N/A"));

                Bundle c = (Bundle) svc.run("patient-cohort-loinc-72166-2", null);
                System.out.println("Bundle total: " + (c.getEntry()!=null? c.getEntry().size() : "N/A"));
                System.out.println("Stopping here!");

            } catch (Exception e) {
                System.out.println("Demo skipped (no server stubbed): " + e.getMessage());
            }
        };
    }
}

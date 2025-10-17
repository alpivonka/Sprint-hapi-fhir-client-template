package com.spring.hapi.fhirclient.util;

import com.spring.hapi.fhirclient.bootstrap.VersionAdaptiveClientFactory;
import com.spring.hapi.fhirclient.client.FhirRelease;
import com.spring.hapi.fhirclient.client.VersionedClient;
import com.spring.hapi.fhirclient.config.SearchProperties;
import com.spring.hapi.fhirclient.search.SearchDef;
import com.spring.hapi.fhirclient.search.YamlSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class FhirSearchYamlVerifier {
    private static final Logger LOG = LoggerFactory.getLogger(FhirSearchYamlVerifier.class);

    private final VersionAdaptiveClientFactory factory;
    private final YamlSearchRepository yamlRepo;
    private final SearchProperties searchProps;

    public FhirSearchYamlVerifier(
            VersionAdaptiveClientFactory factory,
            YamlSearchRepository yamlRepo,
            SearchProperties searchProps
    ) {
        this.factory = factory;
        this.yamlRepo = yamlRepo;
        this.searchProps = searchProps;
    }

    public void verifyAll() {
        LOG.info("Starting FHIR Search YAML verification (no FHIRPath)...");
        VersionedClient vc = factory.create();
        var release = vc.release();

        // Load searches
        List<String> locations = (searchProps == null || searchProps.packs() == null || searchProps.packs().isEmpty())
                ? List.of("classpath:searches/core.searches.yml")
                : searchProps.packs();
        Map<String, SearchDef> all = yamlRepo.loadAll(locations);
        if (all == null || all.isEmpty()) {
            LOG.warn("No YAML search definitions found at: {}", locations);
            return;
        }

        switch (release) {
            case R4, R4B -> verifyAgainstR4(vc, all);
            case R5 -> verifyAgainstR5(vc, all);
            default -> verifyAgainstR4(vc, all); // safe default
        }
        LOG.info("FHIR Search YAML verification complete.");
    }

    private void verifyAgainstR4(VersionedClient vc, Map<String, SearchDef> all) {
        org.hl7.fhir.r4.model.CapabilityStatement cs = vc.capability();
        Map<String, ResourceSupports> supports = buildSupportsR4(cs);

        all.forEach((name, def) -> {
            String resource = def.getResource();
            LOG.info("────────────────────────────────────────────────");
            LOG.info("Search: {}  (resource: {})", name, resource);

            ResourceSupports rs = supports.getOrDefault(resource, ResourceSupports.EMPTY);

            // Params (skip system _*)
            Map<String, String> params = def.getParams() == null ? Map.of() : def.getParams();
            if (params.isEmpty() || params.keySet().stream().allMatch(p -> p.startsWith("_"))) {
                LOG.info("Params: (none or only system params)");
            } else {
                boolean allOk = true;
                for (String p : params.keySet()) {
                    if (p.startsWith("_")) continue;
                    boolean ok = rs.params.contains(p);
                    allOk &= ok;
                    LOG.info("  Param   {} {}", String.format("%-40s", p), ok ? "✅" : "❌");
                }
                if (allOk) LOG.info("Params: ✅ supported");
            }

            // Includes
            List<String> includes = def.getIncludes() == null ? List.of() : def.getIncludes();
            if (includes.isEmpty()) {
                LOG.info("Includes: (none)");
            } else {
                for (String inc : includes) {
                    boolean ok = rs.includes.contains(inc);
                    LOG.info("  Param   {} {}", String.format("%-40s", inc), ok ? "✅" : "❌");
                }
            }

            // RevIncludes
            List<String> revIncludes = def.getRevIncludes() == null ? List.of() : def.getRevIncludes();
            if (revIncludes.isEmpty()) {
                LOG.info("RevIncludes: (none)");
            } else {
                for (String rev : revIncludes) {
                    boolean ok = rs.revIncludes.contains(rev);
                    LOG.info("  RevInclude {} {}", String.format("%-37s", rev), ok ? "✅" : "❌");
                }
            }

            if (def.getElements() != null && !def.getElements().isEmpty()) {
                LOG.info("Elements: {} (treated as `_elements` system param; not capability-validated)", def.getElements().size());
            } else {
                LOG.info("Elements: (none)");
            }
        });
    }

    private Map<String, ResourceSupports> buildSupportsR4(org.hl7.fhir.r4.model.CapabilityStatement cs) {
        Map<String, ResourceSupports> out = new HashMap<>();
        if (cs.getRest() == null) return out;

        for (var rest : cs.getRest()) {
            if (rest.getResource() == null) continue;
            for (var rr : rest.getResource()) {
                String type = rr.getType();
                Set<String> params = rr.getSearchParam() == null ? Set.of() :
                        rr.getSearchParam().stream().map(sp -> sp.getName()).filter(Objects::nonNull).collect(Collectors.toSet());
                Set<String> includes = rr.getSearchInclude() == null ? Set.of() :
                        rr.getSearchInclude().stream().map(st -> st.getValue()).filter(Objects::nonNull).collect(Collectors.toSet());
                Set<String> revIncludes = rr.getSearchRevInclude() == null ? Set.of() :
                        rr.getSearchRevInclude().stream().map(st -> st.getValue()).filter(Objects::nonNull).collect(Collectors.toSet());
                out.put(type, new ResourceSupports(params, includes, revIncludes));
            }
        }
        return out;
    }

    private void verifyAgainstR5(VersionedClient vc, Map<String, SearchDef> all) {
        org.hl7.fhir.r5.model.CapabilityStatement cs = (org.hl7.fhir.r5.model.CapabilityStatement) (Object) vc.capability();
        Map<String, ResourceSupports> supports = buildSupportsR5(cs);

        all.forEach((name, def) -> {
            String resource = def.getResource();
            LOG.info("────────────────────────────────────────────────");
            LOG.info("Search: {}  (resource: {})", name, resource);

            ResourceSupports rs = supports.getOrDefault(resource, ResourceSupports.EMPTY);

            Map<String, String> params = def.getParams() == null ? Map.of() : def.getParams();
            if (params.isEmpty() || params.keySet().stream().allMatch(p -> p.startsWith("_"))) {
                LOG.info("Params: (none or only system params)");
            } else {
                boolean allOk = true;
                for (String p : params.keySet()) {
                    if (p.startsWith("_")) continue;
                    boolean ok = rs.params.contains(p);
                    allOk &= ok;
                    LOG.info("  Param   {:<40} {}", p, ok ? "✅" : "❌");
                }
                if (allOk) LOG.info("Params: ✅ supported");
            }

            List<String> includes = def.getIncludes() == null ? List.of() : def.getIncludes();
            if (includes.isEmpty()) LOG.info("Includes: (none)");
            else for (String inc : includes) LOG.info("  Include {:<40} {}", inc, rs.includes.contains(inc) ? "✅" : "❌");

            List<String> revIncludes = def.getRevIncludes() == null ? List.of() : def.getRevIncludes();
            if (revIncludes.isEmpty()) LOG.info("RevIncludes: (none)");
            else for (String rev : revIncludes) LOG.info("  RevInclude {:<37} {}", rev, rs.revIncludes.contains(rev) ? "✅" : "❌");

            if (def.getElements() != null && !def.getElements().isEmpty())
                LOG.info("Elements: {} (treated as `_elements` system param; not capability-validated)", def.getElements().size());
            else LOG.info("Elements: (none)");
        });
    }

    private Map<String, ResourceSupports> buildSupportsR5(org.hl7.fhir.r5.model.CapabilityStatement cs) {
        Map<String, ResourceSupports> out = new HashMap<>();
        if (cs.getRest() == null) return out;

        for (var rest : cs.getRest()) {
            if (rest.getResource() == null) continue;
            for (var rr : rest.getResource()) {
                String type = rr.getType();
                Set<String> params = rr.getSearchParam() == null ? Set.of() :
                        rr.getSearchParam().stream().map(sp -> sp.getName()).filter(Objects::nonNull).collect(Collectors.toSet());
                Set<String> includes = rr.getSearchInclude() == null ? Set.of() :
                        rr.getSearchInclude().stream().map(st -> st.getValue()).filter(Objects::nonNull).collect(Collectors.toSet());
                Set<String> revIncludes = rr.getSearchRevInclude() == null ? Set.of() :
                        rr.getSearchRevInclude().stream().map(st -> st.getValue()).filter(Objects::nonNull).collect(Collectors.toSet());
                out.put(type, new ResourceSupports(params, includes, revIncludes));
            }
        }
        return out;
    }

    /** Simple holder for supports. */
    private record ResourceSupports(Set<String> params, Set<String> includes, Set<String> revIncludes) {
        static final ResourceSupports EMPTY = new ResourceSupports(Set.of(), Set.of(), Set.of());
    }
}

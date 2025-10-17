# Spring + HAPI FHIR Client Template

Reusable Spring Boot + HAPI FHIR client template:
- Version-adaptive (probes `/metadata`, chooses R4/R4B/R5)
- YAML-defined searches (named + templated)
- OAuth2/OpenID (client credentials) compatible
- Interceptor-first (configurable Logging, RequestId, Timing)
- Mock data + WireMock integration testing

## Quick Start

```bash
mvn -q -DskipTests spring-boot:run
```

Configure `src/main/resources/application.yml`:
- `fhir.base-url`
- `security.oauth2.*` if you need bearer tokens

FHIR Search YML `src/main/resources/core.searches.yml`:
- `fhir.base-url`
- `security.oauth2.*` if you need bearer tokens

Run an example search in `DemoRunner` or wire your own service.
```
mvn --% spring-boot:run -Dspring-boot.run.arguments="--app.verify.enabled=false --app.demo.enabled=true"
```
Run a test of verfiy which runs verification of fhir search yml against fhir server /metadata capabilitystatement
```
mvn --% spring-boot:run -Dspring-boot.run.arguments="--app.verify.enabled=true --app.demo.enabled=false"
```

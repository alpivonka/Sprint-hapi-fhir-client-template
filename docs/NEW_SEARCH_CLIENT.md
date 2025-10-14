# NEW_SEARCH_CLIENT.md

**Goal:** take this repo and spin up a **new FHIR *search* client** (not a server) with your own YAML-defined searches, then run it as a CLI or Spring app.

---

## 1) Copy the template

**Option A — Git clone & rename**
```bash
git clone <this-repo-url> my-fhir-search-client
cd my-fhir-search-client
```

**Option B — ZIP**
- Download the repo ZIP
- Unzip to a new folder, e.g. `my-fhir-search-client/`

---

## 2) Rename coordinates & package

### 2.1 Update Maven coordinates
Edit `pom.xml`:
```xml
<groupId>com.myorg.fhir</groupId>
<artifactId>my-fhir-search-client</artifactId>
<name>my-fhir-search-client</name>
<version>0.1.0</version>
```

### 2.2 (Optional) Change Java base package
If you want a custom package (recommended):
- Move sources from `com.example.fhirclient` → `com.myorg.fhir.client`
- Update the `package` lines inside `.java` files and the import paths accordingly.

---

## 3) Point to your FHIR server & auth

Edit `src/main/resources/application.yml`:

```yaml
fhir:
  # Base URL of your target FHIR server (R4/R4B/R5). Examples:
  # https://my-epic.example.com/fhir/r4
  # https://my-hapi.example.com/baseR4
  base-url: https://hapi.fhir.org/baseR4

security:
  oauth2:
    enabled: false        # true if your server requires OAuth
    token-uri: https://idp.example.com/oauth2/token
    client-id: <client-id>
    client-secret: ${OAUTH_CLIENT_SECRET}  # prefer env var
    scope: "system/*.read"
```

> The client auto-reads `/metadata` and adapts to the server’s FHIR version.

---

## 4) Create your **search pack** (YAML)

Put your searches in a new file, e.g. `src/main/resources/searches/my.searches.yml`, and register it:

```yaml
search:
  packs:
    - classpath:searches/my.searches.yml
```

**Schema:**
```yaml
# optional human label
pkg: "my-client"

searches:
  <unique-search-name>:
    resource: "ResourceName"            # e.g., Patient, Observation
    # path: "/ResourceName"             # optional override
    params:                             # map<string,string>
      <param-name>: "<value>"           # supports ${var} templating
      # examples:
      # identifier: "${system}|${value}"           (token)
      # subject: "Patient/${patientId}"            (reference)
      # date: "ge${since}"                         (prefix)
    elements: ["id","name"]             # -> _elements
    includes: ["Patient:general-practitioner"]   # -> _include
    revIncludes: ["Provenance:target"]           # -> _revinclude
    sort: ["-date","family"]            # '-' for desc
    count: 200                          # -> _count
    summary: count                      # true | count | data | text
    notes: "why this search exists"
```

**Templating:** `${var}` placeholders are replaced at runtime:
```java
Bundle b = fhirSearchService.run("patients-by-identifier",
    Map.of("system","http://hospital.example.org/mrn", "value","12345"));
```

**Token values** like `system|code` are auto-sent as proper FHIR **token** params (e.g., `identifier`, `_has:…:code`).

> Keep `_elements`, `_count`, `_summary`, `_include`, `_revinclude` **out of** `params:`. Use the dedicated top-level keys shown above.

---

## 5) Choose how it runs

### 5.1 CLI app (run and exit)
In `application.yml`:
```yaml
spring:
  main:
    web-application-type: none
```

Use/adjust the existing `DemoRunner` (or `ApplicationRunner`) to call your search:
```java
@Bean
ApplicationRunner cli(FhirSearchService svc) {
  return args -> {
    Bundle b = svc.run("your-search-name", Map.of()); // or vars
    System.out.println("Total: " + (b.hasTotal() ? b.getTotal() : "N/A"));
  };
}
```

Run:
```bash
mvn -DskipTests spring-boot:run
```

### 5.2 Long-running Spring app
Leave web mode enabled (default). Call the service from controllers or scheduled jobs if you add them later.

---

## 6) Logging (see the *actual* URL)

Enable the raw URL interceptor to log wire URLs in a file:

```yaml
interceptors:
  raw-url:
    enabled: true

logging:
  file:
    name: logs/app.log
  level:
    com.example.fhirclient.interceptors.RawUrlLoggerIntc: INFO
```

You’ll get:
```
[FHIR-URL] raw=... ascii=... decoded=...
```
- **ascii** shows the real on-the-wire (percent-encoded) URL
- **decoded** shows a human-friendly version

---

## 7) Smoke test locally with WireMock (optional but recommended)

Use the included WireMock-based integration test pattern to validate:
- `/metadata` versioning
- query params (e.g., `_has:Observation:subject:code`)
- paging

Example snippet (in `src/test/java/...`):
```java
stubFor(get(urlEqualTo("/fhir/metadata"))
  .willReturn(okJson("{"resourceType":"CapabilityStatement","fhirVersion":"4.0.1"}")));

stubFor(get(urlPathEqualTo("/fhir/Patient"))
  .withQueryParam("_has:Observation:subject:code", equalTo("http://loinc.org|72166-2"))
  .withQueryParam("_count", equalTo("200"))
  .willReturn(okJson("{"resourceType":"Bundle","type":"searchset","total":2}")));
```

Test profile config: `src/test/resources/application-test.yml`
```yaml
spring:
  main:
    web-application-type: none
fhir:
  base-url: http://localhost:8089/fhir
search:
  packs:
    - classpath:searches/my.searches.yml
```

Run:
```bash
mvn test
```

---

## 8) Build & run the jar

```bash
mvn -DskipTests package
java -jar target/my-fhir-search-client-0.1.0.jar
# or with a custom log file
java -jar target/my-fhir-search-client-0.1.0.jar --logging.file.name=C:/temp/fhir-client.log
```

---

## 9) Common tweaks you’ll likely make

- **Coordinates & package**: `pom.xml`, `package com.myorg...`  
- **FHIR base URL**: `fhir.base-url`  
- **OAuth**: set `security.oauth2.*` and export `OAUTH_CLIENT_SECRET`  
- **Search pack path**: `search.packs` update to your YAML  
- **New searches**: author them in `searches/my.searches.yml`  
- **CLI vs service**: `spring.main.web-application-type`  
- **Logs to file**: `logging.file.name` or a `logback-spring.xml`  
- **Tests**: add WireMock stubs for `/metadata` and your endpoints

---

## 10) Pitfalls & tips

- **Params map is string-only**: lists under `params:` will fail YAML binding. Use top-level `elements:` (list) and `count:` (int).
- **Backslashes in values**: if you ever pasted `\|` or `\,` from a properties file, the builder **sanitizes** them. Prefer clean YAML (`|` and `,` do not need escaping).
- **Token params**: write as `system|code`; the builder sends them as proper token searches.
- **Large result sets**: set `count`, then iterate with `SearchPager` to follow `next` links.
- **Capability mismatches**: if a server doesn’t support a param, add/update the search or disable the capability check for that call.

---

## 11) Minimal example to copy

**`src/main/resources/searches/my.searches.yml`**
```yaml
searches:
  patients-by-identifier:
    resource: "Patient"
    params:
      identifier: "${system}|${value}"
    elements: ["id","name","identifier"]
    count: 50
```

**Run it**
```java
Bundle b = fhirSearchService.run("patients-by-identifier",
  Map.of("system","http://hospital.example.org/mrn","value","12345"));
```

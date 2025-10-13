com.spring.hapi.fhirclient.SearchDef leverages and consumes a standard Yaml files that defines a FHIR search.

### How the engine builds queries (what’s supported)

* Resource & path → client.search().forResource(resource) and base /path.
* Params → each key=value pair becomes a HAPI search criterion.
  * Token-looking values use TokenClientParam (e.g., identifier, :code, _has:...:code).
  * Others use StringClientParam with .matches().value(value).
* elements → .elementsSubset("id","name",...)
*  includes/revIncludes → .include(new Include(expr)) / .revInclude(new Include(expr))
* sort → .sort().ascending(field) or .descending(field) (strip leading -).
* count → .count(n)
* summary → .summaryMode(TRUE|COUNT|TEXT|DATA)

Put YAML packs under src/main/resources/searches/.

Configure which packs to load (list) in application.yml:
```
search:
  packs:
    - classpath:searches/core.searches.yml

```
On startup, the app reads these files and builds a cache of search name → definition.

To run them by name:
```
Bundle b = fhirSearchService.run("patients-by-identifier", 
     Map.of("system", "http://mrn", "value", "123"));
```

### FILE STRUCTURE
```
# Optional: human label (not used by code)
pkg: "core"

# Required: all search definitions go under 'searches'
searches:
  search-name-1:
    resource: "Patient"                          # FHIR resource type (R4/R4B/R5)
    path: "/Patient"                             # optional; defaults to "/<resource>"
    params:                                      # optional map<String,String>
      family: "${family}"                        # supports ${var} templating
      _has:Observation:subject:code: "http://loinc.org|72166-2"
    elements: ["id","name","address"] # optional; renders _elements=id,name,address
    includes: ["Patient:general-practitioner"]   # optional; renders _include
    revIncludes: ["Provenance:target"]           # optional; renders _revinclude
    sort: ["-date","family"]                     # optional; -desc, +asc (default)
    count: 200                                   # optional; renders _count=200
    summary: true|count|data|text                # optional; renders _summary=
    notes: "free text for humans"                # optional
```

### Field reference (what each key does)

* **resource (required):** FHIR resource name (Patient, Observation, etc.).

* **path (optional):** override the collection path (rarely needed). If omitted, the client uses /<resource>.

* **params (optional):** map of search parameters (keys/values as strings).

  * **Values support templating:** ${var} is substituted from the variables map you pass at runtime.

  * **Token values** like system|code are automatically treated as FHIR tokens (e.g., _has:Observation:subject:code).

* **elements (optional):** list of element names -> _elements query param. (Don’t put _elements inside params.)

* **includes, revIncludes (optional):** arrays of include/revinclude expressions.

* **sort (optional):** array of sort fields. Prefix with - for descending.

* **count (optional):** page size (_count).

* **summary (optional):** true, "count", "data", or "text" -> _summary.

* **notes (optional):** free text for docs.

### Templating variables

Any ${var} inside values is replaced from the variables you pass to run(name, variables).
```
searches:
    patients-by-identifier:
        resource: "Patient"
        params:
            identifier: "${system}|${value}"
```
usage:
```
Bundle b = svc.run("patients-by-identifier",
  Map.of(
        "system","http://hospital.example.org/mrn",
        "value","12345"
        ));
```

### Token parameters

Parameters whose value looks like system|code (one |, no commas) are sent as token params via HAPI’s TokenClientParam (correct semantics for things like identifier, code, _has:...:code, etc.).

```
params:
  _has:Observation:subject:code: "http://loinc.org|72166-2"

```
#### Defensive de-escaping

If someone accidentally pastes property-escaped text (e.g., http://loinc.org\|72166-2), 
the builder sanitizes values by converting \| → | and \, → , before building the URL.

Don’t put these inside params

Use the top-level keys instead of stuffing these under params:

* _elements → use elements: ["id","address"]

* _count → use count: 200

* _summary → use summary: count|true|...

* _include/_revinclude → use includes: / revIncludes:

### Examples:
#### Patient cohort by _has LOINC code
```
searches:
    patient-cohort-loinc-72166-2:
        resource: "Patient"
        path: "/Patient"
        params:
            _has:Observation:subject:code: "http://loinc.org|72166-2"
        elements: ["id","address"]
        count: 200
```

#### Includes & reverse includes

```
searches:
  patients-with-gp-and-provenance:
    resource: "Patient"
    params:
      active: "true"
    includes: ["Patient:general-practitioner"]
    revIncludes: ["Provenance:target"]
    count: 100
```

#### Summary-only (count)
```
searches:
  patient-count-active:
    resource: "Patient"
    params:
      active: "true"
    summary: "count"

```
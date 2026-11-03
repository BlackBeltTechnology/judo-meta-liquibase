# judo-meta-liquibase — module agent doctrine

## Module purpose

`judo-meta-liquibase` is the estate's typed representation of *a database
migration script before it is a file*. It owns the `liquibase` Ecore metamodel
(`nsURI http://www.liquibase.org/xml/ns/dbchangelog`, ~208 classifiers derived
mechanically from the Liquibase XSD: root `databaseChangeLog`, `ChangeSet`, and
one classifier per Liquibase change — `CreateTable`, `Column`, `AddPrimaryKey`,
`AddForeignKeyConstraint`, `AddNotNullConstraint`, `AddAutoIncrement`, …), so a
changelog is an EMF object graph that a transformation can construct, diff and
query instead of a string of XML a template must get syntactically right.

Who produces an instance: `judo-tatami-rdbms2liquibase`, the ETL/EGL step of
the JUDO tatami pipeline (PSM → ASM → RDBMS → **Liquibase**). It compares the
freshly derived RDBMS schema model against the previously deployed one and
emits the difference as a `databaseChangeLog` graph — tables, columns, keys and
constraints expressed as `ChangeSet` children.

What is emitted from it: `LiquibaseModelStreamProvider.getStreamFromLiquibaseModel`
serialises the graph back through the EMF XML resource (genmodel `resource="XML"`,
`fileExtensions="changelog.xml"`) into a real Liquibase changelog XML stream.
`LiquibaseNamespaceFixUriHandler` is a `URIHandlerImpl` that pipes the document
through an XSLT on load/save because EMF's XML resource and Liquibase disagree
on namespace declarations; without it round-tripped changelogs are rejected by
Liquibase itself.

How it reaches an actual database: the emitted changelog stream is handed to
`runtime/osgi-liquibase`, which feeds changelogs to the Liquibase engine from an
in-memory `Map<String, InputStream>`, and to `judo-rdbms-schema`
(`IncrementalLiquibaseExecutor`, `LiquibaseVersionUpdate`) which runs it against
the live JDBC connection and advances the recorded schema version. Nothing in
this repository touches a database — this module ends at the byte stream; the
runtime modules above own the JDBC side.

Reading and asserting on such a graph is `LiquibaseUtils` (constructed over a
`ResourceSet`, `failOnError` toggle, `Optional`-returning navigation:
`getDatabaseChangeLog`, `getChangeSets`, `getChangeSet(id)`,
`getCreateTable(changeSetId, tableName)`, `getColumn(...)`,
`getAddForeignKeyConstraint(...)`). Validation exists in two shapes:
`LiquibaseEpsilonValidator` over `model/src/main/epsilon/validations/liquibase.evl`
— currently an empty rule file, a deliberate placeholder — and the Zeta Java
validation path (`LiquibaseValidator`, `LiquibaseModelProvider`,
`LiquibaseValidationConstants`, `LiquibaseValidationException`, and
`@ValidationContext(ChangeSet.class) ChangeSetValidations`, whose `@Constraint`
rules are presently commented out). Treat "validation passes" as "no rules are
armed yet", not as "the changelog is proven correct".

**Repository:** BlackBeltTechnology/judo-meta-liquibase ·
**Artifact:** `hu.blackbelt.judo.meta:hu.blackbelt.judo.meta.liquibase` ·
**License:** EPL-2.0 · **Java:** 21 ·
**Build:** Maven 3.9.4+ / Eclipse Tycho 4.0.13 ·
**Version:** `1.0.2-SNAPSHOT` (Maven) / `1.0.2.qualifier` (Eclipse).

## Reactor map

The root `pom.xml` declares its `<modules>` inside the `modules` profile, active
unless `-DskipModules=true` is passed. Build order is the declared order —
`model` produces the artifact every other module consumes.

<modules>
  <module>model</module>
  <module>model-test</module>
  <module>osgi</module>
  <module>osgi-itest</module>
  <module>feature</module>
  <module>site</module>
</modules>

| Module | Artifact / packaging | What it contributes |
|---|---|---|
| `model` | `…liquibase.model`, `eclipse-plugin` | The metamodel and everything derived from it: `model/model/liquibase.ecore` (single source of truth, derived from `liquibase.xsd`), `liquibase.genmodel` (EMF generation parameters — `modelName="Liquibase"`, XML resource, `.changelog.xml` extension), the MWE2 workflow `src/workflow/generateModel.mwe2` running four generators (Ecore model classes, navigation helpers, builders, the `LiquibaseModel` runtime wrapper) into `src-gen/`, the EVL rule file, and the hand-written runtime under `src/main/java` (`LiquibaseUtils`, `LiquibaseModelStreamProvider`, `LiquibaseNamespaceFixUriHandler`, `LiquibaseEpsilonValidator`, the `validation` package, and the Eclipse `Activator`). `META-INF/MANIFEST.MF` carries the Eclipse plugin identity — `Export-Package` and `Require-Bundle` — and must move in lockstep with the POM version. |
| `model-test` | `…liquibase.model.test`, `jar` | JUnit 5 proof that the runtime behaves: builder construction, `LiquibaseUtils` navigation, serialisation round-trip, and validation wiring. Depends on `model`, so it exercises the published API surface rather than internals. Test logging comes from the repo-root `logback-test.xml` (INFO). |
| `osgi` | `…liquibase.osgi`, `bundle` (Felix) | Repackages `model` for non-Eclipse OSGi containers. `maven-bundle-plugin` `<instructions>` embed `../model/model` as `meta/liquibase` and `../model/src/main/epsilon/validations` as `validations`, export `hu.blackbelt.judo.meta.liquibase*`, and mark every Epsilon / Zeta import `resolution:=optional` so the bundle resolves without a validation stack present. Ships `LiquibaseModelBundleTracker`, the only Lombok-using code in the repo. |
| `osgi-itest` | `…liquibase.osgi.itest`, `jar` | Pax Exam 4.13.5 integration tests booting an Apache Karaf 4.4.7 container (with `tinybundles`) to assert the bundle really resolves there: model loading, OSGi service registration, manifest-driven discovery — the check unit tests structurally cannot make. |
| `feature` | `…liquibase.feature`, `eclipse-feature` | The installable Eclipse feature grouping the model plugin, so the metamodel is consumable from an IDE workspace. Pure descriptor module — no sources. |
| `site` | `…liquibase.site`, `eclipse-repository` | Builds the P2 update site for "Install New Software" and for downstream Tycho builds that consume it as a P2 repository. |

## Build commands

Use the bundled Maven wrapper (`./mvnw`), never a repo-root `mvn`.

```bash
# Full build (all modules: code generation + compile + test)
./mvnw clean install

# Run all tests
./mvnw clean test

# Build a single module
./mvnw -f model/pom.xml clean install
./mvnw -f model-test/pom.xml clean test
./mvnw -f osgi-itest/pom.xml clean test

# Skip tests
./mvnw clean install -DskipTests

# Skip test compilation entirely
./mvnw clean install -Dmaven.test.skip=true

# Regenerate model code (after editing .ecore or .genmodel)
./mvnw -f model/pom.xml clean install

# Set project version across all modules (Maven first, then Eclipse metadata)
./mvnw -DnewVersion=X.Y.Z -DgenerateBackupPoms=false versions:set
./mvnw tycho-versions:update-eclipse-metadata
```

### Maven profiles

| Profile | Purpose |
|---------|---------|
| `modules` | Build all six submodules. Active by default; skip with `-DskipModules=true`. |
| `sign-artifacts` | GPG-sign artifacts for release builds. |
| `release-judong` | Deploy to the internal JUDO Nexus repository. |
| `release-central` | Deploy to Maven Central via Sonatype OSSRH. |
| `release-dummy` | Local file-based deployment for testing. |
| `generate-github-asciidoc-diagrams` | Generate documentation with PlantUML diagrams. |
| `update-source-code-license` | Update EPL-2.0 license headers across all source files. |

## Technology stack

- **Eclipse EMF** 2.38.0 (`org.eclipse.emf.ecore`), `org.eclipse.emf.common` 2.41.0 — Ecore metamodel definition and code generation
- **Eclipse Tycho** 4.0.13 — Eclipse plugin build; resolves dependencies from `MANIFEST.MF`
- **Epsilon** 2.8.0 — EVL validation (`hu.blackbelt.epsilon:epsilon-runtime-execution`, `epsilon-runtime-osgi`)
- **MWE2** — orchestrates the code generation pipeline (`hu.blackbelt.judo:judo-genmodel-generator` supplies the JUDO-specific generators)
- **OSGi** 7.0.0 — module system for both Eclipse and standalone deployment; `hu.blackbelt.osgi.utils:osgi-api` for bundle tracking
- **JUnit Jupiter** 5.9.1, **Pax Exam** 4.13.5 + **Apache Karaf** 4.4.7, **JaCoCo** 0.8.12, **SonarQube** (develop branch only)
- **Lombok** 1.18.34 — non-Eclipse modules only; Tycho does not support Lombok annotation processing

## Architecture pointers

- The root `pom.xml` is the parent POM: module aggregation, dependency management, plugin configuration, and every profile above.
- `model/model/liquibase.ecore` is the single source of truth; every generated class derives from it. Edit the Ecore, then rebuild `model` — never patch generated output.
- `.mvn/extensions.xml` wires the Maven Wagon extensions needed to reach the BlackBelt repositories; a build that cannot resolve private artifacts usually starts here.
- `.github/workflows/build.yml` is the main CI/CD pipeline (build → test → deploy → tag → release), chained into `merge-pr-tagged` and `create-release-on-master`.
- [README.md](README.md) — project overview and quick start.
- [CONTRIBUTING.md](CONTRIBUTING.md) — development setup, code structure, submission guidelines.
- [.github/CIFLOW.md](.github/CIFLOW.md) — branching strategy and GitHub Actions pipeline detail.
- [JUDO Community](https://github.com/BlackBeltTechnology/judo-community) — parent aggregator project.
- Sibling metamodels in the same pipeline: `judo-meta-psm`, `judo-meta-asm`, `judo-meta-rdbms`.

## Development environment

**Required:** Java 21 JDK · Maven 3.9.4+ (or `./mvnw`).

**For Eclipse IDE development:** m2e, Epsilon, Modeling Tools, XTend / XText /
MWE / MWE2. Code generation from the IDE: right-click
`model/src/workflow/generateModel.mwe2` → Run As → MWE2 Workflow.

**Generated vs. hand-written.** `model/src-gen/` is auto-generated, is **not**
version-controlled, and is cleaned and regenerated on every `model` build.
Safe to edit: `model/src/main/java/` (runtime API),
`model/src/main/epsilon/` (validation rules), `osgi/src/main/java/`
(bundle tracker).

## Git workflow

- **Main branch:** `develop` · **Release branch:** `master` (latest released version)
- **Versioning:** `1.0.2-SNAPSHOT` (Maven) / `1.0.2.qualifier` (Eclipse)
- **Branching model:** GitFlow — `feature/JNG-*`, `release/*`, `bugfix/JNG-*`, `support/JNG-*`, `hotfix/JNG-*`
- **Rule:** every commit and PR must reference a JIRA ticket (`JNG-XXXX`)
- **CI/CD:** GitHub Actions with chained workflows (build → merge-pr-tagged → create-release-on-master)

## Scope guard — invariants an edit must not break

1. **Never edit `src-gen/`** — cleaned and regenerated by the MWE2 workflow on every `model` build. Hand-written code belongs in `src/main/java/`.
2. **The Ecore is the source of truth** — `model/model/liquibase.ecore` (derived from the Liquibase XSD). Behaviour changes start there, not in generated Java.
3. **Lombok is banned in Eclipse plugin modules** — Tycho does not support Lombok annotation processing. It is confined to `osgi/`.
4. **Two build systems coexist** — Tycho resolves `model`/`feature`/`site` from `MANIFEST.MF`; plain Maven resolves `osgi`/`osgi-itest` from POM dependencies. A dependency added to one is not visible to the other.
5. **Version duality is hand-driven here** — the Maven version and the Eclipse `MANIFEST.MF` version must both move: `versions:set` followed by `tycho-versions:update-eclipse-metadata`. Changing only one produces a build that resolves nothing.
6. **The namespace fix handler is load-bearing** — dropping `LiquibaseNamespaceFixUriHandler` from the resource wiring produces changelog XML that Liquibase refuses.
7. **`Liquibase-Models` is a contract** — bundles carrying that manifest header are discovered by `LiquibaseModelBundleTracker` and registered as OSGi services. Renaming it silently breaks every downstream bundle.
8. **Validation is a placeholder, not a guarantee** — `liquibase.evl` is empty and the `ChangeSetValidations` `@Constraint` rules are commented out. Do not cite "validation passed" as evidence of a correct changelog.

## Code instructions

1. First think through the problem, read the codebase for relevant files.
2. Before you make any major changes, check in with me and I will verify the plan.
3. Please every step of the way just give me a high level explanation of what changes you made.
4. Make every task and code change you do as simple as possible. We want to avoid making any massive or complex changes. Every change should impact as little code as possible. Everything is about simplicity.
5. Maintain a documentation file that describes how the architecture of the app works inside and out.
6. Never speculate about code you have not opened. If the user references a specific file, you MUST read the file before answering. Make sure to investigate and read relevant files BEFORE answering questions about the codebase. Never make any claims about code before investigating unless you are certain of the correct answer - give grounded and hallucination-free answers.
7. For implementation use TDD (Test-Driven Development): write or update tests first to define the expected behaviour, verify they fail, then write the minimal implementation to make them pass.
8. Use DRY (Don't Repeat Yourself): extract reusable logic into separate classes, utilities, or components. If the same pattern appears in multiple places, refactor it into a shared helper.

<!-- dox-doctrine -->
## Documentation Update Protocol (WRITE discipline)

Per-directory `AGENTS.md` files form a tree. Each directory `AGENTS.md` is the
per-file record for the files in that directory. This module-root `AGENTS.md`
holds doctrine + architecture pointers only — never a per-file index.

**Keep the root lean.** This file loads into every agent turn — every byte costs
tokens on every turn. A verbose root file buries the rules the model must follow
(signal dilution) and measurably degrades adherence; a lean file keeps doctrine
salient. Default assumption: your update does NOT belong in the root — route it
by the table below.

**Route every doc update by kind:**

| Kind of update | Goes in |
|---|---|
| New file in a directory, or its per-file detail / change history | Nearest directory `AGENTS.md`. Add a `` | `<basename>` | <purpose> | `` row, path-alphabetical. |
| Data flow, protocol, architecture rationale | `docs/architecture.md` or a `docs/<topic>.md` |
| End-user / developer setup | `README.md` |
| Cross-cutting rule every agent needs every turn (rare) | this module-root `AGENTS.md` |

**Read before editing (chain walk).** Before editing a file, read the nearest
`AGENTS.md` chain root→leaf so you know the file's recorded purpose, contracts,
and change history. Do not edit blind.

**Update after editing (closeout pass).** After changing a file, update its row
in the nearest directory `AGENTS.md`: find the file's row, update its purpose in
place; if absent, add it in path-alphabetical order. New directory → scaffold
its `AGENTS.md`. One row per file. The purpose carries a one-line summary, key
exported symbols, contracts/invariants, and `See change: <id>` history.

**Row style (caveman).** Short declarative fragments. Drop articles. Subject →
verb → object, present tense. One fact per row. Prefer concrete tokens (paths,
symbols, env vars) over prose. Keep identifiers verbatim.

**Size rule — split an over-large directory `AGENTS.md` file-based.** pi
auto-injects a directory `AGENTS.md` on every turn when cwd sits at/below it, so
an over-large directory `AGENTS.md` is not supported. Split it file-based: a row
exceeding the length threshold promotes to a per-file `<File>.AGENTS.md`
sidecar carrying that file's full detail (including every `See change:`). The
sidecar is pull-only — its name is not `AGENTS.md`, so pi never auto-injects it
— yet it stays search-indexed (`agents` doc_type). The directory `AGENTS.md`
keeps a one-line summary plus a `→ see `<File>.AGENTS.md`` pointer. Rows within
the threshold stay verbatim (lossless).

## Finding docs (READ discipline)

`kb_*` tools are faster and cheaper than raw search — they return a one-line
purpose + key exports per file, not raw bytes. **This fires on the ACTION, not
the intent** — before you `grep`/`rg` for a symbol, `cat`/read a file to learn
what it does, or chase an import, the kb call goes first. It fires **even
mid-task when you already know the file**; knowing the file does not exempt you.
When your reflex is the left column, run the right column instead:

| You're about to… | Do this FIRST instead |
|---|---|
| `grep -rn "SymbolName" src/` — find where a fn / type / const lives | `kb_search --doc-type agents "SymbolName"` — tree indexes key exports per file |
| `grep -rn "feature\|topic" src/` — how does X work / where's X handled | `kb_search "feature topic"` |
| `cat` / read a file just to learn its purpose before editing | `kb agents <path>` — one-line purpose + exports + change history |
| chase imports / callers across files | `kb_neighbors <path\|heading>` |
| read one doc section in full | `kb_get <path> <section>` |

**Fall-through (explicit):** if the kb call returns nothing relevant, `rg` /
source read is allowed — then add the missing directory `AGENTS.md` row per the
WRITE discipline. kb does NOT replace grep; it goes first.

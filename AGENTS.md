# judo-meta-liquibase - Project Documentation

## Project Overview

**Repository:** BlackBeltTechnology/judo-meta-liquibase
**License:** Eclipse Public License 2.0 (EPL-2.0)
**Java Version:** 21
**Build System:** Maven 3.9.4+ with Tycho 4.0.13 (Eclipse plugin packaging)

1. Provides an EMF (Eclipse Modeling Framework) metamodel for Liquibase database change management, defined in Ecore format and derived from the Liquibase XML Schema
2. Generates Java model classes, builder patterns, navigation helpers, and a runtime wrapper (`LiquibaseModel`) via an MWE2 code generation pipeline
3. Offers a hand-written runtime API for programmatic model building, querying (`LiquibaseUtils`), and Epsilon-based validation (`LiquibaseEpsilonValidator`)
4. Packages the metamodel as both an Eclipse plugin (with feature/update site) and a standalone OSGi bundle for use in non-Eclipse transformation pipelines
5. Part of the JUDO platform's metamodel family, used in model-to-database transformation pipelines alongside judo-meta-psm, judo-meta-asm, and judo-meta-rdbms

## Directory Structure

```
judo-meta-liquibase/
├── model/                  # Core Eclipse plugin: Ecore metamodel + generated code + runtime API
│   ├── model/              # liquibase.ecore, liquibase.genmodel, liquibase.xsd
│   ├── src/main/java/      # Hand-written runtime classes
│   ├── src/main/epsilon/   # Epsilon EVL validation rules
│   ├── src/workflow/        # MWE2 code generation workflow
│   ├── src-gen/            # Generated EMF classes (not in git, regenerated on build)
│   └── META-INF/           # OSGi MANIFEST.MF
├── model-test/             # JUnit 5 unit tests for model
├── osgi/                   # OSGi bundle repackaging (Felix maven-bundle-plugin)
├── osgi-itest/             # OSGi integration tests (Pax Exam + Karaf)
├── feature/                # Eclipse feature definition
├── site/                   # Eclipse P2 update site
├── .github/workflows/      # CI/CD pipelines
├── openspec/               # OpenSpec configuration and specs
└── .claude/                # Claude Code configuration
```

## Core Modules

### Metamodel Layer

| Module | Type | Purpose |
|--------|------|---------|
| `model/` | eclipse-plugin | Core Ecore metamodel (`liquibase.ecore`), EMF-generated Java classes in `src-gen/`, hand-written runtime utilities in `src/main/java/`, Epsilon EVL validation rules, and MWE2 generation workflow |
| `model-test/` | jar | JUnit 5 tests for model builders, LiquibaseUtils query helpers, and Epsilon validation |

### Packaging Layer

| Module | Type | Purpose |
|--------|------|---------|
| `osgi/` | bundle | Repackages the model as a standalone OSGi bundle with `LiquibaseModelBundleTracker` for automatic model loading from bundle headers |
| `osgi-itest/` | test jar | Integration tests using Pax Exam with Apache Karaf 4.4.7 container |
| `feature/` | eclipse-feature | Eclipse feature definition grouping the model plugin for installation |
| `site/` | eclipse-repository | P2 update site generation for Eclipse distribution |

## Technology Stack

### Core Technologies

- **Eclipse Modeling Framework (EMF)** 2.38.0 — Ecore metamodel definition, model code generation
- **Tycho** 4.0.13 — Eclipse plugin build system, handles MANIFEST.MF dependency resolution
- **Epsilon** 2.8.0 — EVL (Epsilon Validation Language) for model validation rules
- **OSGi** 7.0.0 — Module system for both Eclipse and standalone deployment
- **MWE2** (Modeling Workflow Engine 2) — Orchestrates code generation pipeline

### Build & Quality

- **Maven** 3.9.4+ with wrapper (`./mvnw`)
- **JUnit Jupiter** 5.9.1 for unit testing
- **Pax Exam** 4.13.5 + **Apache Karaf** 4.4.7 for OSGi integration testing
- **JaCoCo** 0.8.12 for code coverage
- **SonarQube** for static analysis (develop branch only)
- **Lombok** 1.18.34 (used only in non-Eclipse modules; Tycho does not support Lombok)

### Key Dependencies

- `org.eclipse.emf:org.eclipse.emf.ecore` (2.38.0)
- `org.eclipse.emf:org.eclipse.emf.common` (2.41.0)
- `hu.blackbelt.epsilon:epsilon-runtime-execution` — Epsilon validation runtime
- `hu.blackbelt.epsilon:epsilon-runtime-osgi` — Epsilon OSGi integration
- `hu.blackbelt.osgi.utils:osgi-api` — OSGi bundle tracking utilities
- `hu.blackbelt.judo:judo-genmodel-generator` — JUDO-specific EMF code generators

## Build Commands

```bash
# Full build (all modules)
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

# Set project version across all modules
./mvnw -DnewVersion=X.Y.Z -DgenerateBackupPoms=false versions:set
./mvnw tycho-versions:update-eclipse-metadata
```

### Maven Profiles

| Profile | Purpose |
|---------|---------|
| `modules` | Build all submodules (active by default, skip with `-DskipModules=true`) |
| `sign-artifacts` | GPG-sign artifacts for release builds |
| `release-judong` | Deploy to internal JUDO Nexus repository |
| `release-central` | Deploy to Maven Central via Sonatype OSSRH |
| `release-dummy` | Local file-based deployment for testing |
| `generate-github-asciidoc-diagrams` | Generate documentation with PlantUML diagrams |
| `update-source-code-license` | Update EPL-2.0 license headers across all source files |

## Key Configuration Files

| File | Purpose |
|------|---------|
| `pom.xml` | Root POM: module aggregation, dependency management, profiles, plugin configuration |
| `model/model/liquibase.ecore` | **Source of truth** — Ecore metamodel definition derived from Liquibase XSD |
| `model/model/liquibase.genmodel` | EMF code generation configuration (file extension: `.changelog.xml`, resource type: XML) |
| `model/src/workflow/generateModel.mwe2` | MWE2 workflow running 4 generators: Ecore, Helper, Builder, RuntimeModel |
| `model/META-INF/MANIFEST.MF` | OSGi bundle manifest with Export-Package and Require-Bundle declarations |
| `model/src/main/epsilon/validations/liquibase.evl` | Epsilon EVL validation constraints |
| `.github/workflows/build.yml` | Main CI/CD pipeline (build, test, deploy, tag, release) |
| `.mvn/extensions.xml` | Maven Wagon extensions for repository access |
| `logback-test.xml` | Logback test logging configuration (INFO level) |

## Development Environment

**Required:**
- Java 21 JDK
- Maven 3.9.4+ (wrapper included)

**For Eclipse IDE:**
- m2e (Maven Integration)
- Epsilon
- Modeling Tools
- XTend, XText, MWE, MWE2 (for code generation)

**Generated Code:**
- `model/src-gen/` is auto-generated and not in version control
- Regenerated on every `model/` module build via MWE2 workflow
- **Never edit files in `src-gen/` manually**

**Hand-Written Code (safe to edit):**
- `model/src/main/java/` — Runtime API (LiquibaseModel, LiquibaseUtils, LiquibaseEpsilonValidator, etc.)
- `model/src/main/epsilon/` — Validation rules
- `osgi/src/main/java/` — OSGi bundle tracker

## Git Workflow

- **Main Branch:** `develop`
- **Release Branch:** `master` (latest released version)
- **Versioning:** `1.0.2-SNAPSHOT` (Maven) / `1.0.2.qualifier` (Eclipse)
- **Branching Model:** GitFlow — `feature/JNG-*`, `release/*`, `bugfix/JNG-*`, `support/JNG-*`, `hotfix/JNG-*`
- **Commit Convention:** Every commit and PR must include a JIRA ticket number (`JNG-XXXX`)
- **CI/CD:** GitHub Actions with chained workflows (build → merge-pr-tagged → create-release-on-master)

## Important Notes

1. The Ecore metamodel (`model/model/liquibase.ecore`) is the single source of truth — all generated code derives from it
2. The MWE2 workflow generates four categories of code: EMF model classes, navigation helpers, builder patterns, and the LiquibaseModel runtime wrapper
3. `src-gen/` is not version-controlled and is cleaned + regenerated on every build of the `model/` module
4. Lombok is **not used** in Eclipse plugin code due to Tycho incompatibility — only in the `osgi/` module
5. The project builds with both Tycho (for Eclipse plugin modules) and standard Maven (for osgi/osgi-itest modules) — they have different dependency resolution mechanisms
6. Epsilon validation rules in `liquibase.evl` are currently empty — constraints can be added as needed
7. The OSGi bundle (`osgi/` module) includes an automatic `LiquibaseModelBundleTracker` that discovers and registers `LiquibaseModel` instances from bundle MANIFEST headers (`Liquibase-Models`)
8. Version numbers must be updated in both Maven (`pom.xml`) and Eclipse (`MANIFEST.MF`) using `versions:set` followed by `tycho-versions:update-eclipse-metadata`

## Related Documentation

- [README](README.md) — Project overview and quick start
- [Contributing Guide](CONTRIBUTING.md) — Development setup, code structure, and submission guidelines
- [CI/CD Flow](/.github/CIFLOW.md) — Detailed branching strategy and GitHub Actions pipeline documentation
- [JUDO Community](https://github.com/BlackBeltTechnology/judo-community) — Parent aggregator project

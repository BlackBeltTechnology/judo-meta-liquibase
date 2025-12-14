<!-- OPENSPEC:START -->
# OpenSpec Instructions

These instructions are for AI assistants working in this project.

Always open `@/openspec/AGENTS.md` when the request:
- Mentions planning or proposals (words like proposal, spec, change, plan)
- Introduces new capabilities, breaking changes, architecture shifts, or big performance/security work
- Sounds ambiguous and you need the authoritative spec before coding

Use `@/openspec/AGENTS.md` to learn:
- How to create and apply change proposals
- Spec format and conventions
- Project structure and guidelines

Keep this managed block so 'openspec update' can refresh the instructions.

<!-- OPENSPEC:END -->

# Judo Liquibase Meta - Project Documentation

## Project Overview

**Repository:** BlackBeltTechnology/judo-meta-liquibase
**License:** Eclipse Public License 2.0 (EPL-2.0)
**Java Version:** 21
**Build System:** Maven 3.9.4+ with Tycho (Eclipse build tooling)

This is an Eclipse/Tycho-based metamodel project that:
1. **Defines** the Liquibase metamodel (extended from Liquibase first-party model) via EMF/Ecore
2. **Generates** Java code from the model using MWE2 workflows
3. **Provides** both Eclipse plugin and OSGi standalone runtime
4. **Distributes** via both Maven Central and Eclipse P2 repositories

## Directory Structure

```
judo-meta-liquibase/
├── model/                          # Core Liquibase metamodel (Ecore)
├── model-test/                     # Unit tests for metamodel
├── osgi/                           # OSGi bundle repackaging
├── osgi-itest/                     # OSGi integration tests (Pax Exam)
├── feature/                        # Eclipse feature (model)
├── site/                           # Eclipse P2 update site
└── openspec/                       # OpenSpec change management
```

## Core Modules

### Model Definition Layer

| Module | Type | Purpose |
|--------|------|---------|
| `model/` | eclipse-plugin | Core Liquibase metamodel via Ecore (`liquibase.ecore`). Generates EMF code, builders, helpers. Contains Epsilon validation rules. |
| `model-test/` | test | Unit tests for Liquibase metamodel using JUnit 5 and Epsilon runtime |

### Runtime/OSGi Layer

| Module | Type | Purpose |
|--------|------|---------|
| `osgi/` | bundle | Repackages model for OSGi environments using Apache Felix Bundle Plugin |
| `osgi-itest/` | test | Pax Exam integration tests for Karaf container (4.4.7) |

### Distribution Layer

| Module | Type | Purpose |
|--------|------|---------|
| `feature/` | eclipse-feature | Bundles model and plugins |
| `site/` | eclipse-repository | P2 update site for Eclipse distribution |

## Liquibase Metamodel Structure

The core metamodel (`model/model/liquibase.ecore`) defines Liquibase database changelog elements:
- Database change operations (AddColumn, CreateTable, DropTable, etc.)
- Constraint definitions (PrimaryKey, ForeignKey, UniqueConstraint, etc.)
- Data types and column definitions
- Changelog and changeset structures

This is an extended version of the Liquibase first-party model.

**Validation Rules:**
- **EVL (Epsilon):** Located in `model/src/main/epsilon/validations/` using Epsilon Validation Language (currently minimal)
- **Java Validation Framework:** Located in `model/src/main/java/hu/blackbelt/judo/meta/liquibase/validation/` using Zeta validation framework

## Technology Stack

### Core Technologies
- **Java 21** - Primary language
- **Eclipse Modeling Framework (EMF)** 2.38.0+ - Metamodel foundation
- **Ecore** - Model definition language
- **MWE2** (Model Workflow Engine) 2.13.0 - Code generation workflows
- **Epsilon** 2.8.0 - Model validation (EVL)
- **Tycho** 4.0.13 - Eclipse plugin build

### Validation Technologies
- **Zeta Validation Framework** - Native Java validation with annotations
- **Epsilon EVL** - Epsilon Validation Language for model validation

### Runtime
- **Apache Karaf** 4.4.7 - OSGi container
- **Apache Felix** 6.0.0 - OSGi bundle plugin
- **Pax Exam** 4.13.5 - OSGi testing

### Build & Quality
- **Maven** 3.9.4+ with wrapper
- **JUnit 5** - Unit testing
- **JaCoCo** 0.8.12 - Code coverage
- **Lombok** 1.18.34 - Annotation processing

## Build Commands

```bash
# Standard build
mvn clean install
# or with wrapper
./mvnw clean install

# Memory requirements (configured in .mvn/jvm.config)
# -Xms1024m -Xmx2048m
```

### Maven Profiles

| Profile | Purpose |
|---------|---------|
| `modules` | Includes all submodules (default) |
| `sign-artifacts` | GPG signing for release |
| `release-central` | Maven Central deployment |
| `release-judong` | Internal Judo repository |

## Code Generation Flow

1. **MWE2 Workflow** (`model/src/workflow/generateModel.mwe2`)
   - Generates EMF code from `liquibase.ecore`
   - Produces GenModel-based Java classes
   - Generates builders and helpers

2. **Model Compilation**
   - Tycho compiles eclipse-plugin modules
   - OSGi bundle compilation with Felix

3. **Feature/Site Building**
   - P2 metadata generation
   - Feature packaging
   - Update site assembly

## Key Configuration Files

| File | Purpose |
|------|---------|
| `pom.xml` | Parent POM with module definitions and plugin management |
| `.mvn/jvm.config` | JVM arguments for Maven build |
| `.mvn/extensions.xml` | Maven extensions |
| `model/model/liquibase.ecore` | Core metamodel definition |
| `model/model/liquibase.genmodel` | EMF code generation model |

## Development Environment

**Required:**
- Java 21 JDK
- Maven 3.9.4+
- Eclipse IDE with:
  - m2e (Maven integration)
  - Epsilon plugin
  - Modeling tools

## Git Workflow

- **Main Branch:** `develop`
- **Versioning:** SNAPSHOT-based development (currently 1.0.2-SNAPSHOT)
- **Version Placeholder:** `$VERSION_PLACEHOLDER$` in model metadata
- **Release Process:** CI/CD with Maven Central and P2 deployment

## Important Notes

1. **Understand EMF/Ecore patterns** before modifying model code
2. **Respect Tycho build constraints** when modifying Eclipse plugins
3. **Validation rules** - Two implementations available:
   - **EVL (Epsilon):** Located in `model/src/main/epsilon/validations/` (currently minimal)
   - **Java Validation Framework:** Located in `model/src/main/java/hu/blackbelt/judo/meta/liquibase/validation/`
4. **Use OpenSpec for significant changes** - See `openspec/AGENTS.md` for proposal workflow

## Related Documentation

- `README.md` - Project overview
- `CONTRIBUTING.md` - Contribution guidelines
- `AGENTS.md` - Detailed project documentation for AI assistants
- `docs/validation/README.md` - Validation framework documentation
- `openspec/AGENTS.md` - OpenSpec workflow for spec-driven development
- `openspec/project.md` - Project conventions for OpenSpec
- **Zeta Validation Framework:** Reference at `hu.blackbelt.judo.zeta` (do not copy docs)

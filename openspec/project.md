# Project Context

## Purpose

**Judo Liquibase Meta** is an Eclipse/Tycho-based metamodel project that:
- Defines the Liquibase metamodel via EMF/Ecore (extended from Liquibase first-party model)
- Generates Java code from the model using MWE2 workflows
- Provides both Eclipse plugin and OSGi standalone runtime
- Distributes via both Maven Central and Eclipse P2 repositories

## Tech Stack

### Core Technologies
- **Java 21** - Primary language
- **Eclipse Modeling Framework (EMF)** 2.38.0+ - Metamodel foundation
- **Ecore** - Model definition language
- **MWE2** (Model Workflow Engine) 2.13.0 - Code generation workflows
- **Epsilon** 2.8.0 - Model validation (EVL) and object language (EOL)
- **Tycho** 4.0.13 - Eclipse plugin build

### Runtime
- **Apache Karaf** 4.4.7 - OSGi container
- **Apache Felix** 6.0.0 - OSGi bundle plugin

### Build & Testing
- **Maven** 3.9.4+ with wrapper
- **JUnit 5** - Unit testing
- **Pax Exam** 4.13.5 - OSGi integration testing

## Project Conventions

### Code Style
- Java 21 language features (records, pattern matching, sealed classes where applicable)
- Use Lombok for boilerplate reduction (`@Getter`, `@Setter`, `@Builder`, `@Slf4j`)
- EMF-generated code follows GenModel conventions
- Immutable objects preferred for validation results and cache keys
- Functional interfaces for validation rules and guards

### Architecture Patterns
- **EMF/Ecore patterns** for metamodel definition and manipulation
- **Annotation-based configuration** for validation rules
- **Functional interfaces** for validation logic (lambdas supported)
- **Registry pattern** for scanning and discovering validators
- **Phased execution** for dependency resolution (satisfies)
- **Caching** for expensive operations (extension methods, satisfies results)

### Testing Strategy
- Unit tests in `model-test/` module using JUnit 5
- EVL validation tests follow pattern: `testConstraintName()` methods
- Expected errors/warnings passed to validator for assertion
- Model fixtures created using EMF builders
- OSGi integration tests via Pax Exam in `osgi-itest/`

### Git Workflow
- **Main Branch:** `develop`
- **Versioning:** SNAPSHOT-based development (currently 1.0.2-SNAPSHOT)
- Feature branches for significant changes
- OpenSpec proposals for architectural changes

## Domain Context

### Liquibase Metamodel
The core metamodel (`model/model/liquibase.ecore`) defines Liquibase database changelog elements:
- Database change operations (AddColumn, CreateTable, DropTable, etc.)
- Constraint definitions (PrimaryKey, ForeignKey, UniqueConstraint, etc.)
- Data types and column definitions
- Changelog and changeset structures

### Validation System
Current validation uses **Epsilon Validation Language (EVL)**:
- Rules in `model/src/main/epsilon/validations/`
- Entry point: `liquibase.evl` (currently empty/minimal)
- Validator: `LiquibaseEpsilonValidator.java`

### Key Validation Concepts
- **Constraint**: Error-level rule that must pass
- **Critique**: Warning-level rule (advisory)
- **Guard**: Condition that determines if rule should evaluate
- **Satisfies**: Dependency on another constraint's result (cached)
- **Context**: EClass type the rule applies to

## Important Constraints

### Build Constraints
- Tycho build requires Eclipse plugin structure
- OSGi bundle manifests must be maintained
- P2 update site structure for Eclipse distribution

### Validation Constraints
- EVL rules must remain functional during Java framework migration
- Test parity: Java tests must mirror EVL tests exactly
- Error message format compatibility for test assertions
- Cache results for `satisfies()` calls to avoid re-evaluation

## External Dependencies

### Eclipse Platform
- EMF Runtime 2.38.0+

### Epsilon Runtime
- EVL (Epsilon Validation Language) for model validation
- EOL (Epsilon Object Language) for helper operations
- EMC (Epsilon Model Connectivity) for EMF integration

### Zeta Validation Framework
- `hu.blackbelt.judo.zeta` - Native Java validation framework
- Annotations: `@ValidationContext`, `@Constraint`, `@Critique`, `@Guard`, `@Satisfies`
- Replaces EVL with type-safe Java validation

### Build Infrastructure
- Maven Central for artifact publication
- Eclipse P2 for plugin distribution

## Module Overview

| Module | Purpose |
|--------|---------|
| `model/` | Core Liquibase metamodel, EMF code, Epsilon validation |
| `model-test/` | Unit tests for metamodel and validation |
| `osgi/` | OSGi bundle repackaging |
| `osgi-itest/` | OSGi integration tests |
| `feature/` | Eclipse feature packaging |
| `site/` | P2 update site |

## Active Changes

See `openspec/changes/` for in-progress proposals:
- `add-zeta-validation/` - Native Java validation framework using Zeta to complement EVL

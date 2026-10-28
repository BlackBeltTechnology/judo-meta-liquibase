# Design: Zeta-Based Java Validation Framework for Liquibase

## Context

The Liquibase metamodel currently has minimal EVL validation (the `liquibase.evl` file is essentially empty). This design proposes adding a native Java validation framework using Zeta annotations, following the pattern established in judo-meta-esm.

The Zeta validation framework provides:
- Annotation-based validation rule definition
- Parallel execution support
- Guard conditions for conditional evaluation
- `satisfies()` dependency mechanism with caching
- Pre/post lifecycle hooks

## Goals / Non-Goals

### Goals
- Integrate Zeta validation framework from `hu.blackbelt.judo.zeta`
- Create Java validation infrastructure following ESM patterns
- Support dual validation (EVL and Java) running in parallel tests
- Use constants for all constraint names, guard methods, critique names
- Add performance tests with large model generation
- Update documentation to reference Zeta docs

### Non-Goals
- Copy Zeta framework code (use as dependency only)
- Copy Zeta documentation (reference only)
- Remove EVL validation (both will coexist)
- Automatic EVL-to-Java transpilation

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                     LiquibaseValidator                          │
│  (Entry point - validates entire model)                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────┐    ┌──────────────────┐                  │
│  │ ValidationRegistry│    │ ValidationExecutor│                 │
│  │ (from Zeta)       │    │ (from Zeta)       │                 │
│  │ - Scans classpath │    │ - Parallel jobs   │                 │
│  │ - Registers rules │    │ - Dependency mgmt │                 │
│  └────────┬─────────┘    └────────┬─────────┘                  │
│           │                       │                             │
│           ▼                       ▼                             │
│  ┌──────────────────────────────────────────────────┐          │
│  │            ValidationContext (from Zeta)         │          │
│  │  - Current element                               │          │
│  │  - LiquibaseUtils instance                       │          │
│  │  - Result cache (satisfies)                      │          │
│  │  - Pre/post hooks                                │          │
│  └──────────────────────────────────────────────────┘          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    Validation Rules                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  @ValidationContext(CreateTable.class)                          │
│  public class CreateTableValidations {                          │
│      // Constraint name constant                                │
│      public static final String TABLE_NAME_REQUIRED =           │
│          "CreateTableTableNameRequired";                        │
│                                                                 │
│      @Constraint(                                               │
│          name = TABLE_NAME_REQUIRED,                            │
│          message = "CreateTable must have tableName"            │
│      )                                                          │
│      public ValidationRule tableNameRequired() {                │
│          return (element, ctx) -> {                             │
│              CreateTable ct = (CreateTable) element;            │
│              return ct.getTableName() != null                   │
│                  ? ValidationResult.pass()                      │
│                  : ValidationResult.fail("...");                │
│          };                                                     │
│      }                                                          │
│  }                                                              │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Decisions

### Decision 1: Use Zeta Framework as Dependency (Not Copy)

The Zeta validation framework provides all necessary annotations and infrastructure:
- `@ValidationContext` - Class-level, defines the EClass type
- `@Constraint` - Method-level, error-level rule
- `@Critique` - Method-level, warning-level rule
- `@Guard` - Method-level, references guard predicate
- `@Satisfies` - Method-level, declares dependencies

**Rationale:** Avoids code duplication and ensures consistency with ESM implementation.

### Decision 2: Constants for All Identifiers

All constraint names, guard method names, and critique names SHALL be defined as public static final constants:

```java
public class CreateTableValidations {
    // Constraint names
    public static final String CONSTRAINT_TABLE_NAME_REQUIRED = "CreateTableTableNameRequired";
    public static final String CONSTRAINT_CATALOG_NAME_VALID = "CreateTableCatalogNameValid";

    // Guard method names
    public static final String GUARD_HAS_TABLE_NAME = "hasTableName";

    // Critique names
    public static final String CRITIQUE_SCHEMA_NAME_RECOMMENDED = "CreateTableSchemaNameRecommended";
}
```

**Rationale:** Constants enable compile-time checking, refactoring support, and documentation.

### Decision 3: Dual Validation Test Infrastructure

Tests SHALL run both EVL and Java validators with identical test cases:

```java
public enum ValidatorType {
    EVL,   // LiquibaseEpsilonValidator
    JAVA   // LiquibaseValidator
}

public abstract class AbstractLiquibaseValidationTest {
    protected ValidatorType validatorType;

    @ParameterizedTest(name = "testConstraint [{0}]")
    @EnumSource(ValidatorType.class)
    void testConstraint(ValidatorType type) throws Exception {
        this.validatorType = type;
        // ... test implementation
    }
}
```

**Rationale:** Ensures parity between EVL and Java implementations.

### Decision 4: Performance Test with Large Model

Add performance test generating 10,000+ elements:

```java
@Test
void testPerformanceWithLargeModel() {
    // Generate model with 10,000 elements
    for (int i = 0; i < 10000; i++) {
        // Add elements to model
    }

    // Run both validators and compare timing
    long evlStart = System.nanoTime();
    runEvlValidation();
    long evlTime = System.nanoTime() - evlStart;

    long javaStart = System.nanoTime();
    runJavaValidation();
    long javaTime = System.nanoTime() - javaStart;

    log.info("EVL: {}ms, Java: {}ms", evlTime/1_000_000, javaTime/1_000_000);
}
```

**Rationale:** Validates performance benefits of Java validation with parallel execution.

### Decision 5: Documentation References (Not Copies)

Update documentation to reference Zeta docs:
- Link to Zeta GitHub repository
- Reference Zeta AGENTS.md for validation patterns
- Do not copy Zeta documentation into this repository

**Rationale:** Avoids documentation drift and duplication.

### Decision 6: AsciiDoc to Markdown Conversion

Convert AsciiDoc files to Markdown (except under `pages/` directory):
- `README.adoc` -> `README.md`
- `CONTRIBUTING.adoc` -> `CONTRIBUTING.md`
- Convert PlantUML diagrams to Mermaid

**Rationale:** Better tooling support and GitHub rendering.

## Package Structure

```
model/src/main/java/hu/blackbelt/judo/meta/liquibase/
├── validation/
│   ├── LiquibaseValidator.java          # Entry point
│   ├── LiquibaseValidationConstants.java # All constraint/guard/critique constants
│   └── rules/
│       ├── ChangeSetValidations.java
│       ├── CreateTableValidations.java
│       ├── ColumnValidations.java
│       └── ... (other EClass validations)
└── runtime/
    ├── LiquibaseEpsilonValidator.java   # Existing
    ├── LiquibaseUtils.java              # Existing
    └── LiquibaseModel.java              # Add validation exception

model-test/src/test/java/hu/blackbelt/judo/meta/liquibase/
├── AbstractLiquibaseValidationTest.java
├── ValidatorType.java
├── LiquibaseValidationTest.java         # Modified for dual validation
├── LiquibaseValidationPerformanceTest.java
└── runtime/
    └── ... (existing tests)
```

## Risks / Trade-offs

### Risk 1: Limited Initial Validation Rules
- **Risk**: Liquibase EVL is essentially empty, so initial Java rules may be minimal
- **Mitigation**: Focus on infrastructure first, rules can be added incrementally
- **Trade-off**: Infrastructure investment enables future extensibility

### Risk 2: Test Comparison Complexity
- **Risk**: EVL and Java may have different error formats
- **Mitigation**: Compare constraint names only (not full messages)
- **Trade-off**: Slightly looser test assertions, but achievable parity

### Risk 3: Zeta Dependency Version
- **Risk**: SNAPSHOT version may introduce breaking changes
- **Mitigation**: Pin to specific SNAPSHOT version in property
- **Trade-off**: Manual updates required when Zeta stabilizes

## Migration Plan

1. **Phase 1**: Add Zeta dependency and version property to pom.xml
2. **Phase 2**: Create LiquibaseValidator entry point and infrastructure
3. **Phase 3**: Create ValidatorType enum and AbstractLiquibaseValidationTest
4. **Phase 4**: Modify existing tests for dual validation
5. **Phase 5**: Add validation rule classes with constants
6. **Phase 6**: Add performance test
7. **Phase 7**: Convert documentation to Markdown
8. **Phase 8**: Update documentation with Zeta references

EVL validation remains available throughout for comparison.

## Open Questions

1. **Which Liquibase EClasses need validation?** - Initial focus on core elements (CreateTable, ChangeSet, Column, etc.)
2. **What validation rules are needed?** - Start with required field validation, then add business rules

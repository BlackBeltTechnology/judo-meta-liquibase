# Change: Add Zeta-based Java Validation Framework

**Status: APPLIED** (2025-12-14)

## Why

The Liquibase metamodel currently uses Epsilon Validation Language (EVL) for model validation, which has limitations:
- External dependency on Epsilon runtime
- Limited IDE tooling support compared to Java
- Script interpretation overhead
- Difficult to debug and test in isolation
- Current EVL files are essentially empty with no actual validation rules

Adding a native Java validation framework using Zeta annotations will:
- Provide type-safe, IDE-friendly validation rules
- Enable parallel validation with better performance
- Support dual validation (EVL and Java running side-by-side)
- Match the pattern successfully implemented in judo-meta-esm

## What Changes

### Core Framework Integration
- Add Zeta validation framework dependency (`hu.blackbelt.judo.zeta`)
- Create `LiquibaseValidator` class as entry point for Java validation
- Implement parameterized tests supporting both EVL and Java validation modes

### Validation Rules
- Create Java validation rule classes using Zeta annotations
- Use constants for constraint names, guard method names, and critique names
- Implement validation rules equivalent to any EVL constraints (currently minimal)

### Test Infrastructure
- Add `ValidatorType` enum (EVL, JAVA) for test parameterization
- Create `AbstractLiquibaseValidationTest` base class for dual validation
- Modify existing tests to run with both validators
- Add performance test generating 10,000 elements

### Documentation Updates
- Convert AsciiDoc files (except under `pages/`) to Markdown
- Convert PlantUML diagrams to Mermaid
- Add validation method documentation
- Update references to Zeta documentation (reference only, not copy)

### Build Configuration
- Add `judo-zeta-version` property to root pom.xml
- Update all Zeta references to use the version property
- Set version to SNAPSHOT for development

## Impact

- **Affected specs:** `java-validation` (new capability)
- **Affected code:**
  - `model/pom.xml` - Add Zeta dependency
  - `model/src/main/java/hu/blackbelt/judo/meta/liquibase/validation/` - New validation package
  - `model-test/pom.xml` - Add test dependencies
  - `model-test/src/test/java/` - Test infrastructure changes
  - `README.adoc` -> `README.md` - Documentation conversion
  - `CONTRIBUTING.adoc` -> `CONTRIBUTING.md` - Documentation conversion
  - Root `pom.xml` - Add Zeta version property

## Migration Notes

- Both EVL and Java validation will run in parallel during testing
- EVL validation remains available for backwards compatibility
- Tests are parameterized to validate both implementations produce identical results

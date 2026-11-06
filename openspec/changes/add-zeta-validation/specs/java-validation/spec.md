# Java Validation Capability

## ADDED Requirements

### Requirement: Zeta Framework Integration
The Liquibase model validation system SHALL integrate with the Zeta validation framework from `hu.blackbelt.judo.zeta` to provide native Java-based model validation.

#### Scenario: Zeta dependencies available
- **WHEN** the model project is built
- **THEN** Zeta validation-core and annotations dependencies SHALL be available
- **AND** the `judo-zeta-version` property SHALL be defined in the root pom.xml

#### Scenario: Version property consistency
- **WHEN** any Zeta dependency is referenced in pom.xml files
- **THEN** it SHALL use the `${judo-zeta-version}` property

### Requirement: LiquibaseValidator Entry Point
The system SHALL provide a `LiquibaseValidator` class as the entry point for Java-based model validation.

#### Scenario: Validate model with expected errors
- **WHEN** `LiquibaseValidator.validateLiquibase()` is called with a model and expected error constraints
- **THEN** the validator SHALL execute all registered validation rules
- **AND** SHALL throw `LiquibaseValidationException` if actual errors don't match expected errors

#### Scenario: Validate model with parallel execution
- **WHEN** `LiquibaseValidator.validateLiquibase()` is called with parallel=true
- **THEN** validation rules SHALL execute in parallel using Zeta's executor
- **AND** results SHALL be equivalent to sequential execution

### Requirement: Validation Rule Constants
All validation rule identifiers SHALL be defined as public static final constants.

#### Scenario: Constraint name constants
- **WHEN** a validation constraint is defined
- **THEN** its name SHALL be a constant prefixed with `CONSTRAINT_`
- **AND** the constant SHALL be used in the `@Constraint(name = ...)` annotation

#### Scenario: Guard method name constants
- **WHEN** a guard method is referenced
- **THEN** its name SHALL be a constant prefixed with `GUARD_`
- **AND** the constant SHALL be used in the `@Guard(method = ...)` annotation

#### Scenario: Critique name constants
- **WHEN** a validation critique is defined
- **THEN** its name SHALL be a constant prefixed with `CRITIQUE_`
- **AND** the constant SHALL be used in the `@Critique(name = ...)` annotation

### Requirement: Dual Validation Testing
The test infrastructure SHALL support running both EVL and Java validators with identical test cases.

#### Scenario: Parameterized test execution
- **WHEN** a validation test is executed
- **THEN** it SHALL run with both `ValidatorType.EVL` and `ValidatorType.JAVA`
- **AND** both executions SHALL use the same test model and expected results

#### Scenario: Test result comparison
- **WHEN** both EVL and Java validators complete
- **THEN** the constraint names of found errors SHALL match
- **AND** the constraint names of found warnings SHALL match (when specified)

### Requirement: Performance Testing
The system SHALL include performance tests validating large models.

#### Scenario: Large model generation
- **WHEN** the performance test runs
- **THEN** it SHALL generate a model with at least 10,000 elements
- **AND** the elements SHALL include a mix of different EClasses

#### Scenario: Validation timing comparison
- **WHEN** both EVL and Java validation complete on the large model
- **THEN** execution times SHALL be logged
- **AND** Java validation SHOULD complete in comparable or less time than EVL

### Requirement: Documentation Updates
Project documentation SHALL be updated to Markdown format and reference Zeta documentation.

#### Scenario: AsciiDoc to Markdown conversion
- **WHEN** documentation files are processed
- **THEN** `.adoc` files (except under `pages/`) SHALL be converted to `.md`
- **AND** AsciiDoc syntax SHALL be converted to Markdown syntax

#### Scenario: PlantUML to Mermaid conversion
- **WHEN** diagrams are processed
- **THEN** PlantUML diagrams SHALL be converted to Mermaid
- **AND** the converted diagrams SHALL render correctly in Markdown

#### Scenario: Zeta documentation references
- **WHEN** validation documentation is updated
- **THEN** it SHALL reference Zeta documentation (not copy)
- **AND** links SHALL point to the Zeta repository or documentation site

### Requirement: OSGi Bundle Export
The validation package SHALL be exported in the OSGi bundle.

#### Scenario: Validation package export
- **WHEN** the OSGi bundle is built
- **THEN** `hu.blackbelt.judo.meta.liquibase.validation` package SHALL be exported
- **AND** `hu.blackbelt.judo.meta.liquibase.validation.rules` package SHALL be exported

#### Scenario: OSGi integration test
- **WHEN** OSGi integration tests run
- **THEN** validation classes SHALL be accessible from the bundle
- **AND** validation SHALL execute correctly in OSGi environment

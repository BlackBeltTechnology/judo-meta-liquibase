# model-test Specification

## Purpose

The `model-test` module contains JUnit 5 unit tests that verify the correctness of the model's runtime API, builder pattern, query utilities (`LiquibaseUtils`), and Epsilon EVL validation. It ensures the hand-written and generated code work correctly together.

## Architecture

- `LiquibaseExecutionContextTest` — Tests model creation via the builder pattern and `LiquibaseModelResourceSupport`
- `LiquibaseUtilsTest` — Comprehensive tests for all `LiquibaseUtils` query methods (ChangeSets, CreateTables, Columns, PrimaryKeys, ForeignKeys, NotNullConstraints)
- `LiquibaseValidationTest` — Tests Epsilon EVL validation execution against model instances

Tests use the generated builder API (`LiquibaseBuilders.*`) to construct model instances and assert correct behavior of the runtime API.

## Requirements

### Requirement: ChangeSet query tests

The test suite SHALL verify that `LiquibaseUtils.getChangeSets()` and `LiquibaseUtils.getChangeSet(id)` correctly return model elements by ID.

#### Scenario: Query multiple ChangeSets

- **GIVEN** a model with two ChangeSets ("ChangeSetId" and "ChangeSet1Id")
- **WHEN** `getChangeSets()` is called
- **THEN** both ChangeSets are returned in the result list
- **WHEN** `getChangeSet("ChangeSetId")` is called
- **THEN** the matching ChangeSet is returned

#### Scenario: Query empty model returns empty Optional

- **GIVEN** an empty model with no ChangeSets
- **WHEN** `getChangeSets()` or `getChangeSet(id)` is called
- **THEN** `Optional.empty()` is returned

### Requirement: CreateTable query tests

The test suite SHALL verify that `LiquibaseUtils.getCreateTables(changeSetId)` and `LiquibaseUtils.getCreateTable(changeSetId, tableName)` correctly navigate the model hierarchy.

#### Scenario: Query CreateTables within a ChangeSet

- **GIVEN** a ChangeSet containing two CreateTable elements ("TestTable" and "TestTable1")
- **WHEN** `getCreateTables(changeSetId)` is called
- **THEN** both tables are returned
- **WHEN** `getCreateTable(changeSetId, "TestTable")` is called
- **THEN** the matching CreateTable is returned

### Requirement: Column query tests

The test suite SHALL verify that `LiquibaseUtils.getColumns(changeSetId, tableName)` and `LiquibaseUtils.getColumn(changeSetId, tableName, columnName)` correctly navigate through ChangeSet → CreateTable → Column.

#### Scenario: Query Columns within a CreateTable

- **GIVEN** a CreateTable with columns "_id" and "column"
- **WHEN** `getColumns(changeSetId, tableName)` is called
- **THEN** both columns are returned
- **WHEN** `getColumn(changeSetId, tableName, "_id")` is called
- **THEN** the matching Column is returned

### Requirement: Primary key query tests

The test suite SHALL verify that `LiquibaseUtils.getAddPrimaryKeys()` and `LiquibaseUtils.getAddPrimaryKey()` correctly filter by table name and column names.

#### Scenario: Query AddPrimaryKey by table name

- **GIVEN** a ChangeSet with AddPrimaryKey elements for different tables
- **WHEN** `getAddPrimaryKeys(changeSetId, "TestTable")` is called
- **THEN** only the primary key for "TestTable" is returned (not "TestTable1")

### Requirement: Foreign key constraint query tests

The test suite SHALL verify that `LiquibaseUtils.getAddForeignKeyConstraints()` and `LiquibaseUtils.getAddForeignKeyConstraint()` correctly filter by base table, referenced table, and constraint name.

#### Scenario: Query AddForeignKeyConstraint

- **GIVEN** a ChangeSet with foreign key constraints between different table pairs
- **WHEN** `getAddForeignKeyConstraints(changeSetId, "TestTable1", "TestTable2")` is called
- **THEN** only the matching constraint is returned

### Requirement: Not-null constraint query tests

The test suite SHALL verify that `LiquibaseUtils.getAddNotNullConstraints()` and `LiquibaseUtils.getAddNotNullConstraint()` correctly filter by table name and column name.

#### Scenario: Query AddNotNullConstraint

- **GIVEN** a ChangeSet with not-null constraints on different tables
- **WHEN** `getAddNotNullConstraints(changeSetId, "TestTable")` is called
- **THEN** only the constraint for "TestTable" is returned

### Requirement: Model serialization in tests

The test suite SHALL save models to XML files in `target/test-classes/` for debugging and verification purposes.

#### Scenario: Save test model

- **GIVEN** a model built during a test
- **WHEN** `saveLiquibaseModel(testName)` is called
- **THEN** the model is serialized to `target/test-classes/<testName>-liquibase.xml`

### Requirement: Validation test execution

The test suite SHALL verify that Epsilon EVL validation can be executed against model instances with expected errors and warnings.

#### Scenario: Run Epsilon validation

- **GIVEN** a `LiquibaseModel` and the validation script URI from `calculateLiquibaseValidationScriptURI()`
- **WHEN** `runEpsilon(expectedErrors, expectedWarnings)` is called
- **THEN** validation runs and reports unexpected errors/warnings if they don't match expectations

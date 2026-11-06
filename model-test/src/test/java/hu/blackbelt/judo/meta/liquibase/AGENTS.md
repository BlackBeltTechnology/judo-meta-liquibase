# `model-test/src/test/java/hu/blackbelt/judo/meta/liquibase` — dual-engine (EVL vs Java) validation test harness

| File | Purpose |
|---|---|
| `AbstractLiquibaseValidationTest.java` | Shared fixture running one test body against both validation engines. Exports `initModel()` and `runValidation(expectedErrors, expectedWarnings)`; `initModel()` builds `urn:liquibase.judo-meta-liquibase` and a `LiquibaseModel` named `test`. Subclass must set `validatorType` before calling. EVL branch compares `UnsatisfiedConstraint` names only; null `expectedWarnings` ignores warnings. |
| `LiquibaseValidationPerformanceTest.java` | Scale benchmark over a generated `databaseChangeLog`: `NUM_CHANGE_SETS`=1000 ChangeSets × one `CreateTable` × `COLUMNS_PER_TABLE`=10 (~12,000 elements). Tests `testJavaValidatorPerformance` (sequential then parallel), `testEvlValidatorPerformance`, `testValidatorComparison`. Timings are logged, never asserted — this file can never fail a build on slowness. |
| `ValidatorType.java` | Enum discriminating the two validation engines for `@EnumSource` parameterized runs. `EVL` dispatches to `LiquibaseEpsilonValidator`, `JAVA` to `LiquibaseValidator`. `AbstractLiquibaseValidationTest.runValidation` throws `IllegalStateException` on any other constant, so a new constant must extend that switch. |

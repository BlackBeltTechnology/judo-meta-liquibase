# `model/src/main/epsilon/validations` — EVL constraint sources packaged under `validations/` in the bundle

| File | Purpose |
|---|---|
| `liquibase-plugin-validation.evl` | Eclipse-editor entry point, wired from `model/plugin.xml` as `constraints="validations/liquibase-plugin-validation.evl"`. Imports `liquibase.evl` and adds only a `pre` block instantiating `Native("hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseUtils")(LIQUIBASE.resource.resourceSet, false)` as `liquibaseUtils`. It assumes the model variable is bound as `LIQUIBASE`; headless callers bind `Liquibase` instead and must inject `liquibaseUtils` themselves. |
| `liquibase.evl` | Constraint script resolved by `LiquibaseEpsilonValidator.calculateLiquibaseValidationScriptURI()` via `UriUtil.resolve("liquibase.evl", scriptRoot)`, and imported by `liquibase-plugin-validation.evl`. Currently empty — it declares zero constraints, so EVL runs report no error and no warning. `LiquibaseValidationTest.testEmptyModelValidation` passes for that reason, not because constraints were checked. The file must keep existing: deleting it breaks both resolvers. |

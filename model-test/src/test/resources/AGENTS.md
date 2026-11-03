# `model-test/src/test/resources` — sample changelog fixture for the test module

| File | Purpose |
|---|---|
| `example.changelog.xml` | Hand-written `databaseChangeLog` in the `http://www.liquibase.org/xml/ns/dbchangelog` namespace, used as a realistic parse target rather than a builder-constructed model. One `changeSet author="bob" id="1"` creating table `department` with `id int` (nullable=false, primaryKey=true), `name varchar(50)` (nullable=false) and `active boolean` defaulting true. The default namespace carries no version suffix, so it exercises `LiquibaseNamespaceFixUriHandler`'s rewrite path; changing the namespace or the `changeSet` id changes what that handler is proven to accept. |

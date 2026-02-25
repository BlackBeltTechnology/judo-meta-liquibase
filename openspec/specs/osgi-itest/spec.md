# osgi-itest Specification

## Purpose

The `osgi-itest` module provides integration tests that verify the Liquibase metamodel works correctly inside a real OSGi container (Apache Karaf). It tests the full lifecycle: model building, serialization into an OSGi bundle, automatic discovery by `LiquibaseModelBundleTracker`, service injection, and Epsilon validation.

## Architecture

- **`LiquibaseModelLoadITest`** — Pax Exam integration test that:
  1. Configures a Karaf container with the `hu.blackbelt.judo.meta.liquibase.osgi` bundle
  2. Programmatically builds a `LiquibaseModel`, serializes it, and wraps it in a TinyBundle with `Liquibase-Models` MANIFEST header
  3. Provisions the test bundle into Karaf
  4. Injects the `LiquibaseModel` OSGi service (discovered by `LiquibaseModelBundleTracker`)
  5. Validates the injected model using Epsilon EVL

- **`KarafFeatureProvider`** — Configuration helper that sets up the Karaf test container with required features and bundles

## Requirements

### Requirement: Model round-trip through OSGi

The integration test SHALL verify that a `LiquibaseModel` can be built programmatically, serialized, packaged into an OSGi bundle, and loaded back through the bundle tracker.

#### Scenario: Build, serialize, and load model in Karaf

- **GIVEN** a Karaf container with the `hu.blackbelt.judo.meta.liquibase.osgi` bundle installed
- **WHEN** a TinyBundle is provisioned with:
  - `Liquibase-Models: name=northwind-liquibase;file=model/northwind-liquibase-liquibase.model`
  - The serialized model file at the specified path
- **THEN** `LiquibaseModelBundleTracker` discovers the bundle and registers the model as an OSGi service
- **AND** the `LiquibaseModel` is injectable via `@Inject`

### Requirement: Service injection

The Pax Exam test SHALL verify that `LiquibaseModel` and `BundleTrackerManager` are available as injectable OSGi services.

#### Scenario: Inject LiquibaseModel service

- **GIVEN** a provisioned model bundle in the Karaf container
- **WHEN** the test class requests `@Inject LiquibaseModel liquibaseModel`
- **THEN** the injected instance is non-null and represents the provisioned model

#### Scenario: Inject BundleTrackerManager

- **GIVEN** the Karaf container with OSGi utils installed
- **WHEN** the test class requests `@Inject BundleTrackerManager bundleTrackerManager`
- **THEN** the injected instance is non-null

### Requirement: Model validation in OSGi context

The integration test SHALL verify that Epsilon EVL validation can be executed against a model loaded in an OSGi container.

#### Scenario: Validate model in Karaf

- **GIVEN** an injected `LiquibaseModel` in the Karaf container
- **WHEN** `validateLiquibase(logger, liquibaseModel, calculateLiquibaseValidationScriptURI())` is called
- **THEN** validation completes successfully (the validation script URI resolves correctly from within a JAR/bundle context)

### Requirement: Bundle header format

The test SHALL use the standard `Liquibase-Models` MANIFEST header format for model discovery.

#### Scenario: TinyBundle with correct header

- **WHEN** a test bundle is created with `Liquibase-Models: name=<name>;file=model/<name>-liquibase.model`
- **THEN** the `LiquibaseModelBundleTracker` can parse the header and load the model from the specified file entry

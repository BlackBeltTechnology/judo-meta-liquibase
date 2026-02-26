# osgi Specification

## Purpose

The `osgi` module repackages the `model` Eclipse plugin as a standalone OSGi bundle suitable for non-Eclipse containers (e.g., Apache Karaf). It adds an automatic bundle tracker (`LiquibaseModelBundleTracker`) that discovers and registers `LiquibaseModel` instances as OSGi services based on bundle MANIFEST headers.

## Architecture

- **Bundle packaging**: Uses Apache Felix `maven-bundle-plugin` to create an OSGi bundle that exports all `hu.blackbelt.judo.meta.liquibase*` packages
- **`LiquibaseModelBundleTracker`**: An OSGi Declarative Services `@Component` that uses `BundleTrackerManager` to watch for bundles with the `Liquibase-Models` MANIFEST header
- **Resource inclusion**: Bundles the Ecore model files (`meta/liquibase/`) and Epsilon validation scripts (`validations/`) from the `model` module

Key classes:
- `LiquibaseModelBundleTracker` — OSGi component with `@Activate`/`@Deactivate` lifecycle, registers/unregisters `LiquibaseModel` OSGi services
- `LiquibaseRegisterCallback` — Inner class that loads models from bundle entries and registers them as services
- `LiquibaseUnregisterCallback` — Inner class that unregisters models when bundles are removed

## Requirements

### Requirement: Automatic model discovery from bundles

The `LiquibaseModelBundleTracker` SHALL automatically discover bundles with the `Liquibase-Models` MANIFEST header and load their model files.

#### Scenario: Bundle with Liquibase-Models header is installed

- **GIVEN** an active `LiquibaseModelBundleTracker` component with a `BundleTrackerManager` reference
- **WHEN** a bundle with header `Liquibase-Models: name=demo;file=model/demo-liquibase.model` is installed
- **THEN** the tracker loads the model from `file` entry using `LiquibaseModel.loadLiquibaseModel()`
- **AND** registers it as an OSGi service of type `LiquibaseModel`

#### Scenario: Bundle without Liquibase-Models header is installed

- **GIVEN** an active `LiquibaseModelBundleTracker` component
- **WHEN** a bundle without the `Liquibase-Models` header is installed
- **THEN** the tracker ignores the bundle (predicate `LiquibaseBundlePredicate` returns false)

### Requirement: Model service registration

The tracker SHALL register each discovered `LiquibaseModel` as an OSGi service with its model properties as service properties.

#### Scenario: Service registration with model properties

- **GIVEN** a bundle with `Liquibase-Models: name=mymodel;file=model/mymodel.model`
- **WHEN** the model is loaded successfully
- **THEN** `bundleContext.registerService(LiquibaseModel.class, model, model.toDictionary())` is called
- **AND** the service is discoverable by other OSGi components

### Requirement: Duplicate model prevention

The tracker SHALL reject loading a model if a model with the same name is already registered.

#### Scenario: Duplicate model name

- **GIVEN** a model named "demo" is already registered
- **WHEN** another bundle with `Liquibase-Models: name=demo;file=...` is installed
- **THEN** an error is logged ("Liquibase model already loaded: demo")
- **AND** the duplicate model is not loaded or registered

### Requirement: Model unregistration on bundle removal

The tracker SHALL unregister `LiquibaseModel` OSGi services when their source bundles are removed.

#### Scenario: Bundle with registered model is uninstalled

- **GIVEN** a model named "demo" registered as an OSGi service
- **WHEN** the source bundle is uninstalled
- **THEN** `LiquibaseUnregisterCallback` calls `serviceRegistration.unregister()`
- **AND** the model is removed from the internal maps

### Requirement: Component lifecycle

The tracker SHALL register its bundle callback on activation and unregister on deactivation.

#### Scenario: Component activation

- **WHEN** the `LiquibaseModelBundleTracker` component is activated
- **THEN** it calls `bundleTrackerManager.registerBundleCallback()` with register/unregister callbacks and the `LiquibaseBundlePredicate`

#### Scenario: Component deactivation

- **WHEN** the `LiquibaseModelBundleTracker` component is deactivated
- **THEN** it calls `bundleTrackerManager.unregisterBundleCallback()`

### Requirement: Package exports

The OSGi bundle SHALL export all `hu.blackbelt.judo.meta.liquibase*` packages and include Epsilon runtime imports as optional.

#### Scenario: Bundle resolution without Epsilon

- **GIVEN** an OSGi container without Epsilon runtime bundles
- **WHEN** the `hu.blackbelt.judo.meta.liquibase.osgi` bundle is installed
- **THEN** the bundle resolves successfully (Epsilon imports are `resolution:=optional`)
- **AND** model loading and querying work, but validation is unavailable

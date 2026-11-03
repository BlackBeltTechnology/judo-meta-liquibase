package hu.blackbelt.judo.meta.liquibase.validation;

/*-
 * #%L
 * Judo :: Liquibase :: Model
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModel;
import hu.blackbelt.judo.zeta.common.ExtensionMethodRegistry;
import hu.blackbelt.judo.zeta.validation.core.*;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Entry point for Java-based Liquibase model validation.
 *
 * <p>This validator provides a native Java alternative to EVL (Epsilon Validation Language)
 * validation with better IDE integration, debugging support, and performance.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * LiquibaseValidator.validateLiquibase(log, liquibaseModel);
 * }
 * </pre>
 *
 * @see LiquibaseValidationConstants
 */
public class LiquibaseValidator {

    /**
     * Validate Liquibase model using Java validation rules.
     *
     * @param log the logger
     * @param liquibaseModel the model to validate
     * @throws LiquibaseValidationException if validation fails
     */
    public static void validateLiquibase(Logger log, LiquibaseModel liquibaseModel)
            throws LiquibaseValidationException {
        validateLiquibase(log, liquibaseModel, null, null, false);
    }

    /**
     * Validate Liquibase model with expected errors and warnings (for testing).
     *
     * @param log the logger
     * @param liquibaseModel the model to validate
     * @param expectedErrors expected error constraint names
     * @param expectedWarnings expected warning constraint names
     * @throws LiquibaseValidationException if validation fails
     */
    public static void validateLiquibase(
            Logger log,
            LiquibaseModel liquibaseModel,
            Collection<String> expectedErrors,
            Collection<String> expectedWarnings)
            throws LiquibaseValidationException {
        validateLiquibase(log, liquibaseModel, expectedErrors, expectedWarnings, false);
    }

    /**
     * Validate Liquibase model with all options.
     *
     * @param log the logger
     * @param liquibaseModel the model to validate
     * @param expectedErrors expected error constraint names
     * @param expectedWarnings expected warning constraint names
     * @param parallel use parallel execution
     * @throws LiquibaseValidationException if validation fails
     */
    public static void validateLiquibase(
            Logger log,
            LiquibaseModel liquibaseModel,
            Collection<String> expectedErrors,
            Collection<String> expectedWarnings,
            boolean parallel)
            throws LiquibaseValidationException {
        log.info("Starting Java-based Liquibase validation...");

        // Create validation infrastructure
        ValidationRegistry registry = new ValidationRegistry();
        ExtensionMethodRegistry extensionRegistry = new ExtensionMethodRegistry();

        // Create validation context
        ValidationContext context = new ValidationContext(
                new LiquibaseModelProvider(),
                liquibaseModel.getResourceSet(),
                extensionRegistry
        );
        context.setValidationRegistry(registry);

        // Register validation rule classes
        // TODO: Register validation rule classes as they are implemented
        // registry.register(CreateTableValidations.class);
        // registry.register(ChangeSetValidations.class);

        // Create executor and run validation
        ValidationExecutor executor = new ValidationExecutor(registry, context, parallel);

        // Collect all elements from the model
        List<EObject> allElements = new ArrayList<>();
        for (Resource resource : liquibaseModel.getResourceSet().getResources()) {
            resource.getAllContents().forEachRemaining(allElements::add);
        }

        log.info("Validating {} elements...", allElements.size());

        // Execute validation
        List<ValidationResult> results = executor.validate(allElements);

        // Collect actual errors and warnings
        Set<String> actualErrors = results.stream()
                .filter(r -> r.isFailed() && r.getSeverity() == Severity.ERROR)
                .map(ValidationResult::getConstraintName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> actualWarnings = results.stream()
                .filter(r -> r.isFailed() && r.getSeverity() == Severity.WARNING)
                .map(ValidationResult::getConstraintName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        log.info("Validation found {} errors and {} warnings", actualErrors.size(), actualWarnings.size());

        // Compare with expected results
        Set<String> expectedErrorSet = expectedErrors != null
                ? new HashSet<>(expectedErrors)
                : Collections.emptySet();

        Set<String> expectedWarningSet = expectedWarnings != null
                ? new HashSet<>(expectedWarnings)
                : null; // null means "don't care about warnings"

        // Calculate mismatches
        Set<String> unexpectedErrors = new HashSet<>(actualErrors);
        unexpectedErrors.removeAll(expectedErrorSet);

        Set<String> missingErrors = new HashSet<>(expectedErrorSet);
        missingErrors.removeAll(actualErrors);

        Set<String> unexpectedWarnings = Collections.emptySet();
        Set<String> missingWarnings = Collections.emptySet();

        if (expectedWarningSet != null) {
            unexpectedWarnings = new HashSet<>(actualWarnings);
            unexpectedWarnings.removeAll(expectedWarningSet);

            missingWarnings = new HashSet<>(expectedWarningSet);
            missingWarnings.removeAll(actualWarnings);
        }

        // Check for validation failures
        boolean hasIssues = !unexpectedErrors.isEmpty() ||
                !missingErrors.isEmpty() ||
                !unexpectedWarnings.isEmpty() ||
                !missingWarnings.isEmpty();

        if (hasIssues) {
            log.error("Liquibase validation result mismatch:");
            log.error("  Actual errors: {}", actualErrors);
            log.error("  Expected errors: {}", expectedErrorSet);
            log.error("  Actual warnings: {}", actualWarnings);
            log.error("  Expected warnings: {}", expectedWarningSet);
            if (!unexpectedErrors.isEmpty()) {
                log.error("  Unexpected errors: {}", unexpectedErrors);
            }
            if (!missingErrors.isEmpty()) {
                log.error("  Missing errors: {}", missingErrors);
            }
            if (!unexpectedWarnings.isEmpty()) {
                log.error("  Unexpected warnings: {}", unexpectedWarnings);
            }
            if (!missingWarnings.isEmpty()) {
                log.error("  Missing warnings: {}", missingWarnings);
            }

            throw new LiquibaseValidationException(
                    unexpectedErrors,
                    missingErrors,
                    unexpectedWarnings,
                    missingWarnings,
                    results
            );
        }

        log.info("Java-based Liquibase validation completed successfully");
    }
}

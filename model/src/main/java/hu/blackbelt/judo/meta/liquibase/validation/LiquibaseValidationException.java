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

import hu.blackbelt.judo.zeta.validation.core.ValidationResult;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Exception thrown when Liquibase model validation fails.
 *
 * <p>Contains the validation results including both unexpected errors/warnings
 * and missing expected errors/warnings.</p>
 */
public class LiquibaseValidationException extends Exception {

    private final Set<String> unexpectedErrors;
    private final Set<String> missingErrors;
    private final Set<String> unexpectedWarnings;
    private final Set<String> missingWarnings;
    private final Collection<ValidationResult> allResults;

    public LiquibaseValidationException(
            Set<String> unexpectedErrors,
            Set<String> missingErrors,
            Set<String> unexpectedWarnings,
            Set<String> missingWarnings,
            Collection<ValidationResult> allResults) {
        super(buildMessage(unexpectedErrors, missingErrors, unexpectedWarnings, missingWarnings));
        this.unexpectedErrors = unexpectedErrors != null ? unexpectedErrors : Collections.emptySet();
        this.missingErrors = missingErrors != null ? missingErrors : Collections.emptySet();
        this.unexpectedWarnings = unexpectedWarnings != null ? unexpectedWarnings : Collections.emptySet();
        this.missingWarnings = missingWarnings != null ? missingWarnings : Collections.emptySet();
        this.allResults = allResults != null ? allResults : Collections.emptyList();
    }

    private static String buildMessage(
            Set<String> unexpectedErrors,
            Set<String> missingErrors,
            Set<String> unexpectedWarnings,
            Set<String> missingWarnings) {
        StringBuilder sb = new StringBuilder("Liquibase validation failed:");
        if (unexpectedErrors != null && !unexpectedErrors.isEmpty()) {
            sb.append("\n  Unexpected errors: ").append(unexpectedErrors);
        }
        if (missingErrors != null && !missingErrors.isEmpty()) {
            sb.append("\n  Missing errors: ").append(missingErrors);
        }
        if (unexpectedWarnings != null && !unexpectedWarnings.isEmpty()) {
            sb.append("\n  Unexpected warnings: ").append(unexpectedWarnings);
        }
        if (missingWarnings != null && !missingWarnings.isEmpty()) {
            sb.append("\n  Missing warnings: ").append(missingWarnings);
        }
        return sb.toString();
    }

    public Set<String> getUnexpectedErrors() {
        return unexpectedErrors;
    }

    public Set<String> getMissingErrors() {
        return missingErrors;
    }

    public Set<String> getUnexpectedWarnings() {
        return unexpectedWarnings;
    }

    public Set<String> getMissingWarnings() {
        return missingWarnings;
    }

    public Collection<ValidationResult> getAllResults() {
        return allResults;
    }
}

package hu.blackbelt.judo.meta.liquibase.validation.rules;

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

import hu.blackbelt.judo.meta.liquibase.ChangeSet;
import hu.blackbelt.judo.zeta.annotation.Constraint;
import hu.blackbelt.judo.zeta.annotation.ValidationContext;
import hu.blackbelt.judo.zeta.validation.core.ValidationResult;
import hu.blackbelt.judo.zeta.validation.core.ValidationRule;

/**
 * Validation rules for {@link ChangeSet}.
 *
 * <p>This class contains validation rules for Liquibase ChangeSet elements.
 * Rules are annotated with {@code @Constraint} for error-level checks.</p>
 *
 * <p>Currently empty as there are no EVL validation rules defined.
 * Add rules here as they are needed.</p>
 */
@ValidationContext(ChangeSet.class)
public class ChangeSetValidations {

    // Constraint names (constants for use in tests)
    public static final String CONSTRAINT_CHANGE_SET_HAS_ID = "ChangeSetHasId";
    public static final String CONSTRAINT_CHANGE_SET_HAS_AUTHOR = "ChangeSetHasAuthor";

    // Example constraint (commented out - uncomment when needed):
    //
    // /**
    //  * Constraint: ChangeSet must have an id.
    //  */
    // @Constraint(
    //     name = CONSTRAINT_CHANGE_SET_HAS_ID,
    //     message = "ChangeSet must have an id"
    // )
    // public ValidationRule changeSetHasId() {
    //     return (element, ctx) -> {
    //         ChangeSet self = (ChangeSet) element;
    //         boolean isValid = self.getId() != null && !self.getId().isEmpty();
    //         return isValid
    //             ? ValidationResult.pass()
    //             : ValidationResult.fail("ChangeSet must have an id");
    //     };
    // }
}

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

/**
 * Constants for Liquibase model validation.
 *
 * <p>All constraint names, guard method names, and critique names
 * are defined here as public static final constants to enable
 * compile-time checking and refactoring support.</p>
 *
 * <p>Naming conventions:</p>
 * <ul>
 *   <li>CONSTRAINT_* - Error-level validation rules</li>
 *   <li>CRITIQUE_* - Warning-level validation rules</li>
 *   <li>GUARD_* - Guard method names for conditional validation</li>
 * </ul>
 */
public final class LiquibaseValidationConstants {

    private LiquibaseValidationConstants() {
        // Prevent instantiation
    }

    // ========================================================================
    // CreateTable Constraints
    // ========================================================================

    /**
     * Constraint: CreateTable must have a tableName.
     */
    public static final String CONSTRAINT_CREATE_TABLE_HAS_TABLE_NAME = "CreateTableHasTableName";

    // ========================================================================
    // ChangeSet Constraints
    // ========================================================================

    /**
     * Constraint: ChangeSet must have an id.
     */
    public static final String CONSTRAINT_CHANGE_SET_HAS_ID = "ChangeSetHasId";

    /**
     * Constraint: ChangeSet must have an author.
     */
    public static final String CONSTRAINT_CHANGE_SET_HAS_AUTHOR = "ChangeSetHasAuthor";

    // ========================================================================
    // Column Constraints
    // ========================================================================

    /**
     * Constraint: Column must have a name.
     */
    public static final String CONSTRAINT_COLUMN_HAS_NAME = "ColumnHasName";

    // ========================================================================
    // Critiques (Warnings)
    // ========================================================================

    /**
     * Critique: CreateTable should have a schemaName specified.
     */
    public static final String CRITIQUE_CREATE_TABLE_SHOULD_HAVE_SCHEMA = "CreateTableShouldHaveSchema";

    // ========================================================================
    // Guard Methods
    // ========================================================================

    /**
     * Guard: Check if element has a parent container.
     */
    public static final String GUARD_HAS_CONTAINER = "hasContainer";
}

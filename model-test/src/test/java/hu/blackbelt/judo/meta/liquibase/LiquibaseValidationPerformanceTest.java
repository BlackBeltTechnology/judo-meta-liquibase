package hu.blackbelt.judo.meta.liquibase;

/*-
 * #%L
 * Judo :: Liquibase :: Model :: Test
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

import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.liquibase.*;
import hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseEpsilonValidator;
import hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModel;
import hu.blackbelt.judo.meta.liquibase.support.LiquibaseModelResourceSupport;
import hu.blackbelt.judo.meta.liquibase.validation.LiquibaseValidator;

import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

import static hu.blackbelt.judo.meta.liquibase.support.LiquibaseModelResourceSupport.liquibaseModelResourceSupportBuilder;
import static hu.blackbelt.judo.meta.liquibase.util.builder.LiquibaseBuilders.*;

/**
 * Performance tests for Liquibase validation.
 *
 * <p>Tests validation performance with large models (10,000+ elements)
 * to ensure the Java validator performs well at scale.</p>
 */
public class LiquibaseValidationPerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(LiquibaseValidationPerformanceTest.class);
    private static final String MODEL_NAME = "urn:liquibase.performance-test";

    // Test configuration
    private static final int NUM_CHANGE_SETS = 1000;
    private static final int COLUMNS_PER_TABLE = 10;
    // Total elements = NUM_CHANGE_SETS * (1 ChangeSet + 1 CreateTable + COLUMNS_PER_TABLE Column)
    // = 1000 * 12 = 12,000 elements

    private LiquibaseModelResourceSupport liquibaseModelSupport;
    private LiquibaseModel liquibaseModel;

    @BeforeEach
    void setUp() {
        liquibaseModelSupport = liquibaseModelResourceSupportBuilder()
                .uri(URI.createURI(MODEL_NAME))
                .build();

        liquibaseModel = LiquibaseModel.buildLiquibaseModel()
                .liquibaseModelResourceSupport(liquibaseModelSupport)
                .name("performance-test")
                .build();
    }

    /**
     * Generate a large model with the specified number of ChangeSets,
     * each containing a CreateTable with multiple columns.
     */
    private void generateLargeModel(int numChangeSets, int columnsPerTable) {
        log.info("Generating large model with {} ChangeSets, {} columns per table...",
                numChangeSets, columnsPerTable);

        databaseChangeLog changeLog = newdatabaseChangeLogBuilder()
                .build();

        for (int i = 0; i < numChangeSets; i++) {
            // Create columns for the table
            Column[] columns = new Column[columnsPerTable];
            for (int j = 0; j < columnsPerTable; j++) {
                columns[j] = newColumnBuilder()
                        .withName("column_" + j)
                        .withType("VARCHAR(255)")
                        .build();
            }

            // Create a table with those columns
            CreateTable createTable = newCreateTableBuilder()
                    .withTableName("table_" + i)
                    .withSchemaName("test_schema")
                    .withColumn(columns)
                    .build();

            // Create a ChangeSet containing the table
            ChangeSet changeSet = newChangeSetBuilder()
                    .withId("changeset-" + i)
                    .withAuthor("performance-test")
                    .withCreateTable(createTable)
                    .build();

            changeLog.getChangeSet().add(changeSet);
        }

        liquibaseModel.getResource().getContents().add(changeLog);

        int totalElements = numChangeSets + // ChangeSets
                numChangeSets + // CreateTables
                (numChangeSets * columnsPerTable) + // Columns
                1; // DatabaseChangeLog
        log.info("Model generated with approximately {} elements", totalElements);
    }

    @Test
    void testJavaValidatorPerformance() throws Exception {
        generateLargeModel(NUM_CHANGE_SETS, COLUMNS_PER_TABLE);

        log.info("Running Java validation performance test...");
        try (BufferedSlf4jLogger bufferedLogger = new BufferedSlf4jLogger(log)) {
            long startTime = System.currentTimeMillis();

            // Run Java validation (sequential)
            LiquibaseValidator.validateLiquibase(
                    bufferedLogger,
                    liquibaseModel,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    false // sequential
            );

            long endTime = System.currentTimeMillis();
            long sequentialDuration = endTime - startTime;

            log.info("Java validation (sequential) completed in {} ms", sequentialDuration);

            // Run Java validation (parallel)
            startTime = System.currentTimeMillis();

            LiquibaseValidator.validateLiquibase(
                    bufferedLogger,
                    liquibaseModel,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    true // parallel
            );

            endTime = System.currentTimeMillis();
            long parallelDuration = endTime - startTime;

            log.info("Java validation (parallel) completed in {} ms", parallelDuration);
            log.info("Parallel speedup factor: {}",
                    parallelDuration > 0 ? (double) sequentialDuration / parallelDuration : "N/A");
        }
    }

    @Test
    void testEvlValidatorPerformance() throws Exception {
        generateLargeModel(NUM_CHANGE_SETS, COLUMNS_PER_TABLE);

        log.info("Running EVL validation performance test...");
        try (BufferedSlf4jLogger bufferedLogger = new BufferedSlf4jLogger(log)) {
            long startTime = System.currentTimeMillis();

            // Run EVL validation
            LiquibaseEpsilonValidator.validateLiquibase(
                    bufferedLogger,
                    liquibaseModel,
                    LiquibaseEpsilonValidator.calculateLiquibaseValidationScriptURI(),
                    Collections.emptyList(),
                    Collections.emptyList()
            );

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("EVL validation completed in {} ms", duration);
        }
    }

    @Test
    void testValidatorComparison() throws Exception {
        generateLargeModel(NUM_CHANGE_SETS, COLUMNS_PER_TABLE);

        log.info("Comparing Java vs EVL validator performance...");

        long javaTime = 0;
        long evlTime = 0;

        try (BufferedSlf4jLogger bufferedLogger = new BufferedSlf4jLogger(log)) {
            // Run Java validation
            long startTime = System.currentTimeMillis();
            LiquibaseValidator.validateLiquibase(
                    bufferedLogger,
                    liquibaseModel,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    false
            );
            javaTime = System.currentTimeMillis() - startTime;

            // Run EVL validation
            startTime = System.currentTimeMillis();
            LiquibaseEpsilonValidator.validateLiquibase(
                    bufferedLogger,
                    liquibaseModel,
                    LiquibaseEpsilonValidator.calculateLiquibaseValidationScriptURI(),
                    Collections.emptyList(),
                    Collections.emptyList()
            );
            evlTime = System.currentTimeMillis() - startTime;
        }

        log.info("============================================");
        log.info("Performance Comparison Results:");
        log.info("  Model size: ~{} elements", NUM_CHANGE_SETS * (2 + COLUMNS_PER_TABLE) + 1);
        log.info("  Java validator: {} ms", javaTime);
        log.info("  EVL validator:  {} ms", evlTime);
        if (evlTime > 0) {
            log.info("  Java speedup:   {}x", String.format("%.2f", (double) evlTime / javaTime));
        }
        log.info("============================================");
    }
}

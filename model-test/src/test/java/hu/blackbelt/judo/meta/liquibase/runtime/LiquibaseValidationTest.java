package hu.blackbelt.judo.meta.liquibase.runtime;

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

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.exceptions.EvlScriptExecutionException;
import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.liquibase.support.LiquibaseModelResourceSupport;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collection;

import static hu.blackbelt.judo.meta.liquibase.support.LiquibaseModelResourceSupport.liquibaseModelResourceSupportBuilder;

@Slf4j
public class LiquibaseValidationTest {

    private final String createdSourceModelName = "urn:Liquibase.model";

    LiquibaseModelResourceSupport liquibaseModelSupport;

    private LiquibaseModel liquibaseModel;

    @BeforeEach
    void setUp() {

        liquibaseModelSupport = liquibaseModelResourceSupportBuilder()
                .uri(URI.createURI(createdSourceModelName))
                .build();

        liquibaseModel = LiquibaseModel.buildLiquibaseModel()
        		.liquibaseModelResourceSupport(liquibaseModelSupport)
                .name("test")
                .build();
    }

    private void runEpsilon (Collection<String> expectedErrors, Collection<String> expectedWarnings) throws Exception {
        try (Log bufferedLogger = new BufferedSlf4jLogger(log)) {
            LiquibaseEpsilonValidator.validateLiquibase(bufferedLogger,
                    liquibaseModel,
                    LiquibaseEpsilonValidator.calculateLiquibaseValidationScriptURI(),
                    expectedErrors,
                    expectedWarnings);
        } catch (EvlScriptExecutionException ex) {
            log.error("EVL failed", ex);
            log.error("\u001B[31m - expected errors: {}\u001B[0m", expectedErrors);
            log.error("\u001B[31m - unexpected errors: {}\u001B[0m", ex.getUnexpectedErrors());
            log.error("\u001B[31m - errors not found: {}\u001B[0m", ex.getErrorsNotFound());
            log.error("\u001B[33m - expected warnings: {}\u001B[0m", expectedWarnings);
            log.error("\u001B[33m - unexpected warnings: {}\u001B[0m", ex.getUnexpectedWarnings());
            log.error("\u001B[33m - warnings not found: {}\u001B[0m", ex.getWarningsNotFound());
            throw ex;
        }
    }
}

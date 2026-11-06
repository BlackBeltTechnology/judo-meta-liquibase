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

import hu.blackbelt.epsilon.runtime.execution.exceptions.EvlScriptExecutionException;
import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.liquibase.AbstractLiquibaseValidationTest;
import hu.blackbelt.judo.meta.liquibase.ValidatorType;
import hu.blackbelt.judo.meta.liquibase.support.LiquibaseModelResourceSupport;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Collection;
import java.util.Collections;

import static hu.blackbelt.judo.meta.liquibase.support.LiquibaseModelResourceSupport.liquibaseModelResourceSupportBuilder;

/**
 * Validation tests for Liquibase model.
 *
 * <p>Uses parameterized tests to run validation against both EVL and Java validators,
 * ensuring parity between implementations.</p>
 */
@Slf4j
public class LiquibaseValidationTest extends AbstractLiquibaseValidationTest {

    /**
     * Test that an empty model passes validation with no errors.
     */
    @ParameterizedTest(name = "testEmptyModelValidation [{0}]")
    @EnumSource(ValidatorType.class)
    void testEmptyModelValidation(ValidatorType type) throws Exception {
        this.validatorType = type;
        initModel();

        // Empty model should have no validation errors
        runValidation(
                Collections.emptyList(),
                Collections.emptyList()
        );
    }
}

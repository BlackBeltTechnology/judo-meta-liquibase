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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModel.SaveArguments.liquibaseSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseNamespaceFixUriHandler.fixUriOutputStream;

public class LiquibaseModelStreamProvider {

    static Logger log = LoggerFactory.getLogger(LiquibaseModelStreamProvider.class);

    public static InputStream getStreamFromLiquibaseModel(LiquibaseModel liquibaseModel) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            liquibaseModel.saveLiquibaseModel(liquibaseSaveArgumentsBuilder()
                    .outputStream(fixUriOutputStream(byteArrayOutputStream)));
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        } catch (LiquibaseModel.LiquibaseValidationException | IOException e) {
            log.error("Liquibase error", e);
        }
        return null;
    }
}

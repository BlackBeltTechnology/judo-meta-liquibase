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

import hu.blackbelt.judo.zeta.common.ModelProvider;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Model provider implementation for Liquibase metamodel.
 *
 * <p>Provides model traversal operations for the Zeta validation framework
 * to access Liquibase model elements.</p>
 */
public class LiquibaseModelProvider implements ModelProvider {

    @Override
    public <T extends EObject> Collection<T> getAllContents(ResourceSet resourceSet, Class<T> type) {
        List<T> results = new ArrayList<>();
        for (Resource resource : resourceSet.getResources()) {
            EcoreUtil.getAllContents(resource, true).forEachRemaining(obj -> {
                if (type.isInstance(obj)) {
                    results.add(type.cast(obj));
                }
            });
        }
        return results;
    }
}

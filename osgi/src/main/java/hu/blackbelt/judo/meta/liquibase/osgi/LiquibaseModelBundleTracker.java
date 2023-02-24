package hu.blackbelt.judo.meta.liquibase.osgi;

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
import hu.blackbelt.osgi.utils.osgi.api.BundleCallback;
import hu.blackbelt.osgi.utils.osgi.api.BundleTrackerManager;
import hu.blackbelt.osgi.utils.osgi.api.BundleUtil;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.VersionRange;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModel.LoadArguments.liquibaseLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModel.loadLiquibaseModel;
import static java.util.Optional.ofNullable;

@Component(immediate = true)
@Slf4j
public class LiquibaseModelBundleTracker {

    public static final String LIQUIBASE_MODELS = "Liquibase-Models";

    @Reference
    BundleTrackerManager bundleTrackerManager;

    Map<String, ServiceRegistration<LiquibaseModel>> liquibaseModelRegistrations = new ConcurrentHashMap<>();

    Map<String, LiquibaseModel> liquibaseModels = new HashMap<>();

    @Activate
    public void activate(final ComponentContext componentContext) {
        bundleTrackerManager.registerBundleCallback(this.getClass().getName(),
                new LiquibaseRegisterCallback(componentContext.getBundleContext()),
                new LiquibaseUnregisterCallback(),
                new LiquibaseBundlePredicate());
    }

    @Deactivate
    public void deactivate(final ComponentContext componentContext) {
        bundleTrackerManager.unregisterBundleCallback(this.getClass().getName());
    }

    private static class LiquibaseBundlePredicate implements Predicate<Bundle> {
        @Override
        public boolean test(Bundle trackedBundle) {
            return BundleUtil.hasHeader(trackedBundle, LIQUIBASE_MODELS);
        }
    }

    private class LiquibaseRegisterCallback implements BundleCallback {

        BundleContext bundleContext;

        public LiquibaseRegisterCallback(BundleContext bundleContext) {
            this.bundleContext = bundleContext;
        }


        @Override
        public void accept(Bundle trackedBundle) {
            List<Map<String, String>> entries = BundleUtil.getHeaderEntries(trackedBundle, LIQUIBASE_MODELS);


            for (Map<String, String> params : entries) {
                String key = params.get(LiquibaseModel.NAME);
                if (liquibaseModelRegistrations.containsKey(key)) {
                    log.error("Liquibase model already loaded: " + key);
                } else {
                    // Unpack model
                    try {
                        LiquibaseModel liquibaseModel = loadLiquibaseModel(liquibaseLoadArgumentsBuilder()
                                .inputStream(trackedBundle.getEntry(params.get("file")).openStream())
                                .name(params.get(LiquibaseModel.NAME))
                                .version(trackedBundle.getVersion().toString()));

                        log.info("Registering Liquibase model: " + liquibaseModel);

                        ServiceRegistration<LiquibaseModel> modelServiceRegistration = bundleContext.registerService(LiquibaseModel.class, liquibaseModel, liquibaseModel.toDictionary());
                        liquibaseModels.put(key, liquibaseModel);
                        liquibaseModelRegistrations.put(key, modelServiceRegistration);

                    } catch (IOException | LiquibaseModel.LiquibaseValidationException e) {
                        log.error("Could not load Psm model: " + params.get(LiquibaseModel.NAME) + " from bundle: " + trackedBundle.getBundleId(), e);
                    }
                }
            }
        }

        @Override
        public Thread process(Bundle bundle) {
            return null;
        }
    }

    private class LiquibaseUnregisterCallback implements BundleCallback {

        @Override
        public void accept(Bundle trackedBundle) {
            List<Map<String, String>> entries = BundleUtil.getHeaderEntries(trackedBundle, LIQUIBASE_MODELS);
            for (Map<String, String> params : entries) {
                String key = params.get(LiquibaseModel.NAME);

                if (liquibaseModels.containsKey(key)) {
                    ServiceRegistration<LiquibaseModel> modelServiceRegistration = liquibaseModelRegistrations.get(key);

                    if (modelServiceRegistration != null) {
                        log.info("Unregistering Liquibase model: " + liquibaseModels.get(key));
                        modelServiceRegistration.unregister();
                        liquibaseModelRegistrations.remove(key);
                        liquibaseModels.remove(key);
                    }
                } else {
                    log.error("Liquibase Model is not registered: " + key);
                }
            }
        }

        @Override
        public Thread process(Bundle bundle) {
            return null;
        }
    }

}

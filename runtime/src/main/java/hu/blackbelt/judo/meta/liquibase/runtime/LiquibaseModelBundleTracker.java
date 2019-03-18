package hu.blackbelt.judo.meta.liquibase.runtime;

import hu.blackbelt.osgi.utils.osgi.api.BundleCallback;
import hu.blackbelt.osgi.utils.osgi.api.BundleTrackerManager;
import hu.blackbelt.osgi.utils.osgi.api.BundleUtil;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.osgi.framework.*;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModelLoader.loadLiquibaseModel;

@Component(immediate = true)
@Slf4j
public class LiquibaseModelBundleTracker {

    public static final String LIQUIBASEM_MODELS = "Liquibase-Models";

    @Reference
    BundleTrackerManager bundleTrackerManager;

    Map<String, ServiceRegistration<LiquibaseModel>> liquibaseModelRegistrations = new ConcurrentHashMap<>();

    Map<String, LiquibaseModel> liquibaseModels = new HashMap<>();

    @Activate
    public void activate(final ComponentContext componentContext) {
        bundleTrackerManager.registerBundleCallback(this.getClass().getName(),
                new LiquibaseRegisterCallback(componentContext.getBundleContext()),
                new LiquibaseUnregisterCallback(componentContext.getBundleContext()),
                new LiquibaseBundlePredicate());
    }

    @Deactivate
    public void deactivate(final ComponentContext componentContext) {
        bundleTrackerManager.unregisterBundleCallback(this.getClass().getName());
    }

    private static class LiquibaseBundlePredicate implements Predicate<Bundle> {
        @Override
        public boolean test(Bundle trackedBundle) {
            return BundleUtil.hasHeader(trackedBundle, LIQUIBASEM_MODELS);
        }
    }

    private class LiquibaseRegisterCallback implements BundleCallback {

        BundleContext bundleContext;

        public LiquibaseRegisterCallback(BundleContext bundleContext) {
            this.bundleContext = bundleContext;
        }

        @Override
        public void accept(Bundle trackedBundle) {
            List<Map<String, String>> entries = BundleUtil.getHeaderEntries(trackedBundle, LIQUIBASEM_MODELS);


            for (Map<String, String> params : entries) {
                String key = params.get(LiquibaseModel.NAME);
                if (liquibaseModelRegistrations.containsKey(key)) {
                    log.error("Model already loaded: " + key);
                } else {
                    if (params.containsKey(LiquibaseModel.META_VERSION_RANGE)) {
                        VersionRange versionRange = new VersionRange(params.get(LiquibaseModel.META_VERSION_RANGE).replaceAll("\"", ""));
                        if (versionRange.includes(bundleContext.getBundle().getVersion())) {
                            // Unpack model
                            try {
                                File file = BundleUtil.copyBundleFileToPersistentStorage(trackedBundle, key + ".judo-meta-liquibase", params.get("file"));
                                Version version = bundleContext.getBundle().getVersion();

                                // TODO: JNG-55 Copy mapping XLSX

                                LiquibaseModel liquibaseModel = loadLiquibaseModel(
                                        new ResourceSetImpl(),
                                        URI.createURI(file.getAbsolutePath()),
                                        params.get(LiquibaseModel.NAME),
                                        version.toString(),
                                        params.get(LiquibaseModel.CHECKSUM),
                                        versionRange.toString());

                                log.info("Registering model: " + liquibaseModel);

                                ServiceRegistration<LiquibaseModel> modelServiceRegistration = bundleContext.registerService(LiquibaseModel.class, liquibaseModel, liquibaseModel.toDictionary());
                                liquibaseModels.put(key, liquibaseModel);
                                liquibaseModelRegistrations.put(key, modelServiceRegistration);

                            } catch (IOException e) {
                                log.error("Could not load model: " + params.get(LiquibaseModel.NAME) + " from bundle: " + trackedBundle.getBundleId());
                            }
                        }
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
        BundleContext bundleContext;

        public LiquibaseUnregisterCallback(BundleContext bundleContext) {
            this.bundleContext = bundleContext;
        }

        @Override
        public void accept(Bundle trackedBundle) {
            List<Map<String, String>> entries = BundleUtil.getHeaderEntries(trackedBundle, LIQUIBASEM_MODELS);
            for (Map<String, String> params : entries) {
                String key = params.get(LiquibaseModel.NAME);

                if (liquibaseModels.containsKey(key)) {
                    ServiceRegistration<LiquibaseModel> modelServiceRegistration = liquibaseModelRegistrations.get(key);

                    if (modelServiceRegistration != null) {
                        log.info("Unregistering moodel: " + liquibaseModels.get(key));
                        modelServiceRegistration.unregister();
                        liquibaseModelRegistrations.remove(key);
                        liquibaseModels.remove(key);
                    }
                } else {
                    log.error("Model is not registered: " + key);
                }
            }
        }

        @Override
        public Thread process(Bundle bundle) {
            return null;
        }
    }

}

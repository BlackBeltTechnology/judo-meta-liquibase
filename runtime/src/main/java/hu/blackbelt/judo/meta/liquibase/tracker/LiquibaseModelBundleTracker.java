package hu.blackbelt.judo.meta.liquibase.tracker;

import hu.blackbelt.judo.meta.liquibase.LiquibaseMetaModel;
import hu.blackbelt.judo.meta.liquibase.LiquibaseModelInfo;
import hu.blackbelt.osgi.utils.osgi.api.BundleCallback;
import hu.blackbelt.osgi.utils.osgi.api.BundleTrackerManager;
import hu.blackbelt.osgi.utils.osgi.api.BundleUtil;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
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

@Component(immediate = true)
@Slf4j
public class LiquibaseModelBundleTracker {

    public static final String EXPRESSION_MODELS = "Expression-Models";

    @Reference
    BundleTrackerManager bundleTrackerManager;

    @Reference
    LiquibaseMetaModel expressionMetaModel;

    Map<String, ServiceRegistration<LiquibaseModelInfo>> expressionRegistrations = new ConcurrentHashMap<>();
    Map<String, LiquibaseModelInfo> expressionModels = new HashMap<>();

    @Activate
    public void activate(final ComponentContext componentContext) {
        bundleTrackerManager.registerBundleCallback(this.getClass().getName(),
                new ExpressionRegisterCallback(componentContext.getBundleContext()),
                new ExpressionUnregisterCallback(componentContext.getBundleContext()),
                new ExpressionBundlePredicate());
    }

    @Deactivate
    public void deactivate(final ComponentContext componentContext) {
        bundleTrackerManager.unregisterBundleCallback(this.getClass().getName());
    }

    private static class ExpressionBundlePredicate implements Predicate<Bundle> {
        @Override
        public boolean test(Bundle trackedBundle) {
            return BundleUtil.hasHeader(trackedBundle, EXPRESSION_MODELS);
        }
    }

    private class ExpressionRegisterCallback implements BundleCallback {

        BundleContext bundleContext;

        public ExpressionRegisterCallback(BundleContext bundleContext) {
            this.bundleContext = bundleContext;
        }

        @Override
        public void accept(Bundle trackedBundle) {
            List<Map<String, String>> entries = BundleUtil.getHeaderEntries(trackedBundle, EXPRESSION_MODELS);
            for (Map<String, String> params : entries) {
                if (params.containsKey(LiquibaseModelInfo.META_VERSION)) {
                    VersionRange versionRange = new VersionRange(params.get(LiquibaseModelInfo.META_VERSION).replaceAll("\"", ""));
                    if (versionRange.includes(bundleContext.getBundle().getVersion())) {

                        // Unpack model
                        try {
                            String key = trackedBundle.getBundleId() + "-" + params.get(LiquibaseModelInfo.NAME);

                            File file = BundleUtil.copyBundleFileToPersistentStorage(trackedBundle, key + ".model", params.get(LiquibaseModelInfo.FILE));

                            LiquibaseModelInfo expressionModelInfo = new LiquibaseModelInfo(
                                    file,
                                    params.get(LiquibaseModelInfo.NAME),
                                    new Version(params.get(LiquibaseModelInfo.VERSION)),
                                    URI.createURI(file.getAbsolutePath()),
                                    params.get(LiquibaseModelInfo.CHECKSUM),
                                    versionRange);

                            log.info("Registering model: " + expressionModelInfo);

                            ServiceRegistration<LiquibaseModelInfo> modelServiceRegistration = bundleContext.registerService(LiquibaseModelInfo.class, expressionModelInfo, expressionModelInfo.toDictionary());
                            expressionModels.put(key, expressionModelInfo);
                            expressionRegistrations.put(key, modelServiceRegistration);

                        } catch (IOException e) {
                            log.error("Could not load model: " + params.get(LiquibaseModelInfo.NAME) + " from bundle: " + trackedBundle.getBundleId());
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

    private class ExpressionUnregisterCallback implements BundleCallback {
        BundleContext bundleContext;

        public ExpressionUnregisterCallback(BundleContext bundleContext) {
            this.bundleContext = bundleContext;
        }

        @Override
        public void accept(Bundle trackedBundle) {
            List<Map<String, String>> entries = BundleUtil.getHeaderEntries(trackedBundle, EXPRESSION_MODELS);
            for (Map<String, String> params : entries) {
                VersionRange versionRange = new VersionRange(params.get(LiquibaseModelInfo.META_VERSION).replaceAll("\"", ""));
                if (params.containsKey(LiquibaseModelInfo.META_VERSION)) {
                    if (versionRange.includes(bundleContext.getBundle().getVersion())) {
                        String key = trackedBundle.getBundleId() + "-" + params.get(LiquibaseModelInfo.NAME);
                        ServiceRegistration<LiquibaseModelInfo> modelServiceRegistration = expressionRegistrations.get(key);

                        if (modelServiceRegistration != null) {
                            log.info("Unregistering moodel: " + expressionModels.get(key));
                            modelServiceRegistration.unregister();
                            expressionRegistrations.remove(key);
                            expressionModels.remove(key);
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
}

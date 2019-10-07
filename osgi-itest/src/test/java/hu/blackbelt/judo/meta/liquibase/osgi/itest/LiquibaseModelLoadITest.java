package hu.blackbelt.judo.meta.liquibase.osgi.itest;

import hu.blackbelt.epsilon.runtime.execution.impl.StringBuilderLogger;
import hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseEpsilonValidator;
import hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModel;
import hu.blackbelt.osgi.utils.osgi.api.BundleTrackerManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.log.LogService;

import javax.inject.Inject;
import java.io.*;

import static hu.blackbelt.judo.meta.liquibase.osgi.itest.LiquibaseKarafFeatureProvider.*;
import static org.junit.Assert.assertFalse;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;
import static org.ops4j.pax.tinybundles.core.TinyBundles.withBnd;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class LiquibaseModelLoadITest {

    private static final String DEMO = "northwind-liquibase";

    @Inject
    LogService log;

    @Inject
    protected BundleTrackerManager bundleTrackerManager;

    @Inject
    BundleContext bundleContext;

    @Inject
    LiquibaseModel liquibaseModel;

    @Configuration
    public Option[] config() throws FileNotFoundException {

        return combine(getRuntimeFeaturesForMetamodel(this.getClass()),
                mavenBundle(maven()
                        .groupId("hu.blackbelt.judo.meta")
                        .artifactId("hu.blackbelt.judo.meta.liquibase.osgi")
                        .versionAsInProject()),
                getProvisonModelBundle());
    }

    public Option getProvisonModelBundle() throws FileNotFoundException {
        return provision(
                getLiquibaseModelBundle()
        );
    }

    private InputStream getLiquibaseModelBundle() throws FileNotFoundException {
        return bundle()
                .add( "model/" + DEMO + ".judo-meta-liquibase",
                        new FileInputStream(new File(testTargetDir(getClass()).getAbsolutePath(),  "northwind.changelog.xml")))
                .set( Constants.BUNDLE_MANIFESTVERSION, "2")
                .set( Constants.BUNDLE_SYMBOLICNAME, DEMO + "-liquibase" )
                //set( Constants.IMPORT_PACKAGE, "meta/psm;version=\"" + getConfiguration(META_PSM_IMPORT_RANGE) +"\"")
                .set( "Liquibase-Models", "file=model/" + DEMO + ".judo-meta-liquibase;version=1.0.0;name=" + DEMO + ";checksum=notset;meta-version-range=\"[1.0.0,2)\"")
                .build( withBnd());
    }

    @Test
    public void testModelValidation() {
        StringBuilderLogger logger = new StringBuilderLogger(StringBuilderLogger.LogLevel.DEBUG);
        try {
            LiquibaseEpsilonValidator.validateLiquibase(logger,
                    liquibaseModel,
                    LiquibaseEpsilonValidator.calculateLiquibaseValidationScriptURI());

        } catch (Exception e) {
            log.log(LogService.LOG_ERROR, logger.getBuffer());
            assertFalse(true);
        }
    }
}
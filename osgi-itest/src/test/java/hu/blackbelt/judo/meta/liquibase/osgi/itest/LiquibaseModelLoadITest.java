package hu.blackbelt.judo.meta.liquibase.osgi.itest;

import static hu.blackbelt.judo.meta.liquibase.osgi.itest.KarafFeatureProvider.*;
import static hu.blackbelt.judo.meta.liquibase.util.builder.LiquibaseBuilders.newdatabaseChangeLogBuilder;
import static org.junit.Assert.assertFalse;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.provision;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;
import static org.ops4j.pax.tinybundles.core.TinyBundles.withBnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.eclipse.emf.common.util.URI;
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

import hu.blackbelt.epsilon.runtime.execution.impl.StringBuilderLogger;
import hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseEpsilonValidator;
import hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModel;
import hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModel.LiquibaseValidationException;
import hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModel.SaveArguments;
import hu.blackbelt.osgi.utils.osgi.api.BundleTrackerManager;

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
    public Option[] config() throws IOException, LiquibaseValidationException {

        return combine(karafConfig(this.getClass()),
                mavenBundle(maven()
                        .groupId("hu.blackbelt.judo.meta")
                        .artifactId("hu.blackbelt.judo.meta.liquibase.osgi")
                        .versionAsInProject()),
                getProvisonModelBundle());
    }

    public Option getProvisonModelBundle() throws IOException, LiquibaseValidationException {
        return provision(
                getLiquibaseModelBundle()
        );
    }

    private InputStream getLiquibaseModelBundle() throws IOException, LiquibaseValidationException {
        LiquibaseModel liquibaseModel = LiquibaseModel.buildLiquibaseModel()
                .name(DEMO)
                .uri(URI.createFileURI("test.model"))
                .build();

        liquibaseModel.addContent(
                newdatabaseChangeLogBuilder().build());

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        liquibaseModel.saveLiquibaseModel(SaveArguments.liquibaseSaveArgumentsBuilder().outputStream(os));
        return bundle()
                .add( "model/" + DEMO + ".judo-meta-liquibase",
                        new ByteArrayInputStream(os.toByteArray()))
                .set( Constants.BUNDLE_MANIFESTVERSION, "2")
                .set( Constants.BUNDLE_SYMBOLICNAME, DEMO + "-liquibase" )
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

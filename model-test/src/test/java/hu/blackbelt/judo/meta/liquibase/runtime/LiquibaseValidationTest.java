package hu.blackbelt.judo.meta.liquibase.runtime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseUtils;
import hu.blackbelt.judo.meta.liquibase.support.LiquibaseModelResourceSupport;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.FileURIHandlerImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EvlExecutionContext.evlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;
import static hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModel.LoadArguments.liquibaseLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.liquibase.support.LiquibaseModelResourceSupport.liquibaseModelResourceSupportBuilder;


public class LiquibaseValidationTest {

    private static final Logger log = LoggerFactory.getLogger(LiquibaseValidationTest.class);
    private ExecutionContext executionContext;
    private LiquibaseUtils liquibaseUtils;

    @BeforeEach
    void setUp() throws IOException, LiquibaseModel.LiquibaseValidationException {

        LiquibaseModel liquibaseModel = LiquibaseModel.loadLiquibaseModel(liquibaseLoadArgumentsBuilder()
                .uriHandler(new LiquibaseNamespaceFixUriHandler(new FileURIHandlerImpl()))
                .uri(URI.createFileURI(new File("target/test-classes/model/northwind-liquibase_hsqldb.changelog.xml").getAbsolutePath()))
                .name("test"));

        Log log = new Slf4jLog();

        liquibaseUtils = new LiquibaseUtils(liquibaseModel.getResourceSet(), false);

        // Execution context
        executionContext = executionContextBuilder()
                .log(log)
                .resourceSet(liquibaseModel.getResourceSet())
                .metaModels(ImmutableList.of())
                .modelContexts(ImmutableList.of(
                        wrappedEmfModelContextBuilder()
                                .log(log)
                                .name("LIQUIBASE")
                                .resource(liquibaseModel.getResource())
                                .build()))
                .injectContexts(ImmutableMap.of("liquibaseUtils", liquibaseUtils))
                .build();
    }

    @Test
    public void test() throws Exception {
        runEpsilon(ImmutableList.of(), null);
    }

    private void runEpsilon(Collection<String> expectedErrors, Collection<String> expectedWarnings) throws Exception {
        // run the model / metadata loading
        executionContext.load();

        // Transformation script
        executionContext.executeProgram(
                evlExecutionContextBuilder()
                        .source(new File("../model/src/main/epsilon/validations/liquibase.evl").toURI())
                        .expectedErrors(expectedErrors)
                        .expectedWarnings(expectedWarnings)
                        .build());

        executionContext.commit();
        executionContext.close();
    }
}

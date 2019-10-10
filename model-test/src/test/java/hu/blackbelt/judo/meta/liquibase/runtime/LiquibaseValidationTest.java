package hu.blackbelt.judo.meta.liquibase.runtime;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.exceptions.EvlScriptExecutionException;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.liquibase.support.LiquibaseModelResourceSupport;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.Collection;

import static hu.blackbelt.judo.meta.liquibase.support.LiquibaseModelResourceSupport.liquibaseModelResourceSupportBuilder;


public class LiquibaseValidationTest {

    private static final Logger logger = LoggerFactory.getLogger(LiquibaseValidationTest.class);
    private final String createdSourceModelName = "urn:Liquibase.model";

    LiquibaseModelResourceSupport liquibaseModelSupport;
    
    private LiquibaseModel liquibaseModel; 
    
    Log log = new Slf4jLog();

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
        try {
            LiquibaseEpsilonValidator.validateLiquibase(log,
                    liquibaseModel,
                    LiquibaseEpsilonValidator.calculateLiquibaseValidationScriptURI(),
                    expectedErrors,
                    expectedWarnings);
        } catch (EvlScriptExecutionException ex) {
            logger.error("EVL failed", ex);
            logger.error("\u001B[31m - expected errors: {}\u001B[0m", expectedErrors);
            logger.error("\u001B[31m - unexpected errors: {}\u001B[0m", ex.getUnexpectedErrors());
            logger.error("\u001B[31m - errors not found: {}\u001B[0m", ex.getErrorsNotFound());
            logger.error("\u001B[33m - expected warnings: {}\u001B[0m", expectedWarnings);
            logger.error("\u001B[33m - unexpected warnings: {}\u001B[0m", ex.getUnexpectedWarnings());
            logger.error("\u001B[33m - warnings not found: {}\u001B[0m", ex.getWarningsNotFound());
            throw ex;
        }
    }
}

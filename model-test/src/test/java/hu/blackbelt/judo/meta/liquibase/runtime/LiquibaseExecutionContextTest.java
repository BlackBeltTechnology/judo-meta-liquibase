package hu.blackbelt.judo.meta.liquibase.runtime;

import com.google.common.collect.ImmutableList;
import hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModel;
import hu.blackbelt.judo.meta.liquibase.support.LiquibaseModelResourceSupport;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static hu.blackbelt.judo.meta.liquibase.support.LiquibaseModelResourceSupport.liquibaseModelResourceSupportBuilder;
import static hu.blackbelt.judo.meta.liquibase.util.builder.LiquibaseBuilders.*;

class LiquibaseExecutionContextTest {

    @Test
    @DisplayName("Create Liquibase model with builder pattern")
    void testLiquibaseReflectiveCreated() throws Exception {


        String createdSourceModelName = "urn:liquibase.judo-meta-liquibase";

        LiquibaseModelResourceSupport liquibaseModelSupport = liquibaseModelResourceSupportBuilder().build();
        Resource liquibaseResource = liquibaseModelSupport.getResourceSet().createResource(
                URI.createFileURI(createdSourceModelName));

        // Build model here
    }
}
package hu.blackbelt.judo.meta.liquibase.runtime;

import hu.blackbelt.judo.meta.liquibase.support.LiquibaseModelResourceSupport;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.FileURIHandlerImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

import static hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModel.LoadArguments.loadArgumentsBuilder;

public class LiquibaseModelLoaderTest {

    static Logger log = LoggerFactory.getLogger(LiquibaseModelLoaderTest.class);
	
    @Test
    @DisplayName("Load Liquibase Model")
    void loadLiquibaseModel() throws IOException {
        ResourceSet liquibaseResourceSet = LiquibaseModelResourceSupport.createLiquibaseResourceSet();

        LiquibaseModel liquibaseModel = LiquibaseModel.loadLiquibaseModel(loadArgumentsBuilder()
                .uriHandler(Optional.of(new LiquibaseNamespaceFixUriHandler(new FileURIHandlerImpl())))
                .resourceSet(Optional.of(liquibaseResourceSet))
                .uri(URI.createFileURI(new File("src/test/model/test.liquibase").getAbsolutePath()))
                .name("test")
                .build());

        for (Iterator<EObject> i = liquibaseModel.getResourceSet().getResource(liquibaseModel.getUri(), false).getAllContents(); i.hasNext(); ) {
            log.info(i.next().toString());
        }

        liquibaseModel.saveLiquibaseModel(LiquibaseModel.SaveArguments.saveArgumentsBuilder().file(Optional.of(new File("target/test-classes/test_out.liquibase"))).build());
    }
}
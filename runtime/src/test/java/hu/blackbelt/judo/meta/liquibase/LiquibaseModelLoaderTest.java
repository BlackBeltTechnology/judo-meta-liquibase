package hu.blackbelt.judo.meta.liquibase;

import hu.blackbelt.epsilon.runtime.execution.impl.NioFilesystemnRelativePathURIHandlerImpl;
import hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModel;
import hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModelLoader;
import hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseNamespaceFixUriHandler;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIHandler;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Iterator;

import static hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModelLoader.createLiquibaseResourceSet;
import static hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModelLoader.saveLiquibaseModel;

@Slf4j
class LiquibaseModelLoaderTest {

    @Test
    void loadLiquibaseModel() throws IOException {
        URIHandler uriHandler = new LiquibaseNamespaceFixUriHandler(
                new NioFilesystemnRelativePathURIHandlerImpl("urn", FileSystems.getDefault(), targetDir().getAbsolutePath()));

        ResourceSet liquibaseResourceSet = createLiquibaseResourceSet(uriHandler);
        LiquibaseModel liquibaseModel = LiquibaseModelLoader.loadLiquibaseModel(liquibaseResourceSet,
                URI.createURI("urn:example.changelog.xml"),
                "test",
                "1.0.0");


        for (Iterator<EObject> i = liquibaseModel.getResourceSet().getResource(liquibaseModel.getUri(), false).getAllContents(); i.hasNext(); ) {
            log.info(i.next().toString());
        }

        saveLiquibaseModel(liquibaseModel);
    }


    public File targetDir(){
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath);
        if(!targetDir.exists()) {
            targetDir.mkdir();
        }
        return targetDir;
    }
}
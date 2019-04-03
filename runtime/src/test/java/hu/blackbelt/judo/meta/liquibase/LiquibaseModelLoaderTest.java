package hu.blackbelt.judo.meta.liquibase;

import hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModel;
import hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModelLoader;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

@Slf4j
class LiquibaseModelLoaderTest {

    @Test
    void loadLiquibaseModel() throws IOException {
        LiquibaseModel liquibaseModel = LiquibaseModelLoader.loadLiquibaseModel(
                URI.createURI(new File(srcDir(), "test/models/example.changelog.xml").getAbsolutePath()),
                "test",
                "1.0.0");


        for (Iterator<EObject> i = liquibaseModel.getResourceSet().getResource(liquibaseModel.getUri(), false).getAllContents(); i.hasNext(); ) {
            log.info(i.next().toString());
        }

        LiquibaseModelLoader.saveLiquibaseModel(liquibaseModel);
    }

    public File srcDir(){
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath+"../../src");
        if(!targetDir.exists()) {
            targetDir.mkdir();
        }
        return targetDir;
    }


}
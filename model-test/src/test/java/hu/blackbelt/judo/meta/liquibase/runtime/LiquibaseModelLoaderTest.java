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

import static hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModel.LoadArguments.liquibaseLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModel.SaveArguments.liquibaseSaveArgumentsBuilder;

public class LiquibaseModelLoaderTest {

    static Logger log = LoggerFactory.getLogger(LiquibaseModelLoaderTest.class);
	
    @Test
    @DisplayName("Load Liquibase Model")
    void loadLiquibaseModel() throws IOException, LiquibaseModel.LiquibaseValidationException {
        LiquibaseModel liquibaseModel = LiquibaseModel.loadLiquibaseModel(liquibaseLoadArgumentsBuilder()
                .uriHandler(new LiquibaseNamespaceFixUriHandler(new FileURIHandlerImpl()))
                .uri(URI.createFileURI(new File("src/test/model/test.liquibase").getAbsolutePath()))
                .name("test"));

        for (Iterator<EObject> i = liquibaseModel.getResource().getAllContents(); i.hasNext(); ) {
            log.info(i.next().toString());
        }

        liquibaseModel.saveLiquibaseModel(liquibaseSaveArgumentsBuilder()
                .file(new File("target/test-classes/test_out.liquibase")));

        // TODO: make it work
        /*
        // Executing on HSQLDB
        Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb", "SA", "");
        Database liquibaseDb = new HsqlDatabase();
        liquibaseDb.setConnection(new HsqlConnection(connection));
        Liquibase liquibase = new Liquibase(new File(targetDir(), this.liquibaseModel.getName() + ".changelog.xml").getAbsolutePath(), new FileSystemResourceAccessor(), liquibaseDb);
        liquibase.update("full,1.0.0");
        */
    }
}
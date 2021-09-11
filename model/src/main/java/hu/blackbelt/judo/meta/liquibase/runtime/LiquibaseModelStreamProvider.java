package hu.blackbelt.judo.meta.liquibase.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModel.SaveArguments.liquibaseSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseNamespaceFixUriHandler.fixUriOutputStream;

public class LiquibaseModelStreamProvider {

    static Logger log = LoggerFactory.getLogger(LiquibaseModelStreamProvider.class);

    static InputStream getStreamFromLiquibaseModel(LiquibaseModel liquibaseModel) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            liquibaseModel.saveLiquibaseModel(liquibaseSaveArgumentsBuilder()
                    .outputStream(fixUriOutputStream(byteArrayOutputStream)));
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        } catch (LiquibaseModel.LiquibaseValidationException | IOException e) {
            log.error("Liquibase error", e);
        }
        return null;
    }
}

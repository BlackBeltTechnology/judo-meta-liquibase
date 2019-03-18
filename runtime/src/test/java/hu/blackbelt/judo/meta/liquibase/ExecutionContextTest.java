package hu.blackbelt.judo.meta.liquibase;

import org.junit.jupiter.api.Test;

import java.io.File;

class ExecutionContextTest {

    @Test
    void testReflectiveCreated() throws Exception {

    }

    public File scriptDir(){
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath+"../../src/main");
        if(!targetDir.exists()) {
            targetDir.mkdir();
        }
        return targetDir;
    }

}
package hu.blackbelt.judo.meta.liquibase.runtime;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.UniqueEList;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class LiquibaseUtils {

    private static final Logger log = LoggerFactory.getLogger(LiquibaseUtils.class);

    private boolean failOnError;

    private ResourceSet resourceSet;

    public LiquibaseUtils(final ResourceSet resourceSet) {
        this(resourceSet, false);
    }

    public LiquibaseUtils(final ResourceSet resourceSet, final boolean failOnError) {
        this.resourceSet = resourceSet;
        this.failOnError = failOnError;

        // TODO: Processes here
    }

    public void setFailOnError(final boolean failOnError) {
        this.failOnError = failOnError;
    }
}

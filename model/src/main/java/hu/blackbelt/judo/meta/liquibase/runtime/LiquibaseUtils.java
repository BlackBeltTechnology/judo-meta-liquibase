package hu.blackbelt.judo.meta.liquibase.runtime;

import hu.blackbelt.judo.meta.liquibase.ChangeSet;
import hu.blackbelt.judo.meta.liquibase.support.LiquibaseModelResourceSupport;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class LiquibaseUtils {

    private static final Logger log = LoggerFactory.getLogger(LiquibaseUtils.class);

    private boolean failOnError;

    private ResourceSet resourceSet;

    private LiquibaseModelResourceSupport liquibaseModelResourceSupport;


    //////////////////////////////////////////////////
    ////////////////// CONSTRUCTOR ///////////////////
    //////////////////////////////////////////////////

    public LiquibaseUtils(final ResourceSet resourceSet) {
        this(resourceSet, false);
    }

    public LiquibaseUtils(final ResourceSet resourceSet, final boolean failOnError) {
        this.resourceSet = resourceSet;
        this.failOnError = failOnError;

        liquibaseModelResourceSupport = LiquibaseModelResourceSupport.liquibaseModelResourceSupportBuilder()
                .resourceSet(resourceSet)
                .build();
    }

    //////////////////////////////////////////////////
    ///////////////// OTHER METHODS //////////////////
    //////////////////////////////////////////////////

    public void setFailOnError(final boolean failOnError) {
        this.failOnError = failOnError;
    }

    //////////////////////////////////////////////////
    ////////////////// CHANGE SETS ///////////////////
    //////////////////////////////////////////////////

    /**
     * Get all ChangeSet
     *
     * @return all ChangeSet is exists
     */
    public Optional<EList<ChangeSet>> getChangeSets() {
        EList<ChangeSet> changeSets = new BasicEList<>();
        liquibaseModelResourceSupport.getStreamOfLiquibaseChangeSet().forEach(changeSets::add);
        return !changeSets.isEmpty()
                ? Optional.of(changeSets)
                : Optional.empty();
    }

    /**
     * Get a certain ChangeSet
     *
     * @param changeSetId ChangeSet to search for
     * @return Certain ChangeSet if exists
     */
    public Optional<ChangeSet> getChangeSet(String changeSetId) {
        return getChangeSets().isPresent()
                ? getChangeSets().get().stream().filter(e -> changeSetId.equals(e.getId())).findFirst()
                : Optional.empty();
    }


}

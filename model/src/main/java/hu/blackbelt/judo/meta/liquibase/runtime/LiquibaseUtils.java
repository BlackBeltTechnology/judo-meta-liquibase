package hu.blackbelt.judo.meta.liquibase.runtime;

import hu.blackbelt.judo.meta.liquibase.*;
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
        // TODO: test
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
    public Optional<ChangeSet> getChangeSet(final String changeSetId) {
        // TODO: test
        return getChangeSets().isPresent()
                ? getChangeSets().get().stream().filter(e -> changeSetId.equals(e.getId())).findFirst()
                : Optional.empty();
    }

    //////////////////////////////////////////////////
    ////////////////// CREATE TABLE //////////////////
    //////////////////////////////////////////////////

    /**
     * Get all CreateTable in certain ChangeSet
     *
     * @param changeSetId ChangeSet's id to search in
     * @return All CreateTable in given ChangeSet if exists
     */
    public Optional<EList<CreateTable>> getCreateTables(final String changeSetId) {
        // TODO: test
        final EList<CreateTable> createTables = getChangeSet(changeSetId).get().getCreateTable();
        return getChangeSet(changeSetId).isPresent() && !createTables.isEmpty()
                ? Optional.of(createTables)
                : Optional.empty();
    }

    /**
     * Get certain CreateTable in certain ChangeSet
     *
     * @param changeSetId ChangeSet's id to search in
     * @param tableName   CreateTable's tableName to search for
     * @return certain CreateTable in given ChangeSet if exists
     */
    public Optional<CreateTable> getCreateTable(final String changeSetId, final String tableName) {
        // TODO: test
        return getCreateTables(changeSetId).isPresent()
                ? getCreateTables(changeSetId).get().stream().filter(e -> tableName.equals(e.getTableName())).findFirst()
                : Optional.empty();
    }

    //////////////////////////////////////////////////
    //////////////////// COLUMNS /////////////////////
    //////////////////////////////////////////////////

    /**
     * Get all Columns in certain ChangeSet's CreateTable
     *
     * @param changeSetId     ChangeSet's id to search CreateTable in
     * @param createTableName CreateTable's name to search in
     * @return all Columns in given ChangeSet's CreateTable
     */
    public Optional<EList<Column>> getColumns(final String changeSetId, final String createTableName) {
        // TODO: test
        return getCreateTable(changeSetId, createTableName).isPresent() && !getCreateTable(changeSetId, createTableName).get().getColumn().isEmpty()
                ? Optional.of(getCreateTable(changeSetId, createTableName).get().getColumn())
                : Optional.empty();
    }

    /**
     * Get certain Column in certain ChangeSet's CreateTable
     *
     * @param changeSetId     ChangeSet's id to search CreateTable in
     * @param createTableName CreateTable's name to search in
     * @param columnName      Column's name to search for
     * @return certain Columns in given ChangeSet's CreateTable
     */
    public Optional<Column> getColumn(final String changeSetId, final String createTableName, final String columnName) {
        // TODO: test
        return getColumns(changeSetId, createTableName).isPresent()
                ? getColumns(changeSetId, createTableName).get().stream().filter(e -> columnName.equals(e.getName())).findFirst()
                : Optional.empty();
    }

    //////////////////////////////////////////////////
    ////////////////// PRIMARY KEYS //////////////////
    //////////////////////////////////////////////////

    /**
     * Get all AddPrimaryKey in certain ChangeSet
     *
     * @param changeSetId ChangeSet's id to search in
     * @return All AddPrimaryKey in given ChangeSet if exists
     */
    public Optional<EList<AddPrimaryKey>> getAddPrimaryKeys(final String changeSetId) {
        // TODO: test
        return getChangeSet(changeSetId).isPresent() && !getChangeSet(changeSetId).get().getAddPrimaryKey().isEmpty()
                ? Optional.of(getChangeSet(changeSetId).get().getAddPrimaryKey())
                : Optional.empty();
    }

    /**
     * Get certain AddPrimaryKey in certain ChangeSet
     *
     * @param changeSetId ChangeSet's id to search in
     * @param columnName  AddPrimaryKey's columnName to search for
     * @return certain AddPrimaryKey in given ChangeSet if exists
     */
    public Optional<AddPrimaryKey> getAddPrimaryKey(final String changeSetId, final String columnName) {
        // TODO: test
        return getAddPrimaryKeys(changeSetId).isPresent()
                ? getAddPrimaryKeys(changeSetId).get().stream().filter(e -> columnName.equals(e.getColumnNames())).findFirst()
                : Optional.empty();
    }

    //////////////////////////////////////////////////
    //////////// FOREIGN KEY CONSTRAINTS /////////////
    //////////////////////////////////////////////////

    /**
     * Get all AddForeignKeyConstraint in certain ChangeSet
     *
     * @param changeSetId ChangeSet's id to search in
     * @return All AddForeignKeyConstraint in given ChangeSet if exists
     */
    public Optional<EList<AddForeignKeyConstraint>> getAddForeignKeyConstraints(final String changeSetId) {
        // TODO: test
        return getChangeSet(changeSetId).isPresent() && !getChangeSet(changeSetId).get().getAddForeignKeyConstraint().isEmpty()
                ? Optional.of(getChangeSet(changeSetId).get().getAddForeignKeyConstraint())
                : Optional.empty();
    }

    /**
     * Get certain AddForeignKeyConstraint in certain ChangeSet
     *
     * @param changeSetId    ChangeSet's id to search in
     * @param constraintName AddForeignKeyConstraint's constraintName to search for
     * @return certain AddForeignKeyConstraint in given ChangeSet if exists
     */
    public Optional<AddForeignKeyConstraint> getAddForeignKeyConstraint(final String changeSetId, final String constraintName) {
        // TODO: test
        return getAddForeignKeyConstraints(changeSetId).isPresent()
                ? getAddForeignKeyConstraints(changeSetId).get().stream().filter(e -> constraintName.equals(e.getConstraintName())).findFirst()
                : Optional.empty();
    }

    //////////////////////////////////////////////////
    ////////////// NOT NULL CONSTRAINTS //////////////
    //////////////////////////////////////////////////

    /**
     * Get all AddNotNullConstraint in certain ChangeSet
     *
     * @param changeSetId ChangeSet's id to search in
     * @return All AddNotNullConstraint in given ChangeSet if exists
     */
    public Optional<EList<AddNotNullConstraint>> getAddNotNullConstraints(final String changeSetId) {
        // TODO: test
        return getChangeSet(changeSetId).isPresent() && !getChangeSet(changeSetId).get().getAddNotNullConstraint().isEmpty()
                ? Optional.of(getChangeSet(changeSetId).get().getAddNotNullConstraint())
                : Optional.empty();
    }

    /**
     * Get certain AddNotNullConstraint in certain ChangeSet
     *
     * @param changeSetId ChangeSet's id to search in
     * @param columnName  AddNotNullConstraint's columnName to search for
     * @return certain CreateAddNotNullConstraintTable in given ChangeSet if exists
     */
    public Optional<AddNotNullConstraint> getAddNotNullConstraint(final String changeSetId, final String columnName) {
        // TODO: test
        return getAddNotNullConstraints(changeSetId).isPresent()
                ? getAddNotNullConstraints(changeSetId).get().stream().filter(e -> columnName.equals(e.getColumnName())).findFirst()
                : Optional.empty();
    }

}

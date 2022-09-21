package hu.blackbelt.judo.meta.liquibase.runtime;

/*-
 * #%L
 * Judo :: Liquibase :: Model
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

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
    ////////////// DATA BASE CHANGE LOG //////////////
    //////////////////////////////////////////////////

    public Optional<databaseChangeLog> getDatabaseChangeLog() {
        return liquibaseModelResourceSupport.getStreamOfLiquibasedatabaseChangeLog().findFirst();
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
    public Optional<ChangeSet> getChangeSet(final String changeSetId) {
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
        return getChangeSet(changeSetId).isPresent() && !getChangeSet(changeSetId).get().getCreateTable().isEmpty()
               ? Optional.of(getChangeSet(changeSetId).get().getCreateTable())
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
     * @param tableName   Table's name to search primary key in
     * @return All AddPrimaryKey in given ChangeSet if exists
     */
    public Optional<EList<AddPrimaryKey>> getAddPrimaryKeys(final String changeSetId, final String tableName) {
        if (!getChangeSet(changeSetId).isPresent())
            return Optional.empty();
        final EList<AddPrimaryKey> addPrimaryKeys = new BasicEList<>();
        getChangeSet(changeSetId).get().getAddPrimaryKey().stream().filter(e -> tableName.equals(e.getTableName()))
                .forEach(addPrimaryKeys::add);
        return !addPrimaryKeys.isEmpty()
               ? Optional.of(addPrimaryKeys)
               : Optional.empty();
    }

    /**
     * Get certain AddPrimaryKey in certain ChangeSet
     *
     * @param changeSetId ChangeSet's id to search in
     * @param tableName   Table's name to search primary key in
     * @param columnName  AddPrimaryKey's columnName to search for
     * @return certain AddPrimaryKey in given ChangeSet if exists
     */
    public Optional<AddPrimaryKey> getAddPrimaryKey(final String changeSetId, final String tableName, final String columnName) {
        return getAddPrimaryKeys(changeSetId, tableName).isPresent()
               ? getAddPrimaryKeys(changeSetId, tableName).get().stream().filter(e -> columnName.equals(e.getColumnNames())).findFirst()
               : Optional.empty();
    }

    //////////////////////////////////////////////////
    //////////// FOREIGN KEY CONSTRAINTS /////////////
    //////////////////////////////////////////////////

    /**
     * Get all AddForeignKeyConstraint in certain ChangeSet
     *
     * @param changeSetId         ChangeSet's id to search in
     * @param baseTableName
     * @param referencedTableName
     * @return All AddForeignKeyConstraint in given ChangeSet if exists
     */
    public Optional<EList<AddForeignKeyConstraint>> getAddForeignKeyConstraints(
            final String changeSetId,
            final String baseTableName,
            final String referencedTableName) {
        EList<AddForeignKeyConstraint> addForeignKeyConstraints = new BasicEList<>();
        if (!getChangeSet(changeSetId).isPresent())
            return Optional.empty();
        getChangeSet(changeSetId).get().getAddForeignKeyConstraint().stream()
                .filter(e -> baseTableName.equals(e.getBaseTableName()) && referencedTableName.equals(e.getReferencedTableName()))
                .forEach(addForeignKeyConstraints::add);
        return !addForeignKeyConstraints.isEmpty()
               ? Optional.of(addForeignKeyConstraints)
               : Optional.empty();
    }

    /**
     * Get certain AddForeignKeyConstraint in certain ChangeSet
     *
     * @param changeSetId         ChangeSet's id to search in
     * @param baseTableName
     * @param referencedTableName
     * @param constraintName      AddForeignKeyConstraint's constraintName to search for
     * @return certain AddForeignKeyConstraint in given ChangeSet if exists
     */
    public Optional<AddForeignKeyConstraint> getAddForeignKeyConstraint(
            final String changeSetId,
            final String baseTableName,
            final String referencedTableName,
            final String constraintName) {
        return getAddForeignKeyConstraints(changeSetId, baseTableName, referencedTableName).isPresent()
               ? getAddForeignKeyConstraints(changeSetId, baseTableName, referencedTableName)
                       .get().stream().filter(e -> constraintName.equals(e.getConstraintName())).findFirst()
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
    public Optional<EList<AddNotNullConstraint>> getAddNotNullConstraints(final String changeSetId, final String tableName) {
        if(!getChangeSet(changeSetId).isPresent())
            return Optional.empty();
        EList<AddNotNullConstraint> addNotNullConstraints = new BasicEList<>();
        getChangeSet(changeSetId).get().getAddNotNullConstraint().stream()
                .filter(e -> tableName.equals(e.getTableName()))
                .forEach(addNotNullConstraints::add);
        return !addNotNullConstraints.isEmpty()
               ? Optional.of(addNotNullConstraints)
               : Optional.empty();
    }

    /**
     * Get certain AddNotNullConstraint in certain ChangeSet
     *
     * @param changeSetId ChangeSet's id to search in
     * @param columnName  AddNotNullConstraint's columnName to search for
     * @return certain CreateAddNotNullConstraintTable in given ChangeSet if exists
     */
    public Optional<AddNotNullConstraint> getAddNotNullConstraint(final String changeSetId, final String tableName, final String columnName) {
        return getAddNotNullConstraints(changeSetId, tableName).isPresent()
               ? getAddNotNullConstraints(changeSetId, tableName).get().stream().filter(e -> columnName.equals(e.getColumnName())).findFirst()
               : Optional.empty();
    }

}

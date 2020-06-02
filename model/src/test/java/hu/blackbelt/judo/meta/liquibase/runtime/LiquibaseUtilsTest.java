package hu.blackbelt.judo.meta.liquibase.runtime;

import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.liquibase.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.BasicEList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static hu.blackbelt.judo.meta.liquibase.util.builder.LiquibaseBuilders.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@Slf4j
public class LiquibaseUtilsTest {

    private static final String TARGET_TEST_CLASSES = "target/test-classes";
    private static final Slf4jLog logger = new Slf4jLog(log);

    private LiquibaseModel liquibaseModel;
    private LiquibaseUtils liquibaseUtils;

    @BeforeEach
    public void setup() {
        liquibaseModel = LiquibaseModel.buildLiquibaseModel()
                .name("TestLiquibaseModel")
                .build();

        liquibaseModel.addContent(
                newdatabaseChangeLogBuilder().build()
        );

        liquibaseUtils = new LiquibaseUtils(liquibaseModel.getResourceSet());
    }

    private void saveLiquibaseModel(final String testName) {
        try {
            liquibaseModel.saveLiquibaseModel(LiquibaseModel.SaveArguments.liquibaseSaveArgumentsBuilder()
                    .file(new File(TARGET_TEST_CLASSES, format("%s-liquibase.xml", testName)))
                    .build());
        } catch (IOException e) {
            logger.warn("Unable to save liquibase model");
        } catch (LiquibaseModel.LiquibaseValidationException e) {
            fail("Liquibase model is not valid", e);
        }
    }

    private void addContent(ChangeSet... changeSets) {
        if (changeSets == null || changeSets.length == 0)
            return;
        liquibaseUtils.getDatabaseChangeLog().get().getChangeSet().addAll(asList(changeSets));
    }

    @Test
    public void testGetChangeSets() {
        final String CHANGE_SET_ID = "ChangeSetId";
        final String CHANGE_SET1_ID = "ChangeSet1Id";

        // ASSERTIONS - check optional empty
        assertFalse(liquibaseUtils.getChangeSets().isPresent());
        assertFalse(liquibaseUtils.getChangeSet(CHANGE_SET_ID).isPresent());

        final ChangeSet changeSet = newChangeSetBuilder()
                .withId(CHANGE_SET_ID)
                .withAuthor("testGetChangeSets")
                .build();
        final ChangeSet changeSet1 = newChangeSetBuilder()
                .withId(CHANGE_SET1_ID)
                .withAuthor("testGetChangeSets")
                .build();

        addContent(changeSet, changeSet1);

        saveLiquibaseModel("testGetChangeSets");

        // ASSERTION - check getChangeSets
        assertEquals(
                new BasicEList<>(asList(changeSet, changeSet1)),
                liquibaseUtils.getChangeSets()
                        .orElseThrow(() -> new RuntimeException("No ChangeSet found"))
        );

        // ASSERTIONS - check getChangeSet
        assertEquals(
                changeSet,
                liquibaseUtils.getChangeSet(changeSet.getId())
                        .orElseThrow(() -> new RuntimeException(format("%s not found", changeSet.getId())))
        );
        assertEquals(
                changeSet1,
                liquibaseUtils.getChangeSet(changeSet1.getId())
                        .orElseThrow(() -> new RuntimeException(format("%s not found", changeSet1.getId())))
        );
    }

    @Test
    public void testGetCreateTable() {
        final String CHANGE_SET_ID = "ChangeSetID";
        final String TEST_TABLE_NAME = "TestTable";
        final String TEST_TABLE1_NAME = "TestTable1";
        final String CREATE_TABLE_NAME = "CreateTableName";

        // ASSERTIONS - check optional empty
        assertFalse(liquibaseUtils.getCreateTables(CHANGE_SET_ID).isPresent());
        assertFalse(liquibaseUtils.getCreateTable(CHANGE_SET_ID, CREATE_TABLE_NAME).isPresent());

        final ChangeSet changeSet = newChangeSetBuilder()
                .withId(CHANGE_SET_ID)
                .withAuthor("testGetCreateTable")
                .build();

        addContent(changeSet);

        // ASSERTIONS - check optional empty
        assertFalse(liquibaseUtils.getCreateTables(CHANGE_SET_ID).isPresent());
        assertFalse(liquibaseUtils.getCreateTable(CHANGE_SET_ID, CREATE_TABLE_NAME).isPresent());

        final CreateTable testTable = newCreateTableBuilder()
                .withTableName(TEST_TABLE_NAME)
                .withColumn(
                        newColumnBuilder()
                                .withName("_id")
                                .withType("ID")
                )
                .build();

        final CreateTable testTable1 = newCreateTableBuilder()
                .withTableName(TEST_TABLE1_NAME)
                .withColumn(
                        newColumnBuilder()
                                .withName("_id")
                                .withType("ID")
                )
                .build();

        changeSet.getCreateTable().addAll(asList(testTable, testTable1));

        addContent(changeSet);

        saveLiquibaseModel("testGetCreateTable");

        // ASSERTION - check getCreateTables
        assertEquals(
                new BasicEList<>(asList(testTable, testTable1)),
                liquibaseUtils.getCreateTables(changeSet.getId())
                        .orElseThrow(() -> new RuntimeException(format("%s ChangeSet not found", changeSet.getId())))
        );

        // ASSERTIONS - check getCreateTable
        assertEquals(
                testTable,
                liquibaseUtils.getCreateTable(changeSet.getId(), testTable.getTableName())
                        .orElseThrow(() -> new RuntimeException(format("%s not found", testTable.getTableName())))
        );
        assertEquals(
                testTable1,
                liquibaseUtils.getCreateTable(changeSet.getId(), testTable1.getTableName())
                        .orElseThrow(() -> new RuntimeException(format("%s not found", testTable1.getTableName())))
        );
    }

    @Test
    public void testGetColumns() {
        final String CHANGE_SET_ID = "ChangeSetID";
        final String TEST_TABLE_NAME = "TestTable";
        final String COLUMN_NAME = "column";
        final String ID_NAME = "_id";

        // ASSERTIONS - check optional empty
        assertFalse(liquibaseUtils.getColumns(CHANGE_SET_ID, TEST_TABLE_NAME).isPresent());
        assertFalse(liquibaseUtils.getColumn(CHANGE_SET_ID, TEST_TABLE_NAME, COLUMN_NAME).isPresent());

        final ChangeSet changeSet = newChangeSetBuilder()
                .withId(CHANGE_SET_ID)
                .withAuthor("testGetColumns")
                .build();

        // ASSERTIONS - check optional empty
        assertFalse(liquibaseUtils.getColumns(CHANGE_SET_ID, TEST_TABLE_NAME).isPresent());
        assertFalse(liquibaseUtils.getColumn(CHANGE_SET_ID, TEST_TABLE_NAME, COLUMN_NAME).isPresent());

        final Column id = newColumnBuilder()
                .withName(ID_NAME)
                .withType("ID")
                .build();
        final Column column = newColumnBuilder()
                .withName(COLUMN_NAME)
                .withType("UNKNOWN")
                .build();

        final CreateTable createTable = newCreateTableBuilder()
                .withTableName(TEST_TABLE_NAME)
                .withColumn(asList(id, column))
                .build();

        changeSet.getCreateTable().add(createTable);

        addContent(changeSet);

        saveLiquibaseModel("testGetColumns");

        // ASSERTION - check getColumns
        assertEquals(
                new BasicEList<>(asList(id, column)),
                liquibaseUtils.getColumns(changeSet.getId(), createTable.getTableName())
                        .orElseThrow(() -> new RuntimeException(format("%s, %s not found", changeSet.getId(), createTable.getTableName())))
        );

        // ASSERTIONS - check getColumn
        assertEquals(
                id,
                liquibaseUtils.getColumn(changeSet.getId(), createTable.getTableName(), id.getName())
                        .orElseThrow(() -> new RuntimeException(format("%s not found", id.getName())))
        );
        assertEquals(
                column,
                liquibaseUtils.getColumn(changeSet.getId(), createTable.getTableName(), column.getName())
                        .orElseThrow(() -> new RuntimeException(format("%s not found", column.getName())))
        );
    }

    @Test
    public void testGetAddPrimaryKeys() {
        // ASSERTIONS - check optional empty
        final String TEST_TABLE_NAME = "TestTable";
        final String TEST_TABLE1_NAME = "TestTable1";
        final String CHANGE_SET_ID = "ChangeSetID";
        final String ID_NAME = "_id";

        assertFalse(liquibaseUtils.getAddPrimaryKeys(CHANGE_SET_ID, TEST_TABLE_NAME).isPresent());
        assertFalse(liquibaseUtils.getAddPrimaryKey(CHANGE_SET_ID, TEST_TABLE_NAME, ID_NAME).isPresent());

        final AddPrimaryKey addPrimaryKey = newAddPrimaryKeyBuilder()
                .withTableName(TEST_TABLE_NAME)
                .withColumnNames(ID_NAME)
                .build();
        final AddPrimaryKey addPrimaryKey1 = newAddPrimaryKeyBuilder()
                .withTableName(TEST_TABLE1_NAME)
                .withColumnNames(ID_NAME)
                .build();

        final ChangeSet changeSet = newChangeSetBuilder()
                .withId(CHANGE_SET_ID)
                .withAuthor("testGetAddPrimaryKeys")
                .withAddPrimaryKey(addPrimaryKey1)
                .build();

        addContent(changeSet);

        // ASSERTIONS - check optional empty
        assertFalse(liquibaseUtils.getAddPrimaryKeys(CHANGE_SET_ID, TEST_TABLE_NAME).isPresent());
        assertFalse(liquibaseUtils.getAddPrimaryKey(CHANGE_SET_ID, TEST_TABLE_NAME, ID_NAME).isPresent());

        changeSet.getAddPrimaryKey().add(addPrimaryKey);

        addContent(changeSet);

        saveLiquibaseModel("testGetAddPrimaryKeys");

        // ASSERTION - check getAddPrimaryKeys
        assertEquals(
                new BasicEList<>(asList(addPrimaryKey)),
                liquibaseUtils.getAddPrimaryKeys(changeSet.getId(), TEST_TABLE_NAME)
                        .orElseThrow(() -> new RuntimeException(format("%s, %s not found", changeSet.getId(), TEST_TABLE_NAME)))
        );

        // ASSERTION - check getAddPrimaryKey
        assertEquals(
                addPrimaryKey,
                liquibaseUtils.getAddPrimaryKey(changeSet.getId(), TEST_TABLE_NAME, addPrimaryKey.getColumnNames())
                        .orElseThrow(() -> new RuntimeException(format("%s, %s not found", changeSet.getId(), TEST_TABLE_NAME)))
        );
    }

    @Test
    public void testGetAddForeignKeyConstraints() {
        final String CHANGE_SET_ID = "ChangeSetID";
        final String TEST_TABLE1_NAME = "TestTable1";
        final String TEST_TABLE2_NAME = "TestTable2";
        final String ID_NAME = "_id";

        // ASSERTIONS - check optional empty
        assertFalse(liquibaseUtils.getAddForeignKeyConstraints(
                CHANGE_SET_ID,
                TEST_TABLE1_NAME,
                TEST_TABLE2_NAME).isPresent());
        assertFalse(liquibaseUtils.getAddForeignKeyConstraint(
                CHANGE_SET_ID,
                TEST_TABLE1_NAME,
                TEST_TABLE2_NAME,
                TEST_TABLE2_NAME).isPresent());

        final AddForeignKeyConstraint addForeignKeyConstraint = newAddForeignKeyConstraintBuilder()
                .withBaseTableName(TEST_TABLE1_NAME)
                .withBaseColumnNames("TT2_fk")
                .withConstraintName(TEST_TABLE2_NAME)
                .withReferencedTableName(TEST_TABLE2_NAME)
                .withReferencedColumnNames(ID_NAME)
                .build();

        final AddForeignKeyConstraint addForeignKeyConstraint1 = newAddForeignKeyConstraintBuilder()
                .withBaseTableName("Apple")
                .withBaseColumnNames("Pear_fk")
                .withConstraintName("Pear")
                .withReferencedTableName("Pear")
                .withReferencedColumnNames(ID_NAME)
                .build();

        final ChangeSet changeSet = newChangeSetBuilder()
                .withId(CHANGE_SET_ID)
                .withAuthor("testGetAddForeignKeyConstraints")
                .withAddForeignKeyConstraint(addForeignKeyConstraint1)
                .build();

        addContent(changeSet);

        // ASSERTIONS - check optional empty
        assertFalse(liquibaseUtils.getAddForeignKeyConstraints(
                changeSet.getId(),
                TEST_TABLE1_NAME,
                TEST_TABLE2_NAME).isPresent());
        assertFalse(liquibaseUtils.getAddForeignKeyConstraint(
                changeSet.getId(),
                TEST_TABLE1_NAME,
                TEST_TABLE2_NAME,
                TEST_TABLE2_NAME).isPresent());

        changeSet.getAddForeignKeyConstraint().add(addForeignKeyConstraint);

        addContent(changeSet);

        saveLiquibaseModel("testGetAddForeignKeyConstraints");

        // ASSERTION - check getAddForeignKeyConstraints
        assertEquals(
                new BasicEList<>(asList(addForeignKeyConstraint)),
                liquibaseUtils.getAddForeignKeyConstraints(
                        changeSet.getId(),
                        TEST_TABLE1_NAME,
                        TEST_TABLE2_NAME)
                        .orElseThrow(() -> new RuntimeException(format("cs: %s, base: %s, ref: %s foreign key constraint not found",
                                changeSet.getId(), TEST_TABLE1_NAME, TEST_TABLE2_NAME)))
        );

        // ASSERTION - check getAddForeignKeyConstraint
        assertEquals(
                addForeignKeyConstraint,
                liquibaseUtils.getAddForeignKeyConstraint(
                        changeSet.getId(),
                        TEST_TABLE1_NAME,
                        TEST_TABLE2_NAME,
                        TEST_TABLE2_NAME)
                        .orElseThrow(() -> new RuntimeException(format("cs: %s, base: %s, ref: %s foreign key constraint not found",
                                changeSet.getId(), TEST_TABLE1_NAME, TEST_TABLE2_NAME)))
        );
    }

    @Test
    public void testGetAddNotNullConstraint() {
        final String CHANGE_SET_ID = "ChangeSetID";
        final String TEST_TABLE_NAME = "TestTable";
        final String TEST_TABLE1_NAME = "TestTable1";
        final String ID_NAME = "_id";

        // ASSERTIONS - check optional empty
        assertFalse(liquibaseUtils.getAddNotNullConstraints(CHANGE_SET_ID, TEST_TABLE_NAME).isPresent());
        assertFalse(liquibaseUtils.getAddNotNullConstraint(CHANGE_SET_ID, TEST_TABLE_NAME, ID_NAME).isPresent());

        final AddNotNullConstraint addNotNullConstraint = newAddNotNullConstraintBuilder()
                .withTableName(TEST_TABLE_NAME)
                .withColumnName(ID_NAME)
                .build();

        final AddNotNullConstraint addNotNullConstraint1 = newAddNotNullConstraintBuilder()
                .withTableName(TEST_TABLE1_NAME)
                .withColumnName(ID_NAME)
                .build();

        final ChangeSet changeSet = newChangeSetBuilder()
                .withId(CHANGE_SET_ID)
                .withAuthor("testGetAddNotNullConstraint")
                .withAddNotNullConstraint(asList(addNotNullConstraint1))
                .build();

        addContent(changeSet);

        // ASSERTIONS - check optional empty
        assertFalse(liquibaseUtils.getAddNotNullConstraints(changeSet.getId(), TEST_TABLE_NAME).isPresent());
        assertFalse(liquibaseUtils.getAddNotNullConstraint(changeSet.getId(), TEST_TABLE_NAME, ID_NAME).isPresent());

        changeSet.getAddNotNullConstraint().add(addNotNullConstraint);

        addContent(changeSet);

        // ASSERTION - check getAddNotNullConstraints
        assertEquals(
                new BasicEList<AddNotNullConstraint>(asList(addNotNullConstraint)),
                liquibaseUtils.getAddNotNullConstraints(changeSet.getId(), TEST_TABLE_NAME)
                        .orElseThrow(() -> new RuntimeException(format("cs %s, table %s not found", changeSet.getId(), TEST_TABLE_NAME)))
        );

        // ASSERTION - check getAddNotNullConstraint
        assertEquals(
                addNotNullConstraint,
                liquibaseUtils.getAddNotNullConstraint(changeSet.getId(), TEST_TABLE_NAME, ID_NAME)
                        .orElseThrow(() -> new RuntimeException(format("cs %s, table %s not found", changeSet.getId(), TEST_TABLE_NAME)))
        );

    }

}

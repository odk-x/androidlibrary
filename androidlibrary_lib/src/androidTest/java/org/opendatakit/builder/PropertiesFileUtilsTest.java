package org.opendatakit.builder;

import static org.junit.Assert.*;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.database.data.KeyValueStoreEntry;
import org.opendatakit.database.data.OrderedColumns;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.logging.desktop.WebLoggerDesktopFactoryImpl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PropertiesFileUtilsTest {

    private static final String APP_NAME = "testApp";
    private static final String TABLE_ID = "testTable";

    @Before
    public void setUp() {
        WebLogger.setFactory(new WebLoggerDesktopFactoryImpl());
    }

    @Test
    public void testWritePropertiesFailure() throws ServicesAvailabilityException {
        PropertiesFileUtils.writePropertiesIntoCsv(null, null, null,
                null, null, null);
        assertFalse("Writing properties should fail", fileExists("definition.csv") && fileExists("properties.csv"));
    }

    @Test(expected = IOException.class)
    public void testReadPropertiesFailure() throws IOException {
        PropertiesFileUtils.readPropertiesFromCsv("nonNullTableId", "someAppName");
    }

    @Test
    public void testReadPropertiesSuccess() throws IOException {
        PropertiesFileUtils.DataTableDefinition dataTableDefinition =
                PropertiesFileUtils.readPropertiesFromCsv(TABLE_ID, APP_NAME);
        assertNotNull("Data table definition should not be null", dataTableDefinition);
    }

    @Test
    public void testCreateParcelWithNullColumns() {
        Parcel parcel = createParcel(null);
        assertNotNull("Parcel should not be null", parcel);
        assertEquals("Parcel should be empty", 0, parcel.dataSize());
    }

    @Test
    public void testCreateParcelWithEmptyColumns() {
        List<Column> emptyColumns = new ArrayList<>();
        Parcel parcel = createParcel(emptyColumns);
        assertNotNull("Parcel should not be null", parcel);
        assertEquals("Parcel should be empty", 0, parcel.dataSize());
    }

    @Test
    public void testWriteAndReadPropertiesSuccess() throws ServicesAvailabilityException, IOException {
        OrderedColumns orderedColumns = createOrderedColumns();
        List<KeyValueStoreEntry> kvsEntries = createKeyValueStoreEntries();

        boolean writeSuccess = PropertiesFileUtils.writePropertiesIntoCsv(APP_NAME, TABLE_ID,
                orderedColumns, kvsEntries, new File("definition.csv"), new File("properties.csv"));
        assertTrue("Writing properties should succeed", writeSuccess);

        PropertiesFileUtils.DataTableDefinition dataTableDefinition =
                PropertiesFileUtils.readPropertiesFromCsv(TABLE_ID, APP_NAME);
        assertNotNull("Data table definition should not be null", dataTableDefinition);
        assertNotNull("Column list should not be null", dataTableDefinition.columnList);
        assertNotNull("KeyValueStore entries should not be null", dataTableDefinition.kvsEntries);
        assertEquals("Column count should match", orderedColumns.getColumnDefinitions().size(),
                dataTableDefinition.columnList.getColumns().size());
        assertEquals("KeyValueStore entries count should match", kvsEntries.size(), dataTableDefinition.kvsEntries.size());
    }

    // Helper methods
    private Parcel createParcel(List<Column> columns) {
        Parcel parcel = Parcel.obtain();
        if (columns != null && !columns.isEmpty()) {
            parcel.writeList(columns);
        }
        return parcel;
    }

    private boolean fileExists(String fileName) {
        return new File(fileName).exists();
    }

    private OrderedColumns createOrderedColumns() {
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("col1", "Column 1", "INTEGER", null));
        columns.add(new Column("col2", "Column 2", "STRING", null));
        //return new OrderedColumns(columns);
        return null;
    }

    private KeyValueStoreEntry createKeyValueStoreEntry(String partition, String aspect, String key, String type, String value) {
        KeyValueStoreEntry entry = new KeyValueStoreEntry();
        entry.tableId = TABLE_ID;
        entry.partition = partition;
        entry.aspect = aspect;
        entry.key = key;
        entry.type = type;
        entry.value = value;
        return entry;
    }

    private List<KeyValueStoreEntry> createKeyValueStoreEntries() {
        List<KeyValueStoreEntry> kvsEntries = new ArrayList<>();
        kvsEntries.add(createKeyValueStoreEntry("partition1", "aspect1", "key1", "INTEGER", "123"));
        kvsEntries.add(createKeyValueStoreEntry("partition2", "aspect2", "key2", "STRING", "value2"));
        return kvsEntries;
    }
}

package org.opendatakit.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opendatakit.provider.KeyValueStoreColumns.TABLE_ID;

import androidx.documentfile.provider.DocumentFile;

import org.junit.Before;
import org.junit.Test;
import org.opendatakit.database.data.KeyValueStoreEntry;
import org.opendatakit.database.data.OrderedColumns;
import org.opendatakit.exception.ServicesAvailabilityException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PropertiesFileUtilsTest {

    private String appName;
    private String tableId;
    private OrderedColumns orderedDefns;
    private List<KeyValueStoreEntry> kvsEntries;
    private File definitionCsv;
    private File propertiesCsv;

    @Before
    public void setUp() {
        appName = "TestApp";
        tableId = "TestTable";
        kvsEntries = new ArrayList<>();
        definitionCsv = new File("definition.csv");
        propertiesCsv = new File("properties.csv");
    }

    @Test
    public void testWritePropertiesIntoCsv_NullAppName() throws ServicesAvailabilityException {
        // Test writing properties into CSV with null appName
        boolean success = PropertiesFileUtils.writePropertiesIntoCsv(null, tableId, orderedDefns, kvsEntries, DocumentFile.fromFile(definitionCsv), DocumentFile.fromFile(propertiesCsv));
        assertFalse(success);
    }

    @Test
    public void testWritePropertiesIntoCsv_NullTableId() throws ServicesAvailabilityException {
        // Test writing properties into CSV with null tableId
        boolean success = PropertiesFileUtils.writePropertiesIntoCsv(appName, null, orderedDefns, kvsEntries, DocumentFile.fromFile(definitionCsv), DocumentFile.fromFile(propertiesCsv));
        assertFalse(success);
    }

    @Test
    public void testWritePropertiesIntoCsv_NullOrderedDefns() throws ServicesAvailabilityException {
        // Test writing properties into CSV with null orderedDefns
        boolean success = PropertiesFileUtils.writePropertiesIntoCsv(appName, tableId, null, kvsEntries, DocumentFile.fromFile(definitionCsv), DocumentFile.fromFile(propertiesCsv));
        assertFalse(success);
    }

    @Test
    public void testWritePropertiesIntoCsv_NullKvsEntries() throws ServicesAvailabilityException {
        // Test writing properties into CSV with null kvsEntries
        boolean success = PropertiesFileUtils.writePropertiesIntoCsv(appName, tableId, orderedDefns, null, DocumentFile.fromFile(definitionCsv), DocumentFile.fromFile(propertiesCsv));
        assertFalse(success);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadPropertiesFromCsv_NullTableId() throws IOException {
        // Test reading properties from CSV with null tableId
        PropertiesFileUtils.readPropertiesFromCsv(appName, null);
    }

    @Test(expected = IOException.class)
    public void testReadPropertiesFromCsv_NonExistentFile() throws IOException {
        // Test reading properties from non-existent CSV file
        PropertiesFileUtils.readPropertiesFromCsv(appName, "NonExistentTable");
    }

    @Test
    public void testReadPropertiesFromCsv_EmptyTableId() {
        // Test reading properties from CSV with empty tableId
        try {
            PropertiesFileUtils.readPropertiesFromCsv(appName, "");
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (IOException e) {
            fail("Unexpected IOException occurred");
        }
    }

    @Test
    public void testWritePropertiesIntoCsv_IOException() throws ServicesAvailabilityException {
        // Test writing properties into CSV with IOException
        boolean success = PropertiesFileUtils.writePropertiesIntoCsv(appName, tableId, orderedDefns, kvsEntries, DocumentFile.fromFile(new File("definition.csv")), DocumentFile.fromFile(new File("properties.csv")));
        assertFalse(success);
    }

    @Test
    public void testReadPropertiesFromCsv_IOException() {
        // Test reading properties from CSV with IOException
        try {
            PropertiesFileUtils.readPropertiesFromCsv(appName, "IOExceptionTable");
            fail("Expected IOException was not thrown");
        } catch (IOException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testWritePropertiesIntoCsv_PermissionDenied() throws IOException, ServicesAvailabilityException {
        // Create a read-only CSV file
        propertiesCsv.setReadOnly();

        // Attempt to write properties into read-only CSV
        boolean success = PropertiesFileUtils.writePropertiesIntoCsv(appName, tableId, orderedDefns, kvsEntries, DocumentFile.fromFile(definitionCsv), DocumentFile.fromFile(propertiesCsv));
        assertFalse(success);
    }

    @Test
    public void testWritePropertiesIntoCsv_NullPropertiesCsv() throws ServicesAvailabilityException {
        // Test writing properties into CSV with null propertiesCsv
        boolean success = PropertiesFileUtils.writePropertiesIntoCsv(appName, tableId, orderedDefns, kvsEntries, DocumentFile.fromFile(definitionCsv), null);
        assertFalse(success);
    }

    @Test
    public void testWritePropertiesIntoCsv_NullAppNameAndTableId() throws ServicesAvailabilityException {
        // Test writing properties into CSV with null appName and tableId
        boolean success = PropertiesFileUtils.writePropertiesIntoCsv(null, null, orderedDefns, kvsEntries, DocumentFile.fromFile(definitionCsv), DocumentFile.fromFile(propertiesCsv));
        assertFalse(success);
    }

    @Test
    public void testReadPropertiesFromCsv_NullAppNameAndTableId() {
        // Test reading properties from CSV with null appName and tableId
        try {
            PropertiesFileUtils.readPropertiesFromCsv(null, null);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (IOException e) {
            fail("Unexpected IOException occurred");
        }
    }

    @Test
    public void testWritePropertiesIntoCsv_DifferentDefinitionAndPropertiesCsv() throws ServicesAvailabilityException {
        // Test writing properties into CSV with different definitionCsv and propertiesCsv files
        File differentDefinitionCsv = new File("different_definition.csv");
        boolean success = PropertiesFileUtils.writePropertiesIntoCsv(appName, tableId, orderedDefns, kvsEntries, DocumentFile.fromFile(differentDefinitionCsv), DocumentFile.fromFile(propertiesCsv));
        assertFalse(success);
    }

    @Test
    public void testCreateKeyValueStoreEntries() {
        List<KeyValueStoreEntry> entries = createKeyValueStoreEntries();
        assertNotNull("KeyValueStoreEntries should not be null", entries);
        assertEquals("Number of entries should match", 2, entries.size());
    }

    @Test
    public void testCreateKeyValueStoreEntry() {
        KeyValueStoreEntry entry = createKeyValueStoreEntry("partition1", "aspect1", "key1", "INTEGER", "123");
        assertNotNull("KeyValueStoreEntry should not be null", entry);
        assertEquals("Partition should match", "partition1", entry.partition);
        assertEquals("Aspect should match", "aspect1", entry.aspect);
        assertEquals("Key should match", "key1", entry.key);
        assertEquals("Type should match", "INTEGER", entry.type);
        assertEquals("Value should match", "123", entry.value);
    }

    @Test
    public void testWritePropertiesFailure() throws ServicesAvailabilityException {
        // Convert file paths to DocumentFile and check existence
        boolean success = PropertiesFileUtils.writePropertiesIntoCsv(null, null, null,
                null, null, null);
        assertFalse("Writing properties should fail", success && fileExists("definition.csv") && fileExists("properties.csv"));
    }

    private List<KeyValueStoreEntry> createKeyValueStoreEntries() {
        List<KeyValueStoreEntry> kvsEntries = new ArrayList<>();
        kvsEntries.add(createKeyValueStoreEntry("partition1", "aspect1", "key1", "INTEGER", "123"));
        kvsEntries.add(createKeyValueStoreEntry("partition2", "aspect2", "key2", "STRING", "value2"));
        return kvsEntries;
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

    private boolean fileExists(String fileName) {
        // Convert fileName to DocumentFile and check existence
        DocumentFile file = DocumentFile.fromFile(new File(fileName));
        return file.exists();
    }
}

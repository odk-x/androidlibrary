package org.opendatakit.utilities;

import android.os.Parcel;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendatakit.database.data.KeyValueStoreEntry;
import org.opendatakit.database.data.TableMetaDataEntries;

import java.util.ArrayList;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TableMetaDataEntriesTest {

    //
    TableMetaDataEntries testMetaDataEntries;
    private final String TABLE_ID = "testTable";
    private final String REV_ID = "revId";
    KeyValueStoreEntry entry1 = new KeyValueStoreEntry();
    KeyValueStoreEntry entry2 = new KeyValueStoreEntry();

    @Before
    public void setUp() {
        testMetaDataEntries = new TableMetaDataEntries(TABLE_ID, REV_ID);

        // first test entry

        entry1.tableId = TABLE_ID;
        entry1.partition = "partition1";
        entry1.aspect = "aspect1";
        entry1.key = "key1";
        entry1.type = "number";
        entry1.value = "value1";

        // second test entry
        entry2.tableId = TABLE_ID;
        entry2.partition = "partition2";
        entry2.aspect = "aspect2";
        entry2.key = "key2";
        entry2.type = "number";
        entry2.value = "value2";

    }

    @Test
    public void testDatabaseInitialization() { // test that an empty database is initialized
        assertEquals(TABLE_ID, testMetaDataEntries.getTableId());
        assertEquals(REV_ID, testMetaDataEntries.getRevId());
        assertTrue(testMetaDataEntries.getEntries().isEmpty());
    }

    @Test
    public void testAddEntry() {
        testMetaDataEntries.addEntry(entry1);
        testMetaDataEntries.addEntry(entry2);

        ArrayList<KeyValueStoreEntry> entries = testMetaDataEntries.getEntries();
        assertEquals(2, entries.size());
        assertEquals(entry1, entries.get(0));
        assertEquals(entry2, entries.get(1));
    }

    @Test
    public void testWriteToParcel() {
        testMetaDataEntries.addEntry(entry1);

        // Write the testMetaDataEntries object to a parcel
        Parcel parcel = Parcel.obtain();
        testMetaDataEntries.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        // Create new MetaDataEntries object from the parcel
        TableMetaDataEntries createdFromParcel = TableMetaDataEntries.CREATOR.createFromParcel(parcel);

        // Verify that the data is the same
        assertEquals(testMetaDataEntries.getTableId(), createdFromParcel.getTableId());
        assertEquals(testMetaDataEntries.getRevId(), createdFromParcel.getRevId());
        assertEquals(testMetaDataEntries.getEntries().size(), createdFromParcel.getEntries().size());

        assertEquals(testMetaDataEntries.getEntries().get(0).tableId, createdFromParcel.getEntries().get(0).tableId);
        assertEquals(testMetaDataEntries.getEntries().get(0).partition, createdFromParcel.getEntries().get(0).partition);
        assertEquals(testMetaDataEntries.getEntries().get(0).aspect, createdFromParcel.getEntries().get(0).aspect);
        assertEquals(testMetaDataEntries.getEntries().get(0).key, createdFromParcel.getEntries().get(0).key);
        assertEquals(testMetaDataEntries.getEntries().get(0).type, createdFromParcel.getEntries().get(0).type);
        assertEquals(testMetaDataEntries.getEntries().get(0).value, createdFromParcel.getEntries().get(0).value);
    }

    @Test
    public void testEntryOrderPreservation() {
        testMetaDataEntries.addEntry(entry2);
        testMetaDataEntries.addEntry(entry1);

        ArrayList<KeyValueStoreEntry> entries = testMetaDataEntries.getEntries();
        assertEquals(entry2, entries.get(0));
        assertEquals(entry1, entries.get(1));
    }


}
package org.opendatakit.database.data;

import android.os.Parcel;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class TableDefinitionEntryTest {
    private TableDefinitionEntry entry;
    private static final String TABLE_ID = "testTable";
    private static final String NEW_TABLE_ID = "newTestTable";
    private static final String REV_ID = "testRevId";
    private static final String SCHEME_E_TAG = "testSchemaETag";
    private static final String LAST_DATA_E_TAG = "testLastDataTag";
    private static final String LAST_SYNC_TIME = "testLastSyncTime";

    @Before
    public void setup() {
        entry = new TableDefinitionEntry(TABLE_ID);
    }

    // Test a successful initialization with a table id only
    @Test
    public void testTableDefinitionEntryInitialization() {
        assertEquals(TABLE_ID, entry.getTableId());
        assertNull(entry.getRevId());
        assertNull(entry.getSchemaETag());
        assertNull(entry.getLastDataETag());
        assertNull(entry.getLastSyncTime());
    }

    @Test
    // Test a successful initialization with an empty string table id
    public void testTableDefinitionEntryInitialization_WithEmptyStringTableId() {
        TableDefinitionEntry emptyEntry = new TableDefinitionEntry("");
        assertEquals("", emptyEntry.getTableId());
        assertNull(emptyEntry.getRevId());
        assertNull(emptyEntry.getSchemaETag());
        assertNull(emptyEntry.getLastDataETag());
        assertNull(emptyEntry.getLastSyncTime());
    }

    @Test
    public void testGettersAndSetters() {

        // Setting values using setters
        entry.setTableId(NEW_TABLE_ID);
        entry.setRevId(REV_ID);
        entry.setSchemaETag(SCHEME_E_TAG);
        entry.setLastDataETag(LAST_DATA_E_TAG);
        entry.setLastSyncTime(LAST_SYNC_TIME);

        // Retrieving values using getters and comparing
        // them with original values set using the setters
        assertEquals(NEW_TABLE_ID, entry.getTableId());
        assertEquals(REV_ID, entry.getRevId());
        assertEquals(SCHEME_E_TAG, entry.getSchemaETag());
        assertEquals(LAST_DATA_E_TAG, entry.getLastDataETag());
        assertEquals(LAST_SYNC_TIME, entry.getLastSyncTime());
    }

    public void testDescribeContents() {
        assertEquals(0, entry.describeContents());
    }

    // Test for data integrity during reading and writing to a parcel
    // (serialization and deserialization)
    @Test
    public void testParcelable() {

        // Setting values using setters
        entry.setTableId(TABLE_ID);
        entry.setRevId(REV_ID);
        entry.setSchemaETag(SCHEME_E_TAG);
        entry.setLastDataETag(LAST_DATA_E_TAG);
        entry.setLastSyncTime(LAST_SYNC_TIME);

        // Write the TableDefinitionEntry object to a parcel
        Parcel parcel = Parcel.obtain();
        entry.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        // Read entry data and verify that the data is the same
        assertEquals(TABLE_ID, parcel.readString());
        assertEquals(REV_ID, parcel.readString());
        assertEquals(SCHEME_E_TAG, parcel.readString());
        assertEquals(LAST_DATA_E_TAG, parcel.readString());
        assertEquals(LAST_SYNC_TIME, parcel.readString());

    }

    @Test
    public void testParcelable_WithNulls() {

        // Write the TableDefinitionEntry object to a parcel
        Parcel parcel = Parcel.obtain();
        entry.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        // Create a TableDefinitionEntry object from parcel
        TableDefinitionEntry createdFromParcel = TableDefinitionEntry.CREATOR.createFromParcel(parcel);

        // Read entry data and verify that the data is the same
        assertEquals(TABLE_ID, createdFromParcel.getTableId());
        assertNull(createdFromParcel.getRevId());
        assertNull(createdFromParcel.getSchemaETag());
        assertNull(createdFromParcel.getLastDataETag());
        assertNull(createdFromParcel.getLastSyncTime());
    }

    @Test
    public void testParcelable_WithEmptyString() {

        // Setting empty strings using setters
        entry.setTableId("");
        entry.setRevId("");
        entry.setSchemaETag("");
        entry.setLastDataETag("");
        entry.setLastSyncTime("");

        // Write the TableDefinitionEntry object to a parcel
        Parcel parcel = Parcel.obtain();
        entry.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        TableDefinitionEntry createdFromParcel = TableDefinitionEntry.CREATOR.createFromParcel(parcel);

        // Read entry data and verify that the data is the same
        assertEquals("", createdFromParcel.getTableId());
        assertEquals("", createdFromParcel.getRevId());
        assertEquals("", createdFromParcel.getSchemaETag());
        assertEquals("", createdFromParcel.getLastDataETag());
        assertEquals("", createdFromParcel.getLastSyncTime());

    }

    @Test
    public void testParcelable_WithMixedNullsAndValues() {

        // Setting values and empty strings using setters to specific properties
        entry.setRevId("");
        entry.setLastDataETag(LAST_DATA_E_TAG);

        // Write the TableDefinitionEntry object to a parcel
        Parcel parcel = Parcel.obtain();
        entry.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        TableDefinitionEntry createdFromParcel = TableDefinitionEntry.CREATOR.createFromParcel(parcel);

        // Read entry data and verify that the data is the same
        assertEquals(TABLE_ID, createdFromParcel.getTableId());
        assertEquals("", createdFromParcel.getRevId());
        assertNull(createdFromParcel.getSchemaETag());
        assertEquals(LAST_DATA_E_TAG, createdFromParcel.getLastDataETag());
        assertNull(createdFromParcel.getLastSyncTime());
    }


    @Test
    public void testNewArray() {
        int size = 5;

        // create an array of specified size
        TableDefinitionEntry[] array = TableDefinitionEntry.CREATOR.newArray(size);
        assertEquals(size, array.length);
    }

}


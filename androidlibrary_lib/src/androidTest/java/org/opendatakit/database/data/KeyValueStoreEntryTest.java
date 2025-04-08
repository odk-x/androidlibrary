package org.opendatakit.database.data;

import static org.junit.Assert.*;

import android.os.Parcel;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@SuppressWarnings("ALL")
@RunWith(AndroidJUnit4.class)
public class KeyValueStoreEntryTest {

    private KeyValueStoreEntry entry1;
    private KeyValueStoreEntry entry2;

    // Constants class containing content to be used in tests
    private static class Constants {
        private static final String TABLE_ID = "testTable";

        // first test entry contents
        private static final String PARTITION1 = "partition1";
        private static final String ASPECT1 = "aspect1";
        private static final String KEY1 = "key1";
        private static final String TYPE1 = "number";
        private static final String VALUE1 = "value1";

        // second test entry contents
        private static final String PARTITION2 = "partition2";
        private static final String ASPECT2 = "aspect2";
        private static final String KEY2 = "key2";
        private static final String TYPE2 = "number";
        private static final String VALUE2 = "value2";
    }

    // Test that the KeyValueStoreEntry is initialized correctly and is empty
    @Test
    public void testKeyValueStoreEntryInitialization() {
        entry1 = new KeyValueStoreEntry();
        assertNull(entry1.tableId);
        assertNull(entry1.partition);
        assertNull(entry1.aspect);
        assertNull(entry1.key);
        assertNull(entry1.type);
        assertNull(entry1.value);
    }

    @Before
    public void setUp() {
        // First test entry
        entry1 = new KeyValueStoreEntry();
        entry1.tableId = Constants.TABLE_ID;
        entry1.partition = Constants.PARTITION1;
        entry1.aspect = Constants.ASPECT1;
        entry1.key = Constants.KEY1;
        entry1.type = Constants.TYPE1;
        entry1.value = Constants.VALUE1;

        // Second test entry
        entry2 = new KeyValueStoreEntry();
        entry2.tableId = Constants.TABLE_ID;
        entry2.partition = Constants.PARTITION2;
        entry2.aspect = Constants.ASPECT2;
        entry2.key = Constants.KEY2;
        entry2.type = Constants.TYPE2;
        entry2.value = Constants.VALUE2;
    }

    @Test
    public void testToString() {
        String str = entry1.toString();
        assertTrue(str.contains(Constants.TABLE_ID));
        assertTrue(str.contains(Constants.PARTITION1));
        assertTrue(str.contains(Constants.ASPECT1));
        assertTrue(str.contains(Constants.KEY1));
        assertTrue(str.contains(Constants.TYPE1));
        assertTrue(str.contains(Constants.VALUE1));
    }

    @Test
    public void testHashCodeConsistency() {

        Parcel parcel = Parcel.obtain();
        entry1.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        // Create a new KeyStoreValueEntry object from the parcel
        KeyValueStoreEntry createdFromParcel = new KeyValueStoreEntry(parcel);

        // Checking hash code consistency.
        // If the data contained each KeyValueStoreEntry object
        // is the same, the hash codes should be the same
        assertEquals(entry1.hashCode(), createdFromParcel.hashCode());
    }

    @Test
    public void testHashCodeDifference() {
        // Checking hash code consistency.
        // If the data contained within each KeyValueStoreEntry object is
        // different, then the hash codes should be the different as well.
        assertNotEquals(entry1.hashCode(), entry2.hashCode());
    }

    @Test
    public void testEqualsNull() {
        // Test for equality against null objects
        KeyValueStoreEntry key = null;
        assertFalse(entry1.equals(key));
    }

    @Test
    public void testEqualsSameObject() {
        // Test same instance of an object for equality
        assertTrue(entry1.equals(entry1));
    }

    @Test
    public void testEqualsDifferentObject() {
        // Test different objects of the same type for equality
        assertFalse(entry1.equals(entry2));
    }

    @Test
    public void testEqualsTypeMismatch() {
        String str = "string";
        // Test for equality against an object of a different type
        assertFalse(entry1.equals(str));
    }

    @Test
    public void testCompareToPartition() {
        // Test for partition comparison

        // entry1 should be placed before entry2
        assertTrue(entry1.compareTo(entry2) < 0);

        // entry2 should be placed after entry1
        assertTrue(entry2.compareTo(entry1) > 0);
    }

    @Test
    public void testCompareToAspect() {
        // Test for aspect comparison

        // Modifying entry2 to have the same partition
        // as entry1 and a different aspect
        entry2.partition = Constants.PARTITION1;

        // entry1 should be placed before entry2
        assertTrue(entry1.compareTo(entry2) < 0);

        // entry2 should be placed after entry1
        assertTrue(entry2.compareTo(entry1) > 0);
    }

    @Test
    public void testCompareToKey() {
        // Test for key comparison

        // Modifying entry2 to have the same partition
        // and aspect as entry1 and only a different key
        entry2.partition = Constants.PARTITION1;
        entry2.aspect = Constants.ASPECT1;

        // entry1 should be placed before entry2
        assertTrue(entry1.compareTo(entry2) < 0);

        // entry2 should be placed after entry1
        assertTrue(entry2.compareTo(entry1) > 0);
    }

    @Test
    public void testCompareToEquality() {
        // Test for equality comparison

        // Modifying entry2 to have the same partition
        // and aspect and key as entry1
        entry2.partition = Constants.PARTITION1;
        entry2.aspect = Constants.ASPECT1;
        entry2.key = Constants.KEY1;

        // Order should not matter for in an equality test
        // entry1 should be equal to entry2
        assertEquals(0, entry1.compareTo(entry2));

        // entry2 should be equal to entry1
        assertEquals(0, entry2.compareTo(entry1));
    }

    @Test
    public void testDescribeContents() {
        assertEquals(0, entry1.describeContents());
    }

    // Test for data integrity during reading and writing to a parcel
    // (serialization and deserialization)
    @Test
    public void testWriteToParcel() {
        // Write the KeyStoreValueEntry object to a parcel
        Parcel parcel = Parcel.obtain();
        entry1.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        // Read entry data and verify that the data is the same
        assertEquals(Constants.TABLE_ID, parcel.readString());
        assertEquals(Constants.PARTITION1, parcel.readString());
        assertEquals(Constants.ASPECT1, parcel.readString());
        assertEquals(Constants.KEY1, parcel.readString());
        assertEquals(Constants.TYPE1, parcel.readString());
        assertEquals(Constants.VALUE1, parcel.readString());
    }

}


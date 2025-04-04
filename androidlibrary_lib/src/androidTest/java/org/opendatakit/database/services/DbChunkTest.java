package org.opendatakit.database.services;

import android.os.Parcel;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendatakit.database.service.DbChunk;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public class DbChunkTest {
    // Constants for test data
    private static final String STANDARD_TEST_DATA = "test parcelable data";
    private static final String NO_NEXT_ID_DATA = "test data no next id";
    private static final String NULL_THIS_ID_DATA = "test data null thisID";

    // Common test variables
    private byte[] standardData;
    private byte[] emptyData;
    private byte[] largeData;
    private UUID standardId;
    private UUID nextId;

    @Before
    public void setUp() {
        // Initialize common test data
        standardData = STANDARD_TEST_DATA.getBytes();
        emptyData = new byte[0];
        largeData = new byte[1024 * 1024]; // 1MB
        Arrays.fill(largeData, (byte)0x42);
        standardId = UUID.randomUUID();
        nextId = UUID.randomUUID();
    }

    /**
     * FEATURE: DbChunk with nextID should be correctly parceled and unparceled
     */
    @Test
    public void testParcelableWithNextId() {
        // Given a DbChunk with a nextID set
        DbChunk originalChunk = new DbChunk(standardData, standardId);
        originalChunk.setNextID(nextId);

        // When the chunk is written to a parcel and read back
        DbChunk reconstructedChunk = writeToParcelAndReadBack(originalChunk);

        // Then all fields should be preserved correctly
        assertArrayEquals("Data should match after parceling",
                originalChunk.getData(), reconstructedChunk.getData());
        assertEquals("ThisID should match after parceling",
                originalChunk.getThisID(), reconstructedChunk.getThisID());
        assertEquals("NextID should match after parceling",
                originalChunk.getNextID(), reconstructedChunk.getNextID());
        assertTrue("HasNextID should be true after parceling", reconstructedChunk.hasNextID());
    }

    /**
     * FEATURE: DbChunk without nextID should be correctly parceled and unparceled
     */
    @Test
    public void testParcelableWithoutNextId() {
        // Given a DbChunk without a nextID set
        byte[] testData = NO_NEXT_ID_DATA.getBytes();
        DbChunk originalChunk = new DbChunk(testData, standardId);

        // When the chunk is written to a parcel and read back
        DbChunk reconstructedChunk = writeToParcelAndReadBack(originalChunk);

        // Then all fields should be preserved correctly
        assertArrayEquals("Data should match after parceling",
                originalChunk.getData(), reconstructedChunk.getData());
        assertEquals("ThisID should match after parceling",
                originalChunk.getThisID(), reconstructedChunk.getThisID());
        assertNull("NextID should be null after parceling", reconstructedChunk.getNextID());
        assertFalse("HasNextID should be false after parceling", reconstructedChunk.hasNextID());
    }

    /**
     * FEATURE: DbChunk with null thisID should be correctly parceled and unparceled
     */
    @Test
    public void testParcelableWithNullThisID() {
        // Given a DbChunk with null thisID
        byte[] testData = NULL_THIS_ID_DATA.getBytes();
        DbChunk originalChunk = new DbChunk(testData, null);

        // When the chunk is written to a parcel and read back
        DbChunk reconstructedChunk = writeToParcelAndReadBack(originalChunk);

        // Then all fields should be preserved correctly
        assertArrayEquals("Data should match after parceling",
                originalChunk.getData(), reconstructedChunk.getData());
        assertNull("ThisID should be null after parceling", reconstructedChunk.getThisID());
    }

    /**
     * FEATURE: DbChunk with empty data should be correctly parceled and unparceled
     */
    @Test
    public void testParcelableWithEmptyData() {
        // Given a DbChunk with empty data
        DbChunk originalChunk = new DbChunk(emptyData, standardId);

        // When the chunk is written to a parcel and read back
        DbChunk reconstructedChunk = writeToParcelAndReadBack(originalChunk);

        // Then the empty data and other fields should be preserved correctly
        assertEquals("Empty data length should be preserved",
                0, reconstructedChunk.getData().length);
        assertEquals("ThisID should match after parceling",
                originalChunk.getThisID(), reconstructedChunk.getThisID());
    }

    /**
     * FEATURE: DbChunk with large data should be correctly parceled and unparceled
     */
    @Test
    public void testParcelableWithLargeData() {
        // Given a DbChunk with large data (1MB)
        DbChunk originalChunk = new DbChunk(largeData, standardId);

        // When the chunk is written to a parcel and read back
        DbChunk reconstructedChunk = writeToParcelAndReadBack(originalChunk);

        // Then the large data and other fields should be preserved correctly
        assertEquals("Large data length should be preserved",
                largeData.length, reconstructedChunk.getData().length);
        assertArrayEquals("Large data content should match",
                largeData, reconstructedChunk.getData());
        assertEquals("ThisID should match after parceling",
                originalChunk.getThisID(), reconstructedChunk.getThisID());
    }

    /**
     * FEATURE: Creating a DbChunk from a parcel with invalid data length should fail
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParcelWithInvalidDataLength() {
        // Given a parcel with invalid data length (-1)
        Parcel parcel = Parcel.obtain();
        parcel.writeInt(-1); // Write invalid data length
        parcel.setDataPosition(0);

        try {
            // When attempting to create a DbChunk from this parcel
            // Then an IllegalArgumentException should be thrown
            new DbChunk(parcel);
        } finally {
            parcel.recycle();
        }
    }

    /**
     * FEATURE: DbChunk CREATOR can create arrays of the correct size
     */
    @Test
    public void testCreatorNewArray() {
        // Given a size for a new array
        int arraySize = 5;

        // When creating an array using the CREATOR
        DbChunk[] chunks = DbChunk.CREATOR.newArray(arraySize);

        // Then the array should have the correct size
        assertNotNull("Array should not be null", chunks);
        assertEquals("Array length should match requested size", arraySize, chunks.length);
    }

    /**
     * FEATURE: DbChunk CREATOR can create empty arrays
     */
    @Test
    public void testCreatorNewArrayWithZeroLength() {
        // Given a zero size for a new array
        int arraySize = 0;

        // When creating an array using the CREATOR
        DbChunk[] chunks = DbChunk.CREATOR.newArray(arraySize);

        // Then an empty array should be created
        assertNotNull("Array should not be null", chunks);
        assertEquals("Array length should be 0", arraySize, chunks.length);
    }

    /**
     * Helper method to write a DbChunk to a parcel and read it back
     * @param originalChunk the DbChunk to write to a parcel
     * @return the reconstructed DbChunk read from the parcel
     */
    private DbChunk writeToParcelAndReadBack(DbChunk originalChunk) {
        Parcel parcel = null;
        try {
            parcel = Parcel.obtain();
            originalChunk.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            return DbChunk.CREATOR.createFromParcel(parcel);
        } finally {
            if (parcel != null) {
                parcel.recycle();
            }
        }
    }
}
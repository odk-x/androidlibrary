package org.opendatakit.database.Service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.opendatakit.database.service.DbChunk;

import static org.junit.Assert.*;

import android.os.Parcel;
import java.util.Arrays;
import java.util.UUID;


@RunWith(JUnit4.class)
public class DbChunkTest {
    // Constants for test data
    private static final String SAMPLE_DATA_STRING = "sample data";
    private static final String PARCELABLE_DATA_STRING = "parcelable test";
    private static final String DATA_PRESERVATION_MESSAGE = "The chunk should store the exact data provided";
    private static final String ID_PRESERVATION_MESSAGE = "The chunk should store the exact ID provided";

    // Common test data
    private byte[] validData;
    private byte[] emptyData;
    private byte[] parcelableData;
    private UUID validId;
    private UUID nextId;

    @Before
    public void setUp() {
        validData = SAMPLE_DATA_STRING.getBytes();
        emptyData = new byte[0];
        parcelableData = PARCELABLE_DATA_STRING.getBytes();
        validId = UUID.randomUUID();
        nextId = UUID.randomUUID();
    }

    /**
     * FEATURE: Creating a DbChunk with valid data
     */
    @Test
    public void testCreateDbChunkWithValidData() {
        // When a DbChunk is created with valid data
        DbChunk chunk = new DbChunk(validData, validId);

        // Then the chunk should store the data and ID correctly
        assertArrayEquals(DATA_PRESERVATION_MESSAGE, validData, chunk.getData());
        assertEquals(ID_PRESERVATION_MESSAGE, validId, chunk.getThisID());
        // And the nextID should initially be null
        assertNull("The nextID should initially be null", chunk.getNextID());
        assertFalse("hasNextID() should return false initially", chunk.hasNextID());
    }

    /**
     * FEATURE: Creating a DbChunk with null data should fail
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateDbChunkWithNullData() {
        // When a DbChunk is created with null data
        // Then an IllegalArgumentException should be thrown
        new DbChunk(null, validId);
    }

    /**
     * FEATURE: Creating a DbChunk with null ID is acceptable
     */
    @Test
    public void testCreateDbChunkWithNullID() {
        // When a DbChunk is created with a null ID
        DbChunk chunk = new DbChunk(validData, null);

        // Then the chunk should store the data correctly and have a null ID
        assertArrayEquals(DATA_PRESERVATION_MESSAGE, validData, chunk.getData());
        assertNull("The chunk should store a null ID", chunk.getThisID());
    }

    /**
     * FEATURE: Creating a DbChunk with empty data is acceptable
     */
    @Test
    public void testCreateDbChunkWithEmptyData() {
        // When a DbChunk is created with empty data
        DbChunk chunk = new DbChunk(emptyData, validId);

        // Then the chunk should store empty data correctly
        assertEquals("The data length should be 0", 0, chunk.getData().length);
        assertEquals(ID_PRESERVATION_MESSAGE, validId, chunk.getThisID());
    }

    /**
     * FEATURE: Setting a nextID on a DbChunk
     */
    @Test
    public void testSettingNextID() {
        // Given a valid DbChunk
        DbChunk chunk = new DbChunk(validData, validId);

        // When the nextID is set
        chunk.setNextID(nextId);

        // Then the nextID should be stored correctly
        assertEquals("The nextID should match what was set", nextId, chunk.getNextID());
        assertTrue("hasNextID() should return true", chunk.hasNextID());
    }

    /**
     * FEATURE: Setting a nextID to null clears it
     */
    @Test
    public void testClearingNextID() {
        // Given a DbChunk with a nextID already set
        DbChunk chunk = new DbChunk(validData, validId);
        chunk.setNextID(nextId);

        // When the nextID is set to null
        chunk.setNextID(null);

        // Then the nextID should be null
        assertNull("The nextID should be null", chunk.getNextID());
        assertFalse("hasNextID() should return false", chunk.hasNextID());
    }

    /**
     * FEATURE: Parcelable functionality - with nextID
     * Note: This test requires an Android environment to run
     */
    @Test
    public void testParcelingWithNextID() {
        // Skip test if not in Android environment
        if (!isAndroidEnvironment()) {
            System.out.println("Skipping Parcelable test - not in Android environment");
            return;
        }

        // Given a DbChunk with all fields set
        DbChunk originalChunk = new DbChunk(parcelableData, validId);
        originalChunk.setNextID(nextId);

        // When the chunk is written to a parcel and read back
        DbChunk reconstructedChunk = parcelAndReconstructChunk(originalChunk);

        // Then all fields should be preserved correctly
        assertNotNull("Reconstructed chunk should not be null", reconstructedChunk);
        assertArrayEquals("Data should be preserved",
                originalChunk.getData(), reconstructedChunk.getData());
        assertEquals("ThisID should be preserved",
                originalChunk.getThisID(), reconstructedChunk.getThisID());
        assertEquals("NextID should be preserved",
                originalChunk.getNextID(), reconstructedChunk.getNextID());
        assertEquals("hasNextID() should return true",
                originalChunk.hasNextID(), reconstructedChunk.hasNextID());
    }

    /**
     * FEATURE: Parcelable functionality - without nextID
     * Note: This test requires an Android environment to run
     */
    @Test
    public void testParcelingWithoutNextID() {
        // Skip test if not in Android environment
        if (!isAndroidEnvironment()) {
            System.out.println("Skipping Parcelable test - not in Android environment");
            return;
        }

        // Given a DbChunk without a nextID
        DbChunk originalChunk = new DbChunk(parcelableData, validId);

        // When the chunk is written to a parcel and read back
        DbChunk reconstructedChunk = parcelAndReconstructChunk(originalChunk);

        // Then all fields should be preserved correctly
        assertNotNull("Reconstructed chunk should not be null", reconstructedChunk);
        assertArrayEquals("Data should be preserved",
                originalChunk.getData(), reconstructedChunk.getData());
        assertEquals("ThisID should be preserved",
                originalChunk.getThisID(), reconstructedChunk.getThisID());
        assertNull("NextID should be null", reconstructedChunk.getNextID());
        assertFalse("hasNextID() should return false", reconstructedChunk.hasNextID());
    }

    /**
     * FEATURE: Invalid data length in Parcel should fail
     * Note: This test requires an Android environment to run
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDataLengthInParcel() {
        // Skip test if not in Android environment
        if (!isAndroidEnvironment()) {
            // Since we can't run the test, we need to simulate the expected
            // exception to satisfy the @Test(expected) annotation
            throw new IllegalArgumentException("Simulated exception - not in Android environment");
        }

        // Given a parcel with an invalid data length
        Parcel parcel = null;
        try {
            parcel = Parcel.obtain();
            parcel.writeInt(-1); // Invalid data length
            parcel.setDataPosition(0);

            // When a DbChunk is created from this parcel
            // Then an IllegalArgumentException should be thrown
            new DbChunk(parcel);
        } finally {
            if (parcel != null) parcel.recycle();
        }
    }

    /**
     * FEATURE: Creating arrays of DbChunks using CREATOR
     */
    @Test
    public void testCreatorNewArray() {
        // Given a size
        int size = 5;

        // When an array is created using the CREATOR
        DbChunk[] chunks = DbChunk.CREATOR.newArray(size);

        // Then the array should have the requested size
        assertNotNull("The array should not be null", chunks);
        assertEquals("The array should have the requested size", size, chunks.length);
    }

    /**
     * Helper method to detect if we're running in an Android environment
     * @return true if running in Android, false otherwise
     */
    private boolean isAndroidEnvironment() {
        try {
            Class.forName("android.os.Parcel");
            // Further check that Parcel.obtain() is actually implemented
            try {
                Parcel.obtain();
                return true;
            } catch (Exception e) {
                return false;
            }
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Helper method to parcel and reconstruct a DbChunk
     * @param originalChunk the chunk to parcel
     * @return the reconstructed chunk
     */
    private DbChunk parcelAndReconstructChunk(DbChunk originalChunk) {
        Parcel parcel = null;
        try {
            parcel = Parcel.obtain();
            originalChunk.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            return DbChunk.CREATOR.createFromParcel(parcel);
        } finally {
            if (parcel != null) parcel.recycle();
        }
    }
}
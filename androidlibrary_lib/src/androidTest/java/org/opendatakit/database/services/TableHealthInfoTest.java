package org.opendatakit.database.services;

import android.os.Parcel;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendatakit.database.service.TableHealthInfo;
import org.opendatakit.database.service.TableHealthStatus;

import static org.junit.Assert.*;

/**
 * Android Instrumented Tests for Parcelable functionality in TableHealthInfo
 * Place in src/androidTest/java/org/opendatakit/database/service/
 */
@RunWith(AndroidJUnit4.class)
public class TableHealthInfoTest {

    private static final String TEST_TABLE_ID = "test_table";
    private static final TableHealthStatus TEST_STATUS = TableHealthStatus.TABLE_HEALTH_IS_CLEAN;
    private static final boolean TEST_HAS_CHANGES = true;

    @Test
    public void testParcelable() {
        // Create test object
        TableHealthInfo testInfo = new TableHealthInfo(TEST_TABLE_ID, TEST_STATUS, TEST_HAS_CHANGES);

        // Write the object to a parcel
        Parcel parcel = Parcel.obtain();
        testInfo.writeToParcel(parcel, 0);

        // Reset the parcel for reading
        parcel.setDataPosition(0);

        // Create the object from the parcel
        TableHealthInfo fromParcel = TableHealthInfo.CREATOR.createFromParcel(parcel);
        parcel.recycle();

        // Verify the data is the same
        assertEquals(testInfo.getTableId(), fromParcel.getTableId());
        assertEquals(testInfo.getHealthStatus(), fromParcel.getHealthStatus());
        assertEquals(testInfo.hasChanges(), fromParcel.hasChanges());
    }

    @Test
    public void testParcelableWithAllStatuses() {
        // Test parceling with each possible enum value
        for (TableHealthStatus status : TableHealthStatus.values()) {
            TableHealthInfo info = new TableHealthInfo(TEST_TABLE_ID, status, TEST_HAS_CHANGES);

            // Write to parcel
            Parcel parcel = Parcel.obtain();
            info.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);

            // Read from parcel
            TableHealthInfo fromParcel = TableHealthInfo.CREATOR.createFromParcel(parcel);
            parcel.recycle();

            // Verify
            assertEquals(status, fromParcel.getHealthStatus());
        }
    }

    @Test
    public void testParcelableWithFalseChanges() {
        // Create a TableHealthInfo with hasChanges = false
        TableHealthInfo infoNoChanges = new TableHealthInfo(TEST_TABLE_ID, TEST_STATUS, false);

        // Write the object to a parcel
        Parcel parcel = Parcel.obtain();
        infoNoChanges.writeToParcel(parcel, 0);

        // Reset the parcel for reading
        parcel.setDataPosition(0);

        // Create the object from the parcel
        TableHealthInfo fromParcel = TableHealthInfo.CREATOR.createFromParcel(parcel);
        parcel.recycle();

        // Verify the data is the same
        assertFalse(fromParcel.hasChanges());
    }

    @Test
    public void testParcelableArray() {
        // Test the newArray method of CREATOR
        TableHealthInfo[] array = TableHealthInfo.CREATOR.newArray(5);
        assertNotNull(array);
        assertEquals(5, array.length);
    }

    @Test
    public void testDescribeContents() {
        TableHealthInfo testInfo = new TableHealthInfo(TEST_TABLE_ID, TEST_STATUS, TEST_HAS_CHANGES);
        assertEquals(0, testInfo.describeContents());
    }
}

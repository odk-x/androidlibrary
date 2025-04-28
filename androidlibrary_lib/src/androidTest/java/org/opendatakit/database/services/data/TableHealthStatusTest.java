package org.opendatakit.database.services.data;

import android.os.Parcel;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendatakit.database.service.TableHealthStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class TableHealthStatusTest {

    @Test
    public void testParcelableImplementation() {
        // Test each enum value for parcelability
        testParcelableForValue(TableHealthStatus.TABLE_HEALTH_IS_CLEAN);
        testParcelableForValue(TableHealthStatus.TABLE_HEALTH_HAS_CONFLICTS);
        testParcelableForValue(TableHealthStatus.TABLE_HEALTH_HAS_CHECKPOINTS);
        testParcelableForValue(TableHealthStatus.TABLE_HEALTH_HAS_CHECKPOINTS_AND_CONFLICTS);
    }

    private void testParcelableForValue(TableHealthStatus originalValue) {
        // Write the enum to a parcel
        Parcel parcel = Parcel.obtain();
        originalValue.writeToParcel(parcel, 0);

        // Reset the parcel position for reading
        parcel.setDataPosition(0);

        // Read the enum back using CREATOR
        TableHealthStatus createdFromParcel = TableHealthStatus.CREATOR.createFromParcel(parcel);

        // Verify the read value matches the original
        assertEquals(originalValue, createdFromParcel);

        // Clean up
        parcel.recycle();
    }

    @Test
    public void testCreatorNewArray() {
        // Test the newArray method of CREATOR
        TableHealthStatus[] array = TableHealthStatus.CREATOR.newArray(3);

        // Verify array properties
        assertEquals(3, array.length);
        for (TableHealthStatus status : array) {
            assertNull(status);
        }
    }
}
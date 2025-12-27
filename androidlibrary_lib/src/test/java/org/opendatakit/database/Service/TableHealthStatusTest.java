package org.opendatakit.database.Service;

import org.junit.Test;
import org.opendatakit.database.service.TableHealthStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TableHealthStatusTest {

    @Test
    public void testEnumValues() {
        // Verify all enum values are accessible
        TableHealthStatus[] values = TableHealthStatus.values();
        assertEquals(4, values.length);

        assertEquals(TableHealthStatus.TABLE_HEALTH_IS_CLEAN, values[0]);
        assertEquals(TableHealthStatus.TABLE_HEALTH_HAS_CONFLICTS, values[1]);
        assertEquals(TableHealthStatus.TABLE_HEALTH_HAS_CHECKPOINTS, values[2]);
        assertEquals(TableHealthStatus.TABLE_HEALTH_HAS_CHECKPOINTS_AND_CONFLICTS, values[3]);
    }

    @Test
    public void testValueOf() {
        // Verify valueOf works correctly for each enum value
        assertEquals(TableHealthStatus.TABLE_HEALTH_IS_CLEAN,
                TableHealthStatus.valueOf("TABLE_HEALTH_IS_CLEAN"));
        assertEquals(TableHealthStatus.TABLE_HEALTH_HAS_CONFLICTS,
                TableHealthStatus.valueOf("TABLE_HEALTH_HAS_CONFLICTS"));
        assertEquals(TableHealthStatus.TABLE_HEALTH_HAS_CHECKPOINTS,
                TableHealthStatus.valueOf("TABLE_HEALTH_HAS_CHECKPOINTS"));
        assertEquals(TableHealthStatus.TABLE_HEALTH_HAS_CHECKPOINTS_AND_CONFLICTS,
                TableHealthStatus.valueOf("TABLE_HEALTH_HAS_CHECKPOINTS_AND_CONFLICTS"));
    }

    @Test
    public void testDescribeContents() {
        // Verify describeContents returns 0 as specified in the enum
        assertEquals(0, TableHealthStatus.TABLE_HEALTH_IS_CLEAN.describeContents());
        assertEquals(0, TableHealthStatus.TABLE_HEALTH_HAS_CONFLICTS.describeContents());
        assertEquals(0, TableHealthStatus.TABLE_HEALTH_HAS_CHECKPOINTS.describeContents());
        assertEquals(0, TableHealthStatus.TABLE_HEALTH_HAS_CHECKPOINTS_AND_CONFLICTS.describeContents());
    }

    @Test
    public void testCreator() {
        // Verify CREATOR is properly initialized
        assertNotNull(TableHealthStatus.CREATOR);
    }
}

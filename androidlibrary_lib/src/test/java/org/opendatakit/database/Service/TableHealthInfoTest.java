package org.opendatakit.database.Service;

import org.junit.Before;
import org.junit.Test;
import org.opendatakit.database.service.TableHealthInfo;
import org.opendatakit.database.service.TableHealthStatus;

import static org.junit.Assert.*;

/**
 * JVM Unit Tests for non-Android functionality in TableHealthInfo
 * Place in src/test/java/org/opendatakit/database/service/
 */
public class TableHealthInfoTest {

    private static final String TEST_TABLE_ID = "test_table";
    private static final TableHealthStatus TEST_STATUS = TableHealthStatus.TABLE_HEALTH_IS_CLEAN;
    private static final boolean TEST_HAS_CHANGES = true;

    private TableHealthInfo testInfo;

    @Before
    public void setUp() {
        testInfo = new TableHealthInfo(TEST_TABLE_ID, TEST_STATUS, TEST_HAS_CHANGES);
    }

    @Test
    public void testConstructorAndGetters() {
        // Test that the constructor properly initializes fields
        assertEquals(TEST_TABLE_ID, testInfo.getTableId());
        assertEquals(TEST_STATUS, testInfo.getHealthStatus());
        assertEquals(TEST_HAS_CHANGES, testInfo.hasChanges());
    }

    @Test
    public void testNullTableId() {
        TableHealthInfo info = new TableHealthInfo(null, TEST_STATUS, TEST_HAS_CHANGES);
        assertNull(info.getTableId());
    }

    @Test
    public void testEmptyTableId() {
        String emptyTableId = "";
        TableHealthInfo info = new TableHealthInfo(emptyTableId, TEST_STATUS, TEST_HAS_CHANGES);
        assertEquals(emptyTableId, info.getTableId());
    }

    @Test
    public void testAllHealthStatuses() {
        // Test with each possible enum value
        for (TableHealthStatus status : TableHealthStatus.values()) {
            TableHealthInfo info = new TableHealthInfo(TEST_TABLE_ID, status, TEST_HAS_CHANGES);
            assertEquals(status, info.getHealthStatus());
        }
    }
}

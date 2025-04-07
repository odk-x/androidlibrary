package org.opendatakit.logging.desktop;

import org.junit.Test;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import static org.junit.Assert.*;

/**
 * Tests focused on real-world usage patterns that would be applicable
 * across different logger implementations.
 */
public class RealWorldTests extends BaseLoggerTest {

    @Test
    public void multipleLoggingCalls() {
        // When: Making multiple logging calls
        logger.i("Tag1", "Info 1");
        logger.w("Tag2", "Warning 2");
        logger.e("Tag3", "Error 3");

        // Then: All are captured correctly in order
        List<LogRecord> records = testHandler.getLogRecords();
        assertEquals("Should have captured 3 log records", 3, records.size());

        assertEquals(Level.INFO, records.get(0).getLevel());
        assertEquals("Tag1: Info 1", records.get(0).getMessage());

        assertEquals(Level.WARNING, records.get(1).getLevel());
        assertEquals("Tag2: Warning 2", records.get(1).getMessage());

        assertEquals(Level.SEVERE, records.get(2).getLevel());
        assertEquals("Tag3: Error 3", records.get(2).getMessage());
    }
}

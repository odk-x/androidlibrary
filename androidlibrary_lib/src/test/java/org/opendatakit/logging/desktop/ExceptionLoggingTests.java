package org.opendatakit.logging.desktop;

import org.junit.Test;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import static org.junit.Assert.*;

public class ExceptionLoggingTests extends BaseLoggerTest {

    @Test
    public void exceptionLogging() {
        // When: Logging an exception
        Exception testException = new RuntimeException("Test exception");
        logger.printStackTrace(testException);

        // Then: Stack trace is properly captured
        LogRecord record = testHandler.getLastLogRecord();
        assertNotNull("Log record should be created", record);
        assertEquals(Level.SEVERE, record.getLevel());
        assertEquals("Test exception", record.getMessage());
        assertSame("Exception should be attached to log record", testException, record.getThrown());
    }
}
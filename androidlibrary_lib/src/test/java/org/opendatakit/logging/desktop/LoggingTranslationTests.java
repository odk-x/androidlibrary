package org.opendatakit.logging.desktop;

import org.junit.Test;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import static org.junit.Assert.*;

public class LoggingTranslationTests extends BaseLoggerTest {

    @Test
    public void assertLevelTranslation() {
        // When: Logging at ASSERT level
        String tag = "TestTag";
        String message = "Assert message";
        logger.a(tag, message);

        // Then: Java FINEST level is used
        LogRecord record = testHandler.getLastLogRecord();
        assertNotNull("Log record should be created", record);
        assertEquals(Level.FINEST, record.getLevel());
        assertEquals(tag + ": " + message, record.getMessage());
    }

    @Test
    public void traceLevelTranslation() {
        // When: Logging at TRACE level
        String tag = "TestTag";
        String message = "Trace message";
        logger.t(tag, message);

        // Then: Java finer level is used with prefix
        LogRecord record = testHandler.getLastLogRecord();
        assertNotNull("Log record should be created", record);
        assertEquals(Level.FINER, record.getLevel());
        assertEquals(tag + ": Trace/" + message, record.getMessage());
    }

    @Test
    public void verboseLevelTranslation() {
        // When: Logging at verbose level
        String tag = "TestTag";
        String message = "Verbose message";
        logger.v(tag, message);

        // Then: Java finer level is used with prefix
        LogRecord record = testHandler.getLastLogRecord();
        assertNotNull("Log record should be created", record);
        assertEquals(Level.FINER, record.getLevel());
        assertEquals(tag + ": Verbose/" + message, record.getMessage());
    }

    @Test
    public void debugLevelTranslation() {
        // When: Logging at debug level
        String tag = "TestTag";
        String message = "Debug message";
        logger.d(tag, message);

        // Then: Java fine level is used
        LogRecord record = testHandler.getLastLogRecord();
        assertNotNull("Log record should be created", record);
        assertEquals(Level.FINE, record.getLevel());
        assertEquals(tag + ": " + message, record.getMessage());
    }

    @Test
    public void infoLevelTranslation() {
        // When: Logging at info level
        String tag = "TestTag";
        String message = "Info message";
        logger.i(tag, message);

        // Then: Java info level is used
        LogRecord record = testHandler.getLastLogRecord();
        assertNotNull("Log record should be created", record);
        assertEquals(Level.INFO, record.getLevel());
        assertEquals(tag + ": " + message, record.getMessage());
    }

    @Test
    public void warningLevelTranslation() {
        // When: Logging at warning level
        String tag = "TestTag";
        String message = "Warning message";
        logger.w(tag, message);

        // Then: Java warning level is used
        LogRecord record = testHandler.getLastLogRecord();
        assertNotNull("Log record should be created", record);
        assertEquals(Level.WARNING, record.getLevel());
        assertEquals(tag + ": " + message, record.getMessage());
    }

    @Test
    public void errorLevelTranslation() {
        // When: Logging at error level
        String tag = "TestTag";
        String message = "Error message";
        logger.e(tag, message);

        // Then: Java severe level is used
        LogRecord record = testHandler.getLastLogRecord();
        assertNotNull("Log record should be created", record);
        assertEquals(Level.SEVERE, record.getLevel());
        assertEquals(tag + ": " + message, record.getMessage());
    }

    @Test
    public void successLevelTranslation() {
        // When: Logging at success level
        String tag = "TestTag";
        String message = "Success message";
        logger.s(tag, message);

        // Then: Java info level is used with prefix
        LogRecord record = testHandler.getLastLogRecord();
        assertNotNull("Log record should be created", record);
        assertEquals(Level.INFO, record.getLevel());
        assertEquals(tag + ": Success/" + message, record.getMessage());
    }

    @Test
    public void genericLogMethod() {
        // When: Using generic log method
        String tag = "TestTag";
        String message = "Generic log message";
        int severity = 3;
        logger.log(severity, tag, message);

        // Then: Level is included in message
        LogRecord record = testHandler.getLastLogRecord();
        assertNotNull("Log record should be created", record);
        assertEquals(Level.INFO, record.getLevel());
        assertEquals(tag + ": N:" + severity + "/" + message, record.getMessage());
    }
}
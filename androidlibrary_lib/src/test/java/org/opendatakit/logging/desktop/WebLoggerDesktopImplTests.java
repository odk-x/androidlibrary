package org.opendatakit.logging.desktop;

import org.junit.Test;
import org.opendatakit.logging.WebLoggerIf;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import static org.junit.Assert.*;

/**
 Comprehensive test class for WebLoggerDesktopImpl functionality.
 Tests various aspects of the desktop logger implementation including:
 - Configuration behavior
 - Factory creation
 - Exception logging
 - Log level translation
 */
public class WebLoggerDesktopImplTests extends BaseLoggerTest {

    // Constants for test messages
    private static final String TEST_TAG = "TestTag";
    private static final String ASSERT_MESSAGE = "Assert message";
    private static final String TRACE_MESSAGE = "Trace message";
    private static final String VERBOSE_MESSAGE = "Verbose message";
    private static final String DEBUG_MESSAGE = "Debug message";
    private static final String INFO_MESSAGE = "Info message";
    private static final String WARNING_MESSAGE = "Warning message";
    private static final String ERROR_MESSAGE = "Error message";
    private static final String SUCCESS_MESSAGE = "Success message";
    private static final String GENERIC_MESSAGE = "Generic log message";
    private static final String TEST_EXCEPTION_MESSAGE = "Test exception";

    // Constants for assertion messages
    private static final String DEFAULT_LEVEL_MESSAGE = "Default minimum log level should be VERBOSE";
    private static final String LEVEL_UNCHANGED_MESSAGE = "Log level should remain VERBOSE even after attempted change";
    private static final String NO_EXCEPTIONS_MESSAGE = "No-op methods should not throw exceptions: ";
    private static final String NON_NULL_LOGGER_MESSAGE = "Factory should create a non-null logger";
    private static final String SEPARATE_INSTANCES_MESSAGE = "Factory should create separate logger instances";
    private static final String RECORD_CREATED_MESSAGE = "Log record should be created";
    private static final String EXCEPTION_ATTACHED_MESSAGE = "Exception should be attached to log record";

    // App name constants
    private static final String APP1 = "app1";
    private static final String APP2 = "app2";

    // Configuration Tests

    @Test
    public void logLevelConfiguration() {
        // When: Attempting to change log level
        // Then: Implementation ignores it and always returns VERBOSE
        assertEquals(DEFAULT_LEVEL_MESSAGE, WebLoggerIf.VERBOSE, logger.getMinimumSystemLogLevel());

        logger.setMinimumSystemLogLevel(WebLoggerIf.ERROR);

        assertEquals(LEVEL_UNCHANGED_MESSAGE, WebLoggerIf.VERBOSE, logger.getMinimumSystemLogLevel());
    }

    @Test
    public void noOpMethods() {
        // When: Calling no-op methods
        // Then: They should complete without exceptions
        try {
            logger.staleFileScan(System.currentTimeMillis());
            logger.close();
            // If we get here, the test passes
            assertTrue(true);
        } catch (Exception e) {
            fail(NO_EXCEPTIONS_MESSAGE + e.getMessage());
        }
    }

    // Factory/Environment Tests

    @Test
    public void factoryCreatesLogger() {
        // Then: A valid logger should be created
        assertNotNull(NON_NULL_LOGGER_MESSAGE, logger);
    }

    @Test
    public void factoryCreatesSeparateLoggers() {
        // When: Multiple loggers are created
        WebLoggerIf logger1 = factory.createWebLogger(APP1);
        WebLoggerIf logger2 = factory.createWebLogger(APP2);

        // Then: They should be separate instances
        assertNotNull(logger1);
        assertNotNull(logger2);
        assertNotSame(SEPARATE_INSTANCES_MESSAGE, logger1, logger2);
    }

    // Exception Logging Tests

    @Test
    public void exceptionLogging() {
        // When: Logging an exception
        Exception testException = new RuntimeException(TEST_EXCEPTION_MESSAGE);
        logger.printStackTrace(testException);

        // Then: Stack trace is properly captured
        LogRecord record = testHandler.getLastLogRecord();
        assertNotNull(RECORD_CREATED_MESSAGE, record);
        assertEquals(Level.SEVERE, record.getLevel());
        assertEquals(TEST_EXCEPTION_MESSAGE, record.getMessage());
        assertSame(EXCEPTION_ATTACHED_MESSAGE, testException, record.getThrown());
    }

    // Log Level Translation Tests

    @Test
    public void assertLevelTranslation() {
        // When: Logging at ASSERT level
        logger.a(TEST_TAG, ASSERT_MESSAGE);

        // Then: Java FINEST level is used
        LogRecord record = testHandler.getLastLogRecord();
        assertNotNull(RECORD_CREATED_MESSAGE, record);
        assertEquals(Level.FINEST, record.getLevel());
        assertEquals(TEST_TAG + ": " + ASSERT_MESSAGE, record.getMessage());
    }

    @Test
    public void traceLevelTranslation() {
        // When: Logging at TRACE level
        logger.t(TEST_TAG, TRACE_MESSAGE);

        // Then: Java finer level is used with prefix
        LogRecord record = testHandler.getLastLogRecord();
        assertNotNull(RECORD_CREATED_MESSAGE, record);
        assertEquals(Level.FINER, record.getLevel());
        assertEquals(TEST_TAG + ": Trace/" + TRACE_MESSAGE, record.getMessage());
    }

    @Test
    public void verboseLevelTranslation() {
        // When: Logging at verbose level
        logger.v(TEST_TAG, VERBOSE_MESSAGE);

        // Then: Java finer level is used with prefix
        LogRecord record = testHandler.getLastLogRecord();
        assertNotNull(RECORD_CREATED_MESSAGE, record);
        assertEquals(Level.FINER, record.getLevel());
        assertEquals(TEST_TAG + ": Verbose/" + VERBOSE_MESSAGE, record.getMessage());
    }

    @Test
    public void debugLevelTranslation() {
        // When: Logging at debug level
        logger.d(TEST_TAG, DEBUG_MESSAGE);

        // Then: Java fine level is used
        LogRecord record = testHandler.getLastLogRecord();
        assertNotNull(RECORD_CREATED_MESSAGE, record);
        assertEquals(Level.FINE, record.getLevel());
        assertEquals(TEST_TAG + ": " + DEBUG_MESSAGE, record.getMessage());
    }

    @Test
    public void infoLevelTranslation() {
        // When: Logging at info level
        logger.i(TEST_TAG, INFO_MESSAGE);

        // Then: Java info level is used
        LogRecord record = testHandler.getLastLogRecord();
        assertNotNull(RECORD_CREATED_MESSAGE, record);
        assertEquals(Level.INFO, record.getLevel());
        assertEquals(TEST_TAG + ": " + INFO_MESSAGE, record.getMessage());
    }

    @Test
    public void warningLevelTranslation() {
        // When: Logging at warning level
        logger.w(TEST_TAG, WARNING_MESSAGE);

        // Then: Java warning level is used
        LogRecord record = testHandler.getLastLogRecord();
        assertNotNull(RECORD_CREATED_MESSAGE, record);
        assertEquals(Level.WARNING, record.getLevel());
        assertEquals(TEST_TAG + ": " + WARNING_MESSAGE, record.getMessage());
    }

    @Test
    public void errorLevelTranslation() {
        // When: Logging at error level
        logger.e(TEST_TAG, ERROR_MESSAGE);

        // Then: Java severe level is used
        LogRecord record = testHandler.getLastLogRecord();
        assertNotNull(RECORD_CREATED_MESSAGE, record);
        assertEquals(Level.SEVERE, record.getLevel());
        assertEquals(TEST_TAG + ": " + ERROR_MESSAGE, record.getMessage());
    }

    @Test
    public void successLevelTranslation() {
        // When: Logging at success level
        logger.s(TEST_TAG, SUCCESS_MESSAGE);

        // Then: Java info level is used with prefix
        LogRecord record = testHandler.getLastLogRecord();
        assertNotNull(RECORD_CREATED_MESSAGE, record);
        assertEquals(Level.INFO, record.getLevel());
        assertEquals(TEST_TAG + ": Success/" + SUCCESS_MESSAGE, record.getMessage());
    }

    @Test
    public void genericLogMethod() {
        // When: Using generic log method
        int severity = 3;
        logger.log(severity, TEST_TAG, GENERIC_MESSAGE);

        // Then: Level is included in message
        LogRecord record = testHandler.getLastLogRecord();
        assertNotNull(RECORD_CREATED_MESSAGE, record);
        assertEquals(Level.INFO, record.getLevel());
        assertEquals(TEST_TAG + ": N:" + severity + "/" + GENERIC_MESSAGE, record.getMessage());
    }
}

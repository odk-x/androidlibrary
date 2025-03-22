package org.opendatakit.logging.desktop;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.experimental.runners.Enclosed;
import org.opendatakit.logging.WebLoggerIf;

@RunWith(Enclosed.class)
public class WebLoggerDesktopFactoryImplTest {

    // Custom Handler that stores LogRecords for inspection
    static class TestLogHandler extends Handler {
        private List<LogRecord> logRecords = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            logRecords.add(record);
        }

        @Override
        public void flush() {
            // No-op for testing
        }

        @Override
        public void close() throws SecurityException {
            logRecords.clear();
        }

        public List<LogRecord> getLogRecords() {
            return logRecords;
        }

        public void clear() {
            logRecords.clear();
        }

        public LogRecord getLastLogRecord() {
            if (logRecords.isEmpty()) {
                return null;
            }
            return logRecords.get(logRecords.size() - 1);
        }
    }

    // Base class with common setup/teardown logic
    public static abstract class BaseLoggerTest {
        protected WebLoggerDesktopFactoryImpl factory;
        protected WebLoggerIf logger;
        protected Logger globalLogger;
        protected TestLogHandler testHandler;
        protected Handler[] originalHandlers;
        protected Level originalLevel;

        @Before
        public void setUp() {
            // Get reference to global logger
            globalLogger = Logger.getGlobal();

            // Save original handlers and level
            originalLevel = globalLogger.getLevel();
            originalHandlers = globalLogger.getHandlers();

            // Clear handlers and add our test handler
            for (Handler handler : originalHandlers) {
                globalLogger.removeHandler(handler);
            }

            testHandler = new TestLogHandler();
            globalLogger.addHandler(testHandler);

            // Ensure all log messages get through
            globalLogger.setLevel(Level.ALL);

            // Create the factory and logger
            factory = new WebLoggerDesktopFactoryImpl();
            logger = factory.createWebLogger("test-app");
        }

        @After
        public void tearDown() {
            // Restore original handlers and level
            globalLogger.removeHandler(testHandler);

            globalLogger.setLevel(originalLevel);
            for (Handler handler : originalHandlers) {
                globalLogger.addHandler(handler);
            }
        }
    }

    /**
     * Scenario: Desktop environment setup
     * Given: A desktop development environment
     * When: The logging system is initialized
     */
    public static class DesktopEnvironmentTests extends BaseLoggerTest {

        @Test
        public void factoryCreatesLogger() {
            // Then: A valid logger should be created
            assertNotNull("Factory should create a non-null logger", logger);
        }

        @Test
        public void factoryCreatesSeparateLoggers() {
            // When: Multiple loggers are created
            WebLoggerIf logger1 = factory.createWebLogger("app1");
            WebLoggerIf logger2 = factory.createWebLogger("app2");

            // Then: They should be separate instances
            assertNotNull(logger1);
            assertNotNull(logger2);
            assertNotSame("Factory should create separate logger instances", logger1, logger2);
        }
    }

    /**
     * Scenario: Logging level translation
     * Given: An Android developer using desktop logging
     */
    public static class LoggingTranslationTests extends BaseLoggerTest {

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
            // When: Logging at sucess level
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

    /**
     * Scenario: Exception debugging
     * Given: A developer debugging an exception
     */
    public static class ExceptionLoggingTests extends BaseLoggerTest {

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

    /**
     * Scenario: Configuration and cleanup
     * Given: A need for logger configuration and management
     */
    public static class ConfigurationTests extends BaseLoggerTest {

        @Test
        public void logLevelConfiguration() {
            // When: Attempting to change log level
            // Then: Implementation ignores it and always returns VERBOSE
            assertEquals("Default minimum log level should be VERBOSE",
                    WebLoggerIf.VERBOSE, logger.getMinimumSystemLogLevel());

            logger.setMinimumSystemLogLevel(WebLoggerIf.ERROR);

            assertEquals("Log level should remain VERBOSE even after attempted change",
                    WebLoggerIf.VERBOSE, logger.getMinimumSystemLogLevel());
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
                fail("No-op methods should not throw exceptions: " + e.getMessage());
            }
        }
    }

    /**
     * Scenario: Real-world logging
     * Given: A developer using the logger in a real application
     */
    public static class RealWorldTests extends BaseLoggerTest {

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
}
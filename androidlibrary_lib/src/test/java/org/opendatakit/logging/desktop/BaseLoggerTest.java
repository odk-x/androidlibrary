package org.opendatakit.logging.desktop;

import org.junit.After;
import org.junit.Before;
import org.opendatakit.logging.WebLoggerIf;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for logger tests that handles setup and teardown of the testing environment.
 */
public abstract class BaseLoggerTest {
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

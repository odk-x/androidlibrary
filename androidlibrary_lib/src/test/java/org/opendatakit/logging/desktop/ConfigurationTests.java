package org.opendatakit.logging.desktop;

import org.junit.Test;
import org.opendatakit.logging.WebLoggerIf;
import static org.junit.Assert.*;

public class ConfigurationTests extends BaseLoggerTest {

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
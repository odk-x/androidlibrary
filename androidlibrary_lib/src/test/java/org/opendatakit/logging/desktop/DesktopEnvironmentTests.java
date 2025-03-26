package org.opendatakit.logging.desktop;

import org.junit.Test;
import org.opendatakit.logging.WebLoggerIf;
import static org.junit.Assert.*;

public class DesktopEnvironmentTests extends BaseLoggerTest {

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
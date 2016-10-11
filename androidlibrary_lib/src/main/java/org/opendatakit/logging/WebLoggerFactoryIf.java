package org.opendatakit.logging;

/**
 * Factory interface for producing WebLoggerIf implementations
 *
 * @author mitchellsundt@gmail.com
 */
public interface WebLoggerFactoryIf {
   WebLoggerIf createWebLogger(String appName);
}

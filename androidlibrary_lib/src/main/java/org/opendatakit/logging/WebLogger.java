/*
 * Copyright (C) 2012-2013 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.logging;

import org.opendatakit.utilities.StaticStateManipulator;
import org.opendatakit.utilities.StaticStateManipulator.IStaticFieldManipulator;

import java.util.HashMap;
import java.util.Map;

/**
 * Logger that emits logs to the LOGGING_PATH and recycles them as needed.
 * Useful to separate out ODK log entries from the overall logging stream,
 * especially on heavily logged 4.x systems.
 *
 * @author mitchellsundt@gmail.com
 */
public final class WebLogger {
  static final long MILLISECONDS_DAY = 86400000L;
  private static final Map<String, WebLoggerIf> loggers = new HashMap<>();
  private static long lastStaleScan = 0L;
  private static WebLoggerFactoryIf webLoggerFactory;
  private static ThreadLogger contextLogger = new ThreadLogger();

  static {
    webLoggerFactory = new WebLoggerFactoryImpl();

    // register a state-reset manipulator for 'loggers' field.
    StaticStateManipulator.get().register(new IStaticFieldManipulator() {

      @Override
      public void reset() {
        closeAll();
      }

    });
  }

  /**
   * Do not instantiate this class
   */
  private WebLogger() {
  }

  public static void setFactory(WebLoggerFactoryIf webLoggerFactoryImpl) {
    webLoggerFactory = webLoggerFactoryImpl;
  }

  public static synchronized void closeAll() {
    for (WebLoggerIf l : loggers.values()) {
      l.close();
    }
    loggers.clear();
    // and forget any thread-local associations for the loggers
    contextLogger = new ThreadLogger();
  }

  public static WebLoggerIf getContextLogger() {
    String appNameOfThread = contextLogger.get();
    return getLogger(appNameOfThread);
  }

  public static synchronized WebLoggerIf getLogger(String appName) {
    if (appName == null) {
      // create a one-off logger to handle this case
      // factory will create a logger that doesn't care
      // about the appName (e.g., log to just system log)
      return webLoggerFactory.createWebLogger(null);
    }

    WebLoggerIf logger = loggers.get(appName);
    if (logger == null) {
      logger = webLoggerFactory.createWebLogger(appName);
      loggers.put(appName, logger);
    }

    contextLogger.set(appName);

    long now = System.currentTimeMillis();
    if (lastStaleScan + MILLISECONDS_DAY < now) {
      try {
        logger.staleFileScan(now);
      } finally {
        // whether or not we failed, record that we did the scan.
        lastStaleScan = now;
      }
    }
    return logger;
  }

  private static class ThreadLogger extends ThreadLocal<String> {

    @Override
    protected String initialValue() {
      return null;
    }

  }
}

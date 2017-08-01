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

/**
 * Logger that emits logs to the LOGGING_PATH and recycles them as needed.
 * Useful to separate out ODK log entries from the overall logging stream,
 * especially on heavily logged 4.x systems.
 *
 * @author mitchellsundt@gmail.com
 */
public interface WebLoggerIf {

  /**
   * NOTE: this must match the value in android.util.Log
   * Priority constant for the println method; use Log.v.
   */
  int VERBOSE = 2;

  /**
   * NOTE: this must match the value in android.util.Log
   * Priority constant for the println method; use Log.d.
   */
  int DEBUG = 3;

  /**
   * NOTE: this must match the value in android.util.Log
   * Priority constant for the println method; use Log.i.
   */
  int INFO = 4;

  /**
   * NOTE: this must match the value in android.util.Log
   * Priority constant for the println method; use Log.w.
   */
  int WARN = 5;

  /**
   * NOTE: this must match the value in android.util.Log
   * Priority constant for the println method; use Log.e.
   */
  int ERROR = 6;

  /**
   * NOTE: this must match the value in android.util.Log
   * Priority constant for the println method.
   */
  int ASSERT = 7;

  /**
   * Info-like message that is logged with the same level as an error.
   */
  int SUCCESS = 8;

  /**
   * Info-like message that is logged with the same level as an assert.
   */
  int TIP = 9;

  void staleFileScan(long now);

  void close();

  void log(int severity, String t, String logMsg);

  void a(String t, String logMsg);

  void t(String t, String logMsg);
  @SuppressWarnings("unused")
  void v(String t, String logMsg);
  void d(String t, String logMsg);
  void i(String t, String logMsg);
  void w(String t, String logMsg);
  void e(String t, String logMsg);
  void s(String t, String logMsg);

  void printStackTrace(Throwable e);

  /**
   * Set the minimum log level for logging to the system log.
   *
   * This is actually totally unused
   *
   * @param level the lowest level to log
    */
  void setMinimumSystemLogLevel(int level);

  /**
   * @return the minimum log message level that will be logged to the android system log.
    */
  int getMinimumSystemLogLevel();

}

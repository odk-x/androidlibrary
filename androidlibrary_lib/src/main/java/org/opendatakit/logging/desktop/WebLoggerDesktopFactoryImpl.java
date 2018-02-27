/*
 * Copyright (C) 2015 University of Washington
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

package org.opendatakit.logging.desktop;

import android.annotation.SuppressLint;
import org.opendatakit.logging.WebLoggerFactoryIf;
import org.opendatakit.logging.WebLoggerIf;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Replacement implementation for desktop deoployments (e.g., app-designer) and
 * when running JUnit4 tests on the desktop.
 *
 * @author mitchellsundt@gmail.com
 */
@SuppressLint("NewApi")
public class WebLoggerDesktopFactoryImpl implements WebLoggerFactoryIf {

  private class WebLoggerDesktopImpl implements WebLoggerIf {

    public void staleFileScan(long now) {
     // no-op
    }

    public void close() {
      // no-op
    }

    public void log(int severity, String t, String logMsg) {
      Logger.getGlobal().log(Level.INFO, t + ": " + "N:" + severity + "/" + logMsg);
    }

    public void a(String t, String logMsg) {
      Logger.getGlobal().log(Level.FINEST, t + ": " + logMsg);
    }

    public void t(String t, String logMsg) {
      Logger.getGlobal().log(Level.FINER, t + ": " + "Trace/" + logMsg);
    }

    public void v(String t, String logMsg) {
      Logger.getGlobal().log(Level.FINER, t + ": " + "Verbose/" + logMsg);
    }

    public void d(String t, String logMsg) {
      Logger.getGlobal().log(Level.FINE, t + ": " + logMsg);
    }

    public void i(String t, String logMsg) {
      Logger.getGlobal().log(Level.INFO, t + ": " + logMsg);
    }

    public void w(String t, String logMsg) {
      Logger.getGlobal().log(Level.WARNING, t + ": " + logMsg);
    }

    public void e(String t, String logMsg) {
      Logger.getGlobal().log(Level.SEVERE, t + ": " + logMsg);
    }

    public void s(String t, String logMsg) {
      Logger.getGlobal().log(Level.INFO, t + ": " + "Success/" + logMsg);
    }

    public void printStackTrace(Throwable e) {
      Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
    }

    public void setMinimumSystemLogLevel(int level) {
      // TODO: no-op
    }

    public int getMinimumSystemLogLevel() {
      return WebLoggerIf.VERBOSE;
    }

  }

  public synchronized WebLoggerIf createWebLogger(String appName) {
    return new WebLoggerDesktopImpl();
  }

}

package org.opendatakit.logging;

import android.util.Log;

/**
 * @author mitchellsundt@gmail.com
 */

class WebLoggerAppNameUnknownImpl implements WebLoggerIf {

  @Override
  public void staleFileScan(long now) {
    // no-op
  }

  @Override
  public void close() {
    // no-op
  }

  @Override
  public void log(int severity, String t, String logMsg) {
    Log.println(severity, t, logMsg);
  }

  public void a(String t, String logMsg) {
    log(ASSERT, t, logMsg);
  }

  public void t(String t, String logMsg) {
    log(TIP, t, logMsg);
  }

  public void v(String t, String logMsg) {
    log(VERBOSE, t, logMsg);
  }

  public void d(String t, String logMsg) {
    log(DEBUG, t, logMsg);
  }

  public void i(String t, String logMsg) {
    log(INFO, t, logMsg);
  }

  public void w(String t, String logMsg) {
    log(WARN, t, logMsg);
  }

  public void e(String t, String logMsg) {
    log(ERROR, t, logMsg);
  }

  public void s(String t, String logMsg) {
    log(SUCCESS, t, logMsg);
  }

  @Override
  public void printStackTrace(Throwable e) {
    log(ERROR, "unknown", Log.getStackTraceString(e));
  }

  @Override
  public void setMinimumSystemLogLevel(int level) {
    // no-op
  }

  @Override
  public int getMinimumSystemLogLevel() {
    return ASSERT;
  }
}

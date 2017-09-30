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

import android.util.Log;
import org.opendatakit.utilities.ODKFileUtils;

/**
 * Factory implementation that returns a WebLoggerImpl object
 * for the WebLoggerIf.
 *
 * @author mitchellsundt@gmail.com
 */
class WebLoggerFactoryImpl implements WebLoggerFactoryIf {

  public synchronized WebLoggerIf createWebLogger(String appName) {
    if ( appName == null ) {
      // log to just the system log...
      return new WebLoggerAppNameUnknownImpl();
    } else {

      // ensure we have the directories created...
      try {
        ODKFileUtils.verifyExternalStorageAvailability();
        ODKFileUtils.assertDirectoryStructure(appName);
      } catch (Exception e) {
        Log.e("WebLoggerFactoryImpl", "Unable to create logging directory");
        Log.e("WebLoggerFactoryImpl", Log.getStackTraceString(e), e);
        return new WebLoggerAppNameUnknownImpl();
      }
      return new WebLoggerImpl(appName);
    }
  }
}

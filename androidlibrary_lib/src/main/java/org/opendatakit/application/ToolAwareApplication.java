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

package org.opendatakit.application;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.util.Log;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.utilities.ODKFileUtils;

/**
 * Move some of the functionality of CommonApplication up into androidlibrary
 * so that it can be shared with Services. License reader task is moved
 * out of global state and into the AboutMenuFragment static state.
 *
 * @author mitchellsundt@gmail.com
 */
public abstract class ToolAwareApplication extends Application implements IToolAware {

  private static final String t = ToolAwareApplication.class.getSimpleName();

  /**
   * Creates required directories on the SDCard (or other external storage)
   *
   * @return true if there are tables present
   * @throws RuntimeException
   *           if there is no SDCard or the directory exists as a non directory
   */
  public static void createODKDirs(String appName) throws RuntimeException {

    ODKFileUtils.verifyExternalStorageAvailability();

    ODKFileUtils.assertDirectoryStructure(appName);
  }

  public ToolAwareApplication() {
    super();
  }

  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    Log.i(t, "onConfigurationChanged");
  }

  @Override
  public void onTerminate() {
    WebLogger.closeAll();
    super.onTerminate();
    Log.i(t, "onTerminate");
  }



  /**
   * The tool name is the name of the package after the org.opendatakit. prefix.
   * 
   * @return the tool name.
   */
  @Override
  public String getToolName() {
    String packageName = getPackageName();
    String[] parts = packageName.split("\\.");
    return parts[2];
  }

  @Override
  public String getVersionCodeString() {
    try {
      PackageInfo pinfo;
      pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      int versionNumber = pinfo.versionCode;
      return Integer.toString(versionNumber);
    } catch (NameNotFoundException e) {
      e.printStackTrace();
      return "";
    }
  }

  @Override
  public String getVersionDetail() {
    String versionDetail = "";
    try {
      PackageInfo pinfo;
      pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      String versionName = pinfo.versionName;
      versionDetail = " " + versionName;
    } catch (NameNotFoundException e) {
      e.printStackTrace();
    }
    return versionDetail;
  }

  @Override
  public String getVersionedToolName() {
    String versionDetail = this.getVersionDetail();
    return getString(getApkDisplayNameResourceId()) + versionDetail;
  }
}

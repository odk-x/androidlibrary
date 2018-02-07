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

package org.opendatakit.properties;

import android.content.Context;
import org.opendatakit.utilities.ODKFileUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Used in CommonToolProperties
 */
abstract class PropertiesSingletonFactory {

  private final TreeMap<String,String> mGeneralDefaults;
  private final TreeMap<String,String> mDeviceDefaults;
  private final TreeMap<String,String> mSecureDefaults;

  private String gAppName = null;
  private PropertiesSingleton gSingleton = null;
  private Map<String,ReentrantLock> gAppLockMap = new HashMap<String,ReentrantLock>();
  
  PropertiesSingletonFactory(TreeMap<String, String> generalDefaults,
      TreeMap<String, String> deviceDefaults, TreeMap<String, String> secureDefaults) {
    
    mGeneralDefaults = generalDefaults;
    mDeviceDefaults = deviceDefaults;
    mSecureDefaults = secureDefaults;
  }

  private void verifyDirectories(String appName) {
    try {
      ODKFileUtils.verifyExternalStorageAvailability();
      ODKFileUtils.assertDirectoryStructure(appName);
    } catch (Exception ignored) {
      throw new IllegalArgumentException("External storage not available");
    }
  }

  /**
   * This should be called shortly before accessing the settings.
   * The individual get/set/contains/remove functionality does not
   * check for prior changes to properties, but this does.
   * 
   * @param context an activity to run in
   * @param appName the app name
   * @return a properties singleton for that app name
    */
  synchronized PropertiesSingleton getSingleton(Context context, String appName) {
    if (appName == null || appName.isEmpty()) {
      throw new IllegalArgumentException("Unexpectedly null or empty appName");
    }

    if ( gSingleton == null || gAppName == null || !gAppName.equals(appName) ) {
      // verify of directories needs to occur before we create the singleton.
      verifyDirectories(appName);
      ReentrantLock appLock = gAppLockMap.get(appName);
      if ( appLock == null ) {
        appLock = new ReentrantLock();
        gAppLockMap.put(appName, appLock);
      }
      gSingleton = new PropertiesSingleton(context, appName, appLock,
          mGeneralDefaults, mDeviceDefaults, mSecureDefaults);
      gAppName = appName;
    }
    return gSingleton;
  }

}

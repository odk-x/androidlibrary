/*
 * Copyright (C) 2013-2014 University of Washington
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
import android.content.SharedPreferences;
import android.util.Log;

import org.apache.commons.lang3.CharEncoding;
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.androidlibrary.R;
import org.opendatakit.utilities.ODKFileUtils;
import org.opendatakit.consts.IntentConsts;
import org.opendatakit.logging.WebLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Properties are in 3 classes:
 *
 * (1) general (syncable) -- the contents of config/assets/app.properties
 * (2) device -- the contents of data/device.properties
 * (3) secure -- stored in SharedPreferences (ideally only within ODK Services)
 *
 * The tools provide the different sets of these and default values for these settings.
 *
 * If the general (syncable) file contains values for the device and secure settings,
 * these become the default settings for those values.
 *
 * Device settings and secure settings are not overwritten by changes in the
 * general (syncable) settings. You need to Reset the device configuration to
 * re-initialize these.
 */
public class PropertiesSingleton {

  private static final String t = "PropertiesSingleton";

  private static final String GENERAL_PROPERTIES_FILENAME = "app.properties";
  private static final String DEFAULT_DEVICE_PROPERTIES_FILENAME = "default.device.properties";
  private static final String DEVICE_PROPERTIES_FILENAME = "device.properties";

  private final String mAppName;
  private long lastGeneralModified = 0L;
  private long lastDeviceModified = 0L;

  private final TreeMap<String, String> mGeneralDefaults;
  private final TreeMap<String, String> mDeviceDefaults;
  private final TreeMap<String, String> mSecureDefaults;

  private Properties mGeneralProps;
  private Properties mGlobalDeviceProps;
  private Properties mDeviceProps;
  private Context mBaseContext;

  public String getAppName() {
    return mAppName;
  }

  private boolean isSecureProperty(String propertyName) {
    return mSecureDefaults.containsKey(propertyName);
  }

  private boolean isDeviceProperty(String propertyName) {
    return mDeviceDefaults.containsKey(propertyName);
  }

  void setCurrentContext(Context context) {
    try {
      mBaseContext = context;
      // if we are re-using the existing one, pick up any changes by other apps
      // including, e.g., the reset of the configuration by ODK Services.
      if ( isModified() ) {
        init();
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalStateException("ODK Services must be installed!");
    }
  }

  /**
   * SharedPreferences are ONLY available from within ODK Services.
   *
   * @param context
   * @return
    */
  private static SharedPreferences getSharedPreferences(Context context) {
    try {
      if ( !context.getPackageName().equals(IntentConsts.AppProperties.APPLICATION_NAME) ) {
        return null;
      }
      return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE
            | Context.MODE_MULTI_PROCESS);
    } catch ( Exception e ) {
     Log.e("PropertiesSingleton", "Unable to access SharedPreferences!");
     return null;
    }
  }

  public boolean containsKey(String propertyName) {
    if (isSecureProperty(propertyName)) {
      // this needs to be stored in a protected area
      SharedPreferences sharedPreferences = getSharedPreferences(mBaseContext);
      return (sharedPreferences == null) ? false :
        sharedPreferences.contains(mAppName + "_" + propertyName);
    } else if (isDeviceProperty(propertyName) ) {
      return mDeviceProps.containsKey(propertyName);
    } else {
      return mGeneralProps.containsKey(propertyName);
    }
  }

  /**
   * Accesses the given propertyName. This may be stored in SharedPreferences or
   * in the PROPERTIES_FILENAME in the config/assets directory.
   * 
   * @param propertyName
   * @return null or the string value
   */
  public String getProperty(String propertyName) {
    if (isSecureProperty(propertyName)) {
      // this needs to be stored in a protected area
      SharedPreferences sharedPreferences = getSharedPreferences(mBaseContext);
      return (sharedPreferences == null) ? null : 
        sharedPreferences.getString(mAppName + "_" + propertyName, null);
    } else if (isDeviceProperty(propertyName) ) {
      return mDeviceProps.getProperty(propertyName);
    } else {
      return mGeneralProps.getProperty(propertyName);
    }
  }

  /**
   * Accesses the given propertyName. This may be stored in SharedPreferences or
   * in the PROPERTIES_FILENAME in the config/assets directory.
   * 
   * If the value is not specified, null or an empty string, a null value is
   * returned. Boolean.TRUE is returned if the value is "true", otherwise
   * Boolean.FALSE is returned.
   * 
   * @param propertyName
   * @return null or boolean true/false
   */
  public Boolean getBooleanProperty(String propertyName) {
    Boolean booleanSetting = Boolean.TRUE;
    String value = getProperty(propertyName);
    if (value == null || value.length() == 0) {
      return null;
    }

    if (!"true".equalsIgnoreCase(value)) {
      booleanSetting = Boolean.FALSE;
    }

    return booleanSetting;
  }

  public void setBooleanProperty(String propertyName, boolean value) {
    setProperty(propertyName, Boolean.toString(value));
  }

  /**
   * Accesses the given propertyName. This may be stored in SharedPreferences or
   * in the PROPERTIES_FILENAME in the config/assets directory.
   * 
   * If the value is not specified, null or an empty string, or if the value
   * cannot be parsed as an integer, then null is return. Otherwise, the integer
   * value is returned.
   * 
   * @param propertyName
   * @return
   */
  public Integer getIntegerProperty(String propertyName) {
    String value = getProperty(propertyName);
    if (value == null) {
      return null;
    }
    try {
      int v = Integer.parseInt(value);
      return v;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public void setIntegerProperty(String propertyName, int value) {
    setProperty(propertyName, Integer.toString(value));
  }

  /**
   * Caller is responsible for calling writeProperties() to persist this value
   * to disk.
   * 
   * @param propertyName
   */
  public void removeProperty(String propertyName) {
    if (isSecureProperty(propertyName)) {
      // this needs to be stored in a protected area
      SharedPreferences sharedPreferences = getSharedPreferences(mBaseContext);
      if ( sharedPreferences != null ) {
        sharedPreferences.edit().remove(mAppName + "_" + propertyName).commit();
      } else {
        throw new IllegalStateException("Unable to remove SharedPreferences");
      }
    } else if (isDeviceProperty(propertyName) ) {
      mDeviceProps.remove(propertyName);
    } else {
      mGeneralProps.remove(propertyName);
    }
  }

  /**
   * Caller is responsible for calling writeProperties() to persist this value
   * to disk.
   * 
   * @param propertyName
   * @param value
   */
  public void setProperty(String propertyName, String value) {
    if (isSecureProperty(propertyName)) {
      // this needs to be stored in a protected area
      SharedPreferences sharedPreferences = getSharedPreferences(mBaseContext);
      if ( sharedPreferences != null ) {
        sharedPreferences.edit().putString(mAppName + "_" + propertyName, value).commit();
      } else {
        throw new IllegalStateException("Unable to write SharedPreferences");
      }
    } else {
      if (isModified()) {
        readProperties(false);
      }
      if (isDeviceProperty(propertyName) ) {
        mDeviceProps.setProperty(propertyName, value);
      } else {
        mGeneralProps.setProperty(propertyName, value);
      }
      writeProperties();
    }
  }

  /**
   * Determine whether or not the initialization task for the given toolName
   * should be run.
   *
   * @param toolName
   *          (e.g., survey, tables, scan, etc.)
   */
  public boolean shouldRunInitializationTask(String toolName) {
    // this is stored in the device properties
    if (isModified()) {
      readProperties(false);
    }

    String value = mDeviceProps.getProperty(toolInitializationPropertyName(toolName));
    if (value == null || value.length() == 0) {
      return Boolean.TRUE;
    }

    return Boolean.FALSE;
  }

  /**
   * Indicate that the initialization task for this given tool has been run.
   *
   * @param toolName
   */
  public void clearRunInitializationTask(String toolName) {
    // this is stored in the device properties
    if (isModified()) {
      readProperties(false);
    }

    mDeviceProps.setProperty(toolInitializationPropertyName(toolName),
            TableConstants.nanoSecondsFromMillis(System.currentTimeMillis()));
    writeProperties();
  }

  /**
   * Indicate that the initialization task for this given tool should be run
   * (again).
   *
   * @param toolName
   */
  public void setRunInitializationTask(String toolName) {
    // this is stored in the device properties
    if (isModified()) {
      readProperties(false);
    }

    mDeviceProps.remove(toolInitializationPropertyName(toolName));
    writeProperties();
  }

  /**
   * Indicate that all initialization tasks for all tools should be run (again).
   */
  public void setAllRunInitializationTasks() {
    // this is stored in the device properties
    if (isModified()) {
      readProperties(false);
    }

    ArrayList<Object> keysToRemove = new ArrayList<Object>();

    for ( Object okey : mDeviceProps.keySet() ) {
      if ( isToolInitializationPropertyName((String) okey) ) {
        keysToRemove.add(okey);
      }
    }
    for ( Object okey : keysToRemove ) {
      mDeviceProps.remove(okey);
    }
    writeProperties();
  }

  public String getActiveUser() {
    final String CREDENTIAL_TYPE_NONE = mBaseContext.getString(R.string.credential_type_none);
    final String CREDENTIAL_TYPE_USERNAME_PASSWORD = mBaseContext.getString(R.string.credential_type_username_password);
    final String CREDENTIAL_TYPE_GOOGLE_ACCOUNT = mBaseContext.getString(R.string.credential_type_google_account);

    String authType = getProperty(CommonToolProperties.KEY_AUTHENTICATION_TYPE);
    if (authType.equals(CREDENTIAL_TYPE_NONE)) {
      return "anonymous";
    } else if (authType.equals(CREDENTIAL_TYPE_USERNAME_PASSWORD)) {
      String name = getProperty(CommonToolProperties.KEY_USERNAME);
      if (name != null) {
        return "username:" + name;
      } else {
        return "anonymous";
      }
    } else if (authType.equals(CREDENTIAL_TYPE_GOOGLE_ACCOUNT)) {
      String name = getProperty(CommonToolProperties.KEY_ACCOUNT);
      if (name != null) {
        return "mailto:" + name;
      } else {
        return "anonymous";
      }
    } else {
      throw new IllegalStateException("unexpected authentication type!");
    }
  }

  public String getLocale() {
    return Locale.getDefault().toString();
  }

  private static String TOOL_INITIALIZATION_SUFFIX = ".tool_last_initialization_start_time";
  private static String toolInitializationPropertyName(String toolName) {
    return toolName + TOOL_INITIALIZATION_SUFFIX;
  }

  private static boolean isToolInitializationPropertyName(String toolName) {
    return toolName.endsWith(TOOL_INITIALIZATION_SUFFIX);
  }

  public static String toolVersionPropertyName(String toolName) {
    return toolName + ".tool_version_code";
  }

  public static String toolFirstRunPropertyName(String toolName) {
    return toolName + ".tool_first_run";
  }

  PropertiesSingleton(Context context, String appName, TreeMap<String, String> plainDefaults,
      TreeMap<String, String> deviceDefaults, TreeMap<String, String> secureDefaults) {
    mAppName = appName;
    mGeneralDefaults = plainDefaults;
    mDeviceDefaults = deviceDefaults;
    mSecureDefaults = secureDefaults;

    // initialize the cache of properties read from the sdcard
    mGeneralProps = new Properties();
    mGlobalDeviceProps = new Properties();
    mDeviceProps = new Properties();

    // set our context
    // this will automatically call init();
    setCurrentContext(context);
  }

  void init() {
    // (re)set values to defaults

    lastGeneralModified = 0L;
    lastDeviceModified = 0L;

    mGeneralProps.clear();
    mGlobalDeviceProps.clear();
    mDeviceProps.clear();

    // populate the caches from disk...
    readProperties(true);

    boolean dirtyProps = false;

    // see if there are missing values in the general props
    // and update them from the mGeneralDefaults map.
    for (TreeMap.Entry<String, String> entry : mGeneralDefaults.entrySet()) {
      if (!mGeneralProps.containsKey(entry.getKey())) {
        mGeneralProps.setProperty(entry.getKey(), entry.getValue());
        dirtyProps = true;
      }
    }

    // scan for device properties in the (syncable) device properties file.
    // update the provided mDeviceDefaults with these new default values.
    for (TreeMap.Entry<String, String> entry : mDeviceDefaults.entrySet()) {
      if (mGlobalDeviceProps.containsKey(entry.getKey())) {
        entry.setValue(mGlobalDeviceProps.getProperty(entry.getKey()));
      }
    }

    // see if there are missing values in the device props
    // and update them from the mDeviceDefaults map.
    for (TreeMap.Entry<String, String> entry : mDeviceDefaults.entrySet()) {
      if (!mDeviceProps.containsKey(entry.getKey())) {
        mDeviceProps.setProperty(entry.getKey(), entry.getValue());
        dirtyProps = true;
      }
    }

    // scan for secure properties in the (syncable) app properties file.
    // remove these and do not propagate them into SharedPreferences.
    for (TreeMap.Entry<String, String> entry : mSecureDefaults.entrySet()) {
      if (mGeneralProps.containsKey(entry.getKey())) {
        mGeneralProps.remove(entry.getKey());
        dirtyProps = true;
      }
    }

    // Now, scan through the shared preferences.  These will only be available
    // from within ODK Services. If we try to access them outside of that
    // application, this will be a no-op.
    SharedPreferences sharedPreferences = getSharedPreferences(mBaseContext);
    if ( sharedPreferences != null ) {
      for (TreeMap.Entry<String, String> entry : mSecureDefaults.entrySet()) {
        // NOTE: can't use the methods because this object is not yet fully created
        if ( !sharedPreferences.contains(mAppName + "_" + entry.getKey()) ) {
          sharedPreferences.edit()
              .putString(mAppName + "_" + entry.getKey(), entry.getValue())
              .commit();
        }
      }
    }

    if (dirtyProps) {
      writeProperties();
    }
  }

  private void verifyDirectories() {
    try {
      ODKFileUtils.verifyExternalStorageAvailability();
      ODKFileUtils.assertDirectoryStructure(mAppName);
    } catch (Exception e) {
      Log.e(t, "External storage not available");
      throw new IllegalArgumentException("External storage not available");
    }
  }

  public boolean isModified() {
    File configFile;

    configFile = new File(ODKFileUtils.getAssetsFolder(mAppName), GENERAL_PROPERTIES_FILENAME);

    if (configFile.exists()) {
      if (lastGeneralModified != configFile.lastModified()) {
        return true;
      }
    } else {
      // doesn't exist -- ergo, it has changed.
      return true;
    }

    configFile = new File(ODKFileUtils.getDataFolder(mAppName), DEVICE_PROPERTIES_FILENAME);

    if (configFile.exists()) {
      if (lastDeviceModified != configFile.lastModified()) {
        return true;
      }
    } else {
      // doesn't exist -- ergo, it has changed.
      return true;
    }

    return false;
  }

  public void readProperties(boolean includingGlobalDeviceProps) {
    verifyDirectories();

    FileInputStream configFileInputStream = null;
    try {
      File configFile = new File(ODKFileUtils.getAssetsFolder(mAppName), GENERAL_PROPERTIES_FILENAME);

      if (configFile.exists()) {
        configFileInputStream = new FileInputStream(configFile);

        mGeneralProps.loadFromXML(configFileInputStream);
        lastGeneralModified = configFile.lastModified();
      }
    } catch (Exception e) {
      WebLogger.getLogger(mAppName).printStackTrace(e);
    } finally {
      if (configFileInputStream != null) {
        try {
          configFileInputStream.close();
        } catch (IOException e) {
          // ignore
          WebLogger.getLogger(mAppName).printStackTrace(e);
        }
      }
    }

    // read-only values
    if ( includingGlobalDeviceProps ) {
      configFileInputStream = null;
      try {
        File configFile = new File(ODKFileUtils.getAssetsFolder(mAppName), DEFAULT_DEVICE_PROPERTIES_FILENAME);

        if (configFile.exists()) {
          configFileInputStream = new FileInputStream(configFile);

          mGlobalDeviceProps.loadFromXML(configFileInputStream);
        }
      } catch (Exception e) {
        WebLogger.getLogger(mAppName).printStackTrace(e);
      } finally {
        if (configFileInputStream != null) {
          try {
            configFileInputStream.close();
          } catch (IOException e) {
            // ignore
            WebLogger.getLogger(mAppName).printStackTrace(e);
          }
        }
      }
    }

    configFileInputStream = null;
    try {
      File configFile = new File(ODKFileUtils.getDataFolder(mAppName), DEVICE_PROPERTIES_FILENAME);

      if (configFile.exists()) {
        configFileInputStream = new FileInputStream(configFile);

        mDeviceProps.loadFromXML(configFileInputStream);
        lastDeviceModified = configFile.lastModified();
      }
    } catch (Exception e) {
      WebLogger.getLogger(mAppName).printStackTrace(e);
    } finally {
      if (configFileInputStream != null) {
        try {
          configFileInputStream.close();
        } catch (IOException e) {
          // ignore
          WebLogger.getLogger(mAppName).printStackTrace(e);
        }
      }
    }
  }

  public void writeProperties() {
    verifyDirectories();

    try {
      File tempConfigFile = new File(ODKFileUtils.getAssetsFolder(mAppName), GENERAL_PROPERTIES_FILENAME
          + ".temp");
      FileOutputStream configFileOutputStream = new FileOutputStream(tempConfigFile, false);

      mGeneralProps.storeToXML(configFileOutputStream, null, CharEncoding.UTF_8);
      configFileOutputStream.close();

      File configFile = new File(ODKFileUtils.getAssetsFolder(mAppName), GENERAL_PROPERTIES_FILENAME);

      boolean fileSuccess = tempConfigFile.renameTo(configFile);

      if (!fileSuccess) {
        WebLogger.getLogger(mAppName).i(t, "Temporary General Config File Rename Failed!");
      } else {
        lastGeneralModified = configFile.lastModified();
      }

    } catch (Exception e) {
      WebLogger.getLogger(mAppName).printStackTrace(e);
    }

    try {
      File tempConfigFile = new File(ODKFileUtils.getDataFolder(mAppName), DEVICE_PROPERTIES_FILENAME
          + ".temp");
      FileOutputStream configFileOutputStream = new FileOutputStream(tempConfigFile, false);

      mDeviceProps.storeToXML(configFileOutputStream, null, CharEncoding.UTF_8);
      configFileOutputStream.close();

      File configFile = new File(ODKFileUtils.getDataFolder(mAppName), DEVICE_PROPERTIES_FILENAME);

      boolean fileSuccess = tempConfigFile.renameTo(configFile);

      if (!fileSuccess) {
        WebLogger.getLogger(mAppName).i(t, "Temporary Device Config File Rename Failed!");
      } else {
        lastDeviceModified = configFile.lastModified();
      }

    } catch (Exception e) {
      WebLogger.getLogger(mAppName).printStackTrace(e);
    }
  }

  public void clearSettings() {
    try {
      File f;
      f = new File(ODKFileUtils.getDataFolder(mAppName), DEVICE_PROPERTIES_FILENAME);
      if (f.exists()) {
        f.delete();
      }

      // and now go through the shared preferences and delete any that pertain to this appName
      SharedPreferences sharedPreferences = getSharedPreferences(mBaseContext);
      if (sharedPreferences != null) {
        Map<String, ?> allPreferences = sharedPreferences.getAll();
        for (String key : allPreferences.keySet()) {
          if (key.startsWith(mAppName + "_")) {
            sharedPreferences.edit().remove(key).commit();
          }
        }
      } else {
        throw new IllegalStateException("Clearing settings should only be done within ODK Services");
      }
    } finally {
      init();
    }
  }
}

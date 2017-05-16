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
import android.util.Log;

import org.apache.commons.lang3.CharEncoding;
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.androidlibrary.R;
import org.opendatakit.consts.IntentConsts;
import org.opendatakit.utilities.ODKFileUtils;
import org.opendatakit.logging.WebLogger;

import java.io.*;
import java.util.*;

/**
 * Properties are in 3 classes:
 *
 * (1) general (syncable) -- the contents of config/assets/app.properties
 * (2) device -- the contents of data/device.properties
 * (3) secure -- stored in ODK Services' (private) dataDir under mAppName
 *
 * The tools provide the different sets of these and default values for these settings.
 * What goes into secure and device should be predefined in CommonToolProperties
 *
 * If the general (syncable) file contains values for the device and secure settings,
 * these become the default settings for those values. There is also a
 * default.device.properties
 *
 * Device settings and secure settings are not overwritten by changes in the
 * general (syncable) settings. You need to Reset the device configuration to
 * re-initialize these.
 */
public class PropertiesSingleton {

  private static final String t = "PropertiesSingleton";

  private static final String PROPERTIES_REVISION_FILENAME = "properties.revision";
  private static final String GENERAL_PROPERTIES_FILENAME = "app.properties";
  private static final String DEFAULT_DEVICE_PROPERTIES_FILENAME = "default.device.properties";
  private static final String DEVICE_PROPERTIES_FILENAME = "device.properties";
  private static final String SECURE_PROPERTIES_FILENAME = "secure.properties";

  private static int invalidRevision() {
    return -1;
  }

  private static final String TOOL_INITIALIZATION_SUFFIX = ".tool_last_initialization_start_time";
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

  private final String mAppName;
  private final boolean mHasSecureStorage;
  private final File mSecureStorageDir;
  private final String CREDENTIAL_TYPE_NONE;
  private final String CREDENTIAL_TYPE_USERNAME_PASSWORD;
  private final String CREDENTIAL_TYPE_GOOGLE_ACCOUNT;

  private int currentRevision = invalidRevision();

  private final TreeMap<String, String> mGeneralDefaults;
  private final TreeMap<String, String> mDeviceDefaults;
  private final TreeMap<String, String> mSecureDefaults;

  private final Properties mGeneralProps;
  private final Properties mGlobalDeviceProps;
  private final Properties mDeviceProps;
  private final Properties mSecureProps;

  public String getAppName() {
    return mAppName;
  }

  private boolean isSecureProperty(String propertyName) {
    return mSecureDefaults.containsKey(propertyName);
  }

  private boolean isDeviceProperty(String propertyName) {
    return mDeviceDefaults.containsKey(propertyName);
  }

  public boolean containsKey(String propertyName) {
    readPropertiesIfModified();
    if (isSecureProperty(propertyName)) {
      return mSecureProps.containsKey(propertyName);
    } else if (isDeviceProperty(propertyName) ) {
      return mDeviceProps.containsKey(propertyName);
    } else {
      return mGeneralProps.containsKey(propertyName);
    }
  }

  /**
   * Accesses the given propertyName.
   * 
   * @param propertyName
   * @return null or the string value
   */
  public String getProperty(String propertyName) {
    readPropertiesIfModified();
    if (isSecureProperty(propertyName)) {
      if ( !mHasSecureStorage ) {
        return null;
      }
      return mSecureProps.getProperty(propertyName);
    } else if (isDeviceProperty(propertyName) ) {
      return mDeviceProps.getProperty(propertyName);
    } else {
      return mGeneralProps.getProperty(propertyName);
    }
  }

  /**
   * Accesses the given propertyName.
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

  /**
   * Accesses the given propertyName.
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

  public void setProperties( Map<String,String> properties) {
    readPropertiesIfModified();
    if ( !mHasSecureStorage ) {
      for (String propertyName : properties.keySet()) {
        if (isSecureProperty(propertyName)) {
          throw new IllegalStateException("Attempted to set or clear a secure property outside "
              + "ODK Services");
        }
      }
    }

    boolean updatedSecureProps = false;
    boolean updatedDeviceProps = false;
    boolean updatedGeneralProps = false;

    for ( String propertyName : properties.keySet() ) {
      String value = properties.get(propertyName);
      if ( value == null ) {
        // remove from map
        if (isSecureProperty(propertyName)) {
          mSecureProps.remove(propertyName);
          updatedSecureProps = true;
        } else if (isDeviceProperty(propertyName) ) {
          mDeviceProps.remove(propertyName);
          updatedDeviceProps = true;
        } else {
          mGeneralProps.remove(propertyName);
          updatedGeneralProps = true;
        }
      } else {
        // set into map
        if (isSecureProperty(propertyName)) {
          mSecureProps.setProperty(propertyName, value);
          updatedSecureProps = true;
        } else if (isDeviceProperty(propertyName) ) {
          mDeviceProps.setProperty(propertyName, value);
          updatedDeviceProps = true;
        } else {
          mGeneralProps.setProperty(propertyName, value);
          updatedGeneralProps = true;
        }
      }
    }
    writeProperties(updatedSecureProps, updatedDeviceProps, updatedGeneralProps);
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
    readPropertiesIfModified();
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
    readPropertiesIfModified();
    mDeviceProps.setProperty(toolInitializationPropertyName(toolName),
            TableConstants.nanoSecondsFromMillis(System.currentTimeMillis()));
    writeProperties(false, true, false);
  }

  /**
   * Indicate that the initialization task for this given tool should be run
   * (again).
   *
   * @param toolName
   */
  public void setRunInitializationTask(String toolName) {
    // this is stored in the device properties
    readPropertiesIfModified();
    mDeviceProps.remove(toolInitializationPropertyName(toolName));
    writeProperties(false, true, false);
  }

  /**
   * Indicate that all initialization tasks for all tools should be run (again).
   */
  public void setAllRunInitializationTasks() {
    // this is stored in the device properties
    readPropertiesIfModified();
    ArrayList<Object> keysToRemove = new ArrayList<Object>();

    for ( Object okey : mDeviceProps.keySet() ) {
      if ( isToolInitializationPropertyName((String) okey) ) {
        keysToRemove.add(okey);
      }
    }
    for ( Object okey : keysToRemove ) {
      mDeviceProps.remove(okey);
    }
    writeProperties(false, true, false);
  }

  public void clearActiveUser() {
    Map<String,String> properties = new HashMap<String,String>();
    properties.put(CommonToolProperties.KEY_ROLES_LIST, "");
    properties.put(CommonToolProperties.KEY_DEFAULT_GROUP, "");

    setProperties(properties);
  }

  public String getActiveUser() {

    String activeUserName = null;
    String authType = getProperty(CommonToolProperties.KEY_AUTHENTICATION_TYPE);
    if (authType.equals(CREDENTIAL_TYPE_NONE)) {
      activeUserName = "anonymous";
    } else if (authType.equals(CREDENTIAL_TYPE_USERNAME_PASSWORD)) {
      String name = getProperty(CommonToolProperties.KEY_USERNAME);
      String roles = getProperty(CommonToolProperties.KEY_ROLES_LIST);
      if (name != null && roles != null && roles.length() != 0) {
        activeUserName = "username:" + name;
      } else {
        activeUserName = "anonymous";
      }
    } else if (authType.equals(CREDENTIAL_TYPE_GOOGLE_ACCOUNT)) {
      String name = getProperty(CommonToolProperties.KEY_ACCOUNT);
      String roles = getProperty(CommonToolProperties.KEY_ROLES_LIST);
      if (name != null && roles != null && roles.length() != 0) {
        activeUserName = "mailto:" + name;
      } else {
        activeUserName = "anonymous";
      }
    } else {
      throw new IllegalStateException("unexpected authentication type!");
    }
    return activeUserName;
  }

  /**
   * This may either be the device locale or the locale that the user has selected from the
   * common_translations list of locales (as specified on the framework settings sheet).
   *
   * @return
   */
  public String getUserSelectedDefaultLocale() {
    // this is dependent upon whether the user wants to use the device locale or a
    // locale specified in the common translations file.
    String value = this.getProperty(CommonToolProperties.KEY_COMMON_TRANSLATIONS_LOCALE);
    if ( value != null && value.length() != 0 && !value.equalsIgnoreCase("_")) {
      return value;
    } else {
      return Locale.getDefault().toString();
    }
  }

  PropertiesSingleton(Context context, String appName, TreeMap<String, String> plainDefaults,
      TreeMap<String, String> deviceDefaults, TreeMap<String, String> secureDefaults) {
    mAppName = appName;
    mHasSecureStorage = context.getPackageName().equals(IntentConsts.AppProperties.APPLICATION_NAME);
    if ( mHasSecureStorage ) {
      mSecureStorageDir = context.getDir(mAppName, 0);
    } else {
      mSecureStorageDir = null;
    }
    CREDENTIAL_TYPE_NONE = context.getString(R.string.credential_type_none);
    CREDENTIAL_TYPE_USERNAME_PASSWORD = context.getString(R.string.credential_type_username_password);
    CREDENTIAL_TYPE_GOOGLE_ACCOUNT = context.getString(R.string.credential_type_google_account);

    mGeneralDefaults = plainDefaults;
    mDeviceDefaults = deviceDefaults;
    mSecureDefaults = secureDefaults;

    // initialize the cache of properties read from the sdcard
    mGeneralProps = new Properties();
    mGlobalDeviceProps = new Properties();
    mDeviceProps = new Properties();
    mSecureProps = new Properties();

    // call init
    init();
  }

  private void init() {
    // (re)set values to defaults

    currentRevision = invalidRevision();

    mGeneralProps.clear();
    mGlobalDeviceProps.clear();
    mDeviceProps.clear();
    mSecureProps.clear();

    // populate the caches from disk...
    readProperties(true);

    boolean updatedSecureProps = false;
    boolean updatedDeviceProps = false;
    boolean updatedGeneralProps = false;

    // see if there are missing values in the general props
    // and update them from the mGeneralDefaults map.
    for (TreeMap.Entry<String, String> entry : mGeneralDefaults.entrySet()) {
      if (!mGeneralProps.containsKey(entry.getKey())) {
        mGeneralProps.setProperty(entry.getKey(), entry.getValue());
        updatedGeneralProps = true;
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
        updatedDeviceProps = true;
      }
    }

    // Now, scan through the secure defaults and assign them.  These will only be
    // available from within ODK Services. If we try to access them outside of that
    // application, this will be a skipped and all values return null.
    if ( mHasSecureStorage ) {
      for (TreeMap.Entry<String, String> entry : mSecureDefaults.entrySet()) {
        if (!mSecureProps.containsKey(entry.getKey())) {
          mSecureProps.setProperty(entry.getKey(), entry.getValue());
          updatedSecureProps = true;
        }
      }
    }

    if (updatedSecureProps || updatedDeviceProps || updatedSecureProps) {
      writeProperties(updatedSecureProps, updatedDeviceProps, updatedGeneralProps);
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

  private int getCurrentRevision() {
    int noResult = 0;
    try {
      File dataFolder = new File(ODKFileUtils.getDataFolder(mAppName));
      String[] timestampNames = dataFolder.list(new FilenameFilter() {
        @Override public boolean accept(File file, String s) {
          return s.startsWith(PROPERTIES_REVISION_FILENAME);
        }
      });
      if ( timestampNames == null ) {
        return noResult;
      }
      int bestResult = noResult;
      for ( String timestampName : timestampNames ) {
        String suffix = timestampNames[timestampNames.length - 1].substring(PROPERTIES_REVISION_FILENAME.length());
        if (suffix.length() <= 1) {
          continue;
        }

        try {
          // try to convert suffix to an int value
          int result = Integer.valueOf(suffix.substring(1), 16);

          // is this value greater than all others?
          if (result > bestResult) {
            bestResult = result;
          }
        } catch ( NumberFormatException e) {
          // ignore
        }
      }
      // NOTE: If files cannot be deleted, we may pin at unchanged.
      return bestResult;
    } catch (Exception e) {
      WebLogger.getLogger(mAppName).printStackTrace(e);
    }
    return noResult;
  }

  private int incrementAndWriteRevision(int oldRevision) {
    ++oldRevision;

    String suffix = "." + Integer.toString(oldRevision, 16);
    // write that file
    FileOutputStream timestampOutputStream = null;
    try {

      File timestampFile = new File(ODKFileUtils.getDataFolder(mAppName),
          PROPERTIES_REVISION_FILENAME + suffix);
      timestampOutputStream = new FileOutputStream(timestampFile);
      timestampOutputStream.write(oldRevision);
      timestampOutputStream.flush();
      timestampOutputStream.close();
      timestampOutputStream = null;
    } catch (Exception e) {
      WebLogger.getLogger(mAppName).printStackTrace(e);
    } finally {
      if (timestampOutputStream != null) {
        try {
          timestampOutputStream.close();
        } catch (IOException e) {
          // ignore
          WebLogger.getLogger(mAppName).printStackTrace(e);
        }
      }
    }
    // remove files other than the one we wrote
    File dataFolder = new File(ODKFileUtils.getDataFolder(mAppName));
    String[] timestampNames = dataFolder.list(new FilenameFilter() {
      @Override public boolean accept(File file, String s) {
        return s.startsWith(PROPERTIES_REVISION_FILENAME);
      }
    });
    if ( timestampNames != null ) {
      for ( String name : timestampNames ) {
        if ( !name.equals(PROPERTIES_REVISION_FILENAME + suffix) ) {
          File timestampFile = new File(dataFolder, name);
          timestampFile.delete();
        }
      }
    }
    return oldRevision;
  }

  private void readPropertiesIfModified() {

    int newRevision = getCurrentRevision();
    if ( newRevision != currentRevision ) {
      readProperties(false);
    }

  }

  private void readProperties(boolean includingGlobalDeviceProps) {
    verifyDirectories();

    WebLogger.getLogger(mAppName).i("PropertiesSingleton",
        "readProperties(" + includingGlobalDeviceProps + ")");

    GainPropertiesLock theLock = new GainPropertiesLock(mAppName);
    try {
      // OK. Now access files...
      FileInputStream configFileInputStream = null;
      try {
        File configFile = new File(ODKFileUtils.getAssetsFolder(mAppName), GENERAL_PROPERTIES_FILENAME);

        if (configFile.exists()) {
          configFileInputStream = new FileInputStream(configFile);

          mGeneralProps.loadFromXML(configFileInputStream);
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

      configFileInputStream = null;
      try {
        File configFile = new File(mSecureStorageDir, SECURE_PROPERTIES_FILENAME);

        if (configFile.exists()) {
          configFileInputStream = new FileInputStream(configFile);

          mSecureProps.loadFromXML(configFileInputStream);
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

      currentRevision = getCurrentRevision();

    } finally {
      theLock.release();
    }
  }

  private void writeProperties(boolean updatedSecureProps, boolean updatedDeviceProps, boolean
      updatedGeneralProps) {
    verifyDirectories();

    GainPropertiesLock theLock = new GainPropertiesLock(mAppName);
    try {
      currentRevision = incrementAndWriteRevision(currentRevision);

      if ( updatedGeneralProps ) {
        try {
          File tempConfigFile = new File(ODKFileUtils.getAssetsFolder(mAppName),
              GENERAL_PROPERTIES_FILENAME + ".temp");
          FileOutputStream configFileOutputStream = new FileOutputStream(tempConfigFile, false);

          mGeneralProps.storeToXML(configFileOutputStream, null, CharEncoding.UTF_8);
          configFileOutputStream.close();

          File configFile = new File(ODKFileUtils.getAssetsFolder(mAppName),
              GENERAL_PROPERTIES_FILENAME);

          boolean fileSuccess = tempConfigFile.renameTo(configFile);

          if (!fileSuccess) {
            WebLogger.getLogger(mAppName).i(t, "Temporary General Config File Rename Failed!");
          }

        } catch (Exception e) {
          WebLogger.getLogger(mAppName).printStackTrace(e);
        }
      }

      if ( updatedDeviceProps ) {
        try {
          File tempConfigFile = new File(ODKFileUtils.getDataFolder(mAppName),
              DEVICE_PROPERTIES_FILENAME + ".temp");
          FileOutputStream configFileOutputStream = new FileOutputStream(tempConfigFile, false);

          mDeviceProps.storeToXML(configFileOutputStream, null, CharEncoding.UTF_8);
          configFileOutputStream.close();

          File configFile = new File(ODKFileUtils.getDataFolder(mAppName),
              DEVICE_PROPERTIES_FILENAME);

          boolean fileSuccess = tempConfigFile.renameTo(configFile);

          if (!fileSuccess) {
            WebLogger.getLogger(mAppName).i(t, "Temporary Device Config File Rename Failed!");
          }

        } catch (Exception e) {
          WebLogger.getLogger(mAppName).printStackTrace(e);
        }
      }

      if ( updatedSecureProps && mHasSecureStorage ) {
        try {
          File tempConfigFile = new File(mSecureStorageDir, SECURE_PROPERTIES_FILENAME + ".temp");
          FileOutputStream configFileOutputStream = new FileOutputStream(tempConfigFile, false);

          mSecureProps.storeToXML(configFileOutputStream, null, CharEncoding.UTF_8);
          configFileOutputStream.close();

          File configFile = new File(mSecureStorageDir, SECURE_PROPERTIES_FILENAME);

          boolean fileSuccess = tempConfigFile.renameTo(configFile);

          if (!fileSuccess) {
            WebLogger.getLogger(mAppName).i(t, "Temporary Secure Config File Rename Failed!");
          }

        } catch (Exception e) {
          WebLogger.getLogger(mAppName).printStackTrace(e);
        }
      }
    } finally {
      theLock.release();
    }
  }

  public void clearSettings() {

    try {
      GainPropertiesLock theLock = new GainPropertiesLock(mAppName);
      currentRevision = incrementAndWriteRevision(currentRevision);

      try {
        File f;
        f = new File(ODKFileUtils.getDataFolder(mAppName), DEVICE_PROPERTIES_FILENAME);
        if (f.exists()) {
          f.delete();
        }

        if ( mHasSecureStorage ) {
          f = new File(mSecureStorageDir, SECURE_PROPERTIES_FILENAME);
          if (f.exists()) {
            f.delete();
          }
        }
      } finally {
        theLock.release();
      }

    } finally {
      init();
    }
  }
}

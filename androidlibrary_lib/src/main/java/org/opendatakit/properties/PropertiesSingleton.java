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
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.androidlibrary.R;
import org.opendatakit.consts.IntentConsts;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.utilities.ODKFileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Properties are in 3 classes:
 * <p>
 * (1) general (syncable) -- the contents of config/assets/app.properties
 * (2) device -- the contents of data/device.properties
 * (3) secure -- stored in ODK Services' (private) dataDir under mAppName
 * <p>
 * The tools provide the different sets of these and default values for these settings.
 * What goes into secure and device should be predefined in CommonToolProperties
 * <p>
 * If the general (syncable) file contains values for the device and secure settings,
 * these become the default settings for those values. There is also a
 * default.device.properties
 * <p>
 * Device settings and secure settings are not overwritten by changes in the
 * general (syncable) settings. You need to Reset the device configuration to
 * re-initialize these.
 */
public final class PropertiesSingleton {

  private static final String TAG = PropertiesSingleton.class.getSimpleName();
  private static final int INVALID_REVISION = -1;

  private static final String PROPERTIES_REVISION_FILENAME = "properties.revision";
  private static final String GENERAL_PROPERTIES_FILENAME = "app.properties";
  private static final String DEFAULT_DEVICE_PROPERTIES_FILENAME = "default.device.properties";
  private static final String DEVICE_PROPERTIES_FILENAME = "device.properties";
  private static final String SECURE_PROPERTIES_FILENAME = "secure.properties";
  private static final String SECURE_INSTALLATION_ID_FILENAME_PREFIX = "secure.id.";
  private static final String TOOL_INITIALIZATION_SUFFIX = ".tool_last_initialization_start_time";
  /**
   * These three are used in services.utilities.ODKServicePropertyUtils
   */
  @SuppressWarnings("WeakerAccess")
  public final String CREDENTIAL_TYPE_NONE;
  @SuppressWarnings("WeakerAccess")
  public final String CREDENTIAL_TYPE_USERNAME_PASSWORD;

  private final String mAppName;
  private final ReentrantLock mAppLock;
  private final boolean mHasSecureStorage;
  private final File mSecureStorageDir;
  private final TreeMap<String, String> mGeneralDefaults;
  private final TreeMap<String, String> mDeviceDefaults;
  private final TreeMap<String, String> mSecureDefaults;
  private final Properties mGeneralProps;
  private final Properties mGlobalDeviceProps;
  private final Properties mDeviceProps;
  private final Properties mSecureProps;
  private int currentRevision = INVALID_REVISION;
  private String mInstallationId;

  PropertiesSingleton(Context context, String appName, ReentrantLock appLock,
                      TreeMap<String, String> plainDefaults,
                      TreeMap<String, String> deviceDefaults,
                      TreeMap<String, String> secureDefaults) {
    mAppName = appName;
    mAppLock = appLock;
    mHasSecureStorage = context.getPackageName()
        .equals(IntentConsts.AppProperties.APPLICATION_NAME);
    if (mHasSecureStorage) {
      mSecureStorageDir = context.getDir(mAppName, 0);
    } else {
      mSecureStorageDir = null;
    }
    // these strings are set as untranslateable
    CREDENTIAL_TYPE_NONE = context.getString(R.string.credential_type_none);
    CREDENTIAL_TYPE_USERNAME_PASSWORD = context
        .getString(R.string.credential_type_username_password);

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

  private static String toolInitializationPropertyName(String toolName) {
    return toolName + TOOL_INITIALIZATION_SUFFIX;
  }

  private static boolean isToolInitializationPropertyName(String toolName) {
    return toolName.endsWith(TOOL_INITIALIZATION_SUFFIX);
  }

  private static String appInitializationPropertyName() {
    return CommonToolProperties.KEY_COMMON_INITIALIZATION;
  }

  /**
   * Used in CommonToolProperties and survey's SplashScreenActivity.
   *
   * @param toolName the name of an app
   * @return the name of a property associated with the version of that app
   */
  @SuppressWarnings("WeakerAccess")
  public static String toolVersionPropertyName(String toolName) {
    return toolName + ".tool_version_code";
  }

  /**
   * Used in CommonToolProperties and survey's SplashScreenActivity.
   *
   * @param toolName the name of an app
   * @return the name of a property associated with whether the app has run before or not
   */
  @SuppressWarnings("WeakerAccess")
  public static String toolFirstRunPropertyName(String toolName) {
    return toolName + ".tool_first_run";
  }

  public String getAppName() {
    return mAppName;
  }

  private boolean isSecureProperty(String propertyName) {
    return mSecureDefaults.containsKey(propertyName);
  }

  private boolean isDeviceProperty(String propertyName) {
    return mDeviceDefaults.containsKey(propertyName);
  }

  /**
   * Hard to tell where this is used, it's used in at least DeviceSettingsFragment,
   * AdminConfigurableDeviceSettingsFragment, TablesSettingsFragment and ServerSettingsFragment
   *
   * @param propertyName the name that may or may not exist in the props object
   * @return whether the key exists
   */
  @SuppressWarnings("unused")
  public boolean containsKey(String propertyName) {
    readPropertiesIfModified();
    if (isSecureProperty(propertyName)) {
      if ( propertyName.equals(CommonToolProperties.KEY_INSTALLATION_ID)) {
        return (mInstallationId != null);
      }
      return mSecureProps.containsKey(propertyName);
    } else if (isDeviceProperty(propertyName)) {
      return mDeviceProps.containsKey(propertyName);
    } else {
      return mGeneralProps.containsKey(propertyName);
    }
  }

  /**
   * Accesses the given propertyName.
   *
   * @param propertyName the key to get
   * @return null or the string value
   */
  public String getProperty(String propertyName) {
    readPropertiesIfModified();
    if (isSecureProperty(propertyName)) {
      if (!mHasSecureStorage) {
        throw new IllegalStateException(
            "Attempt to retrieve secured property " + propertyName + " outside of ODK Services");
      }
      if ( propertyName.equals(CommonToolProperties.KEY_INSTALLATION_ID)) {
        return mInstallationId;
      }
      return mSecureProps.getProperty(propertyName);
    } else if (isDeviceProperty(propertyName)) {
      return mDeviceProps.getProperty(propertyName);
    } else {
      return mGeneralProps.getProperty(propertyName);
    }
  }

  /**
   * Accesses the given propertyName.
   * <p>
   * If the value is not specified, null or an empty string, a null value is
   * returned. Boolean.TRUE is returned if the value is "true", otherwise
   * Boolean.FALSE is returned.
   * <p>
   * Used in mostly the same places as containsKey
   *
   * @param propertyName the key to get
   * @return null or boolean true/false
   */
  @SuppressWarnings("unused")
  public Boolean getBooleanProperty(String propertyName) {
    String value = getProperty(propertyName);
    if (value == null || value.isEmpty()) {
      return null;
    }
    return "true".equalsIgnoreCase(value);
  }

  /**
   * Accesses the given propertyName.
   * <p>
   * If the value is not specified, null or an empty string, or if the value
   * cannot be parsed as an integer, then null is return. Otherwise, the integer
   * value is returned.
   * <p>
   * Used in CommonToolProperties, TableUtil and
   * services.preferences.fragments.PasswordDialogFragment
   *
   * @param propertyName the key to get
   * @return an Integer with the value for the requested key or null
   */
  @SuppressWarnings("WeakerAccess")
  public Integer getIntegerProperty(String propertyName) {
    String value = getProperty(propertyName);
    if (value == null) {
      return null;
    }
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ignored) {
      return null;
    }
  }

  public void setProperties(Map<String, String> properties) {
    readPropertiesIfModified();
    if (!mHasSecureStorage) {
      for (String propertyName : properties.keySet()) {
        if (isSecureProperty(propertyName)) {
          throw new IllegalStateException(
              "Attempted to set or clear a secure property outside " + "ODK Services");
        }
      }
    }

    boolean updatedSecureProps = false;
    boolean updatedDeviceProps = false;
    boolean updatedGeneralProps = false;

    for ( String propertyName : properties.keySet() ) {
      if ( propertyName.equals(CommonToolProperties.KEY_INSTALLATION_ID)) {
        throw new IllegalStateException("Attempted to set or clear the installation id property");
      }
      String value = properties.get(propertyName);
      if (value == null) {
        // remove from map
        if (isSecureProperty(propertyName)) {
          if ( mSecureProps.containsKey(propertyName) ) {
            mSecureProps.remove(propertyName);
            updatedSecureProps = true;
          }
        } else if (isDeviceProperty(propertyName)) {
          if ( mDeviceProps.containsKey(propertyName) ) {
            mDeviceProps.remove(propertyName);
            updatedDeviceProps = true;
          }
        } else {
          if ( mGeneralProps.containsKey(propertyName) ) {
            mGeneralProps.remove(propertyName);
            updatedGeneralProps = true;
          }
        }
      } else {
        // set into map
        if (isSecureProperty(propertyName)) {
          String existingValue  = mSecureProps.getProperty(propertyName);
          if ( existingValue == null || !existingValue.equals(value) ) {
            mSecureProps.setProperty(propertyName, value);
            updatedSecureProps = true;
          }
        } else if (isDeviceProperty(propertyName)) {
          String existingValue  = mDeviceProps.getProperty(propertyName);
          if ( existingValue == null || !existingValue.equals(value) ) {
            mDeviceProps.setProperty(propertyName, value);
            updatedDeviceProps = true;
          }
        } else {
          String existingValue  = mGeneralProps.getProperty(propertyName);
          if ( existingValue == null || !existingValue.equals(value) ) {
            mGeneralProps.setProperty(propertyName, value);
            updatedGeneralProps = true;
          }
        }
      }
    }
    if (updatedSecureProps || updatedDeviceProps || updatedGeneralProps) {
      writeProperties(updatedSecureProps, updatedDeviceProps, updatedGeneralProps);
    }
  }

  /**
   * Called during sync to re-read the properties files since they may have changed
   * during the sync process (i.e., due to updates of app.properties pulled down from the
   * server). After re-reading the properties files, this increments the properties
   * revision file so that survey, tables, etc. will also pick up the latest changes.
   */
  public void signalPropertiesChange() {
    // read the current revision and increment it
    {
      /*
       * Manipulate revision within lock to ensure we get the latest
       * update state without any revisions in progress.
       */
      GainPropertiesLock theLock = new GainPropertiesLock(mAppName, mAppLock);
      try {
        currentRevision = getCurrentRevision();
        currentRevision = incrementAndWriteRevision(currentRevision);
      } finally {
        theLock.release();
      }
    }
    // forcibly reload the properties
    readProperties(false);
  }

  /**
   * Determine whether or not the initialization task for the given toolName
   * should be run.
   * <p>
   * Used in survey's MainMenuActivity, scan's MainActivity, androidcommon's CommonActivity and
   * tables' MainActivity
   *
   * @param toolName (e.g., survey, tables, scan, etc.)
   */
  @SuppressWarnings("unused")
  public boolean shouldRunInitializationTask(String toolName) {
    // this is stored in the device properties
    readPropertiesIfModified();
    String value = mDeviceProps.getProperty(toolInitializationPropertyName(toolName));
    return value == null || value.isEmpty();
  }

  public boolean shouldRunCommonInitializationTask() {
    readPropertiesIfModified();
    String value = mDeviceProps.getProperty(CommonToolProperties.KEY_COMMON_INITIALIZATION);
    return value == null || value.isEmpty();
  }

  /**
   * Indicate that the initialization task for this given tool has been run.
   * Used mostly in the same places as shouldRunInitializationTask
   *
   * @param toolName (e.g., survey, tables, scan, etc.)
   */
  @SuppressWarnings("unused")
  public void clearRunInitializationTask(String toolName) {
    // this is stored in the device properties
    readPropertiesIfModified();
    mDeviceProps.setProperty(
        toolInitializationPropertyName(toolName),
        TableConstants.nanoSecondsFromMillis(
            System.currentTimeMillis(), TableConstants.TIMESTAMP_LOCALE)
    );
    writeProperties(false, true, false);
  }

  public void clearRunCommonInitializationTask() {
    readPropertiesIfModified();
    mDeviceProps.setProperty(
        CommonToolProperties.KEY_COMMON_INITIALIZATION,
        TableConstants.nanoSecondsFromMillis(
          System.currentTimeMillis(), TableConstants.TIMESTAMP_LOCALE)
    );
    writeProperties(false, true, false);
  }

  /**
   * Indicate that all initialization tasks for all tools should be run (again).
   * Used in services.sync.service.SyncExecutionContext
   */
  @SuppressWarnings("unused")
  public void setAllRunInitializationTasks() {
    // this is stored in the device properties
    readPropertiesIfModified();
    ArrayList<String> keysToRemove = new ArrayList<>();

    for (Object okey : mDeviceProps.keySet()) {
      String theKey = (String) okey;
      if (isToolInitializationPropertyName(theKey)) {
        keysToRemove.add(theKey);
      } else if (theKey.equals(appInitializationPropertyName())) {
        keysToRemove.add(theKey);
      }
    }
    for (String theKey : keysToRemove) {
      mDeviceProps.remove(theKey);
    }
    writeProperties(false, true, false);
  }

  /**
   * This may either be the device locale or the locale that the user has selected from the
   * common_translations list of locales (as specified on the framework settings sheet).
   * <p>
   * Used in (survey) AndroidShortcuts, MainMenuActivity, FormListLoader, (androidcommon)
   * DoActionUtils, OdkCommon, (services) OdkDatabaseServiceImpl, OdkResolveCheckpointRowLoader,
   * SyncExecutionContext, ActiveUserAndLocale, (tables) OutputUtil, MultipleChoiceSettingDialog,
   * SpreadsheetUserTable, AbsBaseWebActivity, ExportCSVActivity, ColumnListFragment,
   * EditColorRuleFragment, StatusColorRuleListFragment, ColumnPreferenceFragment,
   * TablePreferenceFragment, TableManagerFragment, ColorRuleListFragment
   *
   * @return The user's selected locale or the device's default if they didn't specify one
   */
  @SuppressWarnings("unused")
  public String getUserSelectedDefaultLocale() {
    // this is dependent upon whether the user wants to use the device locale or a
    // locale specified in the common translations file.
    String value = this.getProperty(CommonToolProperties.KEY_COMMON_TRANSLATIONS_LOCALE);
    if (value != null && !value.isEmpty() && !"_".equalsIgnoreCase(value)) {
      return value;
    } else {
      return Locale.getDefault().toString();
    }
  }

  private void init() {
    // (re)set values to defaults

    currentRevision = INVALID_REVISION;

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
    for (Map.Entry<String, String> entry : mGeneralDefaults.entrySet()) {
      if (!mGeneralProps.containsKey(entry.getKey())) {
        mGeneralProps.setProperty(entry.getKey(), entry.getValue());
        updatedGeneralProps = true;
      }
    }

    // scan for device properties in the (syncable) device properties file.
    // update the provided mDeviceDefaults with these new default values.
    for (Map.Entry<String, String> entry : mDeviceDefaults.entrySet()) {
      if (mGlobalDeviceProps.containsKey(entry.getKey())) {
        entry.setValue(mGlobalDeviceProps.getProperty(entry.getKey()));
      }
    }

    // see if there are missing values in the device props
    // and update them from the mDeviceDefaults map.
    for (Map.Entry<String, String> entry : mDeviceDefaults.entrySet()) {
      if (!mDeviceProps.containsKey(entry.getKey())) {
        mDeviceProps.setProperty(entry.getKey(), entry.getValue());
        updatedDeviceProps = true;
      }
    }

    // Now, scan through the secure defaults and assign them.  These will only be
    // available from within ODK Services. If we try to access them outside of that
    // application, this will be a skipped and all values return null.
    if (mHasSecureStorage) {
      for (Map.Entry<String, String> entry : mSecureDefaults.entrySet()) {
        if ( entry.getKey().equals(CommonToolProperties.KEY_INSTALLATION_ID)) {
          // special treatment
          continue;
        }
        if (!mSecureProps.containsKey(entry.getKey())) {
          mSecureProps.setProperty(entry.getKey(), entry.getValue());
          updatedSecureProps = true;
        }
      }
    }

    if (updatedSecureProps || updatedDeviceProps || updatedGeneralProps) {
      writeProperties(updatedSecureProps, updatedDeviceProps, updatedGeneralProps);
    }
  }

  private int getCurrentRevision() {
    int noResult = INVALID_REVISION;
    try {
      File dataFolder = new File(ODKFileUtils.getDataFolder(mAppName));
      String[] timestampNames = dataFolder.list(new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
          return s.startsWith(PROPERTIES_REVISION_FILENAME);
        }
      });
      if (timestampNames == null) {
        return noResult;
      }
      int bestResult = noResult;
      for (String timestampName : timestampNames) {
        String suffix = timestampName.substring(PROPERTIES_REVISION_FILENAME.length());
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
        } catch (NumberFormatException ignored) {
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
      @Override
      public boolean accept(File file, String s) {
        return s.startsWith(PROPERTIES_REVISION_FILENAME);
      }
    });
    if (timestampNames != null) {
      for (String name : timestampNames) {
        if (!name.equals(PROPERTIES_REVISION_FILENAME + suffix)) {
          File timestampFile = new File(dataFolder, name);
          if (!timestampFile.delete()) {
            throw new RuntimeException("Could not delete timestamp " + timestampFile.getName());
          }
        }
      }
    }
    return oldRevision;
  }

  private void readPropertiesIfModified() {

    int newRevision = INVALID_REVISION;

    {
      /*
       * Fetch the disk revision outside of a GainPropertiesLock. 
       * This is a fast-and-loose fetch. 
       * 
       * writeProperties() gains the lock and immediately updates the 
       * revision file before writing any updates.
       *  
       * If we detect any new revision here, outside of the lock, then 
       * readProperties(...) will gain the lock and obtain the actual
       * revision on disk, which might have changed from our newRevision
       * value, because multiple writeProperties() calls might have 
       * intervened between this fast-and-loose fetch and the current
       * thread gaining an exclusive lock within readProperties(...). 
       */
      newRevision = getCurrentRevision();
    }
    
    if (newRevision == INVALID_REVISION || newRevision != currentRevision) {
      readProperties(false);
    }

  }

  private void readProperties(boolean includingGlobalDeviceProps) {

    WebLogger.getLogger(mAppName)
        .i("PropertiesSingleton", "readProperties(" + includingGlobalDeviceProps + ")");

    GainPropertiesLock theLock = new GainPropertiesLock(mAppName, mAppLock);
    try {
      // OK. Now access files...
      FileInputStream configFileInputStream = null;
      try {
        File configFile = new File(ODKFileUtils.getAssetsFolder(mAppName),
            GENERAL_PROPERTIES_FILENAME);

        if (configFile.exists()) {
          configFileInputStream = new FileInputStream(configFile);

          mGeneralProps.clear();
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
      if (includingGlobalDeviceProps) {
        configFileInputStream = null;
        try {
          File configFile = new File(ODKFileUtils.getAssetsFolder(mAppName),
              DEFAULT_DEVICE_PROPERTIES_FILENAME);

          if (configFile.exists()) {
            configFileInputStream = new FileInputStream(configFile);

            mGlobalDeviceProps.clear();
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
        File configFile = new File(ODKFileUtils.getDataFolder(mAppName),
            DEVICE_PROPERTIES_FILENAME);

        if (configFile.exists()) {
          configFileInputStream = new FileInputStream(configFile);

          mDeviceProps.clear();
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
      if ( mHasSecureStorage ) {
        try {
          File configFile = new File(mSecureStorageDir, SECURE_PROPERTIES_FILENAME);

          if (configFile.exists()) {
            configFileInputStream = new FileInputStream(configFile);

            mSecureProps.clear();
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

        // get the installation id. This is the suffix of a file.
        // If no such file exists, create it.
        try {
          if ( mInstallationId == null ) {
            String[] names = mSecureStorageDir.list(new FilenameFilter() {
              @Override
              public boolean accept(File file, String s) {
                return file.getName().startsWith(SECURE_INSTALLATION_ID_FILENAME_PREFIX);
              }
            });
            if ( names == null || names.length == 0 ) {
              mInstallationId = UUID.randomUUID().toString();
              File installationFile = new File(mSecureStorageDir, SECURE_INSTALLATION_ID_FILENAME_PREFIX
                  + mInstallationId);
              installationFile.createNewFile();
            } else {
              // just in case -- if some start-up paths may generate 2 or more files
              // -- use the lexically first
              Arrays.sort(names);
              mInstallationId = names[0].substring(SECURE_INSTALLATION_ID_FILENAME_PREFIX.length());
            }
          }

        } catch (Exception e) {
          WebLogger.getLogger(mAppName).printStackTrace(e);
        }
      }

      currentRevision = getCurrentRevision();

    } finally {
      theLock.release();
    }
  }

  private File backupFile(File toBeOverwritten, int counter) {
    File tempPendingDelete = new File( toBeOverwritten.getParentFile(), 
        toBeOverwritten.getName() + ".del" + Integer.toString(counter));
    return tempPendingDelete;
  }
  
  private void replaceFile(File newFile, File toBeOverwritten) throws IOException {
    int counter = 0;
    // find a file that we can use as a backup file
    // if there is an old backup file, try to remove it
    // before generating yet another backup filename.
    File tempPendingDelete = backupFile(toBeOverwritten, counter++);
    while ( tempPendingDelete.exists() && !tempPendingDelete.delete()) {
      tempPendingDelete = backupFile(toBeOverwritten, counter++);
    }

    if ( toBeOverwritten.exists() ) {
      // rename the current file as the backup filename.
      boolean fileSuccess = toBeOverwritten.renameTo(tempPendingDelete);
      if (!fileSuccess) {
        WebLogger.getLogger(mAppName).i(TAG, "File Rename to Backup Failed! " + toBeOverwritten.getName());
        throw new IOException("Unable to rename configuration file");
      }
    }

    try {
      // rename the new file to the current file
      boolean fileSuccess = newFile.renameTo(toBeOverwritten);
      if (!fileSuccess) {
        WebLogger.getLogger(mAppName).i(TAG, "Temporary to Permanent File Rename Failed! " +
            toBeOverwritten.getName() );
        // try to restore the file we were attempting to replace
        // by renaming the file pending delete back to the current file.
        if ( tempPendingDelete.exists() ) {
          tempPendingDelete.renameTo(toBeOverwritten);
        }
        throw new IOException("Unable to replace configuration file");
      }
    } finally {
      // and if we get here, if the backup file exists, attempt to delete it.
      // (keeps the clutter at bay).
      if ( tempPendingDelete.exists() ) {
        tempPendingDelete.delete();
      }
    }
  }

  private void writeProperties(boolean updatedSecureProps, boolean updatedDeviceProps,
      boolean updatedGeneralProps) {

    GainPropertiesLock theLock = new GainPropertiesLock(mAppName, mAppLock);
    try {
      currentRevision = incrementAndWriteRevision(currentRevision);

      if (updatedGeneralProps) {
        try {
          File tempConfigFile = new File(ODKFileUtils.getAssetsFolder(mAppName),
              GENERAL_PROPERTIES_FILENAME + ".temp");
          FileOutputStream configFileOutputStream = new FileOutputStream(tempConfigFile, false);

          mGeneralProps.storeToXML(configFileOutputStream, null, StandardCharsets.UTF_8.name());
          configFileOutputStream.close();

          File configFile = new File(ODKFileUtils.getAssetsFolder(mAppName),
              GENERAL_PROPERTIES_FILENAME);

          replaceFile(tempConfigFile, configFile);

        } catch (Exception e) {
          WebLogger.getLogger(mAppName).printStackTrace(e);
        }
      }

      if (updatedDeviceProps) {
        try {
          File tempConfigFile = new File(ODKFileUtils.getDataFolder(mAppName),
              DEVICE_PROPERTIES_FILENAME + ".temp");
          FileOutputStream configFileOutputStream = new FileOutputStream(tempConfigFile, false);

          mDeviceProps.storeToXML(configFileOutputStream, null, StandardCharsets.UTF_8.name());
          configFileOutputStream.close();

          File configFile = new File(ODKFileUtils.getDataFolder(mAppName),
              DEVICE_PROPERTIES_FILENAME);

          replaceFile(tempConfigFile, configFile);

        } catch (Exception e) {
          WebLogger.getLogger(mAppName).printStackTrace(e);
        }
      }

      if (updatedSecureProps && mHasSecureStorage) {
        try {
          File tempConfigFile = new File(mSecureStorageDir, SECURE_PROPERTIES_FILENAME + ".temp");
          FileOutputStream configFileOutputStream = new FileOutputStream(tempConfigFile, false);

          mSecureProps.storeToXML(configFileOutputStream, null, StandardCharsets.UTF_8.name());
          configFileOutputStream.close();

          File configFile = new File(mSecureStorageDir, SECURE_PROPERTIES_FILENAME);

          replaceFile(tempConfigFile, configFile);

        } catch (Exception e) {
          WebLogger.getLogger(mAppName).printStackTrace(e);
        }
      }
    } finally {
      theLock.release();
    }
  }

  /**
   * Used only in services.clearAppPropertiesActivity
   */
  @SuppressWarnings("unused")
  public void clearSettings() {
    try {
      GainPropertiesLock theLock = new GainPropertiesLock(mAppName, mAppLock);

      try {
        currentRevision = incrementAndWriteRevision(currentRevision);

        File f;
        f = new File(ODKFileUtils.getDataFolder(mAppName), DEVICE_PROPERTIES_FILENAME);
        if (f.exists()) {
          if (!f.delete()) {
            throw new RuntimeException("Could not delete settings file " + f.getPath());
          }
        }

        if (mHasSecureStorage) {
          f = new File(mSecureStorageDir, SECURE_PROPERTIES_FILENAME);
          if (f.exists()) {
            if (!f.delete()) {
              throw new RuntimeException("Could not delete settings file " + f.getPath());
            }
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

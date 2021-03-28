/*
 * Copyright (C) 2012 The Android Open Source Project
 * Copyright (C) 2009-2013 University of Washington
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

package org.opendatakit.utilities;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.CheckResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.opendatakit.consts.CharsetConsts;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.provider.FormsColumns;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Static methods used for common file operations.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public final class ODKFileUtils {
  /**
   * A mapper that can read json strings into instances of the given class
   */
  public static final ObjectMapper mapper = new ObjectMapper();
  // 2nd level -- directories
  /**
   * Filename for the form definition
   */
  public static final String FORMDEF_JSON_FILENAME = "formDef.json";

  // 1st level -- appId
  private static final String MD5_COLON_PREFIX = "md5:";
  // Used for logging
  private static final String TAG = ODKFileUtils.class.getSimpleName();
  // Default app name when unspecified
  private static final String ODK_DEFAULT_APP_NAME = "default";
  // base path
  private static final String ODK_FOLDER_NAME = "opendatakit";

  // 3rd level -- directories

  // under config
  private static final String CONFIG_FOLDER_NAME = "config";

  // under config and data
  private static final String DATA_FOLDER_NAME = "data";

  // under data
  private static final String OUTPUT_FOLDER_NAME = "output";
  private static final String SYSTEM_FOLDER_NAME = "system";
  private static final String PERMANENT_FOLDER_NAME = "permanent";

  // under output and config/assets
  private static final String ASSETS_FOLDER_NAME = "assets";

  // under output
  private static final String TABLES_FOLDER_NAME = "tables";
  private static final String WEB_DB_FOLDER_NAME = "webDb";

  // under system
  private static final String GEO_CACHE_FOLDER_NAME = "geoCache";
  private static final String APP_CACHE_FOLDER_NAME = "appCache";

  // 4th level 

  // under the config/tables directory...
  private static final String CSV_FOLDER_NAME = "csv";
  private static final String LOGGING_FOLDER_NAME = "logging";
  /**
   * The name of the folder where the debug objects are written.
   */
  private static final String DEBUG_FOLDER_NAME = "debug";
  private static final String STALE_TABLES_FOLDER_NAME = "tables.deleting";

  /**
   * Miscellaneous well-known file names
   */
  private static final String PENDING_TABLES_FOLDER_NAME = "tables.pending";
  private static final String FORMS_FOLDER_NAME = "forms";
  // under the data/tables directory...
  // and under the output/csv/tableId.qual/ and config/assets/csv/tableId.qual/ directories
  private static final String INSTANCES_FOLDER_NAME = "instances";
  // under data/webDb
  private static final String DATABASE_NAME = "sqlite.db";
  // under data/webDb
  private static final String DATABASE_LOCK_FILE_NAME = "db.lock";
  /**
   * Filename holding app-wide definitions (just translations for now).
   */
  private static final String COMMON_DEFINITIONS_JS = "commonDefinitions.js";
  /**
   * Filename for the top-level configuration file (in assets)
   */
  private static final String ODK_TABLES_INIT_FILENAME = "tables.init";
  /**
   * Filename for the ODK Tables home screen (in assets)
   */
  private static final String ODK_TABLES_HOME_SCREEN_FILE_NAME = "index.html";
  /**
   * Filename of the config/tables/tableId/properties.csv file
   * that holds all kvs properties for this tableId.
   */
  private static final String PROPERTIES_CSV = "properties.csv";
  /**
   * Filename of the config/tables/tableId/definition.csv file
   * that holds the table schema for this tableId.
   */
  private static final String DEFINITION_CSV = "definition.csv";
  /**
   * Filename holding table-specific definitions (just translations for now).
   */
  private static final String TABLE_SPECIFIC_DEFINITIONS_JS = "tableSpecificDefinitions.js";
  public static final Pattern VALID_INSTANCE_ID_FOLDER_NAME_PATTERN = Pattern
      .compile("(\\p{P}|\\p{Z})");
  public static final Pattern VALID_FOLDER_PATTERN = Pattern
      .compile("^\\p{L}\\p{M}*(\\p{L}\\p{M}*|\\p{Nd}|_)+$");

  private static final Pattern FORWARD_SLASH_PATTERN = Pattern.compile("/");
  private static final Pattern FILE_SEPARATOR_PATTERN;
  static {
	  if ( File.separator.equals("/") ) {
		  FILE_SEPARATOR_PATTERN = Pattern.compile(File.separator);
	  } else {
		  FILE_SEPARATOR_PATTERN = Pattern.compile(File.separator + File.separator);
	  }
  }
  /**
   * Do not instantiate this class
   */
  private ODKFileUtils() {
  }

  /**
   * uri on web server begins with appName.
   * construct the full file.
   * <p>
   * return null if the file does not exist or is not an
   * accessible uri thru the WebServer. (i.e., the getAsFile() API).
   * used in services/fi.iki.elonen.SimpleWebServer
   * @param uri The uri to translate
   * @return A file with the correct path
   */
  @SuppressWarnings("unused")
  public static File fileFromUriOnWebServer(String uri) {
    String appName;
    String uriFragment;
    int idxAppName = uri.indexOf('/');
    if (idxAppName == -1) {
      return null;
    }
    if (idxAppName == 0) {
      idxAppName = uri.indexOf('/', idxAppName + 1);
      if (idxAppName == -1) {
        return null;
      }
      appName = uri.substring(1, idxAppName);
      uriFragment = uri.substring(idxAppName + 1);
    } else {
      appName = uri.substring(0, idxAppName);
      uriFragment = uri.substring(idxAppName + 1);
    }

    File filename = getAsFile(appName, uriFragment);
    if (!filename.exists()) {
      return null;
    }

    String[] parts = FORWARD_SLASH_PATTERN.split(uriFragment);
    if (parts.length > 1) {
      switch (parts[0]) {
      case CONFIG_FOLDER_NAME:
      case SYSTEM_FOLDER_NAME:
      case PERMANENT_FOLDER_NAME:
        return filename;
      case DATA_FOLDER_NAME:
        if (parts.length > 2 && parts[1].equals(TABLES_FOLDER_NAME)) {
          return filename;
        }
        break;
      default:
        // ignore;
      }
    }
    return null;
  }

  /**
   * Used in AndroidOdkConnection and OdkConnectionFactoryAbstractClass, both in services.database
   *
   * @return sqlite.db
   */
  @SuppressWarnings("unused")
  public static String getNameOfSQLiteDatabase() {
    return DATABASE_NAME;
  }

  /**
   * Used in services.database.OdkConnectionFactoryAbstractClass
   *
   * @return db.lock
   */
  @SuppressWarnings("unused")
  public static String getNameOfSQLiteDatabaseLockFile() {
    return DATABASE_LOCK_FILE_NAME;
  }

  /**
   * Throws an exception if there is no sd card available
   */
  public static void verifyExternalStorageAvailability() {
    String cardstatus = Environment.getExternalStorageState();
    if (cardstatus.equals(Environment.MEDIA_REMOVED) || cardstatus
        .equals(Environment.MEDIA_UNMOUNTABLE) || cardstatus.equals(Environment.MEDIA_UNMOUNTED)
        || cardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY) || cardstatus
        .equals(Environment.MEDIA_SHARED)) {
      throw new RuntimeException(
          "ODK reports :: SDCard error: " + Environment.getExternalStorageState());
    }
  }

  /**
   * Used all over the place
   *
   * @return /sdcard/opendatakit
   */
  @SuppressWarnings("WeakerAccess")
  public static String getOdkFolder() {
    return Environment.getExternalStorageDirectory().getPath() + File.separator + ODK_FOLDER_NAME;
  }

  /**
   * Used in AggregateSynchronizer and ProcessManifestContentAndFileChanges
   *
   * @param path the path of the folder to create
   * @return whether it was created successfully or not
   */
  @CheckResult
  @SuppressWarnings("unused")
  public static boolean createFolder(String path) {
    File dir = new File(path);
    //noinspection SimplifiableIfStatement
    if (dir.exists()) {
      if (dir.isDirectory()) {
        return true;
      }
      dir.delete();
    }
    return dir.mkdirs();
  }

  /**
   * Used all over the place
   *
   * @return default
   */
  @SuppressWarnings("unused")
  public static String getOdkDefaultAppName() {
    return ODK_DEFAULT_APP_NAME;
  }

  /**
   * Used in AndroidShortcuts
   *
   * @return a list of all the folders in /sdcard/opendatakit
   */
  @SuppressWarnings("unused")
  public static File[] getAppFolders() {
    return new File(getOdkFolder()).listFiles(new FileFilter() {

      @Override
      public boolean accept(File pathname) {
        return pathname.isDirectory();
      }
    });
  }

  /**
   * Makes sure that all the necessary directories exist for the given app name
   * @param appName the app name to configure
   */
  public static void assertDirectoryStructure(String appName) {
    String[] dirs = { getAppFolder(appName), getConfigFolder(appName), getDataFolder(appName),
        getOutputFolder(appName), getSystemFolder(appName), getPermanentFolder(appName),
        // under Config
        getAssetsFolder(appName), getTablesFolder(appName),
        // under Data
        getAppCacheFolder(appName), getGeoCacheFolder(appName), getWebDbFolder(appName),
        getTableDataFolder(appName),
        // under Output
        getLoggingFolder(appName), getOutputCsvFolder(appName), getTablesDebugObjectFolder(appName),
        // under System
        getPendingDeletionTablesFolder(appName), getPendingInsertionTablesFolder(appName) };

    for (String dirName : dirs) {
      File dir = new File(dirName);
      File badDir = new File(dir.getParent(), dir.getName() + ".bad");
      if (badDir.exists()) {
        if (!badDir.delete()) {
          throw new RuntimeException("Cannot remove bad directory " + badDir);
        }
      }
      if (!dir.exists()) {
        if (!dir.mkdirs()) {
          throw new RuntimeException("Cannot create directory: " + dirName);
        }
      } else {
        if (!dir.isDirectory()) {
          File retryDir = new File(dir.getParent(), dir.getName());
          if (dir.renameTo(badDir)) {
            if (!retryDir.mkdirs()) {
              throw new RuntimeException("Cannot create directory: " + dirName);
            }
          } else {
            throw new RuntimeException(dirName + " exists, but is not a directory");
          }
        }
      }
    }
    // and create an empty .nomedia file
    File nomedia = new File(getAppFolder(appName), ".nomedia");
    try {
      if (!nomedia.exists() && !nomedia.createNewFile()) {
        throw new IOException();
      }
    } catch (IOException ex) {
      // DO NOT CHANGE THIS TO WebLogger
      // WebLogger.getLogger calls assertDirectoryStructure, and we will end up in an infinite
      // loop of failures
      Log.e(TAG, "Cannot create .nomedia in app directory: " + ex);
      // Also, don't throw an exception or the tests will fail
    }
  }

  /**
   * This routine clears all the marker files used by the various tools to
   * avoid re-running the initialization task.
   * <p>
   * Used in services.preferences.activities.ClearAppPropertiesActivity
   *
   * @param appName the app name
   */
  @SuppressWarnings("unused")
  public static void clearConfiguredToolFiles(String appName) {
    File dataDir = new File(ODKFileUtils.getDataFolder(appName));
    File[] filesToDelete = dataDir.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
          return false;
        }
        String name = pathname.getName();
        int idx = name.lastIndexOf('.');
        if (idx == -1) {
          return false;
        }
        String type = name.substring(idx + 1);
        return "version".equals(type);
      }
    });
    for (File f : filesToDelete) {
      if (!f.delete()) {
        throw new RuntimeException("Could not delete file " + f.getAbsolutePath());
      }
    }
  }

  /**
   * Writes the given tool version to the tool's associated version file
   * @param appName the app name
   * @param toolName the name of the tool, used to generate the version file name
   * @param apkVersion the version to write
   */
  public static void assertConfiguredToolApp(String appName, String toolName, String apkVersion) {
    writeConfiguredOdkAppVersion(appName, toolName + ".version", apkVersion);
  }

  /**
   * TODO this is almost identical to checkOdkAppVersion
   *
   * @param appName           the app name
   * @param odkAppVersionFile the file that contains the installed version
   * @param apkVersion        the version to overwrite odkAppVerisonFile with
   */
  private static void writeConfiguredOdkAppVersion(String appName, String odkAppVersionFile,
      String apkVersion) {
    File versionFile = new File(getDataFolder(appName), odkAppVersionFile);

    if (!versionFile.exists()) {
      if (!versionFile.getParentFile().mkdirs()) {
        //throw new RuntimeException("Failed mkdirs on " + versionFile.getPath());
        WebLogger.getLogger(appName)
            .e(TAG, "Failed mkdirs on " + versionFile.getParentFile().getPath());
      }
    }

    FileOutputStream fs = null;
    OutputStreamWriter w = null;
    BufferedWriter bw = null;
    try {
      fs = new FileOutputStream(versionFile, false);
      //noinspection deprecation
      w = new OutputStreamWriter(fs, CharsetConsts.UTF_8);
      bw = new BufferedWriter(w);
      bw.write(apkVersion);
      bw.write("\n");
    } catch (IOException e) {
      WebLogger.getLogger(appName).printStackTrace(e);
    } finally {
      if (bw != null) {
        try {
          bw.flush();
          bw.close();
        } catch (IOException e) {
          WebLogger.getLogger(appName).printStackTrace(e);
        }
      }
      if (w != null) {
        try {
          w.close();
        } catch (IOException e) {
          WebLogger.getLogger(appName).printStackTrace(e);
        }
      }
      if (fs != null) {
        try {
          fs.close();
        } catch (IOException e) {
          WebLogger.getLogger(appName).printStackTrace(e);
        }
      }
    }
  }

  /**
   * Used in InitializationUtil
   *
   * @param appName    the app name
   * @param toolName   the name of the app we want to check the version of
   * @param apkVersion the version we want to match
   * @return whether the passed apk version matches the version in the file
   */
  public static boolean isConfiguredToolApp(String appName, String toolName, String apkVersion) {
    return checkOdkAppVersion(appName, toolName + ".version", apkVersion);
  }

  /**
   * TODO this is almost identical to writeConfiguredOdkAppVersion
   *
   * @param appName           the app name
   * @param odkAppVersionFile the file that contains the installed app version
   * @param apkVersion        the version to match against
   * @return whether the passed apk version matches the version in the file
   */
  private static boolean checkOdkAppVersion(String appName, String odkAppVersionFile,
      String apkVersion) {
    File versionFile = new File(getDataFolder(appName), odkAppVersionFile);

    if (!versionFile.exists()) {
      return false;
    }

    String versionLine = null;
    FileInputStream fs = null;
    InputStreamReader r = null;
    BufferedReader br = null;
    try {
      fs = new FileInputStream(versionFile);
      //noinspection deprecation
      r = new InputStreamReader(fs, CharsetConsts.UTF_8);
      br = new BufferedReader(r);
      versionLine = br.readLine();
    } catch (IOException e) {
      WebLogger.getLogger(appName).printStackTrace(e);
      return false;
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          WebLogger.getLogger(appName).printStackTrace(e);
        }
      }
      if (r != null) {
        try {
          r.close();
        } catch (IOException e) {
          WebLogger.getLogger(appName).printStackTrace(e);
        }
      }
      try {
        if (fs != null) {
          fs.close();
        }
      } catch (IOException e) {
        WebLogger.getLogger(appName).printStackTrace(e);
      }
    }

    String[] versionRange = versionLine.split(";");
    for (String version : versionRange) {
      if (version.trim().equals(apkVersion)) {
        return true;
      }
    }
    return false;
  }

  private static File fromAppPath(String appPath) {
    String[] terms = FILE_SEPARATOR_PATTERN.split(appPath);
    if (terms.length < 1) {
      return null;
    }
    return new File(new File(getOdkFolder()), appPath);
  }

  /**
   * Gets the folder that contains all the configuration for a particular app
   * @param appName the app name
   * @return opendatakit/:app_name
   */
  public static String getAppFolder(String appName) {
    return getOdkFolder() + File.separator + appName;
  }

  // 1st level folders

  /**
   * Used in ScanUtils, ProcessManifestContentAndFileChanges
   *
   * @param appName the app name
   * @return :app_name/config
   */
  @SuppressWarnings("WeakerAccess")
  public static String getConfigFolder(String appName) {
    return getAppFolder(appName) + File.separator + CONFIG_FOLDER_NAME;
  }

  /**
   * Used in GainPropertiesLock and PropertiesSingleton
   *
   * @param appName the app name
   * @return :app_name/data
   */
  public static String getDataFolder(String appName) {
    return getAppFolder(appName) + File.separator + DATA_FOLDER_NAME;
  }

  /**
   * Used only in NanoHTTPD
   *
   * @param appName the app name
   * @return :app_name/output
   */
  @SuppressWarnings("WeakerAccess")
  public static String getOutputFolder(String appName) {
    return getAppFolder(appName) + File.separator + OUTPUT_FOLDER_NAME;
  }

  /**
   * Used in MainMenuActivity and scan.utils.ScanUtils
   *
   * @param appName the app name
   * @return :app_name/system
   */
  @SuppressWarnings("WeakerAccess")
  public static String getSystemFolder(String appName) {
    return getAppFolder(appName) + File.separator + SYSTEM_FOLDER_NAME;
  }

  private static String getPermanentFolder(String appName) {
    return getAppFolder(appName) + File.separator + PERMANENT_FOLDER_NAME;
  }

  //////////////////////////////////////////////////////////
  // Everything under config folder

  /**
   * Gets the folder with the static app configuration data
   * @param appName the app name
   * @return :app_name/config/assets
   */
  public static String getAssetsFolder(String appName) {
    return getConfigFolder(appName) + File.separator + ASSETS_FOLDER_NAME;
  }

  /**
   * Gets the folder with the csv files to import on first run
   * @param appName the app name
   * @return :app_name/config/assets/csv
   */
  public static String getAssetsCsvFolder(String appName) {
    return getAssetsFolder(appName) + File.separator + CSV_FOLDER_NAME;
  }

  /**
   * Used in CsvUtil
   *
   * @param appName the app name
   * @param tableId the id of the table to get the csv folder for
   * @return :app_name/assets/csv/:table_id/instances
   */
  public static String getAssetsCsvInstancesFolder(String appName, String tableId) {
    return getAssetsCsvFolder(appName) + File.separator + tableId + File.separator
        + INSTANCES_FOLDER_NAME;
  }

  /**
   * Used in CsvUtil, and AbstractPermissionsTestCase in services
   *
   * @param appName    the app name
   * @param tableId    the id of the table to get the instance for
   * @param instanceId the id of the instance to find
   * @return :app_name/assets/csv/:table_id/instances/:instance_id
   */
  public static String getAssetsCsvInstanceFolder(String appName, String tableId,
      String instanceId) {
    return getAssetsCsvInstancesFolder(appName, tableId) + File.separator
        + safeInstanceIdFolderName(instanceId);
  }

  /**
   * Get the path to the tables initialization file for the given app.
   * Used in services.sync.service.logic.ProcessManifestContentAndFileChanges, InitializationUtil
   *
   * @param appName the app name
   * @return :app_name/assets/tables.init
   */
  public static String getTablesInitializationFile(String appName) {
    return getAssetsFolder(appName) + File.separator + ODK_TABLES_INIT_FILENAME;
  }

  /**
   * Get the path to the common definitions file for the given app.
   * Used in LocalizationUtils
   *
   * @param appName the app name
   * @return :app_name/assets/commonDefinitions.js
   */
  @SuppressWarnings("WeakerAccess")
  public static String getCommonDefinitionsFile(String appName) {
    return getAssetsFolder(appName) + File.separator + COMMON_DEFINITIONS_JS;
  }

  /**
   * Get the path to the user-defined home screen file.
   * Used in tables.activities.MainActivity
   *
   * @param appName the app name
   * @return :app_name/assets/index.html
   */
  @SuppressWarnings("unused")
  public static String getTablesHomeScreenFile(String appName) {
    return getAssetsFolder(appName) + File.separator + ODK_TABLES_HOME_SCREEN_FILE_NAME;
  }

  /**
   * Used in InitializationUtil, services.sync.service.logic.ProcessManifestContentAndFileChanges
   *
   * @param appName the app name
   * @return :app_name/config/tables
   */
  public static String getTablesFolder(String appName) {
    return getConfigFolder(appName) + File.separator + TABLES_FOLDER_NAME;
  }

  /**
   * Used in AggregateSynchronizer, services.database.utilities.ODKDatabaseImplUtils,
   * services.sync.service.logic.ProcessManifestContentAndFileChanges
   *
   * @param appName the app name
   * @param tableId the id of the table to get the folder for
   * @return :app_name/config/tables/:table_id
   */
  @SuppressWarnings("WeakerAccess")
  public static String getTablesFolder(String appName, String tableId) {
    String path;
    if (tableId == null || tableId.isEmpty()) {
      throw new IllegalArgumentException("getTablesFolder: tableId is null or the empty string!");
    } else {
      if (!VALID_FOLDER_PATTERN.matcher(tableId).matches()) {
        throw new IllegalArgumentException(
            "getFormFolder: tableId does not begin with a letter and contain only letters, digits or underscores!");
      }
      if (FormsColumns.COMMON_BASE_FORM_ID.equals(tableId)) {
        path = getAssetsFolder(appName) + File.separator + FormsColumns.COMMON_BASE_FORM_ID;
      } else {
        path = getTablesFolder(appName) + File.separator + tableId;
      }
    }
    File f = new File(path);
    if (!f.exists() && !f.mkdirs()) {
      throw new RuntimeException("Could not mkdirs on " + f.getPath());
    }
    return f.getAbsolutePath();
  }

  // files under that

  /**
   * @param appName the app name
   * @param tableId the table id
   * @return :app_name/config/tables/:table_id/definition.csv
   */
  public static String getTableDefinitionCsvFile(String appName, String tableId) {
    return getTablesFolder(appName, tableId) + File.separator + DEFINITION_CSV;
  }

  /**
   * @param appName the app name
   * @param tableId the table id
   * @return :app_name/config/tables/:table_id/properties.csv
   */
  public static String getTablePropertiesCsvFile(String appName, String tableId) {
    return getTablesFolder(appName, tableId) + File.separator + PROPERTIES_CSV;
  }

  /**
   * Used in LocalizationUtils
   *
   * @param appName the app name
   * @param tableId the id of the table to find the definitions file for
   * @return :app_name/config/tables/:table_id/tableSpecificDefinitions.js
   */
  static String getTableSpecificDefinitionsFile(String appName, String tableId) {
    return getTablesFolder(appName, tableId) + File.separator + TABLE_SPECIFIC_DEFINITIONS_JS;
  }

  /**
   * Used in InitializationUtil
   *
   * @param appName the app name
   * @param tableId the table id to find the forms for
   * @return :app_name/config/tables/:table_id/forms
   */
  public static String getFormsFolder(String appName, String tableId) {
    return getTablesFolder(appName, tableId) + File.separator + FORMS_FOLDER_NAME;
  }

  /**
   * Gets the form folder for the given app, table and form id, contains the formDef.json file
   * @param appName the app name
   * @param tableId the table associated with the form
   * @param formId the form to get the folder for
   * @return :app_name/config/tables/:table_id/forms/:form_id/
   */
  public static String getFormFolder(String appName, String tableId, String formId) {
    if (formId == null || formId.isEmpty()) {
      throw new IllegalArgumentException("getFormFolder: formId is null or the empty string!");
    } else {
      if (!VALID_FOLDER_PATTERN.matcher(formId).matches()) {
        throw new IllegalArgumentException(
            "getFormFolder: formId does not begin with a letter and contain only letters, digits or underscores!");
      }
      return getFormsFolder(appName, tableId) + File.separator + formId;
    }
  }

  /////////////////////////////////////////////////////////
  // Everything under data folder

  /**
   * Returns the filename that contains the status for whether tables has finished its one time
   * setup or not
   * @param appName the app name
   * @return :app_name/data/tables.init
   */
  public static String getTablesInitializationCompleteMarkerFile(String appName) {
    return getDataFolder(appName) + File.separator + ODK_TABLES_INIT_FILENAME;
  }

  /**
   * Used in androidCommon.views.ODKWebView
   *
   * @param appName the app name
   * @return :app_name/data/appCache
   */
  @SuppressWarnings("WeakerAccess")
  public static String getAppCacheFolder(String appName) {
    return getDataFolder(appName) + File.separator + APP_CACHE_FOLDER_NAME;
  }

  /**
   * Used in survey.activities.DrawActivity
   *
   * @param appName the app name
   * @return :app_name/data/appCache/tmpDraw.jpg
   */
  @SuppressWarnings("unused")
  public static String getTempDrawFile(String appName) {
    return getAppCacheFolder(appName) + File.separator + "tmpDraw.jpg";
  }

  /**
   * Used in survey.activities.DrawActivity
   *
   * @param appName the app name
   * @return :app_name/data/appCache/tmp.jpg
   */
  @SuppressWarnings("unused")
  public static String getTempFile(String appName) {
    return getAppCacheFolder(appName) + File.separator + "tmp.jpg";
  }

  /**
   * Used in ODKWebView
   *
   * @param appName the app name
   * @return :app_name/data/geoCache
   */
  @SuppressWarnings("WeakerAccess")
  public static String getGeoCacheFolder(String appName) {
    return getDataFolder(appName) + File.separator + GEO_CACHE_FOLDER_NAME;
  }

  /**
   * Used in AndroidOdkConnection, OdkConnectionFactoryAbstractClass, SQLiteConnection (all in
   * services)
   *
   * @param appName the app name
   * @return :app_name/data/webDb
   */
  @SuppressWarnings("WeakerAccess")
  public static String getWebDbFolder(String appName) {
    return getDataFolder(appName) + File.separator + WEB_DB_FOLDER_NAME;
  }

  /**
   * used only locally, but feel free to make it public if you need to use it
   *
   * @param appName the app name
   * @return :app_name/data/tables
   */
  private static String getTableDataFolder(String appName) {
    return getDataFolder(appName) + File.separator + TABLES_FOLDER_NAME;
  }

  /**
   * Used in CsvUtil, scan.utils.ScanUtil
   *
   * @param appName the app name
   * @param tableId the id of the table to find the instances folder for
   * @return :app_name/data/tables/:table_id/instances
   */
  public static String getInstancesFolder(String appName, String tableId) {
    String path;
    path = getTableDataFolder(appName) + File.separator + tableId + File.separator
        + INSTANCES_FOLDER_NAME;

    File f = new File(path);
    if (!f.exists() && !f.mkdirs()) {
      throw new RuntimeException("Could not mkdirs on " + f.getPath());
    }
    return f.getAbsolutePath();
  }

  private static String safeInstanceIdFolderName(String instanceId) {
    if (instanceId == null || instanceId.isEmpty()) {
      throw new IllegalArgumentException(
          "getInstanceFolder: instanceId is null or the empty string!");
    } else {
      return VALID_INSTANCE_ID_FOLDER_NAME_PATTERN.matcher(instanceId).replaceAll("_");
    }
  }

  /**
   * @param appName    the app name
   * @param tableId    the table id
   * @param instanceId the instance id
   * @return :app_name/data/tables/:table_id/instances/:instance_id
   */
  public static String getInstanceFolder(String appName, String tableId, String instanceId) {
    String path;
    String instanceFolder = safeInstanceIdFolderName(instanceId);

    path = getInstancesFolder(appName, tableId) + File.separator + instanceFolder;

    File f = new File(path);
    if (!f.exists() && !f.mkdirs()) {
      throw new RuntimeException("Could not mkdirs on " + f.getPath());
    }
    return f.getAbsolutePath();
  }

  /**
   * Used mostly in Media activities in survey, but also in AndroidSynchronizer
   * TODO I really can't document this without knowing what a rowpath is
   *
   * @param appName    the app name
   * @param tableId    the table id
   * @param instanceId the instance id
   * @param rowpathUri The URI to a rowpath, used to get the filename
   * @return the filename of the rowpath
   */
  public static File getRowpathFile(String appName, String tableId, String instanceId,
      String rowpathUri) {
    // clean up the value...
    if (rowpathUri.startsWith("/")) {
      rowpathUri = rowpathUri.substring(1);
    }
    String instanceFolder = ODKFileUtils.getInstanceFolder(appName, tableId, instanceId);
    String instanceUri = ODKFileUtils.asUriFragment(appName, new File(instanceFolder));
    String fileUri;
    if (rowpathUri.startsWith(instanceUri)) {
      // legacy construction
      WebLogger.getLogger(appName)
          .e(TAG, "table [" + tableId + "] contains old-style rowpath constructs!");
      fileUri = rowpathUri;
    } else {
      fileUri = instanceUri + "/" + rowpathUri;
    }
    return ODKFileUtils.getAsFile(appName, fileUri);
  }

  /**
   * Used in AggregateSynchronizer, scan.activities.JSON2SurveyJSONActivity and several of
   * survey's Media activities
   *
   * @param appName    the app name
   * @param tableId    the table id
   * @param instanceId the instance id
   * @param rowFile    the file that the rowpath uri will point to
   * @return a uri that can be used for an intent and later dereferenced to get a rowpath file
   */
  @SuppressWarnings("unused")
  public static String asRowpathUri(String appName, String tableId, String instanceId,
      File rowFile) {
    String instanceFolder = ODKFileUtils.getInstanceFolder(appName, tableId, instanceId);
    String instanceUri = ODKFileUtils.asUriFragment(appName, new File(instanceFolder));
    String rowpathUri = ODKFileUtils.asUriFragment(appName, rowFile);
    if (!rowpathUri.startsWith(instanceUri)) {
      throw new IllegalArgumentException(
          "asRowpathUri -- rowFile is not in a valid rowpath location!");
    }
    String relativeUri = rowpathUri.substring(instanceUri.length());
    if (relativeUri.startsWith("/")) {
      relativeUri = relativeUri.substring(1);
    }
    return relativeUri;
  }

  ///////////////////////////////////////////////
  // Everything under output folder

  /**
   * Used only in WebLoggerImpl
   *
   * @param appName the app name
   * @return :app_name/output/logging
   */
  public static String getLoggingFolder(String appName) {
    return getOutputFolder(appName) + File.separator + LOGGING_FOLDER_NAME;
  }

  /**
   * Used in SimpleWebServer, tables.utils.OutputUtil
   *
   * @param appName the app name
   * @return :app_name/output/debug
   */
  @SuppressWarnings("WeakerAccess")
  public static String getTablesDebugObjectFolder(String appName) {
    String outputFolder = getOutputFolder(appName);
    return outputFolder + File.separator + DEBUG_FOLDER_NAME;
  }

  /**
   * Used only in CSVUtil
   *
   * @param appName the app name
   * @return :app_name/output/csv
   */
  public static String getOutputCsvFolder(String appName) {
    return getOutputFolder(appName) + File.separator + CSV_FOLDER_NAME;
  }

  /**
   * Used only in CsvUtil
   *
   * @param appName    the app name
   * @param tableId    the table id of the instance
   * @param instanceId the instance id of the form submission
   * @return :app_name/output/csv/:table_id/instances/:instance_id
   */
  public static String getOutputCsvInstanceFolder(String appName, String tableId,
      String instanceId) {
    return getOutputCsvFolder(appName) + File.separator + tableId + File.separator
        + INSTANCES_FOLDER_NAME + File.separator + safeInstanceIdFolderName(instanceId);
  }

  /**
   * @param appName       the app name
   * @param tableId       the id of the table to export
   * @param fileQualifier An optional tag that is appended to the end of the four exported files
   *                      with a dot
   * @return :app_name/output/csv/:table_id(.:file_qualifier).csv
   */
  public static String getOutputTableCsvFile(String appName, String tableId, String fileQualifier) {
    return getOutputCsvFolder(appName) + File.separator + tableId + (
        fileQualifier != null && !fileQualifier.isEmpty() ? "." + fileQualifier : "") + ".csv";
  }

  /**
   * @param appName       the app name
   * @param tableId       the id of the table to export
   * @param fileQualifier An optional tag that is appended to the end of the four exported files
   *                      with a dot
   * @return :app_name/output/csv/:table_id(.:file_qualifier).definition.csv
   */
  public static String getOutputTableDefinitionCsvFile(String appName, String tableId,
      String fileQualifier) {
    return getOutputCsvFolder(appName) + File.separator + tableId + (
        fileQualifier != null && !fileQualifier.isEmpty() ? "." + fileQualifier : "") + "."
        + DEFINITION_CSV;
  }

  /**
   * @param appName       the app name
   * @param tableId       the id of the table to export
   * @param fileQualifier An optional tag that is appended to the end of the four exported files
   *                      with a dot
   * @return :app_name/output/csv/:table_id(.:file_qualifier).properties.csv
   */
  public static String getOutputTablePropertiesCsvFile(String appName, String tableId,
      String fileQualifier) {
    return getOutputCsvFolder(appName) + File.separator + tableId + (
        fileQualifier != null && !fileQualifier.isEmpty() ? "." + fileQualifier : "") + "."
        + PROPERTIES_CSV;
  }

  ////////////////////////////////////////
  // Everything under system folder

  /**
   * Used in FormsProvider, InitializationUtil
   *
   * @param appName the table id
   * @return :app_name/system/tables.deleting
   */
  public static String getPendingDeletionTablesFolder(String appName) {
    return getSystemFolder(appName) + File.separator + STALE_TABLES_FOLDER_NAME;
  }

  /**
   * used only locally, by assertDirectoryStructure
   *
   * @param appName the table id
   * @return :app_name/system/tables.pending
   */
  private static String getPendingInsertionTablesFolder(String appName) {
    return getSystemFolder(appName) + File.separator + PENDING_TABLES_FOLDER_NAME;
  }

  // 4th level config tables tableId folder

  // 3rd level output

  /**
   * Used in services.preferences.fragments.DeviceSettingsFragment
   *
   * @param appName the app name
   * @param path    the path to check against
   * @return whether the path is under :app_name/
   */
  @SuppressWarnings("unused")
  public static boolean isPathUnderAppName(String appName, File path) {

    File parentDir = new File(getAppFolder(appName));

    while (path != null && !path.equals(parentDir)) {
      path = path.getParentFile();
    }

    return path != null;
  }

  /**
   * Used in AggregateSynchronizer, services.sync.logic.ProcessManifestContentAndFileChanges
   *
   * @param path the path that's under an app
   * @return the app name
   */
  @SuppressWarnings("unused")
  public static String extractAppNameFromPath(File path) {
    if (path == null) {
      return null;
    }

    File parent = path.getParentFile();
    File odkDir = new File(getOdkFolder());
    while (parent != null && !parent.equals(odkDir)) {
      path = parent;
      parent = path.getParentFile();
    }

    if (parent == null) {
      return null;
    }
    return path.getName();
  }

  /**
   * Returns the relative path beginning after the getAppFolder(appName) directory.
   * The relative path does not start or end with a '/'
   *
   * @param appName          the app name
   * @param fileUnderAppName a file that's under :app_name/
   * @return a relative path to that file
   */
  public static String asRelativePath(String appName, File fileUnderAppName) {
    // convert fileUnderAppName to a relative path such that if
    // we just append it to the AppFolder, we have a full path.
    File parentDir = new File(getAppFolder(appName));

    ArrayList<String> pathElements = new ArrayList<>();

    File f = fileUnderAppName;
    while (f != null && !f.equals(parentDir)) {
      pathElements.add(f.getName());
      f = f.getParentFile();
    }

    if (f == null) {
      throw new IllegalArgumentException(
          "file is not located under this appName (" + appName + ")!");
    }

    StringBuilder b = new StringBuilder();
    for (int i = pathElements.size() - 1; i >= 0; --i) {
      String element = pathElements.get(i);
      b.append(element);
      if (i != 0) {
        b.append(File.separator);
      }
    }
    return b.toString();

  }

  /**
   * Returns the requested file as a part of a Uri object
   * @param appName the app name
   * @param fileUnderAppName a file with a path inside /sdcard/opendatakit/:app_name
   * @return the filename represented as an encoded part of a uri fragment
   */
  public static String asUriFragment(String appName, File fileUnderAppName) {
    String relativePath = asRelativePath(appName, fileUnderAppName);
    String separatorString;
    if (File.separatorChar == '\\') {
      // Windows Robolectric
      separatorString = File.separator + File.separator;
    } else {
      separatorString = File.separator;
    }
    String[] segments = relativePath.split(separatorString);
    String path;
    {
      Uri.Builder b = Uri.parse("http://localhost/").buildUpon();
      for (String s : segments) {
        b.appendPath(s);
      }
      Uri u = b.build();
      path = u.getEncodedPath().substring(1); // omit the leading slash
    }
    return path;
  }

  /**
   * Reconstructs the full path from the appName and uriFragment
   * Used in AggregateSynchronizer, FileSet and services.submissions.provider.SubmissionProvider
   *
   * @param appName     the app name
   * @param uriFragment a uri that represents a fully qualified path to a file
   * @return a File object to the represented path
   */
  @SuppressWarnings("WeakerAccess")
  public static File getAsFile(String appName, String uriFragment) {

    if (appName == null || appName.isEmpty()) {
      throw new IllegalArgumentException("Not a valid uriFragment: " + appName + "/" + uriFragment
          + " application not specified.");
    }
    if (uriFragment == null || uriFragment.isEmpty()) {
      throw new IllegalArgumentException("Not a valid uriFragment: " + appName + "/" + uriFragment
          + " uri path not specified.");
    }

    File f = fromAppPath(appName);
    if (f == null || !f.exists() || !f.isDirectory()) {
      throw new IllegalArgumentException(
          "Not a valid uriFragment: " + appName + "/" + uriFragment + " - invalid application.");
    }

    // build a bogus Uri so that we can let Uri encode or decode the uriFragment
    // by appending an encoded segment, any embedded slashes will be interpreted as
    // segment delimiters -- which is exactly what we want.
    Uri u = Uri.parse("http://localhost/").buildUpon().appendEncodedPath(uriFragment).build();

    List<String> segments = u.getPathSegments();
    for (String s : segments) {
      f = new File(f, s);
    }
    return f;
  }

  /**
   * Convert a relative path into an application filename. Used all over the place
   *
   * @param appName      the app name
   * @param relativePath the relative path to a file
   * @return A file object to :app_name/*:relative_path
   */
  @SuppressWarnings("unused")
  public static File asAppFile(String appName, String relativePath) {
    return new File(getAppFolder(appName) + File.separator + relativePath);
  }

  /**
   * Used in services.preferences.fragments.DeviceSettingsFragment,
   * services.sync.logic.ProcessManifestContentAndFileChanges
   *
   * @param appName      the app name
   * @param relativePath the relative path to a config file
   * @return a file object to :app_name/config/*:relative_path
   */
  @SuppressWarnings("unused")
  public static File asConfigFile(String appName, String relativePath) {
    return new File(getConfigFolder(appName) + File.separator + relativePath);
  }

  /**
   * Used in AggregateSynchronizer
   *
   * @param appName                the app name
   * @param fileUnderAppConfigName a file object to a file under :app_name/config/
   * @return the relative path to that file
   */
  @SuppressWarnings("unused")
  public static String asConfigRelativePath(String appName, File fileUnderAppConfigName) {
    String relativePath = asRelativePath(appName, fileUnderAppConfigName);
    if (!relativePath.startsWith(CONFIG_FOLDER_NAME + File.separator)) {
      throw new IllegalArgumentException("File is not located under config folder");
    }
    relativePath = relativePath.substring(CONFIG_FOLDER_NAME.length() + File.separator.length());
    if (relativePath.contains(File.separator + "..")) {
      throw new IllegalArgumentException("File contains " + File.separator + "..");
    }
    return relativePath;
  }

  /**
   * The formPath is relative to the framework directory and is passed into
   * the WebKit to specify the form to display.
   * Used in (survey) FormIdStruct, MainMenuActivity, (services) FormInfo
   *
   * @param appName     the app name
   * @param formDefFile a file object to a formDef.json
   * @return a relative path to that formDef file
   */
  @SuppressWarnings("unused")
  public static String getRelativeFormPath(String appName, File formDefFile) {

    // compute FORM_PATH...
    // we need to do this relative to the AppFolder, as the
    // common index.html is under the ./system folder.

    String relativePath = asRelativePath(appName, formDefFile.getParentFile());
    // adjust for relative path from ./system...
    relativePath = ".." + File.separator + relativePath + File.separator;
    return relativePath;
  }

  /**
   * Used as the baseUrl in the webserver, exposed to the javascript interface via getBaseUrl in
   * OdkCommon
   *
   * @return ../system
   */
  @SuppressWarnings("unused")
  public static String getRelativeSystemPath() {
    return ".." + File.separator + ODKFileUtils.SYSTEM_FOLDER_NAME;
  }

  /**
   * Gets the md5sum of the given file with the correct prefix
   * @param appName the app name
   * @param file the file to md5
   * @return md5:(:the hash of the file)
   */
  public static String getMd5Hash(String appName, File file) {
    return MD5_COLON_PREFIX + getNakedMd5Hash(appName, file);
  }

  /**
   * MD5's a file. Used in ODKDatabaseImplUtils and EncryptionUtils
   *
   * @param appName the app name
   * @param file    the file to hash
   * @return the md5sum of that file
   */
  @SuppressWarnings("WeakerAccess")
  public static String getNakedMd5Hash(String appName, Object file) {
    InputStream is = null;
    try {
      // CTS (6/15/2010) : stream file through digest instead of handing
      // it the byte[]
      MessageDigest md = MessageDigest.getInstance("MD5");
      int chunkSize = 8192;

      byte[] chunk = new byte[chunkSize];

      // Get the size of the file
      long lLength;
      if (file instanceof File) {
        lLength = ((File) file).length();
      } else if (file instanceof String) {
        lLength = ((String) file).length();
      } else {
        throw new IllegalArgumentException("Bad object to md5");
      }

      if (lLength > Integer.MAX_VALUE) {
        if (file instanceof File) {
          WebLogger.getLogger(appName).e(TAG, "File " + ((File) file).getName() + " is too large");
        } else {
          WebLogger.getLogger(appName).e(TAG, "String is too large to md5");
        }
        return null;
      }

      if (lLength > Integer.MAX_VALUE) {
        throw new RuntimeException("Refusing to cast from long to int with loss of precision");
      }
      //noinspection NumericCastThatLosesPrecision
      int length = (int) lLength;


      if (file instanceof File) {
        is = new FileInputStream((File) file);
      } else {
        is = new ByteArrayInputStream(((String) file).getBytes(StandardCharsets.UTF_8));
      }

      int l;
      for (l = 0; l + chunkSize < length; l += chunkSize) {
        // TODO double check that this still works after the change
        if (is.read(chunk, 0, chunkSize) == -1)
          break;
        md.update(chunk, 0, chunkSize);
      }

      int remaining = length - l;
      if (remaining > 0) {
        // TODO double check that this still works after the change
        if (is.read(chunk, 0, remaining) != -1) {
          md.update(chunk, 0, remaining);
        }
      }
      byte[] messageDigest = md.digest();

      BigInteger number = new BigInteger(1, messageDigest);
      String md5 = number.toString(16);
      while (md5.length() < 32)
        md5 = "0" + md5;
      is.close();
      return md5;

    } catch (NoSuchAlgorithmException e) {
      WebLogger.getLogger(appName).e("MD5", e.getMessage());
      return null;

    } catch (FileNotFoundException e) {
      WebLogger.getLogger(appName).e("No Cache File", e.getMessage());
      return null;
    } catch (IOException e) {
      WebLogger.getLogger(appName).e("Problem reading from file", e.getMessage());
      return null;
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          WebLogger.getLogger(appName).printStackTrace(e);
        }
      }
    }

  }

  /**
   * Used in WebCursorUtils
   *
   * @param n    the node to read text from
   * @param trim whether to trim extra whitespace from the node
   * @return the text in the node
   */
  @SuppressWarnings("unused")
  public static String getXMLText(Node n, boolean trim) {
    NodeList nl = n.getChildNodes();
    return nl.getLength() == 0 ? null : getXMLText(nl, 0, trim);
  }

  /**
   * reads all subsequent text nodes and returns the combined string needed
   * because escape sequences are parsed into consecutive text nodes e.g.
   * "abc&amp;123" --> (abc)(&)(123)
   **/
  private static String getXMLText(NodeList nl, int i, boolean trim) {
    StringBuffer strBuff = null;

    String text = nl.item(i).getTextContent();
    if (text == null)
      return null;

    for (i++; i < nl.getLength() && nl.item(i).getNodeType() == Node.TEXT_NODE; i++) {
      if (strBuff == null)
        strBuff = new StringBuffer(text);

      strBuff.append(nl.item(i).getTextContent());
    }
    if (strBuff != null)
      text = strBuff.toString();

    if (trim)
      text = text.trim();

    return text;
  }

  /**
   * Copies the given directory, see {@link FileUtils#copyDirectory}
   * @param sourceFolder the directory to copy
   * @param destinationFolder where to copy it to
   * @throws IOException if the action couldn't be completed
   */
  public static void copyDirectory(File sourceFolder, File destinationFolder) throws IOException {
    ContextClassLoaderWrapper wrapper = new ContextClassLoaderWrapper();
    try {
      FileUtils.copyDirectory(sourceFolder, destinationFolder);
    } finally {
      wrapper.release();
    }
  }

  /**
   * Moves the given directory, see {@link FileUtils#moveDirectory}
   * @param sourceFolder the directory to move
   * @param destinationFolder where to move it to
   * @throws IOException if the action couldn't be completed
   */
  public static void moveDirectory(File sourceFolder, File destinationFolder) throws IOException {
    ContextClassLoaderWrapper wrapper = new ContextClassLoaderWrapper();
    try {
      FileUtils.moveDirectory(sourceFolder, destinationFolder);
    } finally {
      wrapper.release();
    }
  }

  /**
   * Returns true if the given directory contains the file. See {@link FileUtils#directoryContains}
   * @param folder the directory that may or may not contain the file
   * @param file the file that may or may not be under the given folder
   * @return whether the file is in the passed directory
   * @throws IOException if the action couldn't be completed
   */
  public static boolean directoryContains(File folder, File file) throws IOException {
    ContextClassLoaderWrapper wrapper = new ContextClassLoaderWrapper();
    try {
      return FileUtils.directoryContains(folder, file);
    } finally {
      wrapper.release();
    }
  }

  /**
   * Deletes the given folder. See {@link FileUtils#deleteDirectory}
   * @param folder the folder to delete
   * @throws IOException if the action couldn't be completed
   */
  public static void deleteDirectory(File folder) throws IOException {
    ContextClassLoaderWrapper wrapper = new ContextClassLoaderWrapper();
    try {
      FileUtils.deleteDirectory(folder);
    } finally {
      wrapper.release();
    }
  }

  /**
   * Copies the given file, see {@link FileUtils#copyFile}
   * @param sourceFile the file to copy
   * @param destinationFile where to move it to
   * @throws IOException if the action couldn't be completed
   */
  public static void copyFile(File sourceFile, File destinationFile) throws IOException {
    ContextClassLoaderWrapper wrapper = new ContextClassLoaderWrapper();
    try {
      FileUtils.copyFile(sourceFile, destinationFile);
    } finally {
      wrapper.release();
    }
  }

  /**
   * Used in ODKDatabaseImplUtils
   *
   * @param directory       a directory to scan through
   * @param fileFileFilter  a filter used to strip unwanted files from the result
   * @param directoryFilter a filter used to strip unwanted directories from the result
   * @return a list of the files that matched the file filter and directory filter
   */
  @SuppressWarnings("unused")
  public static Collection<File> listFiles(File directory, IOFileFilter fileFileFilter,
      IOFileFilter directoryFilter) {
    ContextClassLoaderWrapper wrapper = new ContextClassLoaderWrapper();
    try {
      return FileUtils.listFiles(directory, fileFileFilter, directoryFilter);
    } finally {
      wrapper.release();
    }
  }

  /**
   * Used in ODKDatabaseImplUtils
   *
   * @param file the file to delete
   */
  @SuppressWarnings("unused")
  public static void deleteQuietly(File file) {
    ContextClassLoaderWrapper wrapper = new ContextClassLoaderWrapper();
    try {
      FileUtils.deleteQuietly(file);
    } finally {
      wrapper.release();
    }
  }

  /**
   * This ensures that the current thread's class loader is set, because if it isn't, things in
   * FileUtils won't work. FileUtils uses some fancy Java 7 optimizations, but it will fallback
   * to the slow way if you're on java 6. However the way it detects if these optimizations are
   * present uses the current thread's class loader, and if the class loader is null, it can't
   * tell and just dies.
   */
  private static class ContextClassLoaderWrapper {
    boolean wrapped = false;

    ContextClassLoaderWrapper() {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      if (loader == null) {
        wrapped = true;
        Thread.currentThread().setContextClassLoader(ODKFileUtils.class.getClassLoader());
      }
    }

    void release() {
      if (wrapped) {
        Thread.currentThread().setContextClassLoader(null);
      }
    }
  }
}

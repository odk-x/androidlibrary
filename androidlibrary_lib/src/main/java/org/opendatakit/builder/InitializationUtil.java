/*
 * Copyright (C) 2013 University of Washington
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

package org.opendatakit.builder;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import org.opendatakit.androidlibrary.R;
import org.opendatakit.database.service.DbHandle;
import org.opendatakit.database.utilities.CursorUtils;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.listener.ImportListener;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.provider.FormsColumns;
import org.opendatakit.provider.FormsProviderAPI;
import org.opendatakit.utilities.ODKFileUtils;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Background task for exploding the built-in zipfile resource into the
 * framework directory of the application and doing forms discovery on this
 * appName.
 *
 * @author mitchellsundt@gmail.com
 */
public class InitializationUtil {

  private static final String TAG = InitializationUtil.class.getSimpleName();

  private Context appContext;
  private InitializationSupervisor supervisor;
  private String appName;
  private String displayTablesProgress = null;
  private String tableIdInProgress = null;

  public InitializationUtil(Context appContext, String appName,
      InitializationSupervisor supervisor) {
    this.appContext = appContext;
    this.appName = appName;
    this.supervisor = supervisor;
  }

  private InitializationSupervisor getSupervisor() {
    return supervisor;
  }

  public InitializationOutcome initialize() {
    InitializationOutcome pendingOutcome = new InitializationOutcome();

    // ///////////////////////////////////////////////
    // check that the framework zip has been exploded
    String toolName = getSupervisor().getToolName();
    if (toolName != null && !ODKFileUtils
        .isConfiguredToolApp(appName, toolName, getSupervisor().getVersionCodeString())) {
      getSupervisor()
          .publishProgress(appContext.getString(R.string.expansion_unzipping_begins), null);

      extractFromRawZip(appContext.getResources(), getSupervisor().getSystemZipResourceId(), true,
          pendingOutcome);
      extractFromRawZip(appContext.getResources(), getSupervisor().getConfigZipResourceId(), false,
          pendingOutcome);

      ODKFileUtils
          .assertConfiguredToolApp(appName, toolName, getSupervisor().getVersionCodeString());
    }

    try {
      updateTableDirs(pendingOutcome);
    } catch (ServicesAvailabilityException e) {
      WebLogger.getLogger(appName).printStackTrace(e);
      WebLogger.getLogger(appName).e(TAG, "Error accesssing database during table creation sweep");
      pendingOutcome.add(appContext.getString(R.string.abort_error_accessing_database));
      return pendingOutcome;
    }

    updateFormDirs(pendingOutcome);

    try {
      initTables(pendingOutcome);
    } catch (ServicesAvailabilityException e) {
      WebLogger.getLogger(appName).printStackTrace(e);
      WebLogger.getLogger(appName).e(TAG, "Error accesssing database during CSV import sweep");
      pendingOutcome.add(appContext.getString(R.string.abort_error_accessing_database));
      return pendingOutcome;
    }

    return pendingOutcome;
  }

  private void doActionOnRawZip(Resources resources, int resourceId,
      InitializationOutcome pendingOutcome, ZipAction action) {
    String message;
    InputStream rawInputStream = null;
    try {
      rawInputStream = resources.openRawResource(resourceId);
      ZipInputStream zipInputStream = null;
      ZipEntry entry = null;
      try {
        int countFiles = 0;

        zipInputStream = new ZipInputStream(rawInputStream);
        while ((entry = zipInputStream.getNextEntry()) != null) {
          if (getSupervisor().isCancelled()) {
            message = "cancelled";
            pendingOutcome.add(entry.getName() + " " + message);
            break;
          }
          ++countFiles;
          action.doWorker(entry, zipInputStream, countFiles, 0);
        }
        zipInputStream.close();

        action.done(countFiles);
      } catch (IOException e) {
        WebLogger.getLogger(appName).printStackTrace(e);
        pendingOutcome.problemExtractingToolZipContent = true;
        if (e.getCause() != null) {
          message = e.getCause().getMessage();
        } else {
          message = e.getMessage();
        }
        if (entry != null) {
          pendingOutcome.add(entry.getName() + " " + message);
        } else {
          pendingOutcome.add("Error accessing zipfile resource " + message);
        }
      } finally {
        if (zipInputStream != null) {
          try {
            zipInputStream.close();
          } catch (IOException e) {
            WebLogger.getLogger(appName).printStackTrace(e);
            WebLogger.getLogger(appName).e(TAG, "Closing of ZipFile failed: " + e);
          }
        }
      }
    } catch (Exception e) {
      WebLogger.getLogger(appName).printStackTrace(e);
      pendingOutcome.problemExtractingToolZipContent = true;
      if (e.getCause() != null) {
        message = e.getCause().getMessage();
      } else {
        message = e.getMessage();
      }
      pendingOutcome.add("Error accessing zipfile resource " + message);
    } finally {
      if (rawInputStream != null) {
        try {
          rawInputStream.close();
        } catch (IOException e) {
          WebLogger.getLogger(appName).printStackTrace(e);
        }
      }
    }
  }

  private void extractFromRawZip(Resources resources, int resourceId, final boolean overwrite,
      InitializationOutcome result) {

    final ZipEntryCounter countTotal = new ZipEntryCounter();

    if (resourceId == -1) {
      return;
    }

    doActionOnRawZip(resources, resourceId, result, countTotal);

    if (countTotal.totalFiles == -1) {
      return;
    }

    ZipAction worker = new ZipAction() {

      long bytesProcessed = 0L;
      long lastBytesProcessedThousands = 0L;

      @Override
      public void doWorker(ZipEntry entry, ZipInputStream zipInputStream, int indexIntoZip,
          long size) throws IOException {

        File tempFile = new File(ODKFileUtils.getAppFolder(appName), entry.getName());
        String formattedString = appContext
            .getString(R.string.expansion_unzipping_without_detail, entry.getName(), indexIntoZip,
                countTotal.totalFiles);
        String detail;
        if (entry.isDirectory()) {
          detail = appContext.getString(R.string.expansion_create_dir_detail);
          getSupervisor().publishProgress(formattedString, detail);
          if (!tempFile.exists() && !tempFile.mkdirs()) {
            throw new RuntimeException("Unable to make required directories");
          }
        } else if (overwrite || !tempFile.exists()) {
          int bufferSize = 8192;
          OutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile, false),
              bufferSize);
          try {
            byte buffer[] = new byte[bufferSize];
            int bread;
            while ((bread = zipInputStream.read(buffer)) != -1) {
              bytesProcessed += bread;
              long curThousands = bytesProcessed / 1000L;
              if (curThousands != lastBytesProcessedThousands) {
                detail = appContext
                    .getString(R.string.expansion_unzipping_detail, bytesProcessed, indexIntoZip);
                getSupervisor().publishProgress(formattedString, detail);
                lastBytesProcessedThousands = curThousands;
              }
              out.write(buffer, 0, bread);
            }
          } finally {
            out.flush();
            out.close();
          }

          detail = appContext
              .getString(R.string.expansion_unzipping_detail, bytesProcessed, indexIntoZip);
          getSupervisor().publishProgress(formattedString, detail);
        }
        WebLogger.getLogger(appName).i(TAG, "Extracted ZipEntry: " + entry.getName());
      }

      @Override
      public void done(int totalCount) {
        String completionString = appContext.getString(R.string.expansion_unzipping_complete);
        getSupervisor().publishProgress(completionString, null);
      }

    };

    doActionOnRawZip(resources, resourceId, result, worker);
  }

  /**
   * Remove definitions from the Forms database that are no longer present on
   * disk.
   */
  private void removeStaleFormInfo(List<File> discoveredFormDefDirs) {
    Uri formsProviderContentUri = Uri.parse("content://" + FormsProviderAPI.AUTHORITY);

    String completionString = appContext.getString(R.string.searching_for_deleted_forms);
    getSupervisor().publishProgress(completionString, null);

    WebLogger.getLogger(appName).i(TAG, "removeStaleFormInfo " + appName + " begin");
    ArrayList<Uri> badEntries = new ArrayList<>();
    Cursor c = null;
    try {
      c = appContext.getContentResolver()
          .query(Uri.withAppendedPath(formsProviderContentUri, appName), null, null, null, null);

      if (c == null) {
        WebLogger.getLogger(appName)
            .w(TAG, "removeStaleFormInfo " + appName + " null cursor returned from query.");
        return;
      }

      if (c.moveToFirst()) {
        do {
          String tableId = CursorUtils.getIndexAsString(c, c.getColumnIndex(FormsColumns.TABLE_ID));
          String formId = CursorUtils.getIndexAsString(c, c.getColumnIndex(FormsColumns.FORM_ID));
          Uri otherUri = Uri.withAppendedPath(
              Uri.withAppendedPath(Uri.withAppendedPath(formsProviderContentUri, appName), tableId),
              formId);

          String examString = appContext.getString(R.string.examining_form, tableId, formId);
          getSupervisor().publishProgress(examString, null);

          String formDir = ODKFileUtils.getFormFolder(appName, tableId, formId);
          File f = new File(formDir);
          File formDefJson = new File(f, ODKFileUtils.FORMDEF_JSON_FILENAME);
          if (!f.exists() || !f.isDirectory() || !formDefJson.exists() || !formDefJson.isFile()) {
            // the form definition does not exist
            badEntries.add(otherUri);
          } else {
            // ////////////////////////////////
            // formdef.json exists. See if it is
            // unchanged...
            String json_md5 = CursorUtils
                .getIndexAsString(c, c.getColumnIndex(FormsColumns.JSON_MD5_HASH));
            String fileMd5 = ODKFileUtils.getMd5Hash(appName, formDefJson);
            if (json_md5 != null && json_md5.equals(fileMd5)) {
              // it is unchanged -- no need to rescan it
              discoveredFormDefDirs.remove(f);
            }
          }
        } while (c.moveToNext());
      }
    } catch (Exception e) {
      WebLogger.getLogger(appName)
          .e(TAG, "removeStaleFormInfo " + appName + " exception: " + e.toString());
      WebLogger.getLogger(appName).printStackTrace(e);
    } finally {
      if (c != null && !c.isClosed()) {
        c.close();
      }
    }

    // delete the other entries (and directories)
    for (Uri badUri : badEntries) {
      WebLogger.getLogger(appName)
          .i(TAG, "removeStaleFormInfo: " + appName + " deleting: " + badUri.toString());
      try {
        appContext.getContentResolver().delete(badUri, null, null);
      } catch (Exception e) {
        WebLogger.getLogger(appName)
            .e(TAG, "removeStaleFormInfo " + appName + " exception: " + e.toString());
        WebLogger.getLogger(appName).printStackTrace(e);
        // and continue -- don't throw an error
      }
    }
    WebLogger.getLogger(appName).i(TAG, "removeStaleFormInfo " + appName + " end");
  }

  /**
   * Construct a directory name that is unused in the stale path and move
   * mediaPath there.
   *
   * @param mediaPath
   * @param baseStaleMediaPath -- the stale directory corresponding to the mediaPath container
   * @return the directory within the stale directory that the mediaPath was
   * renamed to.
   * @throws IOException
   */
  private File moveToStaleDirectory(File mediaPath, String baseStaleMediaPath) throws IOException {
    // we have a 'framework' form in the forms directory.
    // Move it to the stale directory.
    // Delete all records referring to this directory.
    int i = 0;
    File tempMediaPath = new File(
        baseStaleMediaPath + mediaPath.getName() + "_" + Integer.toString(i));
    while (tempMediaPath.exists()) {
      ++i;
      tempMediaPath = new File(
          baseStaleMediaPath + mediaPath.getName() + "_" + Integer.toString(i));
    }
    ODKFileUtils.moveDirectory(mediaPath, tempMediaPath);
    return tempMediaPath;
  }

  private void updateTableDirs(InitializationOutcome pendingOutcome)
      throws ServicesAvailabilityException {
    // /////////////////////////////////////////
    // /////////////////////////////////////////
    // /////////////////////////////////////////
    // Scan the tables directory, looking for tableIds with definition.csv
    // files.
    // If the tableId does not exist, try to create it using these files.
    // If the tableId already exists, do nothing -- assume everything is
    // up-to-date.
    // This means we don't pick up properties.csv changes, but the
    // definition.csv
    // should never change. If properties.csv changes, we assume the process
    // that
    // changed it will be triggering a reload of it through other means.

    CsvUtil util = new CsvUtil(getSupervisor(), appName);

    ODKFileUtils.assertDirectoryStructure(appName);
    File tablesDir = new File(ODKFileUtils.getTablesFolder(appName));
    File[] tableIdDirs = tablesDir.listFiles(new FileFilter() {

      @Override
      public boolean accept(File pathname) {
        return pathname.isDirectory();
      }
    });

    for (int i = 0; i < tableIdDirs.length; ++i) {
      File tableIdDir = tableIdDirs[i];
      String tableId = tableIdDir.getName();

      File definitionCsv = new File(ODKFileUtils.getTableDefinitionCsvFile(appName, tableId));
      File propertiesCsv = new File(ODKFileUtils.getTablePropertiesCsvFile(appName, tableId));
      if (definitionCsv.exists() && definitionCsv.isFile() && propertiesCsv.exists()
          && propertiesCsv.isFile()) {

        String formattedString = appContext
            .getString(R.string.scanning_for_table_definitions, tableId, (i + 1),
                tableIdDirs.length);
        String detail = appContext.getString(R.string.processing_file);
        displayTablesProgress = formattedString;
        tableIdInProgress = tableId;
        getSupervisor().publishProgress(formattedString, detail);

        try {
          util.updateTablePropertiesFromCsv(tableId);
        } catch (IOException e) {
          pendingOutcome.add(appContext.getString(R.string.defining_tableid_error, tableId));
          WebLogger.getLogger(appName).e(TAG, "Unexpected error during update from csv");
        }
      }
    }

    DbHandle db = null;
    try {
      db = getSupervisor().getDatabase().openDatabase(appName);
      getSupervisor().getDatabase().deleteAppAndTableLevelManifestSyncETags(appName, db);
    } finally {
      if (db != null) {
        getSupervisor().getDatabase().closeDatabase(appName, db);
      }
    }

  }

  private void initTables(final InitializationOutcome pendingOutcome)
      throws ServicesAvailabilityException {

    final String EMPTY_STRING = "";
    final String SPACE = " ";
    final String TOP_LEVEL_KEY_TABLE_KEYS = "table_keys";
    final String COMMA = ",";
    final String KEY_SUFFIX_CSV_FILENAME = ".filename";

    // Stores the table's key to its filename.
    Map<String, String> mKeyToFileMap = new HashMap<>();

    // /////////////////////////////////////////
    // /////////////////////////////////////////
    // /////////////////////////////////////////
    // and now process tables.init file
    File init = new File(ODKFileUtils.getTablesInitializationFile(appName));
    File completedFile = new File(ODKFileUtils.getTablesInitializationCompleteMarkerFile(appName));
    if (!init.exists()) {
      // no initialization file -- we are done!
      return;
    }
    // Check if we've already processed this file
    if (completedFile.exists()) {
      String initMd5 = ODKFileUtils.getMd5Hash(appName, init);
      String completedFileMd5 = ODKFileUtils.getMd5Hash(appName, completedFile);
      if (initMd5.equals(completedFileMd5)) {
        // we are done!
        return;
      }
    }

    Properties prop = new Properties();
    try {
      prop.load(new FileInputStream(init));
    } catch (IOException ex) {
      WebLogger.getLogger(appName).printStackTrace(ex);
      pendingOutcome.add(appContext.getString(R.string.poorly_formatted_init_file));
      pendingOutcome.problemImportingAssetCsvContent = true;
      return;
    }

    // assume if we load it, we have processed it.

    // We shouldn't really do this, but it avoids an infinite
    // recycle if there is an error during the processing of the
    // file.
    try {
      ODKFileUtils.copyFile(init, completedFile);
    } catch (IOException e) {
      WebLogger.getLogger(appName).printStackTrace(e);
      // ignore this.
    }

    // prop was loaded
    if (prop.size() > 0) {
      String table_keys = prop.getProperty(TOP_LEVEL_KEY_TABLE_KEYS);

      // table_keys is defined
      if (table_keys != null) {
        // remove spaces and split at commas to get key names
        String[] keys = table_keys.replace(SPACE, EMPTY_STRING).split(COMMA);
        int fileCount = keys.length;
        int curFileCount = 0;
        String detail = appContext.getString(R.string.processing_file);

        File file;
        CsvUtil cu = new CsvUtil(getSupervisor(), appName);
        for (String key : keys) {
          curFileCount++;
          String filename = prop.getProperty(key + KEY_SUFFIX_CSV_FILENAME);
          //this.importStatus.put(key, false);
          file = new File(ODKFileUtils.getAppFolder(appName), filename);
          mKeyToFileMap.put(key, filename);
          if (!file.exists()) {
            pendingOutcome.assetsCsvFileNotFoundSet.add(key);
            WebLogger.getLogger(appName).i(TAG, "putting in file not found map true: " + key);
            String formattedString = appContext.getString(R.string.csv_file_not_found, filename);
            pendingOutcome.add(formattedString);
            continue;
          }

          // update dialog message with current filename
          String formattedString = appContext
              .getString(R.string.importing_file_without_detail, curFileCount, fileCount, filename);
          displayTablesProgress = formattedString;
          getSupervisor().publishProgress(formattedString, detail);
          ImportRequest request = null;

          // If the import file is in the config/assets/csv directory
          // and if it is of the form tableId.csv or tableId.fileQualifier.csv
          // and fileQualifier is not 'properties', then assume it is the
          // new-style CSV format.
          //
          String assetsCsvDirPath = ODKFileUtils
              .asRelativePath(appName, new File(ODKFileUtils.getAssetsCsvFolder(appName)));
          if (filename.startsWith(assetsCsvDirPath)) {
            // get past the file separator
            String csvFilename = filename.substring(assetsCsvDirPath.length() + 1);
            String[] terms = csvFilename.split("\\.");
            if (terms.length == 2 && terms[1].equals("csv")) {
              String tableId = terms[0];
              String fileQualifier = null;
              request = new ImportRequest(tableId, fileQualifier);
            } else if (terms.length == 3 && terms[1].equals("properties") && terms[2]
                .equals("csv")) {
              String tableId = terms[0];
              String fileQualifier = null;
              request = new ImportRequest(tableId, fileQualifier);
            } else if (terms.length == 3 && terms[2].equals("csv")) {
              String tableId = terms[0];
              String fileQualifier = terms[1];
              request = new ImportRequest(tableId, fileQualifier);
            } else if (terms.length == 4 && terms[2].equals("properties") && terms[3]
                .equals("csv")) {
              String tableId = terms[0];
              String fileQualifier = terms[1];
              request = new ImportRequest(tableId, fileQualifier);
            }

            if (request != null) {
              tableIdInProgress = request.getTableId();
              boolean success = cu.importSeparable(new ImportListener() {
                @Override
                public void updateProgressDetail(int row) {
                  getSupervisor().publishProgress(displayTablesProgress,
                      appContext.getString(R.string.import_in_progress, row));
                }

                @Override
                public void importComplete(boolean outcome) {
                  if (outcome) {
                    pendingOutcome
                        .add(appContext.getString(R.string.import_csv_success, tableIdInProgress));
                  } else {
                    pendingOutcome
                        .add(appContext.getString(R.string.import_csv_failure, tableIdInProgress));
                  }
                  pendingOutcome.problemImportingAssetCsvContent =
                      pendingOutcome.problemImportingAssetCsvContent || !outcome;
                }
              }, request.getTableId(), request.getFileQualifier(), true);
              //importStatus.put(key, success);
              if (success) {
                detail = appContext.getString(R.string.import_success);
                getSupervisor().publishProgress(appContext
                    .getString(R.string.importing_file_without_detail, curFileCount, fileCount,
                        filename), detail);
              }
            }
          }

          if (request == null) {
            pendingOutcome.add(appContext.getString(R.string.poorly_formatted_init_file));
            pendingOutcome.problemImportingAssetCsvContent = true;
            return;
          }
        }
      } else {
        pendingOutcome.add(appContext.getString(R.string.poorly_formatted_init_file));
        pendingOutcome.problemImportingAssetCsvContent = true;
      }
    }
  }
  //private Map<String, Boolean> importStatus = new TreeMap<>();

  private void updateFormDirs(InitializationOutcome pendingOutcome) {

    // /////////////////////////////////////////
    // /////////////////////////////////////////
    // /////////////////////////////////////////
    // scan for new forms...

    String completionString = appContext.getString(R.string.searching_for_form_defs);
    getSupervisor().publishProgress(completionString, null);

    File tablesDir = new File(ODKFileUtils.getTablesFolder(appName));

    File[] tableIdDirs = tablesDir.listFiles(new FileFilter() {

      @Override
      public boolean accept(File pathname) {
        return pathname.isDirectory();
      }
    });

    List<File> formDirs = new ArrayList<>();
    for (File tableIdDir : tableIdDirs) {
      String tableId = tableIdDir.getName();

      File formDir = new File(ODKFileUtils.getFormsFolder(appName, tableId));
      File[] formIdDirs = formDir.listFiles(new FileFilter() {

        @Override
        public boolean accept(File pathname) {
          File formDef = new File(pathname, ODKFileUtils.FORMDEF_JSON_FILENAME);
          return pathname.isDirectory() && formDef.exists() && formDef.isFile();
        }
      });

      if (formIdDirs != null) {
        formDirs.addAll(Arrays.asList(formIdDirs));
      }
    }

    // /////////////////////////////////////////
    // remove forms that no longer exist
    // remove the forms that haven't changed
    // from the discovered list
    removeStaleFormInfo(formDirs);

    // this is the complete list of forms we need to scan and possibly add
    // to the FormsProvider
    for (int i = 0; i < formDirs.size(); ++i) {
      File formDir = formDirs.get(i);

      String formId = formDir.getName();
      String tableId = formDir.getParentFile().getParentFile().getName();

      // specifically target this form...
      WebLogger.getLogger(appName).i(TAG, "updateFormInfo: form: " + formDir.getAbsolutePath());

      String examString = appContext
          .getString(R.string.updating_form_information, formDir.getName(), i + 1, formDirs.size());
      getSupervisor().publishProgress(examString, null);

      updateFormDir(tableId, formId, formDir,
          ODKFileUtils.getPendingDeletionTablesFolder(appName) + File.separator, pendingOutcome);
    }

  }

  /**
   * Scan the given formDir and update the Forms database. If it is the
   * formsFolder, then any 'framework' forms should be forbidden. If it is not
   * the formsFolder, only 'framework' forms should be allowed
   *
   * @param tableId
   * @param formId
   * @param formDir
   * @param baseStaleMediaPath -- path prefix to the stale forms/framework directory.
   */
  private void updateFormDir(String tableId, String formId, File formDir, String baseStaleMediaPath,
      InitializationOutcome pendingOutcome) {
    Uri formsProviderContentUri = Uri.parse("content://" + FormsProviderAPI.AUTHORITY);
    String formDirectoryPath = formDir.getAbsolutePath();
    WebLogger.getLogger(appName).i(TAG, "updateFormDir: " + formDirectoryPath);

    String successMessage = appContext.getString(R.string.form_register_success, tableId, formId);
    String failureMessage = appContext.getString(R.string.form_register_failure, tableId, formId);

    Cursor c = null;
    try {
      String selection = FormsColumns.TABLE_ID + "=? AND " + FormsColumns.FORM_ID + "=?";
      String[] selectionArgs = { tableId, formId };
      c = appContext.getContentResolver()
          .query(Uri.withAppendedPath(formsProviderContentUri, appName), null, selection,
              selectionArgs, null);

      if (c == null) {
        WebLogger.getLogger(appName)
            .w(TAG, "updateFormDir: " + formDirectoryPath + " null cursor -- cannot update!");
        pendingOutcome.add(failureMessage);
        pendingOutcome.problemDefiningForms = true;
        return;
      }

      if (c.getCount() > 1) {
        c.close();
        WebLogger.getLogger(appName).w(TAG, "updateFormDir: " + formDirectoryPath
            + " multiple records from cursor -- delete all and restore!");
        // we have multiple records for this one directory.
        // Rename the directory. Delete the records, and move the
        // directory back.
        File tempMediaPath = moveToStaleDirectory(formDir, baseStaleMediaPath);

        appContext.getContentResolver()
            .delete(Uri.withAppendedPath(formsProviderContentUri, appName), selection,
                selectionArgs);

        ODKFileUtils.moveDirectory(tempMediaPath, formDir);

        ContentValues cv = new ContentValues();
        cv.put(FormsColumns.TABLE_ID, tableId);
        cv.put(FormsColumns.FORM_ID, formId);
        appContext.getContentResolver()
            .insert(Uri.withAppendedPath(formsProviderContentUri, appName), cv);
      } else if (c.getCount() == 1) {
        c.close();
        ContentValues cv = new ContentValues();
        cv.put(FormsColumns.TABLE_ID, tableId);
        cv.put(FormsColumns.FORM_ID, formId);
        appContext.getContentResolver()
            .update(Uri.withAppendedPath(formsProviderContentUri, appName), cv, null, null);
      } else if (c.getCount() == 0) {
        c.close();
        ContentValues cv = new ContentValues();
        cv.put(FormsColumns.TABLE_ID, tableId);
        cv.put(FormsColumns.FORM_ID, formId);
        appContext.getContentResolver()
            .insert(Uri.withAppendedPath(formsProviderContentUri, appName), cv);
      }
    } catch (IOException e) {
      WebLogger.getLogger(appName).printStackTrace(e);
      WebLogger.getLogger(appName)
          .e(TAG, "updateFormDir: " + formDirectoryPath + " exception: " + e.toString());
      pendingOutcome.add(failureMessage);
      pendingOutcome.problemDefiningForms = true;
      return;
    } catch (IllegalArgumentException e) {
      WebLogger.getLogger(appName).printStackTrace(e);
      WebLogger.getLogger(appName)
          .e(TAG, "updateFormDir: " + formDirectoryPath + " exception: " + e.toString());
      try {
        ODKFileUtils.deleteDirectory(formDir);
        WebLogger.getLogger(appName).i(TAG,
            "updateFormDir: " + formDirectoryPath + " Removing -- unable to parse formDef file: "
                + e.toString());
      } catch (IOException e1) {
        WebLogger.getLogger(appName).printStackTrace(e1);
        WebLogger.getLogger(appName).i(TAG,
            "updateFormDir: " + formDirectoryPath + " Removing -- unable to delete form directory: "
                + formDir.getName() + " error: " + e.toString());
      }
      pendingOutcome.add(failureMessage);
      pendingOutcome.problemDefiningForms = true;
      return;
    } catch (Exception e) {
      WebLogger.getLogger(appName).printStackTrace(e);
      WebLogger.getLogger(appName)
          .e(TAG, "updateFormDir: " + formDirectoryPath + " exception: " + e.toString());
      pendingOutcome.add(failureMessage);
      pendingOutcome.problemDefiningForms = true;
      return;
    } finally {
      if (c != null && !c.isClosed()) {
        c.close();
      }
    }
    pendingOutcome.add(successMessage);
  }

  private interface ZipAction {
    void doWorker(ZipEntry entry, ZipInputStream zipInputStream, int indexIntoZip, long size)
        throws IOException;

    void done(int totalCount);
  }

  private static class ZipEntryCounter implements ZipAction {
    int totalFiles = -1;

    @Override
    public void doWorker(ZipEntry entry, ZipInputStream zipInputStream, int indexIntoZip,
        long size) {
      // no-op
    }

    @Override
    public void done(int totalCount) {
      totalFiles = totalCount;
    }
  }
}

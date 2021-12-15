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

import android.content.Context;
import android.content.res.Resources;
import org.opendatakit.androidlibrary.R;
import org.opendatakit.database.service.DbHandle;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.listener.ImportListener;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.properties.CommonToolProperties;
import org.opendatakit.properties.PropertiesSingleton;
import org.opendatakit.utilities.ODKFileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
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

  private static final String TAG = "InitializationUtil";

  private static final String CSV = "csv";
  private static final String PROPERTIES = "properties";

  private Context appContext;
  private InitializationSupervisor supervisor;
  private String appName;
  private String displayTablesProgress;
  private String tableIdInProgress;

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
    // This will be dependent upon the tool itself
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

    // Check if common initialization has completed successfully and
    PropertiesSingleton propertiesSingleton = CommonToolProperties.get(appContext, appName);
    boolean shouldRunCommonInitTask = propertiesSingleton.shouldRunCommonInitializationTask();
    if (!shouldRunCommonInitTask) {
      return pendingOutcome;
    }

    return commonInitialization(pendingOutcome, propertiesSingleton);
  }



  private InitializationOutcome commonInitialization(InitializationOutcome pendingOutcome,
                                                     PropertiesSingleton propertiesSingleton) {
    try {
      updateTableDirs(pendingOutcome);

    } catch (ServicesAvailabilityException e) {
      WebLogger.getLogger(appName).printStackTrace(e);
      WebLogger.getLogger(appName).e(TAG, "Error accesssing database during table creation sweep");
      pendingOutcome.add(appContext.getString(R.string.abort_error_accessing_database));
      return pendingOutcome;
    }

    try {
      updateFormDirs(pendingOutcome);

    } catch (ServicesAvailabilityException e) {
      WebLogger.getLogger(appName).printStackTrace(e);
      WebLogger.getLogger(appName).e(TAG, "Error accesssing database during form creation sweep");
      pendingOutcome.add(appContext.getString(R.string.abort_error_accessing_database));
      return pendingOutcome;
    }

    try {
      initTables(pendingOutcome);
    } catch (ServicesAvailabilityException e) {
      WebLogger.getLogger(appName).printStackTrace(e);
      WebLogger.getLogger(appName).e(TAG, "Error accesssing database during CSV import sweep");
      pendingOutcome.add(appContext.getString(R.string.abort_error_accessing_database));
      return pendingOutcome;
    }

    propertiesSingleton.clearRunCommonInitializationTask();

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
            WebLogger.getLogger(appName).e(TAG, "Closing of ZipFile failed: " + e.toString());
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
          byte buffer[] = new byte[bufferSize];
          int bread;
          while ((bread = zipInputStream.read(buffer)) != -1) {
            bytesProcessed += bread;
            long curThousands = (bytesProcessed / 1000L);
            if (curThousands != lastBytesProcessedThousands) {
              detail = appContext
                  .getString(R.string.expansion_unzipping_detail, bytesProcessed, indexIntoZip);
              getSupervisor().publishProgress(formattedString, detail);
              lastBytesProcessedThousands = curThousands;
            }
            out.write(buffer, 0, bread);
          }
          out.flush();
          out.close();

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

    List<String> tableIdsWithDefinitions = new ArrayList<String>();
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
          tableIdsWithDefinitions.add(tableId);
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
      List<String> tableIds = getSupervisor().getDatabase().getAllTableIds(appName, db);
      Set<String> tableIdsWithoutDefinitions = new HashSet<String>();
      tableIdsWithoutDefinitions.addAll(tableIds);
      tableIdsWithoutDefinitions.removeAll(tableIdsWithDefinitions);
      for (String tableId : tableIdsWithoutDefinitions) {
        getSupervisor().getDatabase().deleteTableAndAllData(appName, db, tableId);
      }
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
          String srcFilename = prop.getProperty(key + KEY_SUFFIX_CSV_FILENAME);
          //this.importStatus.put(key, false);
          file = new File(ODKFileUtils.getAppFolder(appName), srcFilename);
          String filename = ODKFileUtils.asRelativePath(appName, file);
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
            if (terms.length == 2 && terms[1].equals(CSV)) {
              String tableId = terms[0];
              String fileQualifier = null;
              request = new ImportRequest(tableId, fileQualifier);
            } else if (terms.length == 3 && terms[1].equals(PROPERTIES) && terms[2]
                .equals(CSV)) {
              String tableId = terms[0];
              String fileQualifier = null;
              request = new ImportRequest(tableId, fileQualifier);
            } else if (terms.length == 3 && terms[2].equals(CSV)) {
              String tableId = terms[0];
              String fileQualifier = terms[1];
              request = new ImportRequest(tableId, fileQualifier);
            } else if (terms.length == 4 && terms[2].equals(PROPERTIES) && terms[3]
                .equals(CSV)) {
              String tableId = terms[0];
              String fileQualifier = terms[1];
              request = new ImportRequest(tableId, fileQualifier);
            }

            if (request != null) {
              tableIdInProgress = request.getTableId();
              boolean success = cu.importSeparable(new ImportListener() {
                @Override
                public void updateProgressDetail(int row, int total) {
                  getSupervisor().publishProgress(displayTablesProgress,
                      appContext.getString(R.string.import_in_progress, row, total));
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

  private void updateFormDirs(InitializationOutcome pendingOutcome)
      throws ServicesAvailabilityException {

    // /////////////////////////////////////////
    // /////////////////////////////////////////
    // /////////////////////////////////////////
    // scan for new forms...

    String completionString = appContext.getString(R.string.searching_for_form_defs);
    getSupervisor().publishProgress(completionString, null);

    DbHandle dbHandle = null;
    try {
      dbHandle = getSupervisor().getDatabase().openDatabase(appName);
      List<String> tableIds = getSupervisor().getDatabase().getAllTableIds(appName, dbHandle);

      for ( int i = 0 ; i < tableIds.size() ; ++i ) {
        String tableId = tableIds.get(i);

        // specifically target this tableId...
        WebLogger.getLogger(appName).i(TAG, "updateFormInfo: tableid: " + tableId);

        String examString = appContext
            .getString(R.string.updating_table_form_information, tableId, i + 1, tableIds.size());
        getSupervisor().publishProgress(examString, null);


        boolean outcome = getSupervisor().getDatabase().rescanTableFormDefs(appName, dbHandle,
            tableId);
        if ( outcome ) {

          String successMessage = appContext.getString(R.string.table_forms_register_success,
              tableId);
          pendingOutcome.add(successMessage);
        } else {
          String failureMessage = appContext.getString(R.string.table_forms_register_failure,
              tableId);
          pendingOutcome.add(failureMessage);
          pendingOutcome.problemDefiningForms = true;
        }
      }
    } finally {
      if (dbHandle != null) {
        getSupervisor().getDatabase().closeDatabase(appName, dbHandle);
      }
    }
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

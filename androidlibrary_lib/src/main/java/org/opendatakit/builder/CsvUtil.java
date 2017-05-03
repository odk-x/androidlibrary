/*
 * Copyright (C) 2012 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.builder;

import android.content.ContentValues;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;
import org.opendatakit.aggregate.odktables.rest.ConflictType;
import org.opendatakit.aggregate.odktables.rest.ElementDataType;
import org.opendatakit.aggregate.odktables.rest.KeyValueStoreConstants;
import org.opendatakit.aggregate.odktables.rest.RFC4180CsvReader;
import org.opendatakit.aggregate.odktables.rest.RFC4180CsvWriter;
import org.opendatakit.aggregate.odktables.rest.SavepointTypeManipulator;
import org.opendatakit.aggregate.odktables.rest.SyncState;
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.database.data.ColumnDefinition;
import org.opendatakit.database.data.KeyValueStoreEntry;
import org.opendatakit.database.data.OrderedColumns;
import org.opendatakit.database.data.Row;
import org.opendatakit.database.data.UserTable;
import org.opendatakit.database.service.DbHandle;
import org.opendatakit.database.utilities.CursorUtils;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.listener.ExportListener;
import org.opendatakit.listener.ImportListener;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.provider.DataTableColumns;
import org.opendatakit.utilities.LocalizationUtils;
import org.opendatakit.utilities.ODKFileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Various utilities for importing/exporting tables from/to CSV.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class CsvUtil {

  private static final String TAG = CsvUtil.class.getSimpleName();

  private final String appName;

  private final CsvUtilSupervisor supervisor;

  public CsvUtil(CsvUtilSupervisor supervisor, String appName ) {
    this.supervisor = supervisor;
    this.appName = appName;
  }

  // ===========================================================================================
  // EXPORT
  // ===========================================================================================

  /**
   * Export the given tableId. Exports three csv files to the output/csv
   * directory under the appName:
   * <ul>
   * <li>tableid.fileQualifier.csv - data table</li>
   * <li>tableid.fileQualifier.definition.csv - data table column definition</li>
   * <li>tableid.fileQualifier.properties.csv - key-value store of this table</li>
   * </ul>
   * If fileQualifier is null or an empty string, then it emits to
   * <ul>
   * <li>tableid.csv - data table</li>
   * <li>tableid.definition.csv - data table column definition</li>
   * <li>tableid.properties.csv - key-value store of this table</li>
   * </ul>
   *
   * @param exportListener
   * @param db
   * @param tableId
   * @param orderedDefns
   * @param fileQualifier
   * @return
   * @throws ServicesAvailabilityException
   */
  public boolean exportSeparable(ExportListener exportListener, DbHandle db, String tableId,
      OrderedColumns orderedDefns, String fileQualifier) throws ServicesAvailabilityException {
    // building array of columns to select and header row for output file
    // then we are including all the metadata columns.
    ArrayList<String> columns = new ArrayList<String>();

    WebLogger.getLogger(appName).i(
        TAG,
        "exportSeparable: tableId: " + tableId + " fileQualifier: "
            + ((fileQualifier == null) ? "<null>" : fileQualifier));

    // put the user-relevant metadata columns in leftmost columns
    columns.add(DataTableColumns.ID);
    columns.add(DataTableColumns.FORM_ID);
    columns.add(DataTableColumns.LOCALE);
    columns.add(DataTableColumns.SAVEPOINT_TYPE);
    columns.add(DataTableColumns.SAVEPOINT_TIMESTAMP);
    columns.add(DataTableColumns.SAVEPOINT_CREATOR);

    // add the data columns
    for (ColumnDefinition cd : orderedDefns.getColumnDefinitions()) {
      if (cd.isUnitOfRetention()) {
        columns.add(cd.getElementKey());
      }
    }

    // And now add all remaining export columns
    String[] exportColumns = supervisor.getDatabase().getExportColumns();
    for (String colName : exportColumns) {
      if (columns.contains(colName)) {
        continue;
      }
      columns.add(colName);
    }

    File tableInstancesFolder = new File(ODKFileUtils.getInstancesFolder(appName, tableId));
    HashSet<File> instancesWithData = new HashSet<File>();
    if ( tableInstancesFolder.exists() && tableInstancesFolder.isDirectory() ) {
      File[] subDirectories = tableInstancesFolder.listFiles(new FileFilter() {

        @Override
        public boolean accept(File pathname) {
          return pathname.isDirectory() && (pathname.list().length != 0);
        }});
      instancesWithData.addAll(Arrays.asList(subDirectories));
    }

    OutputStreamWriter output = null;
    File outputCsv = null;
    try {
      // both files go under the output/csv directory...
      outputCsv = new File(ODKFileUtils.getOutputTableCsvFile(appName, tableId, fileQualifier));
      outputCsv.mkdirs();

      // emit properties files
      File definitionCsv = new File(ODKFileUtils.getOutputTableDefinitionCsvFile(appName, tableId,
          fileQualifier));
      File propertiesCsv = new File(ODKFileUtils.getOutputTablePropertiesCsvFile(appName, tableId,
          fileQualifier));

      if (!writePropertiesCsv(db, tableId, orderedDefns, definitionCsv, propertiesCsv)) {
        return false;
      }

      // getting data
      String whereString = DataTableColumns.SAVEPOINT_TYPE + " IS NOT NULL AND ("
          + DataTableColumns.CONFLICT_TYPE + " IS NULL OR " + DataTableColumns.CONFLICT_TYPE
          + " = " + Integer.toString(ConflictType.LOCAL_UPDATED_UPDATED_VALUES) + ")";

      String[] emptyArray = {};

      UserTable table = supervisor.getDatabase()
          .simpleQuery(appName, db, tableId, orderedDefns, whereString, emptyArray, emptyArray,
              null, null, null, null, null);

      // emit data table...
      File file = new File(outputCsv, tableId
          + ((fileQualifier != null && fileQualifier.length() != 0) ? ("." + fileQualifier) : "")
          + ".csv");
      FileOutputStream out = new FileOutputStream(file);
      output = new OutputStreamWriter(out, CharEncoding.UTF_8);
      RFC4180CsvWriter cw = new RFC4180CsvWriter(output);
      // don't have to worry about quotes in elementKeys...
      cw.writeNext(columns.toArray(new String[columns.size()]));
      String[] row = new String[columns.size()];
      for (int i = 0; i < table.getNumberOfRows(); i++) {
        Row dataRow = table.getRowAtIndex(i);
        for (int j = 0; j < columns.size(); ++j) {
          row[j] = dataRow.getDataByKey(columns.get(j));
        }
        cw.writeNext(row);
        /**
         * Copy all attachment files into the output directory tree.
         * Don't worry about whether they are referenced in the current
         * row. This is a simplification (and biases toward preserving
         * data).
         */
        String instanceId = table.getRowId(i);
        File tableInstanceFolder = new File(ODKFileUtils.getInstanceFolder(appName, tableId, instanceId));
        if ( instancesWithData.contains(tableInstanceFolder) ) {
          File outputInstanceFolder = new File(ODKFileUtils.getOutputCsvInstanceFolder(appName, tableId, instanceId));
          outputInstanceFolder.mkdirs();
          FileUtils.copyDirectory(tableInstanceFolder, outputInstanceFolder);
          instancesWithData.remove(tableInstanceFolder);
        }

      }
      cw.flush();
      cw.close();

      return true;
    } catch (IOException e) {
      if (outputCsv != null) {
        try {
          File outputCsvFolder = new File(ODKFileUtils.getOutputCsvFolder(appName));
          while (FileUtils.directoryContains(outputCsvFolder, outputCsv)) {
            FileUtils.deleteDirectory(outputCsv);
            outputCsv = outputCsv.getParentFile();
          }
        } catch (IOException e1) {
          e1.printStackTrace();
          return false;
        }
      }
      return false;
    } finally {
      try {
        if (output != null) {
          output.close();
        }
      } catch (IOException e) {
      }
    }
  }

  /**
   * Writes the definition and properties files for the given tableId. This is
   * written to:
   * <ul>
   * <li>tables/tableId/definition.csv - data table column definition</li>
   * <li>tables/tableId/properties.csv - key-value store of this table</li>
   * </ul>
   * The definition.csv file contains the schema definition. md5hash of it
   * corresponds to the former schemaETag.
   *
   * The properties.csv file contains the table-level metadata (key-value
   * store). The md5hash of it corresponds to the propertiesETag.
   *
   * For use by the sync mechanism.
   *
   * @param db
   * @param tableId
   * @param orderedDefns
   * @return
   * @throws ServicesAvailabilityException
   */
  public boolean writePropertiesCsv(DbHandle db, String tableId,
      OrderedColumns orderedDefns) throws ServicesAvailabilityException {
    File definitionCsv = new File(ODKFileUtils.getTableDefinitionCsvFile(appName, tableId));
    File propertiesCsv = new File(ODKFileUtils.getTablePropertiesCsvFile(appName, tableId));

    return writePropertiesCsv(db, tableId, orderedDefns, definitionCsv, propertiesCsv);
  }

  /**
   * Common routine to write the definition and properties files.
   *
   * @param db
   * @param tableId
   * @param orderedDefns
   * @param definitionCsv
   * @param propertiesCsv
   * @return
   * @throws ServicesAvailabilityException
   */
  private boolean writePropertiesCsv(DbHandle db, String tableId,
      OrderedColumns orderedDefns, File definitionCsv, File propertiesCsv) throws ServicesAvailabilityException {
    WebLogger.getLogger(appName).i(TAG, "writePropertiesCsv: tableId: " + tableId);

    /**
     * Get all the KVS entries and scan through, replacing all choice list
     * choiceListId with the underlying choice list.  On input, these are split
     * off and replaced by choiceListIds.
     */
    List<KeyValueStoreEntry> kvsEntries = supervisor.getDatabase()
        .getTableMetadata(appName, db, tableId, null, null, null, null).getEntries();
    for (int i = 0; i < kvsEntries.size(); i++) {
      KeyValueStoreEntry entry = kvsEntries.get(i);

      // replace all the choiceList entries with their choiceListJSON
      if (entry.partition.equals(KeyValueStoreConstants.PARTITION_COLUMN) && entry.key
          .equals(KeyValueStoreConstants.COLUMN_DISPLAY_CHOICES_LIST)) {
        // exported type is an array -- the choiceListJSON
        entry.type = ElementDataType.array.name();
        if ((entry.value != null) && (entry.value.trim().length() != 0)) {
          String choiceListJSON = supervisor.getDatabase().getChoiceList(appName, db, entry.value);
          entry.value = choiceListJSON;
        } else {
          entry.value = null;
        }
      }
    }

    return PropertiesFileUtils.writePropertiesIntoCsv(appName, tableId, orderedDefns, kvsEntries,
        definitionCsv, propertiesCsv);
  }

  private int countUpToLastNonNullElement(String[] row) {
    for (int i = row.length - 1; i >= 0; --i) {
      if (row[i] != null) {
        return (i + 1);
      }
    }
    return 0;
  }

  /**
   * Update tableId from
   * <ul>
   * <li>tables/tableId/properties.csv</li>
   * <li>tables/tableId/definition.csv</li>
   * </ul>
   *
   * This will either create a table, or verify that the table structure matches
   * that defined in the csv. It will then override all the KVS entries with
   * those present in the file.
   *
   * @param tableId
   * @throws IOException
   * @throws ServicesAvailabilityException
   */
  public synchronized void updateTablePropertiesFromCsv(String tableId)
      throws IOException, ServicesAvailabilityException {

    PropertiesFileUtils.DataTableDefinition dtd = PropertiesFileUtils.readPropertiesFromCsv(appName,
        tableId);

    DbHandle db = null;
    try {

      db = supervisor.getDatabase().openDatabase(appName);

      // Go through the KVS list and replace all the choiceList entries with their choiceListId
      for ( KeyValueStoreEntry entry : dtd.kvsEntries ) {
        if ( entry.partition.equals(KeyValueStoreConstants.PARTITION_COLUMN) &&
            entry.key.equals(KeyValueStoreConstants.COLUMN_DISPLAY_CHOICES_LIST) ) {
          // stored type is a string -- the choiceListId
          entry.type = ElementDataType.string.name();
          if ((entry.value != null) && (entry.value.trim().length() != 0)) {
            String choiceListId = supervisor.getDatabase().setChoiceList(appName, db, entry.value);
            entry.value = choiceListId;
          } else {
            entry.value = null;
          }
        }
      }

      supervisor.getDatabase().createOrOpenTableWithColumnsAndProperties(appName, db, tableId,
          dtd.columnList, dtd.kvsEntries, true);

    } finally {
      if (db != null) {
        supervisor.getDatabase().closeDatabase(appName, db);
      }
    }
  }

  /**
   * Imports data from a csv file with elementKey headings. This csv file is
   * assumed to be under:
   * <ul>
   * <li>config/assets/csv/tableId.fileQualifier.csv</li>
   * </ul>
   * If the table does not exist, it attempts to create it using the schema and
   * metadata located here:
   * <ul>
   * <li>tables/tableId/definition.csv - data table definition</li>
   * <li>tables/tableId/properties.csv - key-value store</li>
   * </ul>
   *
   * @param importListener
   * @param tableId
   * @param fileQualifier
   * @param createIfNotPresent
   *          -- true if we should try to create the table.
   * @return
   * @throws ServicesAvailabilityException
   */
  public boolean importSeparable(ImportListener importListener, String tableId,
      String fileQualifier, boolean createIfNotPresent) throws ServicesAvailabilityException {

    DbHandle db = null;
    try {
      db = supervisor.getDatabase().openDatabase(appName);
      if (!supervisor.getDatabase().hasTableId(appName, db, tableId)) {
        if (createIfNotPresent) {
          updateTablePropertiesFromCsv(tableId);
          if (!supervisor.getDatabase().hasTableId(appName, db, tableId)) {
            return false;
          }
        } else {
          return false;
        }
      }

      OrderedColumns orderedDefns = supervisor.getDatabase().getUserDefinedColumns(appName, db, tableId);

      WebLogger.getLogger(appName).i(
          TAG,
          "importSeparable: tableId: " + tableId + " fileQualifier: "
              + ((fileQualifier == null) ? "<null>" : fileQualifier));

      // reading data
      InputStreamReader input = null;
      try {

        File assetsCsvInstances = new File(ODKFileUtils.getAssetsCsvInstancesFolder(appName, tableId));
        HashSet<File> instancesHavingData = new HashSet<File>();
        if ( assetsCsvInstances.exists() && assetsCsvInstances.isDirectory() ) {
          File[] subDirectories = assetsCsvInstances.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
              return pathname.isDirectory() && (pathname.list().length != 0);
            }});
          instancesHavingData.addAll(Arrays.asList(subDirectories));
        }

        // both files are read from config/assets/csv directory...
        File assetsCsv = new File(ODKFileUtils.getAssetsCsvFolder(appName));

        // read data table...
        File file = new File(assetsCsv, tableId
            + ((fileQualifier != null && fileQualifier.length() != 0) ? ("." + fileQualifier) : "")
            + ".csv");
        FileInputStream in = new FileInputStream(file);
        input = new InputStreamReader(in, CharEncoding.UTF_8);
        RFC4180CsvReader cr = new RFC4180CsvReader(input);
        // don't have to worry about quotes in elementKeys...
        String[] columnsInFile = cr.readNext();
        int columnsInFileLength = countUpToLastNonNullElement(columnsInFile);

        String v_id;
        String v_form_id;
        String v_locale;
        String v_savepoint_type;
        String v_savepoint_creator;
        String v_savepoint_timestamp;
        String v_row_etag;
        String v_default_access;
        String v_owner;
        String v_group_read_only;
        String v_group_modify;
        String v_group_privileged;

        Map<String, String> valueMap = new HashMap<String, String>();

        int rowCount = 0;
        String[] row;
        for (;;) {
          row = cr.readNext();
          rowCount++;
          if (rowCount % 5 == 0) {
            importListener.updateProgressDetail("Importing row " + rowCount);
          }
          if (row == null || countUpToLastNonNullElement(row) == 0) {
            break;
          }
          int rowLength = countUpToLastNonNullElement(row);

          // default values for metadata columns if not provided
          v_id = UUID.randomUUID().toString();
          v_form_id = null;
          v_locale = CursorUtils.DEFAULT_LOCALE;
          v_savepoint_type = SavepointTypeManipulator.complete();
          v_savepoint_creator = CursorUtils.DEFAULT_CREATOR;
          v_savepoint_timestamp = TableConstants.nanoSecondsFromMillis(System.currentTimeMillis());
          v_row_etag = null;
          v_default_access = DataTableColumns.DEFAULT_DEFAULT_ACCESS;
          v_owner = DataTableColumns.DEFAULT_OWNER;
          v_group_read_only = DataTableColumns.DEFAULT_GROUP_READ_ONLY;
          v_group_modify = DataTableColumns.DEFAULT_GROUP_MODDIFY;
          v_group_privileged = DataTableColumns.DEFAULT_GROUP_PRIVILEGED;

          // clear value map
          valueMap.clear();

          boolean foundId = false;
          for (int i = 0; i < columnsInFileLength; ++i) {
            if (i >= rowLength)
              break;
            String column = columnsInFile[i];
            String tmp = row[i];
            if (DataTableColumns.ID.equals(column)) {
              if (tmp != null && tmp.length() != 0) {
                foundId = true;
                v_id = tmp;
              }
              continue;
            }
            if (DataTableColumns.FORM_ID.equals(column)) {
              if (tmp != null && tmp.length() != 0) {
                v_form_id = tmp;
              }
              continue;
            }
            if (DataTableColumns.LOCALE.equals(column)) {
              if (tmp != null && tmp.length() != 0) {
                v_locale = tmp;
              }
              continue;
            }
            if (DataTableColumns.SAVEPOINT_TYPE.equals(column)) {
              if (tmp != null && tmp.length() != 0) {
                v_savepoint_type = tmp;
              }
              continue;
            }
            if (DataTableColumns.SAVEPOINT_CREATOR.equals(column)) {
              if (tmp != null && tmp.length() != 0) {
                v_savepoint_creator = tmp;
              }
              continue;
            }
            if (DataTableColumns.SAVEPOINT_TIMESTAMP.equals(column)) {
              if (tmp != null && tmp.length() != 0) {
                v_savepoint_timestamp = tmp;
              }
              continue;
            }
            if (DataTableColumns.ROW_ETAG.equals(column)) {
              if (tmp != null && tmp.length() != 0) {
                v_row_etag = tmp;
              }
              continue;
            }
            if (DataTableColumns.DEFAULT_ACCESS.equals(column)) {
              if (tmp != null && tmp.length() != 0) {
                v_default_access = tmp;
              }
              continue;
            }
            if (DataTableColumns.OWNER.equals(column)) {
              if (tmp != null && tmp.length() != 0) {
                v_owner = tmp;
              }
              continue;
            }
            if (DataTableColumns.GROUP_READ_ONLY.equals(column)) {
              if (tmp != null && tmp.length() != 0) {
                v_group_read_only = tmp;
              }
              continue;
            }
            if (DataTableColumns.GROUP_MODIFY.equals(column)) {
              if (tmp != null && tmp.length() != 0) {
                v_group_modify = tmp;
              }
              continue;
            }
            if (DataTableColumns.GROUP_PRIVILEGED.equals(column)) {
              if (tmp != null && tmp.length() != 0) {
                v_group_privileged = tmp;
              }
              continue;
            }

            try {
              orderedDefns.find(column);
              valueMap.put(column, tmp);
            } catch (IllegalArgumentException e) {
              // this is OK --
              // the csv contains an extra column
            }
          }

          // TODO: should resolve this properly when we have conflict rows and
          // uncommitted edits. For now, we just add our csv import to those,
          // rather
          // than resolve the problems.
          UserTable table = supervisor.getDatabase().privilegedGetRowsWithId(appName, db,
              tableId, orderedDefns, v_id);
          if (table.getNumberOfRows() > 1) {
            throw new IllegalStateException(
                "There are either checkpoint or conflict rows in the destination table");
          }

          SyncState syncState = null;
          if (foundId && table.getNumberOfRows() == 1) {
            String syncStateStr = table.getRowAtIndex(0).getDataByKey(DataTableColumns.SYNC_STATE);
            if (syncStateStr == null) {
              throw new IllegalStateException("Unexpected null syncState value");
            }
            syncState = SyncState.valueOf(syncStateStr);
          }

          /**
           * Insertion will set the SYNC_STATE to new_row.
           *
           * If the table is sync'd to the server, this will cause one sync
           * interaction with the server to confirm that the server also has
           * this record.
           *
           * If a record with this same rowId already exists, if it is in an
           * new_row sync state, we update it here. Otherwise, if there were any
           * local changes, we leave the row unchanged.
           */
          if (syncState != null) {

            ContentValues cv = new ContentValues();
            for (String column : valueMap.keySet()) {
              if (column != null) {
                cv.put(column, valueMap.get(column));
              }
            }

            // The admin columns get added here
            cv.put(DataTableColumns.FORM_ID, v_form_id);
            cv.put(DataTableColumns.LOCALE, v_locale);
            cv.put(DataTableColumns.SAVEPOINT_TYPE, v_savepoint_type);
            cv.put(DataTableColumns.SAVEPOINT_TIMESTAMP, v_savepoint_timestamp);
            cv.put(DataTableColumns.SAVEPOINT_CREATOR, v_savepoint_creator);
            cv.put(DataTableColumns.ROW_ETAG, v_row_etag);
            cv.put(DataTableColumns.DEFAULT_ACCESS, v_default_access);
            cv.put(DataTableColumns.OWNER, v_owner);
            cv.put(DataTableColumns.GROUP_READ_ONLY, v_group_read_only);
            cv.put(DataTableColumns.GROUP_MODIFY, v_group_modify);
            cv.put(DataTableColumns.GROUP_PRIVILEGED, v_group_privileged);

            cv.put(DataTableColumns.SYNC_STATE, SyncState.new_row.name());
            cv.putNull(DataTableColumns.CONFLICT_TYPE);

            if (v_id != null) {
              cv.put(DataTableColumns.ID, v_id);
            }

            if (syncState == SyncState.new_row) {
              // delete the existing row then insert the new values for it
              supervisor.getDatabase().privilegedDeleteRowWithId(appName, db, tableId, orderedDefns,
                  v_id);
              supervisor.getDatabase().privilegedInsertRowWithId(appName, db, tableId, orderedDefns,
                  cv, v_id, true);
            }
            // otherwise, do NOT update the row.
            // i.e., if the row has been sync'd with
            // the server, then we don't revise it.

          } else {

            ContentValues cv = new ContentValues();
            for (String column : valueMap.keySet()) {
              if (column != null) {
                cv.put(column, valueMap.get(column));
              }
            }

            // The admin columns get added here
            cv.put(DataTableColumns.FORM_ID, v_form_id);
            cv.put(DataTableColumns.LOCALE, v_locale);
            cv.put(DataTableColumns.SAVEPOINT_TYPE, v_savepoint_type);
            cv.put(DataTableColumns.SAVEPOINT_TIMESTAMP, v_savepoint_timestamp);
            cv.put(DataTableColumns.SAVEPOINT_CREATOR, v_savepoint_creator);
            cv.put(DataTableColumns.ROW_ETAG, v_row_etag);
            cv.put(DataTableColumns.DEFAULT_ACCESS, v_default_access);
            cv.put(DataTableColumns.OWNER, v_owner);
            cv.put(DataTableColumns.GROUP_READ_ONLY, v_group_read_only);
            cv.put(DataTableColumns.GROUP_MODIFY, v_group_modify);
            cv.put(DataTableColumns.GROUP_PRIVILEGED, v_group_privileged);

            cv.put(DataTableColumns.SYNC_STATE, SyncState.new_row.name());
            cv.putNull(DataTableColumns.CONFLICT_TYPE);

            if (v_id == null) {
              v_id = LocalizationUtils.genUUID();
            }

            cv.put(DataTableColumns.ID, v_id);

            // imports assume super-user level powers. Treat these as if they were
            // directed by the server during a sync.
            supervisor.getDatabase().privilegedInsertRowWithId(appName, db, tableId, orderedDefns,
                cv, v_id, true);
          }

          /**
           * Copy all attachment files into the destination row.
           * The attachments are in instance-id-labeled sub-directories.
           * Anything in the corresponding subdirectory should be
           * referenced by the valuesMap above. If it isn't, don't worry about
           * it. This is a simplification.
           */
          File assetsInstanceFolder = new File(ODKFileUtils.getAssetsCsvInstanceFolder(appName, tableId, v_id));
          if ( instancesHavingData.contains(assetsInstanceFolder) ) {
            File tableInstanceFolder = new File(ODKFileUtils.getInstanceFolder(appName, tableId, v_id));
            tableInstanceFolder.mkdirs();
            FileUtils.copyDirectory(assetsInstanceFolder, tableInstanceFolder);
            instancesHavingData.remove(assetsInstanceFolder);
          }

        }
        cr.close();
        return true;
      } catch (IOException e) {
        return false;
      } finally {
        try {
          input.close();
        } catch (IOException e) {
        }
      }
    } catch (IOException e) {
      return false;
    } finally {
      if (db != null) {
        supervisor.getDatabase().closeDatabase(appName, db);
      }
    }
  }

}

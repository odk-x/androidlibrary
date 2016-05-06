/*
 * Copyright (C) 2016 University of Washington
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

package org.opendatakit.database;

import android.content.ContentValues;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import org.opendatakit.common.android.data.*;
import org.opendatakit.common.android.utilities.OdkDbChunkUtil;
import org.opendatakit.database.service.KeyValueStoreEntry;
import org.opendatakit.database.service.OdkDbChunk;
import org.opendatakit.database.service.OdkDbHandle;
import org.opendatakit.database.service.OdkDbInterface;
import org.opendatakit.database.service.TableHealthInfo;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

public class OdkDbSerializedInterface {

   private static final String TAG = OdkDbSerializedInterface.class.getSimpleName();

   private OdkDbInterface dbInterface;

   public OdkDbSerializedInterface(OdkDbInterface dbInterface) throws IllegalArgumentException {
      if (dbInterface == null) {
         throw new IllegalArgumentException("Database Interface must not be null");
      }

      this.dbInterface = dbInterface;
   }

   public OdkDbInterface getDbInterface() {
      return dbInterface;
   }

   public void setDbInterface(OdkDbInterface dbInterface) {
      if (dbInterface == null) {
         throw new IllegalArgumentException("Database Interface must not be null");
      }

      this.dbInterface = dbInterface;
   }

   /**
    * Obtain a databaseHandleName
    *
    * @param appName
    * @return dbHandleName
    */
   public OdkDbHandle openDatabase(String appName) throws RemoteException {
      return dbInterface.openDatabase(appName);
   }

   /**
    * Release the databaseHandle. Will roll back any outstanding transactions
    * and release/close the database handle.
    *
    * @param appName
    * @param dbHandleName
    */
   public void closeDatabase(String appName, OdkDbHandle dbHandleName) throws RemoteException {
      dbInterface.closeDatabase(appName, dbHandleName);
   }

   /**
    * Call this when the schemaETag for the given tableId has changed on the server.
    * <p/>
    * This is a combination of:
    * <p/>
    * Clean up this table and set the dataETag to null.
    * <p/>
    * changeDataRowsToNewRowState(sc.getAppName(), db, tableId);
    * <p/>
    * we need to clear out the dataETag so
    * that we will pull all server changes and sync our properties.
    * <p/>
    * updateDBTableETags(sc.getAppName(), db, tableId, schemaETag, null);
    * <p/>
    * Although the server does not recognize this tableId, we can
    * keep our record of the ETags for the table-level files and
    * manifest. These may enable us to short-circuit the restoration
    * of the table-level files should another client be simultaneously
    * trying to restore those files to the server.
    * <p/>
    * However, we do need to delete all the instance-level files,
    * as these are tied to the schemaETag we hold, and that is now
    * invalid.
    * <p/>
    * if the local table ever had any server sync information for this
    * host then clear it. If the user changed the server URL, we have
    * already cleared this information.
    * <p/>
    * Clearing it here handles the case where an admin deleted the
    * table on the server and we are now re-pushing that table to
    * the server.
    * <p/>
    * We do not know whether the rows on the device match those on the server.
    * We will find out later, in the course of the sync.
    * <p/>
    * if (tableInstanceFilesUri != null) {
    * deleteAllSyncETagsUnderServer(sc.getAppName(), db, tableInstanceFilesUri);
    * }
    */
   public void serverTableSchemaETagChanged(String appName, OdkDbHandle dbHandleName,
       String tableId, String schemaETag, String tableInstanceFilesUri) throws RemoteException {
      dbInterface.serverTableSchemaETagChanged(appName, dbHandleName, tableId, schemaETag,
          tableInstanceFilesUri);
   }

   /**
    * Compute the app-global choiceListId for this choiceListJSON
    * and register the tuple of (choiceListId, choiceListJSON).
    * Return choiceListId.
    *
    * @param appName
    * @param dbHandleName
    * @param choiceListJSON -- the actual JSON choice list text.
    * @return choiceListId -- the unique code mapping to the choiceListJSON
    */
   public String setChoiceList(String appName, OdkDbHandle dbHandleName, String choiceListJSON)
       throws RemoteException {
      return dbInterface.setChoiceList(appName, dbHandleName, choiceListJSON);
   }

   /**
    * Return the choice list JSON corresponding to the choiceListId
    *
    * @param appName
    * @param dbHandleName
    * @param choiceListId -- the md5 hash of the choiceListJSON
    * @return choiceListJSON -- the actual JSON choice list text.
    */
   public String getChoiceList(String appName, OdkDbHandle dbHandleName, String choiceListId)
       throws RemoteException {
      return dbInterface.getChoiceList(appName, dbHandleName, choiceListId);
   }

   /**
    * If the tableId is not recorded in the TableDefinition metadata table, then
    * create the tableId with the indicated columns. This will synthesize
    * reasonable metadata KVS entries for table.
    * <p/>
    * If the tableId is present, then this is a no-op.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param columns      simple transport wrapper for List<Columns>
    * @return the OrderedColumns of the user columns in the table.
    */
   public OrderedColumns createOrOpenDBTableWithColumns(String appName, OdkDbHandle dbHandleName,
       String tableId, ColumnList columns) throws RemoteException {

      return fetchAndRebuildChunks(
          dbInterface.createOrOpenDBTableWithColumns(appName, dbHandleName, tableId, columns),
          OrderedColumns.CREATOR);
   }

   /**
    * If the tableId is not recorded in the TableDefinition metadata table, then
    * create the tableId with the indicated columns. And apply the supplied KVS
    * settings. If some are missing, this will synthesize reasonable metadata KVS
    * entries for table.
    * <p/>
    * If the table is present, this will delete and replace the KVS with the given KVS
    * entries if the clear flag is true
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param columns      simple transport wrapper for List<Columns>
    * @param metaData     a List<KeyValueStoreEntry>
    * @param clear        if true then delete the existing set of values for this
    *                     tableId before inserting or replacing with the new ones.
    * @return the OrderedColumns of the user columns in the table.
    */
   public OrderedColumns createOrOpenDBTableWithColumnsAndProperties(String appName,
       OdkDbHandle dbHandleName, String tableId, ColumnList columns,
       List<KeyValueStoreEntry> metaData, boolean clear) throws RemoteException {

      return fetchAndRebuildChunks(dbInterface
          .createOrOpenDBTableWithColumnsAndProperties(appName, dbHandleName, tableId, columns,
              metaData, clear), OrderedColumns.CREATOR);
   }

   /**
    * Drop the given tableId and remove all the files (both configuration and
    * data attachments) associated with that table.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    */
   public void deleteDBTableAndAllData(String appName, OdkDbHandle dbHandleName, String tableId)
       throws RemoteException {
      dbInterface.deleteDBTableAndAllData(appName, dbHandleName, tableId);
   }

   /**
    * The deletion filter includes all non-null arguments. If all arguments
    * (except the db) are null, then all properties are removed.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param partition
    * @param aspect
    * @param key
    */
   public void deleteDBTableMetadata(String appName, OdkDbHandle dbHandleName, String tableId,
       String partition, String aspect, String key) throws RemoteException {
      dbInterface.deleteDBTableMetadata(appName, dbHandleName, tableId, partition, aspect, key);
   }

   /**
    * Return an array of the admin columns that must be present in
    * every database table.
    *
    * @return
    */
   public String[] getAdminColumns() throws RemoteException {
      return fetchAndRebuildChunks(dbInterface.getAdminColumns(), String[].class);
   }

   /**
    * Return all the columns in the given table, including any metadata columns.
    * This does a direct query against the database and is suitable for accessing
    * non-managed tables. It does not access any metadata and therefore will not
    * report non-unit-of-retention (grouping) columns.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @return
    */
   public String[] getAllColumnNames(String appName, OdkDbHandle dbHandleName, String tableId)
       throws RemoteException {
      return fetchAndRebuildChunks(dbInterface.getAllColumnNames(appName, dbHandleName, tableId),
          String[].class);
   }

   /**
    * Return all the tableIds in the database.
    *
    * @param appName
    * @param dbHandleName
    * @return List<String> of tableIds
    */
   public List<String> getAllTableIds(String appName, OdkDbHandle dbHandleName)
       throws RemoteException {
      Serializable result = fetchAndRebuildChunks(dbInterface.getAllTableIds(appName, dbHandleName),
          Serializable.class);
      return (List<String>) result;
   }

   /**
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param partition
    * @param aspect
    * @param key
    * @return list of KeyValueStoreEntry values matching the filter criteria
    * @throws RemoteException
    */
   public List<KeyValueStoreEntry> getDBTableMetadata(String appName, OdkDbHandle dbHandleName,
       String tableId, String partition, String aspect, String key) throws RemoteException {
      Serializable result = fetchAndRebuildChunks(
          dbInterface.getDBTableMetadata(appName, dbHandleName, tableId, partition, aspect, key),
          Serializable.class);
      return (List<KeyValueStoreEntry>) result;
   }

   /**
    * Return an array of the admin columns that should be exported to
    * a CSV file. This list excludes the SYNC_STATE and CONFLICT_TYPE columns.
    *
    * @return
    */
   public String[] getExportColumns() throws RemoteException {
      return fetchAndRebuildChunks(dbInterface.getExportColumns(), String[].class);
   }

   /**
    * Get the table definition entry for a tableId. This specifies the schema
    * ETag, the data-modification ETag, and the date-time of the last successful
    * sync of the table to the server.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @return
    */
   public TableDefinitionEntry getTableDefinitionEntry(String appName, OdkDbHandle dbHandleName,
       String tableId) throws RemoteException {
      return fetchAndRebuildChunks(
          dbInterface.getTableDefinitionEntry(appName, dbHandleName, tableId),
          TableDefinitionEntry.CREATOR);
   }

   /**
    * Return the list of all tables and their health status.
    *
    * @param appName
    * @param dbHandleName
    * @return the list of TableHealthInfo records for this appName
    */
   public List<TableHealthInfo> getTableHealthStatuses(String appName, OdkDbHandle dbHandleName)
       throws RemoteException {
      Serializable results = fetchAndRebuildChunks(
          dbInterface.getTableHealthStatuses(appName, dbHandleName), Serializable.class);

      return (List<TableHealthInfo>) results;
   }

   /**
    * Retrieve the list of user-defined columns for a tableId using the metadata
    * for that table. Returns the unit-of-retention and non-unit-of-retention
    * (grouping) columns.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @return
    */
   public OrderedColumns getUserDefinedColumns(String appName, OdkDbHandle dbHandleName,
       String tableId) throws RemoteException {
      return fetchAndRebuildChunks(
          dbInterface.getUserDefinedColumns(appName, dbHandleName, tableId),
          OrderedColumns.CREATOR);
   }

   /**
    * Verifies that the tableId exists in the database.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @return true if table is listed in table definitions.
    */
   public boolean hasTableId(String appName, OdkDbHandle dbHandleName, String tableId)
       throws RemoteException {
      return dbInterface.hasTableId(appName, dbHandleName, tableId);
   }

   /**
    * Get a {@link UserTable} for this table based on the given where clause. All
    * columns from the table are returned.
    * <p/>
    * SELECT * FROM table WHERE whereClause GROUP BY groupBy[]s HAVING
    * havingClause ORDER BY orderbyElement orderByDirection
    * <p/>
    * If any of the clause parts are omitted (null), then the appropriate
    * simplified SQL statement is constructed.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param columnDefns
    * @param whereClause       the whereClause for the selection, beginning with "WHERE". Must
    *                          include "?" instead of actual values, which are instead passed in
    *                          the selectionArgs.
    * @param selectionArgs     an array of string values for bind parameters
    * @param groupBy           an array of elementKeys
    * @param having
    * @param orderByElementKey elementKey to order the results by
    * @param orderByDirection  either "ASC" or "DESC"
    * @return An {@link OdkDbChunk} containing the first partition of the {@link UserTable}. Use
    * {@link getChunk} to retrieve the rest of the chunks.
    */
   public UserTable rawSqlQuery(String appName, OdkDbHandle dbHandleName, String tableId,
       OrderedColumns columnDefns, String whereClause, String[] selectionArgs, String[] groupBy,
       String having, String orderByElementKey, String orderByDirection) throws RemoteException {

      return fetchAndRebuildChunks(dbInterface
          .rawSqlQuery(appName, dbHandleName, tableId, columnDefns, whereClause, selectionArgs,
              groupBy, having, orderByElementKey, orderByDirection), UserTable.CREATOR);
   }

   /**
    * Get a {@link RawUserTable} for the result set of an arbitrary sql query
    * and bind parameters. If the result set has an _id column, it is used as
    * the RowId of the RawRow. Otherwise, an ordinal number is generated and used.
    * <p/>
    * The sql query can be arbitrarily complex and can include joins, unions, etc.
    * The data are returned as string values.
    *
    * @param appName
    * @param dbHandleName
    * @param sqlCommand
    * @param sqlBindArgs
    * @return An {@link OdkDbChunk} containing the first partition of the {@link RawUserTable}. Use
    * {@link getChunk} to retrieve the rest of the chunks.
    */
   public RawUserTable arbitraryQuery(String appName, OdkDbHandle dbHandleName, String sqlCommand,
       String[] sqlBindArgs) throws RemoteException {

      return fetchAndRebuildChunks(
          dbInterface.arbitraryQuery(appName, dbHandleName, sqlCommand, sqlBindArgs),
          RawUserTable.CREATOR);
   }

   /**
    * Insert or update a single table-level metadata KVS entry.
    *
    * @param appName
    * @param dbHandleName
    * @param entry
    */
   public void replaceDBTableMetadata(String appName, OdkDbHandle dbHandleName,
       KeyValueStoreEntry entry) throws RemoteException {

      dbInterface.replaceDBTableMetadata(appName, dbHandleName, entry);
   }

   /**
    * Insert or update a list of table-level metadata KVS entries. If clear is
    * true, then delete the existing set of values for this tableId before
    * inserting the new values.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param metadata     a List<KeyValueStoreEntry>
    * @param clear        if true then delete the existing set of values for this tableId
    *                     before inserting the new ones.
    */
   public void replaceDBTableMetadataList(String appName, OdkDbHandle dbHandleName, String tableId,
       List<KeyValueStoreEntry> entries, boolean clear) throws RemoteException {

      dbInterface.replaceDBTableMetadataList(appName, dbHandleName, tableId, entries, clear);
   }

   /**
    * Atomically delete all the fields under the given (tableId, partition, aspect)
    * and replace with the supplied values.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param partition
    * @param aspect
    * @param metadata     a List<KeyValueStoreEntry>
    */
   public void replaceDBTableMetadataSubList(String appName, OdkDbHandle dbHandleName,
       String tableId, String partition, String aspect, List<KeyValueStoreEntry> entries)
       throws RemoteException {

      dbInterface.replaceDBTableMetadataSubList(appName, dbHandleName, tableId, partition, aspect,
          entries);
   }

   /**
    * Update the schema and data-modification ETags of a given tableId.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param schemaETag
    * @param lastDataETag
    */
   public void updateDBTableETags(String appName, OdkDbHandle dbHandleName, String tableId,
       String schemaETag, String lastDataETag) throws RemoteException {

      dbInterface.updateDBTableETags(appName, dbHandleName, tableId, schemaETag, lastDataETag);
   }

   /**
    * Update the timestamp of the last entirely-successful synchronization
    * attempt of this table.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    */
   public void updateDBTableLastSyncTime(String appName, OdkDbHandle dbHandleName, String tableId)
       throws RemoteException {

      dbInterface.updateDBTableLastSyncTime(appName, dbHandleName, tableId);
   }

   /**
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param rowId
    * @return the sync state of the row (use {@link SyncState.valueOf()} to reconstruct), or null if the
    * row does not exist.
    */
   public String getSyncState(String appName, OdkDbHandle dbHandleName, String tableId,
       String rowId) throws RemoteException {

      return dbInterface.getSyncState(appName, dbHandleName, tableId, rowId);
   }

   /**
    * Update the ETag and SyncState of a given rowId. There should be exactly one
    * record for this rowId in thed database (i.e., no conflicts or checkpoints).
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param rowId
    * @param rowETag
    * @param syncState    - the SyncState.name()
    */
   public void updateRowETagAndSyncState(String appName, OdkDbHandle dbHandleName, String tableId,
       String rowId, String rowETag, String syncState) throws RemoteException {

      dbInterface
          .updateRowETagAndSyncState(appName, dbHandleName, tableId, rowId, rowETag, syncState);
   }

   /**
    * Return the row(s) for the given tableId and rowId. If the row has
    * checkpoints or conflicts, the returned UserTable will have more than one
    * Row returned. Otherwise, it will contain a single row.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param orderedDefns
    * @param rowId
    * @return one or more rows (depending upon sync conflict and edit checkpoint states)
    */
   public UserTable getRowsWithId(String appName, OdkDbHandle dbHandleName, String tableId,
       OrderedColumns orderedDefns, String rowId) throws RemoteException {

      return fetchAndRebuildChunks(
          dbInterface.getRowsWithId(appName, dbHandleName, tableId, orderedDefns, rowId),
          UserTable.CREATOR);
   }

   /**
    * Return the row with the most recent changes for the given tableId and rowId.
    * If the row has conflicts, it throws an exception. Otherwise, it returns the
    * most recent checkpoint or non-checkpoint value; it will contain a single row.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param orderedDefns
    * @param rowId
    * @return
    */
   public UserTable getMostRecentRowWithId(String appName, OdkDbHandle dbHandleName, String tableId,
       OrderedColumns orderedDefns, String rowId) throws RemoteException {

      return fetchAndRebuildChunks(
          dbInterface.getMostRecentRowWithId(appName, dbHandleName, tableId, orderedDefns, rowId),
          UserTable.CREATOR);
   }

   /**
    * A combination of:
    * <p/>
    * deleteServerConflictRowWithId(appName, db, tableId, rowId)
    * placeRowIntoConflict(appName, db, tableId, rowId, localRowConflictType)
    * and, for the values which are the server row changes:
    * insertDataIntoExistingDBTableWithId( appName, db, tableId, orderedColumns, values, rowId)
    * <p/>
    * Change the conflictType for the given row from null (not in conflict) to
    * the specified one.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param orderedColumns
    * @param cvValues
    * @param rowId
    * @param localRowConflictType expected to be one of ConflictType.LOCAL_DELETED_OLD_VALUES (0) or
    *                             ConflictType.LOCAL_UPDATED_UPDATED_VALUES (1)
    */
   public UserTable placeRowIntoServerConflictWithId(String appName, OdkDbHandle dbHandleName,
       String tableId, OrderedColumns orderedColumns, ContentValues cvValues, String rowId,
       int localRowConflictType) throws RemoteException {

      return fetchAndRebuildChunks(dbInterface
          .placeRowIntoServerConflictWithId(appName, dbHandleName, tableId, orderedColumns,
              cvValues, rowId, localRowConflictType), UserTable.CREATOR);
   }

   /**
    * Inserts a checkpoint row for the given rowId in the tableId. Checkpoint
    * rows are created by ODK Survey to hold intermediate values during the
    * filling-in of the form. They act as restore points in the Survey, should
    * the application die.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param orderedColumns
    * @param cvValues
    * @param rowId
    * @return single-row table with the content of the inserted checkpoint
    */
   public UserTable insertCheckpointRowWithId(String appName, OdkDbHandle dbHandleName,
       String tableId, OrderedColumns orderedColumns, ContentValues cvValues, String rowId)
       throws RemoteException {

      return fetchAndRebuildChunks(dbInterface
          .insertCheckpointRowWithId(appName, dbHandleName, tableId, orderedColumns, cvValues,
              rowId), UserTable.CREATOR);
   }

   /**
    * Insert the given rowId with the values in the cvValues. If certain metadata
    * values are not specified in the cvValues, then suitable default values may
    * be supplied for them.
    * <p/>
    * If a row with this rowId and certain matching metadata fields is present,
    * then an exception is thrown.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param orderedColumns
    * @param cvValues
    * @param rowId
    * @return single-row table with the content of the inserted row
    */
   public UserTable insertRowWithId(String appName, OdkDbHandle dbHandleName, String tableId,
       OrderedColumns orderedColumns, ContentValues cvValues, String rowId) throws RemoteException {

      return fetchAndRebuildChunks(dbInterface
              .insertRowWithId(appName, dbHandleName, tableId, orderedColumns, cvValues, rowId),
          UserTable.CREATOR);
   }

   /**
    * Delete any checkpoint rows for the given rowId in the tableId. Checkpoint
    * rows are created by ODK Survey to hold intermediate values during the
    * filling-in of the form. They act as restore points in the Survey, should
    * the application die.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param orderedColumns
    * @param rowId
    */
   public UserTable deleteAllCheckpointRowsWithId(String appName, OdkDbHandle dbHandleName,
       String tableId, OrderedColumns orderedDefns, String rowId) throws RemoteException {

      return fetchAndRebuildChunks(dbInterface
              .deleteAllCheckpointRowsWithId(appName, dbHandleName, tableId, orderedDefns, rowId),
          UserTable.CREATOR);
   }

   /**
    * Delete any checkpoint rows for the given rowId in the tableId. Checkpoint
    * rows are created by ODK Survey to hold intermediate values during the
    * filling-in of the form. They act as restore points in the Survey, should
    * the application die.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param orderedColumns
    * @param rowId
    */
   public UserTable deleteLastCheckpointRowWithId(String appName, OdkDbHandle dbHandleName,
       String tableId, OrderedColumns orderedDefns, String rowId) throws RemoteException {

      return fetchAndRebuildChunks(dbInterface
              .deleteLastCheckpointRowWithId(appName, dbHandleName, tableId, orderedDefns, rowId),
          UserTable.CREATOR);
   }

   /**
    * Delete the specified rowId in this tableId. Deletion respects sync
    * semantics. If the row is in the SyncState.new_row state, then the row and
    * its associated file attachments are immediately deleted. Otherwise, the row
    * is placed into the SyncState.deleted state and will be retained until the
    * device can delete the record on the server.
    * <p/>
    * If you need to immediately delete a record that would otherwise sync to the
    * server, call updateRowETagAndSyncState(...) to set the row to
    * SyncState.new_row, and then call this method and it will be immediately
    * deleted (in this case, unless the record on the server was already deleted,
    * it will remain and not be deleted during any subsequent synchronizations).
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param orderedColumns
    * @param rowId
    */
   public UserTable deleteRowWithId(String appName, OdkDbHandle dbHandleName, String tableId,
       OrderedColumns orderedDefns, String rowId) throws RemoteException {

      return fetchAndRebuildChunks(
          dbInterface.deleteRowWithId(appName, dbHandleName, tableId, orderedDefns, rowId),
          UserTable.CREATOR);
   }

   /**
    * Update all rows for the given rowId to SavepointType 'INCOMPLETE' and
    * remove all but the most recent row. When used with a rowId that has
    * checkpoints, this updates to the most recent checkpoint and removes any
    * earlier checkpoints, incomplete or complete savepoints. Otherwise, it has
    * the general effect of resetting the rowId to an INCOMPLETE state.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param columnDefns
    * @param cvValues
    * @param rowId
    * @return single-row table with the content of the saved-as-incomplete row
    */
   public UserTable saveAsIncompleteMostRecentCheckpointRowWithId(String appName,
       OdkDbHandle dbHandleName, String tableId, OrderedColumns orderedColumns,
       ContentValues cvValues, String rowId) throws RemoteException {

      return fetchAndRebuildChunks(dbInterface
          .saveAsIncompleteMostRecentCheckpointRowWithId(appName, dbHandleName, tableId,
              orderedColumns, cvValues, rowId), UserTable.CREATOR);
   }

   /**
    * Update all rows for the given rowId to SavepointType 'INCOMPLETE' and
    * remove all but the most recent row. When used with a rowId that has
    * checkpoints, this updates to the most recent checkpoint and removes any
    * earlier checkpoints, incomplete or complete savepoints. Otherwise, it has
    * the general effect of resetting the rowId to an INCOMPLETE state.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param columnDefns
    * @param cvValues
    * @param rowId
    * @return single-row table with the content of the saved-as-incomplete row
    */
   public UserTable saveAsCompleteMostRecentCheckpointRowWithId(String appName,
       OdkDbHandle dbHandleName, String tableId, OrderedColumns orderedColumns,
       ContentValues cvValues, String rowId) throws RemoteException {

      return fetchAndRebuildChunks(dbInterface
          .saveAsCompleteMostRecentCheckpointRowWithId(appName, dbHandleName, tableId,
              orderedColumns, cvValues, rowId), UserTable.CREATOR);
   }

   /**
    * Update the given rowId with the values in the cvValues. If certain metadata
    * values are not specified in the cvValues, then suitable default values may
    * be supplied for them. Furthermore, if the cvValues do not specify certain
    * metadata fields, then an exception may be thrown if there are more than one
    * row matching this rowId.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param orderedColumns
    * @param cvValues
    * @param rowId
    * @return single-row table with the content of the saved-as-incomplete row
    */
   public UserTable updateRowWithId(String appName, OdkDbHandle dbHandleName, String tableId,
       OrderedColumns orderedColumns, ContentValues cvValues, String rowId) throws RemoteException {

      return fetchAndRebuildChunks(dbInterface
              .updateRowWithId(appName, dbHandleName, tableId, orderedColumns, cvValues, rowId),
          UserTable.CREATOR);
   }

   /**
    * Delete the local and server conflict records to resolve a server conflict
    * <p/>
    * A combination of primitive actions, all performed in one transaction:
    * <p/>
    * // delete the record of the server row
    * deleteServerConflictRowWithId(appName, dbHandleName, tableId, rowId);
    * <p/>
    * // move the local record into the 'new_row' sync state
    * // so it can be physically deleted.
    * updateRowETagAndSyncState(appName, dbHandleName, tableId, rowId, null,
    * SyncState.new_row.name());
    * // move the local conflict back into the normal (null) state
    * deleteRowWithId(appName, dbHandleName, tableId, rowId);
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param rowId
    */
   public void resolveServerConflictWithDeleteRowWithId(String appName, OdkDbHandle dbHandleName,
       String tableId, String rowId) throws RemoteException {

      dbInterface.resolveServerConflictWithDeleteRowWithId(appName, dbHandleName, tableId, rowId);
   }

   /**
    * Resolve the server conflict by taking the local changes.
    * If the local changes are to delete this record, the record will be deleted
    * upon the next successful sync.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param rowId
    */
   public void resolveServerConflictTakeLocalRowWithId(String appName, OdkDbHandle dbHandleName,
       String tableId, String rowId) throws RemoteException {

      dbInterface.resolveServerConflictTakeLocalRowWithId(appName, dbHandleName, tableId, rowId);
   }

   /**
    * Resolve the server conflict by taking the local changes plus a value map
    * of select server field values.  This map should not update any metadata
    * fields -- it should just contain user data fields.
    * <p/>
    * It is an error to call this if the local change is to delete the row.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param cvValues
    * @param rowId
    */
   public void resolveServerConflictTakeLocalRowPlusServerDeltasWithId(String appName,
       OdkDbHandle dbHandleName, String tableId, ContentValues cvValues, String rowId)
       throws RemoteException {

      dbInterface
          .resolveServerConflictTakeLocalRowPlusServerDeltasWithId(appName, dbHandleName, tableId,
              cvValues, rowId);
   }

   /**
    * Resolve the server conflict by taking the server changes.  This may delete the local row.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param rowId
    */
   public void resolveServerConflictTakeServerRowWithId(String appName, OdkDbHandle dbHandleName,
       String tableId, String rowId) throws RemoteException {

      dbInterface.resolveServerConflictTakeServerRowWithId(appName, dbHandleName, tableId, rowId);
   }

   /**
    * Forget the document ETag values for the given tableId on all servers.
    * Used when deleting a table. Exposed mainly for integration testing.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    */
   public void deleteAllSyncETagsForTableId(String appName, OdkDbHandle dbHandleName,
       String tableId) throws RemoteException {

      dbInterface.deleteAllSyncETagsForTableId(appName, dbHandleName, tableId);
   }

   /**
    * Forget the document ETag values for everything except the specified Uri.
    * Call this when the server URI we are syncing against has changed.
    *
    * @param appName
    * @param dbHandleName
    * @param verifiedUri  (e.g., https://opendatakit-tablesdemo.appspot.com)
    */
   public void deleteAllSyncETagsExceptForServer(String appName, OdkDbHandle dbHandleName,
       String verifiedUri) throws RemoteException {

      dbInterface.deleteAllSyncETagsExceptForServer(appName, dbHandleName, verifiedUri);
   }

   /**
    * Forget the document ETag values for everything under the specified Uri.
    *
    * @param appName
    * @param dbHandleName
    * @param verifiedUri  (e.g., https://opendatakit-tablesdemo.appspot.com)
    */
   public void deleteAllSyncETagsUnderServer(String appName, OdkDbHandle dbHandleName,
       String verifiedUri) throws RemoteException {

      dbInterface.deleteAllSyncETagsUnderServer(appName, dbHandleName, verifiedUri);
   }

   /**
    * Get the document ETag values for the given file under the specified Uri.
    * The assumption is that the file system will update the modification timestamp
    * if the file has changed. This eliminates the need for computing an md5
    * hash on files that haven't changed. We can just retrieve that from the database.
    *
    * @param appName
    * @param dbHandleName
    * @param verifiedUri           (e.g., https://opendatakit-tablesdemo.appspot.com)
    * @param tableId               (null if an application-level file)
    * @param modificationTimestamp timestamp of last file modification
    */
   public String getFileSyncETag(String appName, OdkDbHandle dbHandleName, String verifiedUri,
       String tableId, long modificationTimestamp) throws RemoteException {

      return dbInterface
          .getFileSyncETag(appName, dbHandleName, verifiedUri, tableId, modificationTimestamp);
   }

   /**
    * Get the document ETag values for the given manifest under the specified Uri.
    *
    * @param appName
    * @param dbHandleName
    * @param verifiedUri  (e.g., https://opendatakit-tablesdemo.appspot.com)
    * @param tableId      (null if an application-level manifest)
    */
   public String getManifestSyncETag(String appName, OdkDbHandle dbHandleName, String verifiedUri,
       String tableId) throws RemoteException {

      return dbInterface.getManifestSyncETag(appName, dbHandleName, verifiedUri, tableId);
   }

   /**
    * Update the document ETag values for the given file under the specified Uri.
    * The assumption is that the file system will update the modification timestamp
    * if the file has changed. This eliminates the need for computing an md5
    * hash on files that haven't changed. We can just retrieve that from the database.
    *
    * @param appName
    * @param dbHandleName
    * @param verifiedUri           (e.g., https://opendatakit-tablesdemo.appspot.com)
    * @param tableId               (null if an application-level file)
    * @param modificationTimestamp timestamp of last file modification
    * @param eTag
    */
   public void updateFileSyncETag(String appName, OdkDbHandle dbHandleName, String verifiedUri,
       String tableId, long modificationTimestamp, String eTag) throws RemoteException {

      dbInterface
          .updateFileSyncETag(appName, dbHandleName, verifiedUri, tableId, modificationTimestamp,
              eTag);
   }

   /**
    * Update the document ETag values for the given manifest under the specified Uri.
    *
    * @param appName
    * @param dbHandleName
    * @param verifiedUri (e.g., https://opendatakit-tablesdemo.appspot.com)
    * @param tableId  (null if an application-level manifest)
    * @param eTag
    */
   public void updateManifestSyncETag(String appName, OdkDbHandle dbHandleName,
       String verifiedUri, String tableId, String eTag) throws RemoteException {

      dbInterface.updateManifestSyncETag(appName, dbHandleName, verifiedUri, tableId, eTag);
   }

   /**
    * Retrieve all the pieces of the data across the wire and rebuild them into their original type
    *
    * @param firstChunk The first chunk, which contains a pointer to the next
    * @param creator The parcelable reconstructor
    * @param <T> The type to reconstruct into
    * @return The original object
    * @throws RemoteException
    */
   private <T> T fetchAndRebuildChunks(OdkDbChunk firstChunk, Parcelable.Creator<T> creator)
       throws RemoteException {

      List<OdkDbChunk> aggregatedChunks = retrieveChunks(firstChunk);

      return OdkDbChunkUtil.rebuildFromChunks(aggregatedChunks, creator);
   }

   /**
    * Retrieve all the pieces of the data across the wire and rebuild them into their original type
    *
    * @param firstChunk The first chunk, which contains a pointer to the next
    * @param serializable
    * @param <T> The type to reconstruct into
    * @return  The original object
    * @throws RemoteException
    */
   private <T> T fetchAndRebuildChunks(OdkDbChunk firstChunk, Class<T> serializable)
       throws RemoteException {

      List<OdkDbChunk> aggregatedChunks = retrieveChunks(firstChunk);

      try {
         return OdkDbChunkUtil.rebuildFromChunks(aggregatedChunks, serializable);
      } catch (Exception e) {
         Log.e(TAG, "Failed to rebuild serialized object from chunks");
         return null;
      }
   }

   /**
    * Iterate through the chunks like a linked list, retrieving them one by one from the service
    *
    * @param firstChunk
    * @return
    * @throws RemoteException
    */
   private List<OdkDbChunk> retrieveChunks(OdkDbChunk firstChunk) throws RemoteException {
      List<OdkDbChunk> aggregatedChunks = new LinkedList<>();
      aggregatedChunks.add(firstChunk);

      OdkDbChunk currChunk = firstChunk;
      while (currChunk.hasNextID()) {
         ParcelUuid parcelUuid = new ParcelUuid(currChunk.getNextID());
         currChunk = dbInterface.getChunk(parcelUuid);
         aggregatedChunks.add(currChunk);
      }

      return aggregatedChunks;
   }
}

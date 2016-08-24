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
import android.database.sqlite.SQLiteException;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;
import org.opendatakit.common.android.data.ColumnList;
import org.opendatakit.common.android.data.OrderedColumns;
import org.opendatakit.common.android.data.TableDefinitionEntry;
import org.opendatakit.common.android.data.UserTable;
import org.opendatakit.common.android.exception.ActionNotAuthorizedException;
import org.opendatakit.common.android.exception.ServicesAvailabilityException;
import org.opendatakit.common.android.provider.DataTableColumns;
import org.opendatakit.database.service.BindArgs;
import org.opendatakit.database.service.KeyValueStoreEntry;
import org.opendatakit.database.service.OdkDbChunk;
import org.opendatakit.database.service.OdkDbHandle;
import org.opendatakit.database.service.OdkDbTable;
import org.opendatakit.database.service.OdkDbInterface;
import org.opendatakit.database.service.TableHealthInfo;
import org.opendatakit.database.service.queries.OdkDbAbstractQuery;
import org.opendatakit.database.service.queries.OdkDbResumableQuery;
import org.opendatakit.database.service.queries.OdkDbSimpleQuery;
import org.opendatakit.database.service.queries.QueryBounds;
import org.opendatakit.database.utilities.OdkDbChunkUtil;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

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

   private void rethrowNotAuthorizedRemoteException(Exception e)
       throws IllegalArgumentException, IllegalStateException, SQLiteException,
       ActionNotAuthorizedException, ServicesAvailabilityException {
      if ( (e instanceof IllegalStateException) || (e instanceof RemoteException) ) {
         String prefix = "via RemoteException on OdkDbInterface: ";
         String msg = e.getMessage();
         int idx = msg.indexOf(':');
         if (idx == -1) {
            throw new ServicesAvailabilityException(prefix + msg);
         }
         String exceptionName = msg.substring(0, idx);
         String message = msg.substring(idx + 2);
         if (!exceptionName.startsWith("org.opendatakit|")) {
            throw new ServicesAvailabilityException(prefix + msg);
         }
         exceptionName = exceptionName.substring(exceptionName.indexOf('|') + 1);
         if (exceptionName.equals(ActionNotAuthorizedException.class.getName())) {
            throw new ActionNotAuthorizedException(prefix + message);
         }
         if (exceptionName.equals(IllegalArgumentException.class.getName())) {
            throw new IllegalArgumentException(prefix + message);
         }
         if (exceptionName.equals(SQLiteException.class.getName())) {
            throw new SQLiteException(prefix + message);
         }
         // should only throw illegal argument exceptions.
         // anything else is not properly detected in the server layer
         throw new IllegalStateException(prefix + msg);
      } else {
         throw new IllegalStateException(
             "not IllegalStateException or RemoteException on OdkDbInterface: " +
             e.getClass().getName() + ": " + e.toString());
      }
   }


   private void rethrowAlwaysAllowedRemoteException(Exception e)
       throws IllegalArgumentException, IllegalStateException, SQLiteException,
       ServicesAvailabilityException {
      if ( (e instanceof IllegalStateException) || (e instanceof RemoteException) ) {
         String prefix = "via RemoteException on OdkDbInterface: ";
         String msg = e.getMessage();
         int idx = msg.indexOf(':');
         if (idx == -1) {
            throw new ServicesAvailabilityException(prefix + msg);
         }
         String exceptionName = msg.substring(0, idx);
         String message = msg.substring(idx + 2);
         if (!exceptionName.startsWith("org.opendatakit|")) {
            throw new ServicesAvailabilityException(prefix + msg);
         }
         exceptionName = exceptionName.substring(exceptionName.indexOf('|') + 1);
         if (exceptionName.equals(IllegalArgumentException.class.getName())) {
            throw new IllegalArgumentException(prefix + message);
         }
         if (exceptionName.equals(SQLiteException.class.getName())) {
            throw new SQLiteException(prefix + message);
         }
         // should only throw illegal argument exceptions.
         // anything else is not properly detected in the server layer
         throw new IllegalStateException(prefix + msg);
      } else {
         throw new IllegalStateException(
             "not IllegalStateException or RemoteException on OdkDbInterface: " +
             e.getClass().getName() + ": " + e.toString());
      }
   }

   /**
    * Return the roles of a verified username or google account.
    * If the username or google account have not been verified,
    * or if the server settings specify to use an anonymous user,
    * then return an empty string.
    *
    * @param appName
    *
    * @return empty string or JSON serialization of an array of ROLES. See RoleConsts for possible values.
    */
   public String getRolesList(String appName) throws ServicesAvailabilityException {
      try {
         return dbInterface.getRolesList(appName);
      } catch ( Exception e ) {
         rethrowAlwaysAllowedRemoteException(e);
        throw new IllegalStateException("unreachable - keep IDE happy");
      }
   }

   /**
    * Return the users configured on the server if the current
    * user is verified to have Tables Super-user, Administer Tables or
    * Site Administrator roles. Otherwise, returns information about
    * the current user. If the user is syncing anonymously with the
    * server, this returns an empty string.
    *
    * @param appName
    *
    * @return empty string or JSON serialization of an array of objects
    * structured as { "user_id": "...", "full_name": "...", "roles": ["...",...] }
    */
   public String getUsersList(String appName) throws ServicesAvailabilityException {
      try {
         return dbInterface.getUsersList(appName);
      } catch ( Exception e ) {
        rethrowAlwaysAllowedRemoteException(e);
        throw new IllegalStateException("unreachable - keep IDE happy");
      }
   }

   /**
    * Obtain a databaseHandleName
    *
    * @param appName
    * @return dbHandleName
    */
   public OdkDbHandle openDatabase(String appName) throws ServicesAvailabilityException {
      try {
         return dbInterface.openDatabase(appName);
      } catch ( Exception e ) {
         rethrowAlwaysAllowedRemoteException(e);
        throw new IllegalStateException("unreachable - keep IDE happy");
      }
   }

   /**
    * Release the databaseHandle. Will roll back any outstanding transactions
    * and release/close the database handle.
    *
    * @param appName
    * @param dbHandleName
    */
   public void closeDatabase(String appName, OdkDbHandle dbHandleName) throws ServicesAvailabilityException {
     try {
       dbInterface.closeDatabase(appName, dbHandleName);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * Create a local only table and prepend the given id with an "L_"
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param columns
    * @return
    * @throws ServicesAvailabilityException
    */
   public OrderedColumns createLocalOnlyDbTableWithColumns(String appName, OdkDbHandle dbHandleName,
       String tableId, ColumnList columns) throws ServicesAvailabilityException {

     if (!tableId.startsWith("L_")) {
       tableId = "L_" + tableId;
     }

     try {
       return fetchAndRebuildChunks(
          dbInterface.createLocalOnlyDbTableWithColumns(appName, dbHandleName, tableId, columns),
          OrderedColumns.CREATOR);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * Drop the given local only table
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @throws ServicesAvailabilityException
    */
   public void deleteLocalOnlyDBTable(String appName, OdkDbHandle dbHandleName, String tableId) throws ServicesAvailabilityException {

     if (!tableId.startsWith("L_")) {
       tableId = "L_" + tableId;
     }

     try {
       dbInterface.deleteLocalOnlyDBTable(appName, dbHandleName, tableId);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * Insert a row into a local only table
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param rowValues
    * @throws ServicesAvailabilityException
    */
   public void insertLocalOnlyRow(String appName, OdkDbHandle dbHandleName, String tableId,
       ContentValues rowValues) throws ServicesAvailabilityException {

     if (!tableId.startsWith("L_")) {
       tableId = "L_" + tableId;
     }

	  try {
       dbInterface.insertLocalOnlyRow(appName, dbHandleName, tableId, rowValues);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * Update a row in a local only table
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param rowValues
    * @param whereClause
    * @param whereArgs
    * @throws ServicesAvailabilityException
    */
   public void updateLocalOnlyRow(String appName, OdkDbHandle dbHandleName, String tableId,
       ContentValues rowValues, String whereClause, String[] whereArgs) throws ServicesAvailabilityException {
 
     if (!tableId.startsWith("L_")) {
       tableId = "L_" + tableId;
     }
 
     try {
       dbInterface
           .updateLocalOnlyRow(appName, dbHandleName, tableId, rowValues, whereClause, whereArgs);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

     /**
      * Delete a row in a local only table
      *
      * @param appName
      * @param dbHandleName
      * @param tableId
      * @param whereClause
      * @param whereArgs
      * @throws ServicesAvailabilityException
      */
   public void deleteLocalOnlyRow(String appName, OdkDbHandle dbHandleName, String tableId,
       String whereClause, String[] whereArgs) throws ServicesAvailabilityException {
 
     if (!tableId.startsWith("L_")) {
       tableId = "L_" + tableId;
     }
 
     try {
       dbInterface.deleteLocalOnlyRow(appName, dbHandleName, tableId, whereClause, whereArgs);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * Get a {@link OdkDbTable} for this local only table based on the given SQL command. All
    * columns from the table are returned.
    *
    * If any of the clause parts are omitted (null), then the appropriate
    * simplified SQL statement is constructed.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param whereClause       the whereClause for the selection, beginning with "WHERE". Must
    *                          include "?" instead of actual values, which are instead passed in
    *                          the selectionArgs.
    * @param bindArgs          an array of primitive values (String, Boolean, int, double) for
    *                          bind parameters
    * @param groupBy           an array of elementKeys
    * @param having
    * @param orderByColNames   array of columns to order the results by
    * @param orderByDirections either "ASC" or "DESC", corresponding to each column name
    * @param limit             the maximum number of rows to return
    * @param offset            the index to start counting the limit from
    * @return A {@link UserTable} containing the results of the query
    * @throws ServicesAvailabilityException
    */
   public OdkDbTable rawSqlQueryLocalOnlyTables(String appName, OdkDbHandle dbHandleName,
       String tableId, String whereClause, Object[] bindArgs, String[] groupBy, String having,
       String[] orderByColNames, String[] orderByDirections, Integer limit, Integer offset)
       throws ServicesAvailabilityException {

      if (!tableId.startsWith("L_")) {
         tableId = "L_" + tableId;
      }

      return rawSqlQuery(appName, dbHandleName, tableId, whereClause, bindArgs, groupBy, having,
          orderByColNames, orderByDirections, limit, offset);
   }

   /**
    * Get a {@link OdkDbTable} for the result set of an arbitrary sql query
    * and bind parameters on this local only table.
    *
    * The sql query can be arbitrarily complex and can include joins, unions, etc.
    * The data are returned as string values.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param sqlCommand
    * @param bindArgs          an array of primitive values (String, Boolean, int, double) for
    *                          bind parameters
    * @param orderByColNames   array of columns to order the results by (optional)
    * @param orderByDirections  either "ASC" or "DESC" (optional)
    * @param limit             the maximum number of rows to return (optional)
    * @param offset            the index to start counting the limit from (optional)
    * @return An  {@link OdkDbTable}. Containing the results of the query
    * @throws ServicesAvailabilityException
    */
   public OdkDbTable arbitrarySqlQueryLocalOnlyTables(String appName, OdkDbHandle dbHandleName,
       String tableId, String sqlCommand, Object[] bindArgs, String[] orderByColNames,
       String[] orderByDirections, Integer limit, Integer offset) throws ServicesAvailabilityException {

      if (!tableId.startsWith("L_")) {
         tableId = "L_" + tableId;
      }

      return arbitrarySqlQuery(appName, dbHandleName, tableId, sqlCommand, bindArgs,
          orderByColNames, orderByDirections, limit, offset);
   }

   /**
    * Get a {@link OdkDbTable} that holds the results from the continued query. Note that this is
    * just a wrapper of resumeRawSqlQuery, which could successfully be used instead.
    *
    * @param appName
    * @param dbHandleName
    * @param query The original query with the bounds adjusted
    * @return
    * @throws ServicesAvailabilityException
    */
   public OdkDbTable resumeRawSqlQueryLocalOnlyTables(String appName, OdkDbHandle dbHandleName,
       OdkDbResumableQuery query) throws ServicesAvailabilityException {

      return resumeRawSqlQuery(appName, dbHandleName, query);
   }

   /**
    * SYNC Only. ADMIN Privileges
    *
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
   public void privilegedServerTableSchemaETagChanged(String appName, OdkDbHandle dbHandleName,
       String tableId, String schemaETag, String tableInstanceFilesUri) throws ServicesAvailabilityException {
     try {
       dbInterface.privilegedServerTableSchemaETagChanged(appName, dbHandleName, tableId,
           schemaETag,
          tableInstanceFilesUri);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
   public String setChoiceList(String appName, OdkDbHandle dbHandleName, String choiceListJSON) throws ServicesAvailabilityException {
     try {
       return dbInterface.setChoiceList(appName, dbHandleName, choiceListJSON);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * Return the choice list JSON corresponding to the choiceListId
    *
    * @param appName
    * @param dbHandleName
    * @param choiceListId -- the md5 hash of the choiceListJSON
    * @return choiceListJSON -- the actual JSON choice list text.
    */
   public String getChoiceList(String appName, OdkDbHandle dbHandleName, String choiceListId) throws ServicesAvailabilityException {
     try {
       return dbInterface.getChoiceList(appName, dbHandleName, choiceListId);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
       String tableId, ColumnList columns) throws ServicesAvailabilityException {
     try {
      return fetchAndRebuildChunks(
          dbInterface.createOrOpenDBTableWithColumns(appName, dbHandleName, tableId, columns),
          OrderedColumns.CREATOR);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
       List<KeyValueStoreEntry> metaData, boolean clear) throws ServicesAvailabilityException {
      try {
        return fetchAndRebuildChunks(dbInterface
          .createOrOpenDBTableWithColumnsAndProperties(appName, dbHandleName, tableId, columns,
              metaData, clear), OrderedColumns.CREATOR);
      } catch ( Exception e ) {
        rethrowAlwaysAllowedRemoteException(e);
        throw new IllegalStateException("unreachable - keep IDE happy");
      }
   }

   /**
    * Drop the given tableId and remove all the files (both configuration and
    * data attachments) associated with that table.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    */
   public void deleteDBTableAndAllData(String appName, OdkDbHandle dbHandleName, String tableId) throws ServicesAvailabilityException {
     try {
        dbInterface.deleteDBTableAndAllData(appName, dbHandleName, tableId);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
       String partition, String aspect, String key) throws ServicesAvailabilityException {
     try {
       dbInterface.deleteDBTableMetadata(appName, dbHandleName, tableId, partition, aspect, key);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * Return an array of the admin columns that must be present in
    * every database table.
    *
    * @return
    */
   public String[] getAdminColumns() throws ServicesAvailabilityException {
     try {
       return fetchAndRebuildChunks(dbInterface.getAdminColumns(), String[].class);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
   public String[] getAllColumnNames(String appName, OdkDbHandle dbHandleName, String tableId) throws ServicesAvailabilityException {
     try {
       return fetchAndRebuildChunks(dbInterface.getAllColumnNames(appName, dbHandleName, tableId),
          String[].class);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * Return all the tableIds in the database.
    *
    * @param appName
    * @param dbHandleName
    * @return List<String> of tableIds
    */
   @SuppressWarnings("unchecked")
   public List<String> getAllTableIds(String appName, OdkDbHandle dbHandleName) throws ServicesAvailabilityException {
     try {
       Serializable result = fetchAndRebuildChunks(dbInterface.getAllTableIds(appName,
           dbHandleName),
          Serializable.class);
       return (List<String>) result;
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param partition
    * @param aspect
    * @param key
    * @return list of KeyValueStoreEntry values matching the filter criteria
    */
   @SuppressWarnings("unchecked")
   public List<KeyValueStoreEntry> getDBTableMetadata(String appName, OdkDbHandle dbHandleName,
       String tableId, String partition, String aspect, String key) throws ServicesAvailabilityException {
     try {
       Serializable result = fetchAndRebuildChunks(
          dbInterface.getDBTableMetadata(appName, dbHandleName, tableId, partition, aspect, key),
          Serializable.class);
       return (List<KeyValueStoreEntry>) result;
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }

   }

   /**
    * Return an array of the admin columns that should be exported to
    * a CSV file. This list excludes the SYNC_STATE and CONFLICT_TYPE columns.
    *
    * @return
    */
   public String[] getExportColumns() throws ServicesAvailabilityException {
     try {
       return fetchAndRebuildChunks(dbInterface.getExportColumns(), String[].class);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
       String tableId) throws ServicesAvailabilityException {
     try {
       return fetchAndRebuildChunks(
           dbInterface.getTableDefinitionEntry(appName, dbHandleName, tableId), TableDefinitionEntry.CREATOR);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * Return the list of all tables and their health status.
    *
    * @param appName
    * @param dbHandleName
    * @return the list of TableHealthInfo records for this appName
    */
   @SuppressWarnings("unchecked")
   public List<TableHealthInfo> getTableHealthStatuses(String appName, OdkDbHandle dbHandleName) throws ServicesAvailabilityException {
     try {
       Serializable results = fetchAndRebuildChunks(
          dbInterface.getTableHealthStatuses(appName, dbHandleName), Serializable.class);

       return (List<TableHealthInfo>) results;
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
       String tableId) throws ServicesAvailabilityException {
     try {
       return fetchAndRebuildChunks(dbInterface.getUserDefinedColumns(appName, dbHandleName, tableId),
           OrderedColumns.CREATOR);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * Verifies that the tableId exists in the database.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @return true if table is listed in table definitions.
    */
   public boolean hasTableId(String appName, OdkDbHandle dbHandleName, String tableId) throws ServicesAvailabilityException {
     try {
      return dbInterface.hasTableId(appName, dbHandleName, tableId);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /********** RAW GENERIC QUERIES **********/

   /**
    * Get a {@link OdkDbTable} for this table based on the given SQL command. All
    * columns from the table are returned.
    *
    * If any of the clause parts are omitted (null), then the appropriate
    * simplified SQL statement is constructed.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param whereClause       the whereClause for the selection, beginning with "WHERE". Must
    *                          include "?" instead of actual values, which are instead passed in
    *                          the selectionArgs.
    * @param bindArgs          an array of primitive values (String, Boolean, int, double) for
    *                          bind parameters
    * @param groupBy           an array of elementKeys
    * @param having
    * @param orderByColNames   array of columns to order the results by
    * @param orderByDirections either "ASC" or "DESC", corresponding to each column name
    * @param limit             the maximum number of rows to return
    * @param offset            the index to start counting the limit from
    * @return A {@link UserTable} containing the results of the query
    */
   public OdkDbTable rawSqlQuery(String appName, OdkDbHandle dbHandleName, String tableId,
       String whereClause, Object[] bindArgs, String[] groupBy, String having,
       String[] orderByColNames, String[] orderByDirections, Integer limit, Integer offset) throws ServicesAvailabilityException {

     try {
       OdkDbSimpleQuery query = new OdkDbSimpleQuery(tableId, new BindArgs(bindArgs), whereClause,
          groupBy, having, orderByColNames, orderByDirections, limit, offset);

       String paginatedSqlQuery = query.getPaginatedSqlCommand();
       BindArgs args = query.getSqlBindArgs();
       QueryBounds bounds = query.getSqlQueryBounds();

       OdkDbTable baseTable = fetchAndRebuildChunks(dbInterface
          .rawSqlQuery(appName, dbHandleName, query.getPaginatedSqlCommand(),
              query.getSqlBindArgs(), query.getSqlQueryBounds()), OdkDbTable.CREATOR);

       baseTable.setQuery(query);

       return baseTable;
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

  /**
   * SYNC ONLY
   *
   * Privileged version of above query.
   *
   * Get a {@link OdkDbTable} for this table based on the given SQL command. All
   * columns from the table are returned.
   *
   * If any of the clause parts are omitted (null), then the appropriate
   * simplified SQL statement is constructed.
   *
   * @param appName
   * @param dbHandleName
   * @param tableId
   * @param whereClause       the whereClause for the selection, beginning with "WHERE". Must
   *                          include "?" instead of actual values, which are instead passed in
   *                          the selectionArgs.
   * @param bindArgs          an array of primitive values (String, Boolean, int, double) for
   *                          bind parameters
   * @param groupBy           an array of elementKeys
   * @param having
   * @param orderByColNames   array of columns to order the results by
   * @param orderByDirections either "ASC" or "DESC", corresponding to each column name
   * @param limit             the maximum number of rows to return
   * @param offset            the index to start counting the limit from
   * @return A {@link UserTable} containing the results of the query
   */
  public OdkDbTable privilegedRawSqlQuery(String appName, OdkDbHandle dbHandleName, String tableId,
      String whereClause, Object[] bindArgs, String[] groupBy, String having,
      String[] orderByColNames, String[] orderByDirections, Integer limit, Integer offset) throws ServicesAvailabilityException {

    try {
      OdkDbSimpleQuery query = new OdkDbSimpleQuery(tableId, new BindArgs(bindArgs), whereClause,
          groupBy, having, orderByColNames, orderByDirections, limit, offset);

      String paginatedSqlQuery = query.getPaginatedSqlCommand();
      BindArgs args = query.getSqlBindArgs();
      QueryBounds bounds = query.getSqlQueryBounds();

      OdkDbTable baseTable = fetchAndRebuildChunks(dbInterface
          .privilegedRawSqlQuery(appName, dbHandleName, query.getPaginatedSqlCommand(),
              query.getSqlBindArgs(), query.getSqlQueryBounds()), OdkDbTable.CREATOR);

      baseTable.setQuery(query);

      return baseTable;
    } catch ( Exception e ) {
      rethrowAlwaysAllowedRemoteException(e);
      throw new IllegalStateException("unreachable - keep IDE happy");
    }
  }

   /**
    * Get a {@link OdkDbTable} for the result set of an arbitrary sql query
    * and bind parameters.
    *
    * The sql query can be arbitrarily complex and can include joins, unions, etc.
    * The data are returned as string values.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param sqlCommand
    * @param bindArgs          an array of primitive values (String, Boolean, int, double) for
    *                          bind parameters
    * @param orderByColNames   array of columns to order the results by (optional)
    * @param orderByDirections  either "ASC" or "DESC" (optional)
    * @param limit             the maximum number of rows to return (optional)
    * @param offset            the index to start counting the limit from (optional)
    * @return An  {@link OdkDbTable}. Containing the results of the query
    */
   public OdkDbTable arbitrarySqlQuery(String appName, OdkDbHandle dbHandleName, String tableId,
       String sqlCommand, Object[] bindArgs, String[] orderByColNames,
       String[] orderByDirections, Integer limit, Integer offset) throws ServicesAvailabilityException {
     try {
       OdkDbAbstractQuery query = new OdkDbAbstractQuery(tableId, new BindArgs(bindArgs),
          sqlCommand, orderByColNames, orderByDirections, limit, offset);

       OdkDbTable baseTable = fetchAndRebuildChunks(dbInterface.rawSqlQuery(appName, dbHandleName,
          query.getPaginatedSqlCommand(), query.getSqlBindArgs(), query.getSqlQueryBounds()),
          OdkDbTable.CREATOR);

       baseTable.setQuery(query);

       return baseTable;
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * Get a {@link OdkDbTable} that holds the results from the continued query.
    *
    * @param appName
    * @param dbHandleName
    * @param query The original query with the bounds adjusted
    * @return
    */
   public OdkDbTable resumeRawSqlQuery(String appName, OdkDbHandle dbHandleName,
       OdkDbResumableQuery query) throws ServicesAvailabilityException {
     try {
       OdkDbTable baseTable = fetchAndRebuildChunks(dbInterface.rawSqlQuery(appName, dbHandleName,
          query.getPaginatedSqlCommand(), query.getSqlBindArgs(), query.getSqlQueryBounds()),
          OdkDbTable.CREATOR);

       baseTable.setQuery(query);

       return baseTable;
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

  /**
   * SYNC ONLY
   *
   * Privileged version of above query.
   * Get a {@link OdkDbTable} that holds the results from the continued query.
   *
   * @param appName
   * @param dbHandleName
   * @param query The original query with the bounds adjusted
   * @return
   */
  public OdkDbTable resumePrivilegedRawSqlQuery(String appName, OdkDbHandle dbHandleName,
      OdkDbResumableQuery query) throws ServicesAvailabilityException {
    try {
      OdkDbTable baseTable = fetchAndRebuildChunks(dbInterface.privilegedRawSqlQuery(appName,
          dbHandleName,
          query.getPaginatedSqlCommand(), query.getSqlBindArgs(), query.getSqlQueryBounds()),
          OdkDbTable.CREATOR);

      baseTable.setQuery(query);

      return baseTable;
    } catch ( Exception e ) {
      rethrowAlwaysAllowedRemoteException(e);
      throw new IllegalStateException("unreachable - keep IDE happy");
    }
  }

   /********** USERTABLE QUERY WRAPPERS **********/

   /**
    * Get a {@link UserTable} for this table based on the given SQL command. All
    * columns from the table are returned.
    *
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
    * @param bindArgs          an array of primitive values (String, Boolean, int, double) for
    *                          bind parameters
    * @param groupBy           an array of elementKeys
    * @param having
    * @param orderByColNames   array of columns to order the results by
    * @param orderByDirections either "ASC" or "DESC", corresponding to each column name
    * @param limit             the maximum number of rows to return
    * @param offset            the index to start counting the limit from
    * @return A {@link UserTable} containing the results of the query
    */
   public UserTable rawSqlQuery(String appName, OdkDbHandle dbHandleName, String tableId,
       OrderedColumns columnDefns, String whereClause, Object[] bindArgs, String[] groupBy,
       String having, String[] orderByColNames, String[] orderByDirections, Integer limit,
       Integer offset) throws ServicesAvailabilityException {
     try {
       OdkDbTable baseTable = rawSqlQuery(appName, dbHandleName, tableId, whereClause, bindArgs,
           groupBy, having, orderByColNames, orderByDirections, limit, offset);

       return new UserTable(baseTable, columnDefns, getAdminColumns());
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

  /**
   * SYNC ONLY
   *
   * Privileged version of the above query.
   * Get a {@link UserTable} for this table based on the given SQL command. All
   * columns from the table are returned.
   *
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
   * @param bindArgs          an array of primitive values (String, Boolean, int, double) for
   *                          bind parameters
   * @param groupBy           an array of elementKeys
   * @param having
   * @param orderByColNames   array of columns to order the results by
   * @param orderByDirections either "ASC" or "DESC", corresponding to each column name
   * @param limit             the maximum number of rows to return
   * @param offset            the index to start counting the limit from
   * @return A {@link UserTable} containing the results of the query
   */
  public UserTable privilegedRawSqlQuery(String appName, OdkDbHandle dbHandleName, String tableId,
      OrderedColumns columnDefns, String whereClause, Object[] bindArgs, String[] groupBy,
      String having, String[] orderByColNames, String[] orderByDirections, Integer limit,
      Integer offset) throws ServicesAvailabilityException {
    try {
      OdkDbTable baseTable = privilegedRawSqlQuery(appName, dbHandleName, tableId, whereClause,
          bindArgs,
          groupBy, having, orderByColNames, orderByDirections, limit, offset);

      return new UserTable(baseTable, columnDefns, getAdminColumns());
    } catch ( Exception e ) {
      rethrowAlwaysAllowedRemoteException(e);
      throw new IllegalStateException("unreachable - keep IDE happy");
    }
  }

   /**
    * Get a {@link UserTable} for the result set of an arbitrary sql query
    * and bind parameters.
    *
    * The sql query can be arbitrarily complex and can include joins, unions, etc.
    * The data are returned as string values.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param sqlCommand
    * @param bindArgs          an array of primitive values (String, Boolean, int, double) for
    *                          bind parameters
    * @param orderByColNames   array of columns to order the results by
    * @param orderByDirections  either "ASC" or "DESC"
    * @param limit             the maximum number of rows to return
    * @param offset            the index to start counting the limit from
    * @return An  {@link OdkDbTable}. Containing the results of the query
    */
   public UserTable arbitrarySqlQuery(String appName, OdkDbHandle dbHandleName, String tableId,
       OrderedColumns columnDefns,
       String sqlCommand, Object[] bindArgs, String[] orderByColNames,
       String[] orderByDirections, Integer limit, Integer offset) throws ServicesAvailabilityException {
     try {
       OdkDbTable baseTable = arbitrarySqlQuery(appName, dbHandleName, tableId, sqlCommand, bindArgs,
           orderByColNames, orderByDirections, limit, offset);

       return new UserTable(baseTable, columnDefns, getAdminColumns());
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }

   }

   /**
    * Get a {@link UserTable} that holds the results from the continued query.
    *
    * @param appName
    * @param dbHandleName
    * @param columnDefns
    * @param query The original query with the bounds adjusted
    * @return
    */
   public UserTable resumeRawSqlQuery(String appName, OdkDbHandle dbHandleName, OrderedColumns
       columnDefns, OdkDbResumableQuery query) throws ServicesAvailabilityException {
     try {
       OdkDbTable baseTable = resumeRawSqlQuery(appName, dbHandleName, query);

       return new UserTable(baseTable, columnDefns, getAdminColumns());
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }

   }


  /**
   * SYNC ONLY
   *
   * Privileged version of the above.
   * Get a {@link UserTable} that holds the results from the continued query.
   *
   * @param appName
   * @param dbHandleName
   * @param columnDefns
   * @param query The original query with the bounds adjusted
   * @return
   */
  public UserTable resumePrivilegedRawSqlQuery(String appName, OdkDbHandle dbHandleName,
      OrderedColumns
      columnDefns, OdkDbResumableQuery query) throws ServicesAvailabilityException {
    try {
      OdkDbTable baseTable = resumePrivilegedRawSqlQuery(appName, dbHandleName, query);

      return new UserTable(baseTable, columnDefns, getAdminColumns());
    } catch ( Exception e ) {
      rethrowAlwaysAllowedRemoteException(e);
      throw new IllegalStateException("unreachable - keep IDE happy");
    }

  }



  /**
    * Insert or update a single table-level metadata KVS entry.
    *
    * @param appName
    * @param dbHandleName
    * @param entry
    */
   public void replaceDBTableMetadata(String appName, OdkDbHandle dbHandleName,
       KeyValueStoreEntry entry) throws ServicesAvailabilityException {
     try {
       dbInterface.replaceDBTableMetadata(appName, dbHandleName, entry);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * Insert or update a list of table-level metadata KVS entries. If clear is
    * true, then delete the existing set of values for this tableId before
    * inserting the new values.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param entries      List<KeyValueStoreEntry>
    * @param clear        if true then delete the existing set of values for this tableId
    *                     before inserting the new ones.
    */
   public void replaceDBTableMetadataList(String appName, OdkDbHandle dbHandleName, String tableId,
       List<KeyValueStoreEntry> entries, boolean clear) throws ServicesAvailabilityException {
     try {
       dbInterface.replaceDBTableMetadataList(appName, dbHandleName, tableId, entries, clear);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
    * @param entries     List<KeyValueStoreEntry>
    */
   public void replaceDBTableMetadataSubList(String appName, OdkDbHandle dbHandleName,
       String tableId, String partition, String aspect, List<KeyValueStoreEntry> entries) throws ServicesAvailabilityException {
     try {
       dbInterface.replaceDBTableMetadataSubList(appName, dbHandleName, tableId, partition, aspect,
           entries);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * SYNC Only. ADMIN Privileges
    *
    * Update the schema and data-modification ETags of a given tableId.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param schemaETag
    * @param lastDataETag
    */
   public void privilegedUpdateDBTableETags(String appName, OdkDbHandle dbHandleName, String
       tableId, String schemaETag, String lastDataETag) throws ServicesAvailabilityException {
     try {
       dbInterface
           .privilegedUpdateDBTableETags(appName, dbHandleName, tableId, schemaETag, lastDataETag);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * SYNC Only. ADMIN Privileges
    *
    * Update the timestamp of the last entirely-successful synchronization
    * attempt of this table.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    */
   public void privilegedUpdateDBTableLastSyncTime(String appName, OdkDbHandle dbHandleName,
       String tableId) throws ServicesAvailabilityException {
     try {
       dbInterface.privilegedUpdateDBTableLastSyncTime(appName, dbHandleName, tableId);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param rowId
    * @return the sync state of the row (use SyncState.valueOf() to reconstruct), or null if the
    * row does not exist.
    */
   public String getSyncState(String appName, OdkDbHandle dbHandleName, String tableId,
       String rowId) throws ServicesAvailabilityException {
     try {
       return dbInterface.getSyncState(appName, dbHandleName, tableId, rowId);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * SYNC Only. ADMIN Privileges!
    *
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
   public void privilegedUpdateRowETagAndSyncState(String appName, OdkDbHandle dbHandleName,
       String tableId, String rowId, String rowETag, String syncState) throws ServicesAvailabilityException {
     try {
       dbInterface
          .privilegedUpdateRowETagAndSyncState(appName, dbHandleName, tableId, rowId, rowETag,
              syncState);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * Return the row with the most recent changes for the given tableId and rowId.
    * If the row has conflicts, it throws an exception. Otherwise, it returns the
    * most recent checkpoint or non-checkpoint value; it will contain a single row.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param orderedColumns
    * @param rowId
    * @return
    */
   public UserTable getRowsWithId(String appName, OdkDbHandle dbHandleName, String tableId,
       OrderedColumns orderedColumns, String rowId) throws ServicesAvailabilityException {
     try {
      OdkDbTable baseTable = fetchAndRebuildChunks(
          dbInterface.getRowsWithId(appName, dbHandleName, tableId, rowId),
          OdkDbTable.CREATOR);

      return new UserTable(baseTable, orderedColumns, getAdminColumns());
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * Return the row with the most recent changes for the given tableId and rowId.
    * If the row has conflicts, it throws an exception. Otherwise, it returns the
    * most recent checkpoint or non-checkpoint value; it will contain a single row.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param orderedColumns
    * @param rowId
    * @return
    */
   public UserTable privilegedGetRowsWithId(String appName, OdkDbHandle dbHandleName, String
       tableId, OrderedColumns orderedColumns, String rowId) throws ServicesAvailabilityException {
      try {
         OdkDbTable baseTable = fetchAndRebuildChunks(
             dbInterface.privilegedGetRowsWithId(appName, dbHandleName, tableId, rowId),
             OdkDbTable.CREATOR);

         return new UserTable(baseTable, orderedColumns, getAdminColumns());
      } catch ( Exception e ) {
         rethrowAlwaysAllowedRemoteException(e);
         throw new IllegalStateException("unreachable - keep IDE happy");
      }
   }

   /**
    * Return the row with the most recent changes for the given tableId and rowId.
    * If the row has conflicts, it throws an exception. Otherwise, it returns the
    * most recent checkpoint or non-checkpoint value; it will contain a single row.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param orderedColumns
    * @param rowId
    * @return
    */
   public UserTable getMostRecentRowWithId(String appName, OdkDbHandle dbHandleName, String tableId,
       OrderedColumns orderedColumns, String rowId)
       throws ActionNotAuthorizedException, ServicesAvailabilityException  {
     try {
      OdkDbTable baseTable = fetchAndRebuildChunks(
          dbInterface.getMostRecentRowWithId(appName, dbHandleName, tableId, rowId),
          OdkDbTable.CREATOR);

      return new UserTable(baseTable, orderedColumns, getAdminColumns());
     } catch ( Exception e ) {
       rethrowNotAuthorizedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * SYNC ONLY
    *
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
    * @param cvValues   the field values on the server
    * @param rowId
    * @param localRowConflictType expected to be one of ConflictType.LOCAL_DELETED_OLD_VALUES (0) or
    *                             ConflictType.LOCAL_UPDATED_UPDATED_VALUES (1)
    */
   public UserTable privilegedPlaceRowIntoConflictWithId(String appName, OdkDbHandle dbHandleName,
       String tableId, OrderedColumns orderedColumns, ContentValues cvValues, String rowId,
       int localRowConflictType) throws ServicesAvailabilityException {
     try {
       OdkDbTable baseTable = fetchAndRebuildChunks(dbInterface
          .privilegedPlaceRowIntoConflictWithId(appName, dbHandleName, tableId, orderedColumns,
              cvValues, rowId, localRowConflictType), OdkDbTable.CREATOR);

       return new UserTable(baseTable, orderedColumns, getAdminColumns());
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * SYNC, CSV Import ONLY
    *
    * Insert the given rowId with the values in the cvValues. This is data from
    * the server. All metadata values must be specified in the cvValues (even null values).
    *
    * If a row with this rowId is present, then an exception is thrown.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param orderedColumns
    * @param cvValues
    * @param rowId
    * @param asCsvRequestedChange
    * @return single-row table with the content of the inserted row
    */
   public UserTable privilegedInsertRowWithId(String appName, OdkDbHandle dbHandleName, String tableId,
                                              OrderedColumns orderedColumns, ContentValues cvValues,
       String rowId, boolean asCsvRequestedChange) throws ServicesAvailabilityException {
     try {
       OdkDbTable baseTable = fetchAndRebuildChunks(dbInterface
          .privilegedInsertRowWithId(appName, dbHandleName, tableId, orderedColumns, cvValues,
              rowId, asCsvRequestedChange), OdkDbTable.CREATOR);

       return new UserTable(baseTable, orderedColumns, getAdminColumns());
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
       throws ActionNotAuthorizedException, ServicesAvailabilityException {
     try {
       OdkDbTable baseTable = fetchAndRebuildChunks(dbInterface
                .insertCheckpointRowWithId(appName, dbHandleName, tableId, orderedColumns, cvValues,
                    rowId), OdkDbTable.CREATOR);

       return new UserTable(baseTable, orderedColumns, getAdminColumns());
     } catch ( Exception e ) {
       rethrowNotAuthorizedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
       OrderedColumns orderedColumns, ContentValues cvValues, String rowId)
       throws ActionNotAuthorizedException, ServicesAvailabilityException {
     try {
      OdkDbTable baseTable = fetchAndRebuildChunks(dbInterface
              .insertRowWithId(appName, dbHandleName, tableId, orderedColumns, cvValues, rowId),
          OdkDbTable.CREATOR);

      return new UserTable(baseTable, orderedColumns, getAdminColumns());
     } catch ( Exception e ) {
       rethrowNotAuthorizedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
    * @param rowId
    * @return table with the content for this rowId. May be empty.
    */
   public UserTable deleteAllCheckpointRowsWithId(String appName, OdkDbHandle dbHandleName,
       String tableId, OrderedColumns orderedColumns, String rowId)
       throws ActionNotAuthorizedException, ServicesAvailabilityException {
     try {
       OdkDbTable baseTable = fetchAndRebuildChunks(
          dbInterface.deleteAllCheckpointRowsWithId(appName, dbHandleName, tableId, rowId),
          OdkDbTable.CREATOR);

       return new UserTable(baseTable, orderedColumns, getAdminColumns());
     } catch ( Exception e ) {
       rethrowNotAuthorizedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
    * @return table with the content for this rowId. May be empty.
    */
   public UserTable deleteLastCheckpointRowWithId(String appName, OdkDbHandle dbHandleName,
       String tableId, OrderedColumns orderedColumns, String rowId)
       throws ActionNotAuthorizedException, ServicesAvailabilityException {
     try {
       OdkDbTable baseTable = fetchAndRebuildChunks(
          dbInterface.deleteLastCheckpointRowWithId(appName, dbHandleName, tableId, rowId),
          OdkDbTable.CREATOR);

       return new UserTable(baseTable, orderedColumns, getAdminColumns());
     } catch ( Exception e ) {
       rethrowNotAuthorizedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * SYNC, Conflict Resolution ONLY
    *
    * Delete the specified rowId in this tableId. This is enforcing the server
    * state on the device. I.e., the sync interaction instructed us to delete
    * this row.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param orderedColumns
    * @param rowId
    * @return table with the content for this rowId. May be empty. May be marked as deleted and awaiting sync
    */
   public UserTable privilegedDeleteRowWithId(String appName, OdkDbHandle dbHandleName,
       String tableId, OrderedColumns orderedColumns, String rowId) throws ServicesAvailabilityException {
     try {
       OdkDbTable baseTable = fetchAndRebuildChunks(
          dbInterface.privilegedDeleteRowWithId(appName, dbHandleName, tableId, rowId),
          OdkDbTable.CREATOR);

       return new UserTable(baseTable, orderedColumns, getAdminColumns());
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
    * @return table with the content for this rowId. May be empty. May be marked as deleted and awaiting sync
    */
   public UserTable deleteRowWithId(String appName, OdkDbHandle dbHandleName, String tableId,
       OrderedColumns orderedColumns, String rowId)
       throws ActionNotAuthorizedException, ServicesAvailabilityException {
     try {
       OdkDbTable baseTable = fetchAndRebuildChunks(
          dbInterface.deleteRowWithId(appName, dbHandleName, tableId, rowId), OdkDbTable.CREATOR);

       return new UserTable(baseTable, orderedColumns, getAdminColumns());
     } catch ( Exception e ) {
       rethrowNotAuthorizedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
    * @param orderedColumns
    * @param rowId
    * @return single-row table with the content of the row as specified
    */
   public UserTable saveAsIncompleteMostRecentCheckpointRowWithId(String appName,
       OdkDbHandle dbHandleName, String tableId, OrderedColumns orderedColumns,
       String rowId) throws ActionNotAuthorizedException, ServicesAvailabilityException {
     try {
       OdkDbTable baseTable = fetchAndRebuildChunks(dbInterface
              .saveAsIncompleteMostRecentCheckpointRowWithId(appName, dbHandleName, tableId, rowId),
          OdkDbTable.CREATOR);

       return new UserTable(baseTable, orderedColumns, getAdminColumns());
     } catch ( Exception e ) {
       rethrowNotAuthorizedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
    * @param orderedColumns
    * @param rowId
    * @return single-row table with the content of the saved-as-incomplete row
    */
   public UserTable saveAsCompleteMostRecentCheckpointRowWithId(String appName,
       OdkDbHandle dbHandleName, String tableId, OrderedColumns orderedColumns,
       String rowId) throws ActionNotAuthorizedException, ServicesAvailabilityException {
     try {
       OdkDbTable baseTable = fetchAndRebuildChunks(dbInterface
              .saveAsCompleteMostRecentCheckpointRowWithId(appName, dbHandleName, tableId, rowId),
          OdkDbTable.CREATOR);

       return new UserTable(baseTable, orderedColumns, getAdminColumns());
     } catch ( Exception e ) {
       rethrowNotAuthorizedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
       OrderedColumns orderedColumns, ContentValues cvValues, String rowId)
       throws ActionNotAuthorizedException, ServicesAvailabilityException {
     try {
       OdkDbTable baseTable = fetchAndRebuildChunks(dbInterface
              .updateRowWithId(appName, dbHandleName, tableId, orderedColumns, cvValues, rowId),
          OdkDbTable.CREATOR);

       return new UserTable(baseTable, orderedColumns, getAdminColumns());
     } catch ( Exception e ) {
       rethrowNotAuthorizedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

  /**
   * Client-side wrapper to make changing the row filter easier.
   *
   * @param appName
   * @param dbHandleName
   * @param tableId
   * @param orderedColumns
   * @param filterType
   * @param filterValue
   * @param rowId
   * @return
   */
   public UserTable changeRowFilterWithId(String appName, OdkDbHandle dbHandleName, String tableId,
       OrderedColumns orderedColumns, String filterType, String filterValue,
       String rowId) throws IllegalArgumentException,
       ActionNotAuthorizedException, ServicesAvailabilityException {

      if ( filterType == null ) {
         throw new IllegalArgumentException("filterType is null");
      }
      // verify that filterType is a known type
      RowFilterScope.Type.valueOf(filterType);

      ContentValues cvValues = new ContentValues();
      cvValues.put(DataTableColumns.FILTER_TYPE, filterType);
      if ( filterValue == null ) {
         cvValues.putNull(DataTableColumns.FILTER_VALUE);
      } else {
         cvValues.put(DataTableColumns.FILTER_VALUE, filterValue);
      }

     try {
       OdkDbTable baseTable = fetchAndRebuildChunks(dbInterface
              .updateRowWithId(appName, dbHandleName, tableId, orderedColumns, cvValues, rowId),
          OdkDbTable.CREATOR);

       return new UserTable(baseTable, orderedColumns, getAdminColumns());
     } catch ( Exception e ) {
       rethrowNotAuthorizedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * SYNC, CSV Import ONLY
    *
    * Update the given rowId with the values in the cvValues. All field
    * values are specified in the cvValues. This is a server-induced update
    * of the row to match all fields from the server. An error is thrown if
    * there isn't a row matching this rowId or if there are checkpoint or
    * conflict entries for this rowId.
    *
    * @param appName
    * @param dbHandleName
    * @param tableId
    * @param orderedColumns
    * @param cvValues
    * @param rowId
    * @param asCsvRequestedChange
    * @return single-row table with the content of the saved-as-incomplete row
    */
   public UserTable privilegedUpdateRowWithId(String appName, OdkDbHandle dbHandleName,
          String tableId, OrderedColumns orderedColumns, ContentValues cvValues, String rowId,
       boolean asCsvRequestedChange) throws ServicesAvailabilityException {
     try {
       OdkDbTable baseTable = fetchAndRebuildChunks(dbInterface
          .privilegedUpdateRowWithId(appName, dbHandleName, tableId, orderedColumns, cvValues,
              rowId, asCsvRequestedChange), OdkDbTable.CREATOR);

       return new UserTable(baseTable, orderedColumns, getAdminColumns());
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
       String tableId, String rowId) throws ActionNotAuthorizedException, ServicesAvailabilityException {
     try {
       dbInterface.resolveServerConflictWithDeleteRowWithId(appName, dbHandleName, tableId, rowId);
     } catch ( Exception e ) {
       rethrowNotAuthorizedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
       String tableId, String rowId) throws ActionNotAuthorizedException, ServicesAvailabilityException {
     try {
      dbInterface.resolveServerConflictTakeLocalRowWithId(appName, dbHandleName, tableId, rowId);
     } catch ( Exception e ) {
       rethrowNotAuthorizedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
       throws ActionNotAuthorizedException, ServicesAvailabilityException {
     try {
      dbInterface
          .resolveServerConflictTakeLocalRowPlusServerDeltasWithId(appName, dbHandleName, tableId,
              cvValues, rowId);
     } catch ( Exception e ) {
       rethrowNotAuthorizedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
       String tableId, String rowId) throws ServicesAvailabilityException {
     try {
       dbInterface.resolveServerConflictTakeServerRowWithId(appName, dbHandleName, tableId, rowId);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
       String tableId) throws ServicesAvailabilityException {
     try {
       dbInterface.deleteAllSyncETagsForTableId(appName, dbHandleName, tableId);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
       String verifiedUri) throws ServicesAvailabilityException {
     try {
       dbInterface.deleteAllSyncETagsExceptForServer(appName, dbHandleName, verifiedUri);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
   }

   /**
    * Forget the document ETag values for everything under the specified Uri.
    *
    * @param appName
    * @param dbHandleName
    * @param verifiedUri  (e.g., https://opendatakit-tablesdemo.appspot.com)
    */
   public void deleteAllSyncETagsUnderServer(String appName, OdkDbHandle dbHandleName,
       String verifiedUri) throws ServicesAvailabilityException {
     try {
       dbInterface.deleteAllSyncETagsUnderServer(appName, dbHandleName, verifiedUri);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
       String tableId, long modificationTimestamp) throws ServicesAvailabilityException {
     try {
       return dbInterface
          .getFileSyncETag(appName, dbHandleName, verifiedUri, tableId, modificationTimestamp);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
       String tableId) throws ServicesAvailabilityException {
     try {
       return dbInterface.getManifestSyncETag(appName, dbHandleName, verifiedUri, tableId);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
       String tableId, long modificationTimestamp, String eTag) throws ServicesAvailabilityException {
     try {
       dbInterface
          .updateFileSyncETag(appName, dbHandleName, verifiedUri, tableId, modificationTimestamp,
              eTag);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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
       String verifiedUri, String tableId, String eTag) throws ServicesAvailabilityException {
     try {
       dbInterface.updateManifestSyncETag(appName, dbHandleName, verifiedUri, tableId, eTag);
     } catch ( Exception e ) {
       rethrowAlwaysAllowedRemoteException(e);
       throw new IllegalStateException("unreachable - keep IDE happy");
     }
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

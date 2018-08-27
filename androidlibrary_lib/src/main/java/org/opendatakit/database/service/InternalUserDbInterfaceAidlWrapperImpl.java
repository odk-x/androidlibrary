/*
 * Copyright (C) 2017 University of Washington
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

package org.opendatakit.database.service;

import android.content.ContentValues;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.os.RemoteException;
import org.opendatakit.database.data.BaseTable;
import org.opendatakit.database.data.ColumnList;
import org.opendatakit.database.data.KeyValueStoreEntry;
import org.opendatakit.database.data.OrderedColumns;
import org.opendatakit.database.data.TableDefinitionEntry;
import org.opendatakit.database.data.TableMetaDataEntries;
import org.opendatakit.database.queries.BindArgs;
import org.opendatakit.database.queries.QueryBounds;
import org.opendatakit.database.utilities.DbChunkUtil;
import org.opendatakit.exception.ActionNotAuthorizedException;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.logging.WebLogger;
import org.sqlite.database.sqlite.SQLiteException;

import org.opendatakit.database.service.IDbInterface;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Wrapper for the Aidl DbChunk and Exception pass-through interface that presents
 * an InternalUserDbInterface to the client-side database API wrapper which exposes
 * the more useable UserDbInterface.
 *
 * @author mitchellsundt@gmail.com
 */
public class InternalUserDbInterfaceAidlWrapperImpl implements InternalUserDbInterface {

  private static final String TAG = InternalUserDbInterfaceAidlWrapperImpl.class.getSimpleName();

  private IDbInterface dbInterface;

  public InternalUserDbInterfaceAidlWrapperImpl(IDbInterface dbInterface) throws IllegalArgumentException {
    if (dbInterface == null) {
      throw new IllegalArgumentException("Database Interface must not be null");
    }

    this.dbInterface = dbInterface;
  }

  public IDbInterface getDbInterface() {
    return dbInterface;
  }

  public void setDbInterface(IDbInterface dbInterface) {
    if (dbInterface == null) {
      throw new IllegalArgumentException("Database Interface must not be null");
    }

    this.dbInterface = dbInterface;
  }

  private void rethrowNotAuthorizedRemoteException(Exception e)
      throws IllegalArgumentException, IllegalStateException, SQLiteException,
      ActionNotAuthorizedException, ServicesAvailabilityException {
    if ((e instanceof IllegalStateException) || (e instanceof RemoteException)) {
      String prefix = "via RemoteException on IDbInterface: ";
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
          "not IllegalStateException or RemoteException on IDbInterface: " + e.getClass()
              .getName() + ": " + e.toString());
    }
  }

  private void rethrowAlwaysAllowedRemoteException(Exception e)
      throws IllegalArgumentException, IllegalStateException, SQLiteException,
      ServicesAvailabilityException {
    if ((e instanceof IllegalStateException) || (e instanceof RemoteException)) {
      String prefix = "via RemoteException on IDbInterface: ";
      String msg = e.getMessage();
      if (msg == null) {
        throw new IllegalStateException(prefix + e.toString());
      }
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
          "not IllegalStateException or RemoteException on IDbInterface: " + e.getClass()
              .getName() + ": " + e.toString());
    }
  }

  /**
   * Return the active user or "anonymous" if the user
   * has not been authenticated against the server.
   *
   * @param appName
   * @return the user reported from the server or "anonymous" if
   * server authentication has not been completed.
   */
  @Override
  public String getActiveUser(String appName) throws ServicesAvailabilityException {
    try {
      return dbInterface.getActiveUser(appName);
    } catch (Exception e) {
      rethrowAlwaysAllowedRemoteException(e);
      throw new IllegalStateException("unreachable - keep IDE happy");
    }
  }

  /**
   * Return the roles and groups of a verified username or google account.
   * If the username or google account have not been verified,
   * or if the server settings specify to use an anonymous user,
   * then return an empty string.
   *
   * @param appName
   * @return null or JSON serialization of an array of ROLES. See RoleConsts for possible values.
   */
  @Override
  public String getRolesList(String appName) throws ServicesAvailabilityException {
    try {
      return dbInterface.getRolesList(appName);
    } catch (Exception e) {
      rethrowAlwaysAllowedRemoteException(e);
      throw new IllegalStateException("unreachable - keep IDE happy");
    }
  }

  /**
   * Return the default group of the current user.
   *
   * @param appName
   * @return null or the name of the default group for this user.
   * @throws ServicesAvailabilityException
   */
  @Override
  public String getDefaultGroup(String appName) throws ServicesAvailabilityException {
    try {
      return dbInterface.getDefaultGroup(appName);
    } catch (Exception e) {
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
   * @return null or JSON serialization of an array of objects
   * structured as { "user_id": "...", "full_name": "...", "roles": ["...",...] }
   */
  @Override
  public String getUsersList(String appName) throws ServicesAvailabilityException {
    try {
      return fetchAndRebuildChunksAllowNull(dbInterface.getUsersList(appName), String.class);
    } catch (Exception e) {
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
  @Override
  public DbHandle openDatabase(String appName) throws ServicesAvailabilityException {
    try {
      return dbInterface.openDatabase(appName);
    } catch (Exception e) {
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
  @Override
  public void closeDatabase(String appName, DbHandle dbHandleName)
      throws ServicesAvailabilityException {
    try {
      dbInterface.closeDatabase(appName, dbHandleName);
    } catch (Exception e) {
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
  @Override
  public OrderedColumns createLocalOnlyTableWithColumns(String appName, DbHandle dbHandleName,
                                                        String tableId, ColumnList columns)
      throws ServicesAvailabilityException {

    try {
      return fetchAndRebuildChunks(
          dbInterface.createLocalOnlyTableWithColumns(appName, dbHandleName, tableId, columns),
          OrderedColumns.CREATOR);
    } catch (Exception e) {
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
  @Override
  public void deleteLocalOnlyTable(String appName, DbHandle dbHandleName, String tableId)
      throws ServicesAvailabilityException {

    try {
      dbInterface.deleteLocalOnlyTable(appName, dbHandleName, tableId);
    } catch (Exception e) {
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
  @Override
  public void insertLocalOnlyRow(String appName, DbHandle dbHandleName, String tableId,
                                 ContentValues rowValues) throws ServicesAvailabilityException {

    try {
      dbInterface.insertLocalOnlyRow(appName, dbHandleName, tableId, rowValues);
    } catch (Exception e) {
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
   * @param bindArgs
   * @throws ServicesAvailabilityException
   */
  @Override
  public void updateLocalOnlyRows(String appName, DbHandle dbHandleName, String tableId,
                                 ContentValues rowValues, String whereClause, BindArgs bindArgs)
      throws ServicesAvailabilityException {

    try {
      dbInterface.updateLocalOnlyRow(appName, dbHandleName, tableId, rowValues, whereClause,
          bindArgs);
    } catch (Exception e) {
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
   * @param bindArgs
   * @throws ServicesAvailabilityException
   */
  @Override
  public void deleteLocalOnlyRows(String appName, DbHandle dbHandleName, String tableId,
                                 String whereClause, BindArgs bindArgs)
      throws ServicesAvailabilityException {

    try {
      dbInterface
          .deleteLocalOnlyRow(appName, dbHandleName, tableId, whereClause, bindArgs);
    } catch (Exception e) {
      rethrowAlwaysAllowedRemoteException(e);
      throw new IllegalStateException("unreachable - keep IDE happy");
    }
  }

  /**
   * SYNC Only. ADMIN Privileges
   * <p>
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
   * updateTableETags(sc.getAppName(), db, tableId, schemaETag, null);
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
  @Override
  public void privilegedServerTableSchemaETagChanged(String appName, DbHandle dbHandleName,
                                                     String tableId, String schemaETag,
                                                     String tableInstanceFilesUri)
      throws ServicesAvailabilityException {
    try {
      dbInterface.privilegedServerTableSchemaETagChanged(appName, dbHandleName, tableId, schemaETag,
          tableInstanceFilesUri);
    } catch (Exception e) {
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
  @Override
  public String setChoiceList(String appName, DbHandle dbHandleName, String choiceListJSON)
      throws ServicesAvailabilityException {
    try {
      return dbInterface.setChoiceList(appName, dbHandleName, choiceListJSON);
    } catch (Exception e) {
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
   * @return null or choiceListJSON -- the actual JSON choice list text.
   */
  @Override
  public String getChoiceList(String appName, DbHandle dbHandleName, String choiceListId)
      throws ServicesAvailabilityException {
    try {
      return fetchAndRebuildChunksAllowNull(dbInterface.getChoiceList(appName, dbHandleName,
          choiceListId),
          String.class);
    } catch (Exception e) {
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
  @Override
  public OrderedColumns createOrOpenTableWithColumns(String appName, DbHandle dbHandleName,
                                                     String tableId, ColumnList columns)
      throws ServicesAvailabilityException {
    try {
      return fetchAndRebuildChunks(
          dbInterface.createOrOpenTableWithColumns(appName, dbHandleName, tableId, columns),
          OrderedColumns.CREATOR);
    } catch (Exception e) {
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
  @Override
  public OrderedColumns createOrOpenTableWithColumnsAndProperties(String appName,
                                                                  DbHandle dbHandleName,
                                                                  String tableId,
                                                                  ColumnList columns,
                                                                  List<KeyValueStoreEntry> metaData,
                                                                  boolean clear)
      throws ServicesAvailabilityException {
    try {
      return fetchAndRebuildChunks(dbInterface
          .createOrOpenTableWithColumnsAndProperties(appName, dbHandleName, tableId, columns,
              metaData, clear), OrderedColumns.CREATOR);
    } catch (Exception e) {
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
  @Override
  public void deleteTableAndAllData(String appName, DbHandle dbHandleName, String tableId)
      throws ServicesAvailabilityException {
    try {
      dbInterface.deleteTableAndAllData(appName, dbHandleName, tableId);
    } catch (Exception e) {
      rethrowAlwaysAllowedRemoteException(e);
      throw new IllegalStateException("unreachable - keep IDE happy");
    }
  }

  /**
   * Rescan the config directory tree of the given tableId and update the forms table
   * with revised information from the formDef.json files that it contains.
   *
   * @param appName
   * @param dbHandleName
   * @param tableId
   * @return true if there were no problems
   */
  @Override
  public boolean rescanTableFormDefs(String appName, DbHandle dbHandleName, String tableId)
      throws ServicesAvailabilityException {
    try {
      return dbInterface.rescanTableFormDefs(appName, dbHandleName, tableId);
    } catch (Exception e) {
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
  @Override
  public void deleteTableMetadata(String appName, DbHandle dbHandleName, String tableId,
                                  String partition, String aspect, String key)
      throws ServicesAvailabilityException {
    try {
      dbInterface.deleteTableMetadata(appName, dbHandleName, tableId, partition, aspect, key);
    } catch (Exception e) {
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
  @Override
  public String[] getAdminColumns() throws ServicesAvailabilityException {
    try {
      return fetchAndRebuildChunks(dbInterface.getAdminColumns(), String[].class);
    } catch (Exception e) {
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
  @Override
  public String[] getAllColumnNames(String appName, DbHandle dbHandleName, String tableId)
      throws ServicesAvailabilityException {
    try {
      return fetchAndRebuildChunks(dbInterface.getAllColumnNames(appName, dbHandleName, tableId),
          String[].class);
    } catch (Exception e) {
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
  @Override
  @SuppressWarnings("unchecked")
  public List<String> getAllTableIds(String appName, DbHandle dbHandleName)
      throws ServicesAvailabilityException {
    try {
      Serializable result = fetchAndRebuildChunks(dbInterface.getAllTableIds(appName, dbHandleName),
          Serializable.class);
      return (List<String>) result;
    } catch (Exception e) {
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
  @Override
  @SuppressWarnings("unchecked")
  public TableMetaDataEntries getTableMetadata(String appName, DbHandle dbHandleName, String tableId,
                                               String partition, String aspect, String key)
      throws ServicesAvailabilityException {

    TableMetaDataEntries entries = null;

    try {
      entries = fetchAndRebuildChunks(
          dbInterface.getTableMetadata(appName, dbHandleName, tableId, partition, aspect, key),
          TableMetaDataEntries.CREATOR);
    } catch (Exception e) {
      rethrowAlwaysAllowedRemoteException(e);
      WebLogger.getLogger(appName).printStackTrace(e);
      throw new IllegalStateException("unreachable - keep IDE happy");
    }

    if (entries == null) {
      // Do not return null. API is expected to return an empty list if no results are found.
      entries = new TableMetaDataEntries(tableId, null);
    }
    return entries;

  }

  /**
   * @param appName
   * @param dbHandleName
   * @param tableId
   * @param revId
   * @return list of KeyValueStoreEntry values if changed
   */
  @Override
  public TableMetaDataEntries getTableMetadataIfChanged(String appName, DbHandle dbHandleName,
                                                        String tableId, String revId)
      throws ServicesAvailabilityException {

    TableMetaDataEntries entries = null;

    try {
      // If there is a cache hit, check if it is stale
      entries = fetchAndRebuildChunks(dbInterface
              .getTableMetadataIfChanged(appName, dbHandleName, tableId, revId),
          TableMetaDataEntries.CREATOR);
    } catch (Exception e) {
      rethrowAlwaysAllowedRemoteException(e);
      WebLogger.getLogger(appName).printStackTrace(e);
      throw new IllegalStateException("unreachable - keep IDE happy");
    }

    if (entries == null) {
      // Do not return null. API is expected to return an empty list if no results are found.
      entries = new TableMetaDataEntries(tableId, null);
    }
    return entries;
  }

  private TableMetaDataEntries filterEntries(TableMetaDataEntries allEntries, String partition,
                                             String aspect, String key) {
    if (partition == null && aspect == null && key == null) {
      return new TableMetaDataEntries(allEntries);
    }

    TableMetaDataEntries entries = new TableMetaDataEntries(allEntries.getTableId(),
        allEntries.getRevId());

    for (KeyValueStoreEntry entry : allEntries.getEntries()) {
      if ((partition == null || entry.partition.equals(partition)) && (aspect == null
          || entry.aspect.equals(aspect)) && (key == null || entry.key.equals(key))) {
        entries.addEntry(entry);
      }
    }
    return entries;
  }

  /**
   * Return an array of the admin columns that should be exported to
   * a CSV file. This list excludes the SYNC_STATE and CONFLICT_TYPE columns.
   *
   * @return
   */
  @Override
  public String[] getExportColumns() throws ServicesAvailabilityException {
    try {
      return fetchAndRebuildChunks(dbInterface.getExportColumns(), String[].class);
    } catch (Exception e) {
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
  @Override
  public TableDefinitionEntry getTableDefinitionEntry(String appName, DbHandle dbHandleName,
                                                      String tableId)
      throws ServicesAvailabilityException {
    try {
      return fetchAndRebuildChunks(
          dbInterface.getTableDefinitionEntry(appName, dbHandleName, tableId),
          TableDefinitionEntry.CREATOR);
    } catch (Exception e) {
      rethrowAlwaysAllowedRemoteException(e);
      throw new IllegalStateException("unreachable - keep IDE happy");
    }
  }

  /**
   * Return a table's health status.
   *
   * @param appName
   * @param dbHandleName
   * @param tableId
   * @return the TableHealthInfo record for this appName and tableId
   */
  @Override
  @SuppressWarnings("unchecked")
  public TableHealthInfo getTableHealthStatus(String appName, DbHandle dbHandleName, String tableId)
      throws ServicesAvailabilityException {
    try {
      Parcelable results = fetchAndRebuildChunks(
          dbInterface.getTableHealthStatus(appName, dbHandleName, tableId), Parcelable.class);

      return (TableHealthInfo) results;
    } catch (Exception e) {
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
  @Override
  @SuppressWarnings("unchecked")
  public List<TableHealthInfo> getTableHealthStatuses(String appName, DbHandle dbHandleName)
      throws ServicesAvailabilityException {
    try {
      Serializable results = fetchAndRebuildChunks(
          dbInterface.getTableHealthStatuses(appName, dbHandleName), Serializable.class);

      return (List<TableHealthInfo>) results;
    } catch (Exception e) {
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
  @Override
  public OrderedColumns getUserDefinedColumns(String appName, DbHandle dbHandleName, String tableId)
      throws ServicesAvailabilityException {
    try {
      return fetchAndRebuildChunks(
          dbInterface.getUserDefinedColumns(appName, dbHandleName, tableId),
          OrderedColumns.CREATOR);
    } catch (Exception e) {
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
  @Override
  public boolean hasTableId(String appName, DbHandle dbHandleName, String tableId)
      throws ServicesAvailabilityException {
    try {
      return dbInterface.hasTableId(appName, dbHandleName, tableId);
    } catch (Exception e) {
      rethrowAlwaysAllowedRemoteException(e);
      throw new IllegalStateException("unreachable - keep IDE happy");
    }
  }

  /********** RAW GENERIC QUERIES **********/

  /**
   * Get a {@link BaseTable} for this table based on the given SQL command.
   *
   * @param appName
   * @param dbHandleName
   * @param sqlCommand
   * @param bindArgs       (effectively a typed array of values)
   * @param sqlQueryBounds bounds for query (offset and limit)
   * @param tableId        if supplied, resolve column data types based upon columns in this table
   * @return
   * @throws ServicesAvailabilityException
   */
  @Override
  public BaseTable simpleQuery(String appName, DbHandle dbHandleName, String sqlCommand,
                               BindArgs bindArgs, QueryBounds sqlQueryBounds, String tableId)
      throws ServicesAvailabilityException {

    try {

      BaseTable baseTable = fetchAndRebuildChunks(dbInterface
          .simpleQuery(appName, dbHandleName, sqlCommand, bindArgs,
              sqlQueryBounds, tableId), BaseTable.CREATOR);

      return baseTable;
    } catch (Exception e) {
      rethrowAlwaysAllowedRemoteException(e);
      throw new IllegalStateException("unreachable - keep IDE happy");
    }
  }

  /**
   * SYNC ONLY
   * <p>
   * Privileged version of above query.
   * <p>
   * Get a {@link BaseTable} for this table based on the given SQL command.
   *
   * @param appName
   * @param dbHandleName
   * @param sqlCommand
   * @param bindArgs       (effectively a typed array of values)
   * @param sqlQueryBounds bounds for query (offset and limit)
   * @param tableId        if supplied, resolve column data types based upon columns in this table
   * @return
   * @throws ServicesAvailabilityException
   */
  @Override
  public BaseTable privilegedSimpleQuery(String appName, DbHandle dbHandleName, String sqlCommand,
                                         BindArgs bindArgs, QueryBounds sqlQueryBounds, String
                                               tableId)
      throws ServicesAvailabilityException {

    try {
      BaseTable baseTable = fetchAndRebuildChunks(dbInterface
              .privilegedSimpleQuery(appName, dbHandleName, sqlCommand,
                  bindArgs, sqlQueryBounds, tableId),
          BaseTable.CREATOR);
      return baseTable;
    } catch (Exception e) {
      rethrowAlwaysAllowedRemoteException(e);
      throw new IllegalStateException("unreachable - keep IDE happy");
    }
  }

  /**
   * Execute an arbitrary command with bind parameters.
   *
   * @param appName
   * @param dbHandleName
   * @param sqlCommand
   * @param bindArgs     an array of primitive values (String, Boolean, int, double) for
   *                     bind parameters
   */
  @Override
  public void privilegedExecute(String appName, DbHandle dbHandleName, String sqlCommand,
                                BindArgs bindArgs) throws ServicesAvailabilityException {
    try {
      dbInterface.privilegedExecute(appName, dbHandleName, sqlCommand, bindArgs);
    } catch (Exception e) {
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
  @Override
  public void replaceTableMetadata(String appName, DbHandle dbHandleName, KeyValueStoreEntry entry)
      throws ServicesAvailabilityException {
    try {
      dbInterface.replaceTableMetadata(appName, dbHandleName, entry);
    } catch (Exception e) {
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
  @Override
  public void replaceTableMetadataList(String appName, DbHandle dbHandleName, String tableId,
                                       List<KeyValueStoreEntry> entries, boolean clear)
      throws ServicesAvailabilityException {
    try {
      dbInterface.replaceTableMetadataList(appName, dbHandleName, tableId, entries, clear);
    } catch (Exception e) {
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
   * @param entries      List<KeyValueStoreEntry>
   */
  @Override
  public void replaceTableMetadataSubList(String appName, DbHandle dbHandleName, String tableId,
                                          String partition, String aspect,
                                          List<KeyValueStoreEntry> entries)
      throws ServicesAvailabilityException {
    try {
      dbInterface
          .replaceTableMetadataSubList(appName, dbHandleName, tableId, partition, aspect, entries);
    } catch (Exception e) {
      rethrowAlwaysAllowedRemoteException(e);
      throw new IllegalStateException("unreachable - keep IDE happy");
    }
  }

  /**
   * SYNC Only. ADMIN Privileges
   * <p>
   * Update the schema and data-modification ETags of a given tableId.
   *
   * @param appName
   * @param dbHandleName
   * @param tableId
   * @param schemaETag
   * @param lastDataETag
   */
  @Override
  public void privilegedUpdateTableETags(String appName, DbHandle dbHandleName, String tableId,
                                         String schemaETag, String lastDataETag)
      throws ServicesAvailabilityException {
    try {
      dbInterface
          .privilegedUpdateTableETags(appName, dbHandleName, tableId, schemaETag, lastDataETag);
    } catch (Exception e) {
      rethrowAlwaysAllowedRemoteException(e);
      throw new IllegalStateException("unreachable - keep IDE happy");
    }
  }

  /**
   * SYNC Only. ADMIN Privileges
   * <p>
   * Update the timestamp of the last entirely-successful synchronization
   * attempt of this table.
   *
   * @param appName
   * @param dbHandleName
   * @param tableId
   */
  @Override
  public void privilegedUpdateTableLastSyncTime(String appName, DbHandle dbHandleName,
                                                String tableId)
      throws ServicesAvailabilityException {
    try {
      dbInterface.privilegedUpdateTableLastSyncTime(appName, dbHandleName, tableId);
    } catch (Exception e) {
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
  @Override
  public String getSyncState(String appName, DbHandle dbHandleName, String tableId, String rowId)
      throws ServicesAvailabilityException {
    try {
      return dbInterface.getSyncState(appName, dbHandleName, tableId, rowId);
    } catch (Exception e) {
      rethrowAlwaysAllowedRemoteException(e);
      throw new IllegalStateException("unreachable - keep IDE happy");
    }
  }

  /**
   * SYNC Only. ADMIN Privileges!
   * <p>
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
  @Override
  public void privilegedUpdateRowETagAndSyncState(String appName, DbHandle dbHandleName,
                                                  String tableId, String rowId, String rowETag,
                                                  String syncState)
      throws ServicesAvailabilityException {
    try {
      dbInterface
          .privilegedUpdateRowETagAndSyncState(appName, dbHandleName, tableId, rowId, rowETag,
              syncState);
    } catch (Exception e) {
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
   * @param rowId
   * @return
   */
  @Override
  public BaseTable getRowsWithId(String appName, DbHandle dbHandleName, String tableId,
                                 String rowId)
      throws ServicesAvailabilityException {
    try {
      BaseTable baseTable = fetchAndRebuildChunks(
          dbInterface.getRowsWithId(appName, dbHandleName, tableId, rowId), BaseTable.CREATOR);

      return baseTable;
    } catch (Exception e) {
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
   * @param rowId
   * @return
   */
  @Override
  public BaseTable privilegedGetRowsWithId(String appName, DbHandle dbHandleName, String tableId,
                                           String rowId)
      throws ServicesAvailabilityException {
    try {
      BaseTable baseTable = fetchAndRebuildChunks(
          dbInterface.privilegedGetRowsWithId(appName, dbHandleName, tableId, rowId),
          BaseTable.CREATOR);

      return baseTable;
    } catch (Exception e) {
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
   * @param rowId
   * @return
   */
  @Override
  public BaseTable getMostRecentRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                          String rowId)
      throws ServicesAvailabilityException {
    try {
      BaseTable baseTable = fetchAndRebuildChunks(
          dbInterface.getMostRecentRowWithId(appName, dbHandleName, tableId, rowId),
          BaseTable.CREATOR);

      return baseTable;
    } catch (Exception e) {
      rethrowAlwaysAllowedRemoteException(e);
      throw new IllegalStateException("unreachable - keep IDE happy");
    }
  }

  /**
   * SYNC ONLY
   * <p>
   * Takes the row field values from a server Row (obtained through the Sync protocol)
   * and applies them to the local row (which must already exist and cannot have checkpoints).
   * <p/>
   * The outcome may delete the local row, update it to the server's values, or
   * place the local row into conflict with the server values (ending with 2 rows
   * in the database for this rowId).
   *
   * @param appName
   * @param dbHandleName
   * @param tableId
   * @param cvValues       the field values on the server
   * @param rowId
   * @return The rows in the database matching the rowId (may be 0, 1 or 2 rows).
   */
  @Override
  public BaseTable privilegedPerhapsPlaceRowIntoConflictWithId(String appName,
                                                               DbHandle dbHandleName,
                                                               String tableId,
                                                               ContentValues cvValues, String rowId)
      throws ServicesAvailabilityException {
    try {
      BaseTable baseTable = fetchAndRebuildChunks(dbInterface
          .privilegedPerhapsPlaceRowIntoConflictWithId(appName, dbHandleName, tableId,
              cvValues, rowId), BaseTable.CREATOR);

      return baseTable;
    } catch (Exception e) {
      rethrowAlwaysAllowedRemoteException(e);
      throw new IllegalStateException("unreachable - keep IDE happy");
    }
  }

  /**
   * SYNC, CSV Import ONLY
   * <p>
   * Insert the given rowId with the values in the cvValues. This is data from
   * the server. All metadata values must be specified in the cvValues (even null values).
   * <p>
   * If a row with this rowId is present, then an exception is thrown.
   *
   * @param appName
   * @param dbHandleName
   * @param tableId
   * @param cvValues
   * @param rowId
   * @param asCsvRequestedChange
   * @return single-row table with the content of the inserted row
   */
  @Override
  public BaseTable privilegedInsertRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                             ContentValues cvValues,
                                             String rowId, boolean asCsvRequestedChange)
      throws ServicesAvailabilityException {
    try {
      BaseTable baseTable = fetchAndRebuildChunks(dbInterface
          .privilegedInsertRowWithId(appName, dbHandleName, tableId, cvValues,
              rowId, asCsvRequestedChange), BaseTable.CREATOR);

      return baseTable;
    } catch (Exception e) {
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
   * @param cvValues
   * @param rowId
   * @return single-row table with the content of the inserted checkpoint
   */
  @Override
  public BaseTable insertCheckpointRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                             ContentValues cvValues,
                                             String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException {
    try {
      BaseTable baseTable = fetchAndRebuildChunks(dbInterface
          .insertCheckpointRowWithId(appName, dbHandleName, tableId, cvValues,
              rowId), BaseTable.CREATOR);

      return baseTable;
    } catch (Exception e) {
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
   * @param cvValues
   * @param rowId
   * @return single-row table with the content of the inserted row
   */
  @Override
  public BaseTable insertRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                   ContentValues cvValues, String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException {
    try {
      BaseTable baseTable = fetchAndRebuildChunks(dbInterface
              .insertRowWithId(appName, dbHandleName, tableId, cvValues, rowId),
          BaseTable.CREATOR);

      return baseTable;
    } catch (Exception e) {
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
  @Override
  public BaseTable deleteAllCheckpointRowsWithId(String appName, DbHandle dbHandleName,
                                                 String tableId,
                                                 String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException {
    try {
      BaseTable baseTable = fetchAndRebuildChunks(
          dbInterface.deleteAllCheckpointRowsWithId(appName, dbHandleName, tableId, rowId),
          BaseTable.CREATOR);

      return baseTable;
    } catch (Exception e) {
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
  @Override
  public BaseTable deleteLastCheckpointRowWithId(String appName, DbHandle dbHandleName,
                                                 String tableId,
                                                 String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException {
    try {
      BaseTable baseTable = fetchAndRebuildChunks(
          dbInterface.deleteLastCheckpointRowWithId(appName, dbHandleName, tableId, rowId),
          BaseTable.CREATOR);

      return baseTable;
    } catch (Exception e) {
      rethrowNotAuthorizedRemoteException(e);
      throw new IllegalStateException("unreachable - keep IDE happy");
    }
  }

  /**
   * SYNC, Conflict Resolution ONLY
   * <p>
   * Delete the specified rowId in this tableId. This is enforcing the server
   * state on the device. I.e., the sync interaction instructed us to delete
   * this row.
   *
   * @param appName
   * @param dbHandleName
   * @param tableId
   * @param rowId
   * @return table with the content for this rowId. May be empty. May be marked as deleted and awaiting sync
   */
  @Override
  public BaseTable privilegedDeleteRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                             String rowId)
      throws ServicesAvailabilityException {
    try {
      BaseTable baseTable = fetchAndRebuildChunks(
          dbInterface.privilegedDeleteRowWithId(appName, dbHandleName, tableId, rowId),
          BaseTable.CREATOR);

      return baseTable;
    } catch (Exception e) {
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
   * @param rowId
   * @return table with the content for this rowId. May be empty. May be marked as deleted and awaiting sync
   */
  @Override
  public BaseTable deleteRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                   String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException {
    try {
      BaseTable baseTable = fetchAndRebuildChunks(
          dbInterface.deleteRowWithId(appName, dbHandleName, tableId, rowId), BaseTable.CREATOR);

      return baseTable;
    } catch (Exception e) {
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
   * @param rowId
   * @return single-row table with the content of the row as specified
   */
  @Override
  public BaseTable saveAsIncompleteMostRecentCheckpointRowWithId(String appName,
                                                                 DbHandle dbHandleName,
                                                                 String tableId,
                                                                 String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException {
    try {
      BaseTable baseTable = fetchAndRebuildChunks(dbInterface
              .saveAsIncompleteMostRecentCheckpointRowWithId(appName, dbHandleName, tableId, rowId),
          BaseTable.CREATOR);

      return baseTable;
    } catch (Exception e) {
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
   * @param rowId
   * @return single-row table with the content of the saved-as-incomplete row
   */
  @Override
  public BaseTable saveAsCompleteMostRecentCheckpointRowWithId(String appName,
                                                               DbHandle dbHandleName,
                                                               String tableId,
                                                               String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException {
    try {
      BaseTable baseTable = fetchAndRebuildChunks(dbInterface
              .saveAsCompleteMostRecentCheckpointRowWithId(appName, dbHandleName, tableId, rowId),
          BaseTable.CREATOR);

      return baseTable;
    } catch (Exception e) {
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
   * @param cvValues
   * @param rowId
   * @return single-row table with the content of the saved-as-incomplete row
   */
  @Override
  public BaseTable updateRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                   ContentValues cvValues,
                                   String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException {
    try {
      BaseTable baseTable = fetchAndRebuildChunks(dbInterface
              .updateRowWithId(appName, dbHandleName, tableId, cvValues, rowId),
          BaseTable.CREATOR);

      return baseTable;
    } catch (Exception e) {
      rethrowNotAuthorizedRemoteException(e);
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
  @Override
  public void resolveServerConflictWithDeleteRowWithId(String appName, DbHandle dbHandleName,
                                                       String tableId, String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException {
    try {
      dbInterface.resolveServerConflictWithDeleteRowWithId(appName, dbHandleName, tableId, rowId);
    } catch (Exception e) {
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
  @Override
  public void resolveServerConflictTakeLocalRowWithId(String appName, DbHandle dbHandleName,
                                                      String tableId, String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException {
    try {
      dbInterface.resolveServerConflictTakeLocalRowWithId(appName, dbHandleName, tableId, rowId);
    } catch (Exception e) {
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
  @Override
  public void resolveServerConflictTakeLocalRowPlusServerDeltasWithId(String appName,
                                                                      DbHandle dbHandleName,
                                                                      String tableId,
                                                                      ContentValues cvValues,
                                                                      String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException {
    try {
      dbInterface
          .resolveServerConflictTakeLocalRowPlusServerDeltasWithId(appName, dbHandleName, tableId,
              cvValues, rowId);
    } catch (Exception e) {
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
  @Override
  public void resolveServerConflictTakeServerRowWithId(String appName, DbHandle dbHandleName,
                                                       String tableId, String rowId)
      throws ServicesAvailabilityException {
    try {
      dbInterface.resolveServerConflictTakeServerRowWithId(appName, dbHandleName, tableId, rowId);
    } catch (Exception e) {
      rethrowAlwaysAllowedRemoteException(e);
      throw new IllegalStateException("unreachable - keep IDE happy");
    }
  }

  /**
   * Remove app and table level manifests. Invoked when we select reset configuration
   * and the initialization task is executed.
   *
   * @param appName
   * @param dbHandleName
   */
  @Override
  public void deleteAppAndTableLevelManifestSyncETags(String appName, DbHandle dbHandleName)
      throws ServicesAvailabilityException {
    try {
      dbInterface.deleteAppAndTableLevelManifestSyncETags(appName, dbHandleName);
    } catch (Exception e) {
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
  @Override
  public void deleteAllSyncETagsForTableId(String appName, DbHandle dbHandleName, String tableId)
      throws ServicesAvailabilityException {
    try {
      dbInterface.deleteAllSyncETagsForTableId(appName, dbHandleName, tableId);
    } catch (Exception e) {
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
  @Override
  public void deleteAllSyncETagsExceptForServer(String appName, DbHandle dbHandleName,
                                                String verifiedUri)
      throws ServicesAvailabilityException {
    try {
      dbInterface.deleteAllSyncETagsExceptForServer(appName, dbHandleName, verifiedUri);
    } catch (Exception e) {
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
  @Override
  public void deleteAllSyncETagsUnderServer(String appName, DbHandle dbHandleName,
                                            String verifiedUri)
      throws ServicesAvailabilityException {
    try {
      dbInterface.deleteAllSyncETagsUnderServer(appName, dbHandleName, verifiedUri);
    } catch (Exception e) {
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
  @Override
  public String getFileSyncETag(String appName, DbHandle dbHandleName, String verifiedUri,
                                String tableId, long modificationTimestamp)
      throws ServicesAvailabilityException {
    try {
      return dbInterface
          .getFileSyncETag(appName, dbHandleName, verifiedUri, tableId, modificationTimestamp);
    } catch (Exception e) {
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
  @Override
  public String getManifestSyncETag(String appName, DbHandle dbHandleName, String verifiedUri,
                                    String tableId) throws ServicesAvailabilityException {
    try {
      return dbInterface.getManifestSyncETag(appName, dbHandleName, verifiedUri, tableId);
    } catch (Exception e) {
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
  @Override
  public void updateFileSyncETag(String appName, DbHandle dbHandleName, String verifiedUri,
                                 String tableId, long modificationTimestamp, String eTag)
      throws ServicesAvailabilityException {
    try {
      dbInterface
          .updateFileSyncETag(appName, dbHandleName, verifiedUri, tableId, modificationTimestamp,
              eTag);
    } catch (Exception e) {
      rethrowAlwaysAllowedRemoteException(e);
      throw new IllegalStateException("unreachable - keep IDE happy");
    }
  }

  /**
   * Update the document ETag values for the given manifest under the specified Uri.
   *
   * @param appName
   * @param dbHandleName
   * @param verifiedUri  (e.g., https://opendatakit-tablesdemo.appspot.com)
   * @param tableId      (null if an application-level manifest)
   * @param eTag
   */
  @Override
  public void updateManifestSyncETag(String appName, DbHandle dbHandleName, String verifiedUri,
                                     String tableId, String eTag)
      throws ServicesAvailabilityException {
    try {
      dbInterface.updateManifestSyncETag(appName, dbHandleName, verifiedUri, tableId, eTag);
    } catch (Exception e) {
      rethrowAlwaysAllowedRemoteException(e);
      throw new IllegalStateException("unreachable - keep IDE happy");
    }
  }

  /**
   * Retrieve all the pieces of the data across the wire and rebuild them into their original type
   *
   * @param firstChunk The first chunk, which contains a pointer to the next
   * @param creator    The parcelable reconstructor
   * @param <T>        The type to reconstruct into
   * @return The original object
   * @throws RemoteException
   */
  private <T> T fetchAndRebuildChunks(DbChunk firstChunk, Parcelable.Creator<T> creator)
      throws RemoteException {

    List<DbChunk> aggregatedChunks = retrieveChunks(firstChunk);

    return DbChunkUtil.rebuildFromChunks(aggregatedChunks, creator);
  }

  /**
   * Retrieve all the pieces of the data across the wire and rebuild them into their original type
   *
   * @param firstChunk   The first chunk, which contains a pointer to the next
   * @param serializable
   * @param <T>          The type to reconstruct into
   * @return The original object
   * @throws RemoteException
   */
  private <T> T fetchAndRebuildChunks(DbChunk firstChunk, Class<T> serializable)
      throws RemoteException {

    List<DbChunk> aggregatedChunks = retrieveChunks(firstChunk);

    try {
      return DbChunkUtil.rebuildFromChunks(aggregatedChunks, serializable);
    } catch (Exception e) {
      WebLogger.getContextLogger().e(TAG, "Failed to rebuild serialized object from chunks");
      return null;
    }
  }

  /**
   * Retrieve all the pieces of the data across the wire and rebuild them into their original type
   *
   * @param firstChunk   The first chunk, which contains a pointer to the next. May be null.
   * @param serializable
   * @param <T>          The type to reconstruct into
   * @return The original object
   * @throws RemoteException
   */
  private <T> T fetchAndRebuildChunksAllowNull(DbChunk firstChunk, Class<T> serializable)
      throws RemoteException {

    if ( firstChunk == null ) {
      return null;
    }

    return fetchAndRebuildChunks(firstChunk, serializable);
  }

  /**
   * Iterate through the chunks like a linked list, retrieving them one by one from the service
   *
   * @param firstChunk
   * @return
   * @throws RemoteException
   */
  private List<DbChunk> retrieveChunks(DbChunk firstChunk) throws RemoteException {
    List<DbChunk> aggregatedChunks = new LinkedList<>();
    aggregatedChunks.add(firstChunk);

    DbChunk currChunk = firstChunk;
    while (currChunk.hasNextID()) {
      ParcelUuid parcelUuid = new ParcelUuid(currChunk.getNextID());
      currChunk = dbInterface.getChunk(parcelUuid);
      aggregatedChunks.add(currChunk);
    }

    return aggregatedChunks;
  }
}

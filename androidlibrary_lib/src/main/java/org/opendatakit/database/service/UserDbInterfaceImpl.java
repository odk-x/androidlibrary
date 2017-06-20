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

package org.opendatakit.database.service;

import android.content.ContentValues;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;
import org.opendatakit.database.data.*;
import org.opendatakit.database.queries.ArbitraryQuery;
import org.opendatakit.database.queries.BindArgs;
import org.opendatakit.database.queries.ResumableQuery;
import org.opendatakit.database.queries.SimpleQuery;
import org.opendatakit.exception.ActionNotAuthorizedException;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.provider.DataTableColumns;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDbInterfaceImpl implements UserDbInterface {

  private final InternalUserDbInterface internalUserDbInterface;

  /**
   * Access this ONLY through getMetadata(tableId) and putMetadata(tableId, entries).
   * Multiple threads may be accessing this.
   */
  private final Map<String, TableMetaDataEntries> privateMetaDataCache = new HashMap<>();

  /**
   * Access this ONLY through internalGetAdminColumns()
   * Multiple threads may be accessing this.
   */
  private String[] internalAdminColumns;

  public UserDbInterfaceImpl(InternalUserDbInterface internalUserDbInterface) throws IllegalArgumentException {
    if (internalUserDbInterface == null) {
      throw new IllegalArgumentException("Database Interface must not be null");
    }

    this.internalUserDbInterface = internalUserDbInterface;
  }

  private TableMetaDataEntries getMetadata(String tableId) {
    synchronized (privateMetaDataCache) {
      return privateMetaDataCache.get(tableId);
    }
  }

  private void putMetadata(String tableId, TableMetaDataEntries entries) {
    synchronized (privateMetaDataCache) {
      privateMetaDataCache.put(tableId, entries);
    }
  }

  public InternalUserDbInterface getInternalUserDbInterface() {
    return internalUserDbInterface;
  }

  private synchronized String[] internalGetAdminColumns() throws ServicesAvailabilityException {
    if ( internalAdminColumns != null ) {
      return internalAdminColumns;
    } else {
      internalAdminColumns = getAdminColumns();
      return internalAdminColumns;
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

    return internalUserDbInterface.getActiveUser(appName);
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

    return internalUserDbInterface.getRolesList(appName);
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

    return internalUserDbInterface.getDefaultGroup(appName);
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

    return internalUserDbInterface.getUsersList(appName);
  }

  /**
   * Obtain a databaseHandleName
   *
   * @param appName
   * @return dbHandleName
   */
  @Override
  public DbHandle openDatabase(String appName) throws ServicesAvailabilityException {

    return internalUserDbInterface.openDatabase(appName);
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

    internalUserDbInterface.closeDatabase(appName, dbHandleName);
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

    if (!tableId.startsWith("L_")) {
      tableId = "L_" + tableId;
    }

    return internalUserDbInterface
        .createLocalOnlyTableWithColumns(appName, dbHandleName, tableId, columns);
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

    if (!tableId.startsWith("L_")) {
      tableId = "L_" + tableId;
    }

    internalUserDbInterface
        .deleteLocalOnlyTable(appName, dbHandleName, tableId);
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

    if (!tableId.startsWith("L_")) {
      tableId = "L_" + tableId;
    }

    internalUserDbInterface
        .insertLocalOnlyRow(appName, dbHandleName, tableId, rowValues);
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
  public void updateLocalOnlyRow(String appName, DbHandle dbHandleName, String tableId,
                                 ContentValues rowValues, String whereClause, BindArgs bindArgs)
      throws ServicesAvailabilityException {

    if (!tableId.startsWith("L_")) {
      tableId = "L_" + tableId;
    }

    internalUserDbInterface
        .updateLocalOnlyRow(appName, dbHandleName, tableId, rowValues, whereClause,
         bindArgs);
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
  public void deleteLocalOnlyRow(String appName, DbHandle dbHandleName, String tableId,
                                 String whereClause, BindArgs bindArgs)
      throws ServicesAvailabilityException {

    if (!tableId.startsWith("L_")) {
      tableId = "L_" + tableId;
    }

    internalUserDbInterface
          .deleteLocalOnlyRow(appName, dbHandleName, tableId, whereClause, bindArgs);
  }

  /**
   * Get a {@link BaseTable} for this local only table based on the given SQL command. All
   * columns from the table are returned.
   * <p>
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
  @Override
  public BaseTable simpleQueryLocalOnlyTables(String appName, DbHandle dbHandleName, String tableId,
                                              String whereClause, BindArgs bindArgs,
                                              String[] groupBy, String having,
                                              String[] orderByColNames, String[] orderByDirections,
                                              Integer limit, Integer offset)
      throws ServicesAvailabilityException {

    if (!tableId.startsWith("L_")) {
      tableId = "L_" + tableId;
    }

    return simpleQuery(appName, dbHandleName, tableId, whereClause, bindArgs, groupBy, having,
        orderByColNames, orderByDirections, limit, offset);
  }

  /**
   * Get a {@link BaseTable} for the result set of an arbitrary sql query
   * and bind parameters on this local only table.
   * <p>
   * The sql query can be arbitrarily complex and can include joins, unions, etc.
   * The data are returned as string values.
   *
   * @param appName
   * @param dbHandleName
   * @param tableId
   * @param sqlCommand
   * @param bindArgs     an array of primitive values (String, Boolean, int, double) for
   *                     bind parameters
   * @param limit        the maximum number of rows to return (optional)
   * @param offset       the index to start counting the limit from (optional)
   * @return An  {@link BaseTable}. Containing the results of the query
   * @throws ServicesAvailabilityException
   */
  @Override
  public BaseTable arbitrarySqlQueryLocalOnlyTables(String appName, DbHandle dbHandleName,
                                                    String tableId, String sqlCommand,
                                                    BindArgs bindArgs, Integer limit,
                                                    Integer offset)
      throws ServicesAvailabilityException {

    if (!tableId.startsWith("L_")) {
      tableId = "L_" + tableId;
    }

    return arbitrarySqlQuery(appName, dbHandleName, tableId, sqlCommand, bindArgs, limit, offset);
  }

  /**
   * Get a {@link BaseTable} that holds the results from the continued query. Note that this is
   * just a wrapper of resumeSimpleQuery, which could successfully be used instead.
   *
   * @param appName
   * @param dbHandleName
   * @param query        The original query with the bounds adjusted
   * @return
   * @throws ServicesAvailabilityException
   */
  @Override
  public BaseTable resumeSimpleQueryLocalOnlyTables(String appName, DbHandle dbHandleName,
                                                    ResumableQuery query)
      throws ServicesAvailabilityException {

    return resumeSimpleQuery(appName, dbHandleName, query);
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

    internalUserDbInterface
        .privilegedServerTableSchemaETagChanged(appName, dbHandleName, tableId, schemaETag,
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
  @Override
  public String setChoiceList(String appName, DbHandle dbHandleName, String choiceListJSON)
      throws ServicesAvailabilityException {

    return internalUserDbInterface
        .setChoiceList(appName, dbHandleName, choiceListJSON);
  }

  /**
   * Return the choice list JSON corresponding to the choiceListId
   *
   * @param appName
   * @param dbHandleName
   * @param choiceListId -- the md5 hash of the choiceListJSON
   * @return choiceListJSON -- the actual JSON choice list text.
   */
  @Override
  public String getChoiceList(String appName, DbHandle dbHandleName, String choiceListId)
      throws ServicesAvailabilityException {

    return internalUserDbInterface
        .getChoiceList(appName, dbHandleName, choiceListId);
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

    return internalUserDbInterface
        .createOrOpenTableWithColumns(appName, dbHandleName, tableId, columns);
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


    return internalUserDbInterface
          .createOrOpenTableWithColumnsAndProperties(appName, dbHandleName, tableId, columns,
              metaData, clear);
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

    internalUserDbInterface.deleteTableAndAllData(appName, dbHandleName, tableId);
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

    internalUserDbInterface.deleteTableMetadata(appName, dbHandleName, tableId, partition, aspect, key);
  }

  /**
   * Return an array of the admin columns that must be present in
   * every database table.
   *
   * @return
   */
  @Override
  public String[] getAdminColumns() throws ServicesAvailabilityException {

    return internalUserDbInterface.getAdminColumns();
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

    return internalUserDbInterface.getAllColumnNames(appName, dbHandleName, tableId);
  }

  /**
   * Return all the tableIds in the database.
   *
   * @param appName
   * @param dbHandleName
   * @return List<String> of tableIds
   */
  @Override
  public List<String> getAllTableIds(String appName, DbHandle dbHandleName)
      throws ServicesAvailabilityException {

    return internalUserDbInterface.getAllTableIds(appName, dbHandleName);
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
  public TableMetaDataEntries getTableMetadata(String appName, DbHandle dbHandleName,
                                               String tableId, String partition, String aspect,
                                               String key, String revId)
      throws ServicesAvailabilityException {

    TableMetaDataEntries entries = null;

    if (tableId == null) {
      // Ignore the cache, as it only caches table specific metadata
      entries = internalUserDbInterface.getTableMetadata(appName, dbHandleName, tableId, partition, aspect, key);
    } else {
      TableMetaDataEntries allEntries = getMetadata(tableId);

      if (allEntries == null) {
        // If there is no cache hit, fetch from the database
        allEntries = internalUserDbInterface.getTableMetadata(appName, dbHandleName, tableId, null, null, null);
        putMetadata(tableId, allEntries);
      } else {
        // If there is a cache hit, check if it is stale
        TableMetaDataEntries newEntries = internalUserDbInterface
                .getTableMetadataIfChanged(appName, dbHandleName, tableId, allEntries.getRevId());
        String newEntRevId = newEntries.getRevId();
        // We want to update the cache if the condition of the revIds being the
        // same does NOT hold
        if (!(newEntRevId != null && newEntRevId.equals(allEntries.getRevId()))) {
          putMetadata(tableId, newEntries);
          allEntries = newEntries;
        }
      }

      // Filter the requested entries from the full list
      entries = filterEntries(allEntries, partition, aspect, key);
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

    return internalUserDbInterface.getExportColumns();
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

    return internalUserDbInterface.getTableDefinitionEntry(appName, dbHandleName, tableId);
  }

  /**
   * Return the list of all tables and their health status.
   *
   * @param appName
   * @param dbHandleName
   * @return the list of TableHealthInfo records for this appName
   */
  @Override
  public List<TableHealthInfo> getTableHealthStatuses(String appName, DbHandle dbHandleName)
      throws ServicesAvailabilityException {

    return internalUserDbInterface.getTableHealthStatuses(appName, dbHandleName);
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

    return internalUserDbInterface.getUserDefinedColumns(appName, dbHandleName, tableId);
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

    return internalUserDbInterface.hasTableId(appName, dbHandleName, tableId);
  }

  /********** RAW GENERIC QUERIES **********/

  /**
   * Get a {@link BaseTable} for this table based on the given SQL command. All
   * columns from the table are returned.
   * <p>
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
  @Override
  public BaseTable simpleQuery(String appName, DbHandle dbHandleName, String tableId,
                               String whereClause, BindArgs bindArgs, String[] groupBy,
                               String having, String[] orderByColNames, String[] orderByDirections,
                               Integer limit, Integer offset)
      throws ServicesAvailabilityException {

    SimpleQuery query = new SimpleQuery(tableId, bindArgs, whereClause, groupBy,
        having, orderByColNames, orderByDirections, limit, offset);

    BaseTable baseTable = internalUserDbInterface
        .simpleQuery(appName, dbHandleName, query.getSqlCommand(), query.getSqlBindArgs(),
            query.getSqlQueryBounds(), query.getTableId());

    baseTable.setQuery(query);

    return baseTable;
  }

  /**
   * SYNC ONLY
   * <p>
   * Privileged version of above query.
   * <p>
   * Get a {@link BaseTable} for this table based on the given SQL command. All
   * columns from the table are returned.
   * <p>
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
  @Override
  public BaseTable privilegedSimpleQuery(String appName, DbHandle dbHandleName, String tableId,
                                         String whereClause, BindArgs bindArgs, String[] groupBy,
                                         String having, String[] orderByColNames,
                                         String[] orderByDirections, Integer limit, Integer offset)
      throws ServicesAvailabilityException {

    SimpleQuery query = new SimpleQuery(tableId, bindArgs, whereClause, groupBy,
        having, orderByColNames, orderByDirections, limit, offset);

    BaseTable baseTable = internalUserDbInterface
            .privilegedSimpleQuery(appName, dbHandleName, query.getSqlCommand(),
                query.getSqlBindArgs(), query.getSqlQueryBounds(), query.getTableId());

    baseTable.setQuery(query);

    return baseTable;
  }

  /**
   * Get a {@link BaseTable} for the result set of an arbitrary sql query
   * and bind parameters.
   * <p>
   * The sql query can be arbitrarily complex and can include joins, unions, etc.
   * The data are returned as string values.
   *
   * @param appName
   * @param dbHandleName
   * @param tableId
   * @param sqlCommand
   * @param bindArgs     an array of primitive values (String, Boolean, int, double) for
   *                     bind parameters
   * @param limit        the maximum number of rows to return (optional)
   * @param offset       the index to start counting the limit from (optional)
   * @return An  {@link BaseTable}. Containing the results of the query
   */
  @Override
  public BaseTable arbitrarySqlQuery(String appName, DbHandle dbHandleName, String tableId,
                                     String sqlCommand, BindArgs bindArgs, Integer limit,
                                     Integer offset)
      throws ServicesAvailabilityException {

    ArbitraryQuery query = new ArbitraryQuery(tableId, bindArgs, sqlCommand, limit,
        offset);

    BaseTable baseTable = internalUserDbInterface
        .simpleQuery(appName, dbHandleName, query.getSqlCommand(), query.getSqlBindArgs(),
            query.getSqlQueryBounds(), query.getTableId());

    baseTable.setQuery(query);

    return baseTable;
  }

  /**
   * Get a {@link BaseTable} that holds the results from the continued query.
   *
   * @param appName
   * @param dbHandleName
   * @param query        The original query with the bounds adjusted
   * @return
   */
  @Override
  public BaseTable resumeSimpleQuery(String appName, DbHandle dbHandleName, ResumableQuery query)
      throws ServicesAvailabilityException {

    BaseTable baseTable = internalUserDbInterface
        .simpleQuery(appName, dbHandleName, query.getSqlCommand(), query.getSqlBindArgs(),
            query.getSqlQueryBounds(), query.getTableId());

    baseTable.setQuery(query);

    return baseTable;
  }

  /**
   * SYNC ONLY
   * <p>
   * Privileged version of above query.
   * Get a {@link BaseTable} that holds the results from the continued query.
   *
   * @param appName
   * @param dbHandleName
   * @param query        The original query with the bounds adjusted
   * @return
   */
  @Override
  public BaseTable resumePrivilegedSimpleQuery(String appName, DbHandle dbHandleName,
                                               ResumableQuery query)
      throws ServicesAvailabilityException {

    BaseTable baseTable = internalUserDbInterface
            .privilegedSimpleQuery(appName, dbHandleName, query.getSqlCommand(),
                query.getSqlBindArgs(), query.getSqlQueryBounds(), query.getTableId());

    baseTable.setQuery(query);

    return baseTable;
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

    internalUserDbInterface
        .privilegedExecute(appName, dbHandleName, sqlCommand, bindArgs);
  }

  /********** USERTABLE QUERY WRAPPERS **********/

  /**
   * Get a {@link UserTable} for this table based on the given SQL command. All
   * columns from the table are returned.
   * <p>
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
  @Override
  public UserTable simpleQuery(String appName, DbHandle dbHandleName, String tableId,
                               OrderedColumns columnDefns, String whereClause, BindArgs bindArgs,
                               String[] groupBy, String having, String[] orderByColNames,
                               String[] orderByDirections, Integer limit, Integer offset)
      throws ServicesAvailabilityException {

    BaseTable baseTable = simpleQuery(appName, dbHandleName, tableId, whereClause, bindArgs,
        groupBy, having, orderByColNames, orderByDirections, limit, offset);

    return new UserTable(baseTable, columnDefns, internalGetAdminColumns());
  }

  /**
   * SYNC ONLY
   * <p>
   * Privileged version of the above query.
   * Get a {@link UserTable} for this table based on the given SQL command. All
   * columns from the table are returned.
   * <p>
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
  @Override
  public UserTable privilegedSimpleQuery(String appName, DbHandle dbHandleName, String tableId,
                                         OrderedColumns columnDefns, String whereClause,
                                         BindArgs bindArgs, String[] groupBy, String having,
                                         String[] orderByColNames, String[] orderByDirections,
                                         Integer limit, Integer offset)
      throws ServicesAvailabilityException {

    BaseTable baseTable = privilegedSimpleQuery(appName, dbHandleName, tableId, whereClause,
        bindArgs, groupBy, having, orderByColNames, orderByDirections, limit, offset);

    return new UserTable(baseTable, columnDefns, internalGetAdminColumns());
  }

  /**
   * Get a {@link UserTable} for the result set of an arbitrary sql query
   * and bind parameters.
   * <p>
   * The sql query can be arbitrarily complex and can include joins, unions, etc.
   * The data are returned as string values.
   *
   * @param appName
   * @param dbHandleName
   * @param tableId
   * @param sqlCommand
   * @param bindArgs     an array of primitive values (String, Boolean, int, double) for
   *                     bind parameters
   * @param limit        the maximum number of rows to return
   * @param offset       the index to start counting the limit from
   * @return An  {@link BaseTable}. Containing the results of the query
   */
  @Override
  public UserTable arbitrarySqlQuery(String appName, DbHandle dbHandleName, String tableId,
                                     OrderedColumns columnDefns, String sqlCommand,
                                     BindArgs bindArgs, Integer limit, Integer offset)
      throws ServicesAvailabilityException {

    BaseTable baseTable = arbitrarySqlQuery(appName, dbHandleName, tableId, sqlCommand, bindArgs,
        limit, offset);

    return new UserTable(baseTable, columnDefns, internalGetAdminColumns());
  }

  /**
   * Get a {@link UserTable} that holds the results from the continued query.
   *
   * @param appName
   * @param dbHandleName
   * @param columnDefns
   * @param query        The original query with the bounds adjusted
   * @return
   */
  @Override
  public UserTable resumeSimpleQuery(String appName, DbHandle dbHandleName,
                                     OrderedColumns columnDefns, ResumableQuery query)
      throws ServicesAvailabilityException {

    BaseTable baseTable = resumeSimpleQuery(appName, dbHandleName, query);

    return new UserTable(baseTable, columnDefns, internalGetAdminColumns());
  }

  /**
   * SYNC ONLY
   * <p>
   * Privileged version of the above.
   * Get a {@link UserTable} that holds the results from the continued query.
   *
   * @param appName
   * @param dbHandleName
   * @param columnDefns
   * @param query        The original query with the bounds adjusted
   * @return
   */
  @Override
  public UserTable resumePrivilegedSimpleQuery(String appName, DbHandle dbHandleName,
                                               OrderedColumns columnDefns, ResumableQuery query)
      throws ServicesAvailabilityException {

    BaseTable baseTable = resumePrivilegedSimpleQuery(appName, dbHandleName, query);

    return new UserTable(baseTable, columnDefns, internalGetAdminColumns());
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

    internalUserDbInterface.replaceTableMetadata(appName, dbHandleName, entry);
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

    internalUserDbInterface
        .replaceTableMetadataList(appName, dbHandleName, tableId, entries, clear);
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

    internalUserDbInterface
        .replaceTableMetadataSubList(appName, dbHandleName, tableId, partition, aspect, entries);
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

    internalUserDbInterface
        .privilegedUpdateTableETags(appName, dbHandleName, tableId, schemaETag, lastDataETag);
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

    internalUserDbInterface.privilegedUpdateTableLastSyncTime(appName, dbHandleName, tableId);
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

    return internalUserDbInterface.getSyncState(appName, dbHandleName, tableId, rowId);
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

    internalUserDbInterface
        .privilegedUpdateRowETagAndSyncState(appName, dbHandleName, tableId, rowId, rowETag,
              syncState);
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
  @Override
  public UserTable getRowsWithId(String appName, DbHandle dbHandleName, String tableId,
                                 OrderedColumns orderedColumns, String rowId)
      throws ServicesAvailabilityException {

    BaseTable baseTable = internalUserDbInterface
      .getRowsWithId(appName, dbHandleName, tableId, rowId);

    return new UserTable(baseTable, orderedColumns, internalGetAdminColumns());
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
  @Override
  public UserTable privilegedGetRowsWithId(String appName, DbHandle dbHandleName, String tableId,
                                           OrderedColumns orderedColumns, String rowId)
      throws ServicesAvailabilityException {

    BaseTable baseTable = internalUserDbInterface
        .privilegedGetRowsWithId(appName, dbHandleName, tableId, rowId);

    return new UserTable(baseTable, orderedColumns, internalGetAdminColumns());
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
  @Override
  public UserTable getMostRecentRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                          OrderedColumns orderedColumns, String rowId)
      throws ServicesAvailabilityException {

    BaseTable baseTable = internalUserDbInterface
        .getMostRecentRowWithId(appName, dbHandleName, tableId, rowId);

    return new UserTable(baseTable, orderedColumns, internalGetAdminColumns());
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
   * @param orderedColumns
   * @param cvValues       the field values on the server
   * @param rowId
   * @return The rows in the database matching the rowId (may be 0, 1 or 2 rows).
   */
  @Override
  public UserTable privilegedPerhapsPlaceRowIntoConflictWithId(String appName,
                                                               DbHandle dbHandleName,
                                                               String tableId,
                                                               OrderedColumns orderedColumns,
                                                               ContentValues cvValues, String rowId)
      throws ServicesAvailabilityException {

    BaseTable baseTable = internalUserDbInterface
        .privilegedPerhapsPlaceRowIntoConflictWithId(appName, dbHandleName, tableId,
            cvValues, rowId);

    return new UserTable(baseTable, orderedColumns, internalGetAdminColumns());
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
   * @param orderedColumns
   * @param cvValues
   * @param rowId
   * @param asCsvRequestedChange
   * @return single-row table with the content of the inserted row
   */
  @Override
  public UserTable privilegedInsertRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                             OrderedColumns orderedColumns, ContentValues cvValues,
                                             String rowId, boolean asCsvRequestedChange)
      throws ServicesAvailabilityException {

    BaseTable baseTable = internalUserDbInterface
        .privilegedInsertRowWithId(appName, dbHandleName, tableId, cvValues,
            rowId, asCsvRequestedChange);

    return new UserTable(baseTable, orderedColumns, internalGetAdminColumns());
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
  @Override
  public UserTable insertCheckpointRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                             OrderedColumns orderedColumns, ContentValues cvValues,
                                             String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException {

    BaseTable baseTable = internalUserDbInterface
        .insertCheckpointRowWithId(appName, dbHandleName, tableId, cvValues, rowId);

    return new UserTable(baseTable, orderedColumns, internalGetAdminColumns());
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
  @Override
  public UserTable insertRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                   OrderedColumns orderedColumns, ContentValues cvValues,
                                   String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException {

    BaseTable baseTable = internalUserDbInterface
            .insertRowWithId(appName, dbHandleName, tableId, cvValues, rowId);

    return new UserTable(baseTable, orderedColumns, internalGetAdminColumns());
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
  public UserTable deleteAllCheckpointRowsWithId(String appName, DbHandle dbHandleName,
                                                 String tableId, OrderedColumns orderedColumns,
                                                 String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException {

    BaseTable baseTable = internalUserDbInterface
        .deleteAllCheckpointRowsWithId(appName, dbHandleName, tableId, rowId);

    return new UserTable(baseTable, orderedColumns, internalGetAdminColumns());
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
  @Override
  public UserTable deleteLastCheckpointRowWithId(String appName, DbHandle dbHandleName,
                                                 String tableId, OrderedColumns orderedColumns,
                                                 String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException {

    BaseTable baseTable = internalUserDbInterface
        .deleteLastCheckpointRowWithId(appName, dbHandleName, tableId, rowId);

    return new UserTable(baseTable, orderedColumns, internalGetAdminColumns());
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
   * @param orderedColumns
   * @param rowId
   * @return table with the content for this rowId. May be empty. May be marked as deleted and awaiting sync
   */
  @Override
  public UserTable privilegedDeleteRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                             OrderedColumns orderedColumns, String rowId)
      throws ServicesAvailabilityException {

    BaseTable baseTable = internalUserDbInterface
        .privilegedDeleteRowWithId(appName, dbHandleName, tableId, rowId);

    return new UserTable(baseTable, orderedColumns, internalGetAdminColumns());
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
  @Override
  public UserTable deleteRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                   OrderedColumns orderedColumns, String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException {

    BaseTable baseTable = internalUserDbInterface
        .deleteRowWithId(appName, dbHandleName, tableId, rowId);

    return new UserTable(baseTable, orderedColumns, internalGetAdminColumns());
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
  @Override
  public UserTable saveAsIncompleteMostRecentCheckpointRowWithId(String appName,
                                                                 DbHandle dbHandleName,
                                                                 String tableId,
                                                                 OrderedColumns orderedColumns,
                                                                 String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException {

    BaseTable baseTable = internalUserDbInterface
            .saveAsIncompleteMostRecentCheckpointRowWithId(appName, dbHandleName, tableId, rowId);

    return new UserTable(baseTable, orderedColumns, internalGetAdminColumns());
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
  @Override
  public UserTable saveAsCompleteMostRecentCheckpointRowWithId(String appName,
                                                               DbHandle dbHandleName,
                                                               String tableId,
                                                               OrderedColumns orderedColumns,
                                                               String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException {

    BaseTable baseTable = internalUserDbInterface
            .saveAsCompleteMostRecentCheckpointRowWithId(appName, dbHandleName, tableId, rowId);

    return new UserTable(baseTable, orderedColumns, internalGetAdminColumns());
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
  @Override
  public UserTable updateRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                   OrderedColumns orderedColumns, ContentValues cvValues,
                                   String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException {

    BaseTable baseTable = internalUserDbInterface
            .updateRowWithId(appName, dbHandleName, tableId, cvValues, rowId);

    return new UserTable(baseTable, orderedColumns, internalGetAdminColumns());
  }

  /**
   * Client-side wrapper to make changing the row filter easier.
   *
   * @param appName
   * @param dbHandleName
   * @param tableId
   * @param orderedColumns
   * @param defaultAccess
   * @param rowOwner
   * @param rowId
   * @return
   */
  @Override
  public UserTable changeRowFilterWithId(String appName, DbHandle dbHandleName, String tableId,
                                         OrderedColumns orderedColumns, String defaultAccess,
                                         String rowOwner, String groupReadOnly, String groupModify,
                                         String groupPrivileged, String rowId)
      throws IllegalArgumentException, ActionNotAuthorizedException, ServicesAvailabilityException {

    if (defaultAccess == null) {
      throw new IllegalArgumentException("defaultAccess is null");
    }
    // verify that defaultAccess is a known type
    RowFilterScope.Access.valueOf(defaultAccess);

    ContentValues cvValues = new ContentValues();
    cvValues.put(DataTableColumns.DEFAULT_ACCESS, defaultAccess);

    if (rowOwner == null) {
      cvValues.putNull(DataTableColumns.ROW_OWNER);
    } else {
      cvValues.put(DataTableColumns.ROW_OWNER, rowOwner);
    }

    if (groupReadOnly == null) {
      cvValues.putNull(DataTableColumns.GROUP_READ_ONLY);
    } else {
      cvValues.put(DataTableColumns.GROUP_READ_ONLY, groupReadOnly);
    }

    if (groupModify == null) {
      cvValues.putNull(DataTableColumns.GROUP_MODIFY);
    } else {
      cvValues.put(DataTableColumns.GROUP_MODIFY, groupModify);
    }

    if (groupPrivileged == null) {
      cvValues.putNull(DataTableColumns.GROUP_PRIVILEGED);
    } else {
      cvValues.put(DataTableColumns.GROUP_PRIVILEGED, groupPrivileged);
    }

    BaseTable baseTable = internalUserDbInterface.updateRowWithId(appName, dbHandleName, tableId, cvValues, rowId);

    return new UserTable(baseTable, orderedColumns, internalGetAdminColumns());
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
    internalUserDbInterface.resolveServerConflictWithDeleteRowWithId(appName, dbHandleName,
        tableId, rowId);
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
    internalUserDbInterface.resolveServerConflictTakeLocalRowWithId(appName, dbHandleName,
        tableId, rowId);
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
    internalUserDbInterface.resolveServerConflictTakeLocalRowPlusServerDeltasWithId(appName,
        dbHandleName, tableId, cvValues, rowId);
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
    internalUserDbInterface.resolveServerConflictTakeServerRowWithId(appName, dbHandleName,
        tableId, rowId);
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
    internalUserDbInterface.deleteAppAndTableLevelManifestSyncETags(appName, dbHandleName);
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
    internalUserDbInterface.deleteAllSyncETagsForTableId(appName, dbHandleName, tableId);
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
    internalUserDbInterface.deleteAllSyncETagsExceptForServer(appName, dbHandleName, verifiedUri);
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
    internalUserDbInterface.deleteAllSyncETagsUnderServer(appName, dbHandleName, verifiedUri);
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
    return internalUserDbInterface.getFileSyncETag(appName, dbHandleName, verifiedUri, tableId,
        modificationTimestamp);
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
    return internalUserDbInterface.getManifestSyncETag(appName, dbHandleName, verifiedUri, tableId);
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
    internalUserDbInterface.updateFileSyncETag(appName, dbHandleName, verifiedUri, tableId,
        modificationTimestamp, eTag);
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
    internalUserDbInterface.updateManifestSyncETag(appName, dbHandleName, verifiedUri, tableId, eTag);
  }
}

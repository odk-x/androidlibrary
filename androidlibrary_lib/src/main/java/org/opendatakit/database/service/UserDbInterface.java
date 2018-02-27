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
import org.opendatakit.database.data.BaseTable;
import org.opendatakit.database.data.ColumnList;
import org.opendatakit.database.data.KeyValueStoreEntry;
import org.opendatakit.database.data.OrderedColumns;
import org.opendatakit.database.data.TableDefinitionEntry;
import org.opendatakit.database.data.TableMetaDataEntries;
import org.opendatakit.database.data.UserTable;
import org.opendatakit.database.queries.BindArgs;
import org.opendatakit.database.queries.ResumableQuery;
import org.opendatakit.exception.ActionNotAuthorizedException;
import org.opendatakit.exception.ServicesAvailabilityException;

import java.util.List;

public interface UserDbInterface {

  /**
   * Return the active user or "anonymous" if the user
   * has not been authenticated against the server.
   *
   * @param appName
   * @return the user reported from the server or "anonymous" if
   * server authentication has not been completed.
   */
  String getActiveUser(String appName) throws ServicesAvailabilityException;

  /**
   * Return the roles and groups of a verified username or google account.
   * If the username or google account have not been verified,
   * or if the server settings specify to use an anonymous user,
   * then return an empty string.
   *
   * @param appName
   * @return null or JSON serialization of an array of ROLES. See RoleConsts for possible values.
   */
  String getRolesList(String appName) throws ServicesAvailabilityException;

  /**
   * Return the default group of the current user.
   *
   * @param appName
   * @return null or the name of the default group for this user.
   * @throws ServicesAvailabilityException
   */
  String getDefaultGroup(String appName) throws ServicesAvailabilityException;

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
  String getUsersList(String appName) throws ServicesAvailabilityException;

  /**
   * Obtain a databaseHandleName
   *
   * @param appName
   * @return dbHandleName
   */
  DbHandle openDatabase(String appName) throws ServicesAvailabilityException;

  /**
   * Release the databaseHandle. Will roll back any outstanding transactions
   * and release/close the database handle.
   *
   * @param appName
   * @param dbHandleName
   */
  void closeDatabase(String appName, DbHandle dbHandleName) throws ServicesAvailabilityException;

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
  OrderedColumns createLocalOnlyTableWithColumns(String appName, DbHandle dbHandleName,
                                                 String tableId, ColumnList columns)
      throws ServicesAvailabilityException;

  /**
   * Drop the given local only table
   *
   * @param appName
   * @param dbHandleName
   * @param tableId
   * @throws ServicesAvailabilityException
   */
  void deleteLocalOnlyTable(String appName, DbHandle dbHandleName, String tableId)
      throws ServicesAvailabilityException;

  /**
   * Insert rows into a local only table
   *
   * @param appName
   * @param dbHandleName
   * @param tableId
   * @param rowValues
   * @throws ServicesAvailabilityException
   */
  void insertLocalOnlyRow(String appName, DbHandle dbHandleName, String tableId,
                          ContentValues rowValues) throws ServicesAvailabilityException;

  /**
   * Update rows in a local only table
   *
   * @param appName
   * @param dbHandleName
   * @param tableId
   * @param rowValues
   * @param whereClause
   * @param bindArgs
   * @throws ServicesAvailabilityException
   */
  void updateLocalOnlyRows(String appName, DbHandle dbHandleName, String tableId,
                          ContentValues rowValues, String whereClause, BindArgs bindArgs)
      throws ServicesAvailabilityException;

  /**
   * Delete rows in a local only table
   *
   * @param appName
   * @param dbHandleName
   * @param tableId
   * @param whereClause
   * @param bindArgs
   * @throws ServicesAvailabilityException
   */
  void deleteLocalOnlyRows(String appName, DbHandle dbHandleName, String tableId, String whereClause,
                          BindArgs bindArgs) throws ServicesAvailabilityException;

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
  BaseTable simpleQueryLocalOnlyTables(String appName, DbHandle dbHandleName, String tableId,
                                       String whereClause, BindArgs bindArgs, String[] groupBy,
                                       String having, String[] orderByColNames,
                                       String[] orderByDirections, Integer limit, Integer offset)
      throws ServicesAvailabilityException;

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
  BaseTable arbitrarySqlQueryLocalOnlyTables(String appName, DbHandle dbHandleName, String tableId,
                                             String sqlCommand, BindArgs bindArgs, Integer limit,
                                             Integer offset)
      throws ServicesAvailabilityException;

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
  BaseTable resumeSimpleQueryLocalOnlyTables(String appName, DbHandle dbHandleName,
                                             ResumableQuery query)
      throws ServicesAvailabilityException;

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
  void privilegedServerTableSchemaETagChanged(String appName, DbHandle dbHandleName, String tableId,
                                              String schemaETag, String tableInstanceFilesUri)
      throws ServicesAvailabilityException;

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
  String setChoiceList(String appName, DbHandle dbHandleName, String choiceListJSON)
      throws ServicesAvailabilityException;

  /**
   * Return the choice list JSON corresponding to the choiceListId
   *
   * @param appName
   * @param dbHandleName
   * @param choiceListId -- the md5 hash of the choiceListJSON
   * @return choiceListJSON -- the actual JSON choice list text.
   */
  String getChoiceList(String appName, DbHandle dbHandleName, String choiceListId)
      throws ServicesAvailabilityException;

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
  OrderedColumns createOrOpenTableWithColumns(String appName, DbHandle dbHandleName, String tableId,
                                              ColumnList columns)
      throws ServicesAvailabilityException;

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
  OrderedColumns createOrOpenTableWithColumnsAndProperties(String appName, DbHandle dbHandleName,
                                                           String tableId, ColumnList columns,
                                                           List<KeyValueStoreEntry> metaData,
                                                           boolean clear)
      throws ServicesAvailabilityException;

  /**
   * Drop the given tableId and remove all the files (both configuration and
   * data attachments) associated with that table.
   *
   * @param appName
   * @param dbHandleName
   * @param tableId
   */
  void deleteTableAndAllData(String appName, DbHandle dbHandleName, String tableId)
      throws ServicesAvailabilityException;

  /**
   * Rescan the config directory tree of the given tableId and update the forms table
   * with revised information from the formDef.json files that it contains.
   *
   * @param appName
   * @param dbHandleName
   * @param tableId
   * @return true if there were no problems
   */
  boolean rescanTableFormDefs(String appName, DbHandle dbHandleName, String tableId)
      throws ServicesAvailabilityException;

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
  void deleteTableMetadata(String appName, DbHandle dbHandleName, String tableId, String partition,
                           String aspect, String key) throws ServicesAvailabilityException;

  /**
   * Return an array of the admin columns that must be present in
   * every database table.
   *
   * @return
   */
  String[] getAdminColumns() throws ServicesAvailabilityException;

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
  String[] getAllColumnNames(String appName, DbHandle dbHandleName, String tableId)
      throws ServicesAvailabilityException;

  /**
   * Return all the tableIds in the database.
   *
   * @param appName
   * @param dbHandleName
   * @return List<String> of tableIds
   */
  List<String> getAllTableIds(String appName, DbHandle dbHandleName)
      throws ServicesAvailabilityException;

  /**
   * @param appName
   * @param dbHandleName
   * @param tableId
   * @param partition
   * @param aspect
   * @param key
   * @return list of KeyValueStoreEntry values matching the filter criteria
   */
  TableMetaDataEntries getTableMetadata(String appName, DbHandle dbHandleName, String tableId,
                                        String partition, String aspect, String key, String revId)
      throws ServicesAvailabilityException;

  /**
   * Return an array of the admin columns that should be exported to
   * a CSV file. This list excludes the SYNC_STATE and CONFLICT_TYPE columns.
   *
   * @return
   */
  String[] getExportColumns() throws ServicesAvailabilityException;

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
  TableDefinitionEntry getTableDefinitionEntry(String appName, DbHandle dbHandleName,
                                               String tableId) throws ServicesAvailabilityException;

  /**
   * Return the list of all tables and their health status.
   *
   * @param appName
   * @param dbHandleName
   * @return the list of TableHealthInfo records for this appName
   */
  List<TableHealthInfo> getTableHealthStatuses(String appName, DbHandle dbHandleName)
      throws ServicesAvailabilityException;

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
  OrderedColumns getUserDefinedColumns(String appName, DbHandle dbHandleName, String tableId)
      throws ServicesAvailabilityException;

  /**
   * Verifies that the tableId exists in the database.
   *
   * @param appName
   * @param dbHandleName
   * @param tableId
   * @return true if table is listed in table definitions.
   */
  boolean hasTableId(String appName, DbHandle dbHandleName, String tableId)
      throws ServicesAvailabilityException;

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
  BaseTable simpleQuery(String appName, DbHandle dbHandleName, String tableId, String whereClause,
                        BindArgs bindArgs, String[] groupBy, String having,
                        String[] orderByColNames, String[] orderByDirections, Integer limit,
                        Integer offset)
      throws ServicesAvailabilityException;

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
  BaseTable privilegedSimpleQuery(String appName, DbHandle dbHandleName, String tableId,
                                  String whereClause, BindArgs bindArgs, String[] groupBy,
                                  String having, String[] orderByColNames,
                                  String[] orderByDirections, Integer limit, Integer offset)
      throws ServicesAvailabilityException;

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
  BaseTable arbitrarySqlQuery(String appName, DbHandle dbHandleName, String tableId,
                              String sqlCommand, BindArgs bindArgs, Integer limit, Integer offset)
      throws ServicesAvailabilityException;

  /**
   * Get a {@link BaseTable} that holds the results from the continued query.
   *
   * @param appName
   * @param dbHandleName
   * @param query        The original query with the bounds adjusted
   * @return
   */
  BaseTable resumeSimpleQuery(String appName, DbHandle dbHandleName, ResumableQuery query)
      throws ServicesAvailabilityException;

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
  BaseTable resumePrivilegedSimpleQuery(String appName, DbHandle dbHandleName, ResumableQuery query)
      throws ServicesAvailabilityException;

  /**
   * Execute an arbitrary command with bind parameters.
   *
   * @param appName
   * @param dbHandleName
   * @param sqlCommand
   * @param bindArgs     an array of primitive values (String, Boolean, int, double) for
   *                     bind parameters
   */
  void privilegedExecute(String appName, DbHandle dbHandleName, String sqlCommand,
                         BindArgs bindArgs) throws ServicesAvailabilityException;

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
  UserTable simpleQuery(String appName, DbHandle dbHandleName, String tableId,
                        OrderedColumns columnDefns, String whereClause, BindArgs bindArgs,
                        String[] groupBy, String having, String[] orderByColNames,
                        String[] orderByDirections, Integer limit, Integer offset)
      throws ServicesAvailabilityException;

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
  UserTable privilegedSimpleQuery(String appName, DbHandle dbHandleName, String tableId,
                                  OrderedColumns columnDefns, String whereClause, BindArgs bindArgs,
                                  String[] groupBy, String having, String[] orderByColNames,
                                  String[] orderByDirections, Integer limit, Integer offset)
      throws ServicesAvailabilityException;

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
  UserTable arbitrarySqlQuery(String appName, DbHandle dbHandleName, String tableId,
                              OrderedColumns columnDefns, String sqlCommand, BindArgs bindArgs,
                              Integer limit, Integer offset)
      throws ServicesAvailabilityException;

  /**
   * Get a {@link UserTable} that holds the results from the continued query.
   *
   * @param appName
   * @param dbHandleName
   * @param columnDefns
   * @param query        The original query with the bounds adjusted
   * @return
   */
  UserTable resumeSimpleQuery(String appName, DbHandle dbHandleName, OrderedColumns columnDefns,
                              ResumableQuery query)
      throws ServicesAvailabilityException;

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
  UserTable resumePrivilegedSimpleQuery(String appName, DbHandle dbHandleName,
                                        OrderedColumns columnDefns, ResumableQuery query)
      throws ServicesAvailabilityException;

  /**
   * Insert or update a single table-level metadata KVS entry.
   *
   * @param appName
   * @param dbHandleName
   * @param entry
   */
  void replaceTableMetadata(String appName, DbHandle dbHandleName, KeyValueStoreEntry entry)
      throws ServicesAvailabilityException;

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
  void replaceTableMetadataList(String appName, DbHandle dbHandleName, String tableId,
                                List<KeyValueStoreEntry> entries, boolean clear)
      throws ServicesAvailabilityException;

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
  void replaceTableMetadataSubList(String appName, DbHandle dbHandleName, String tableId,
                                   String partition, String aspect,
                                   List<KeyValueStoreEntry> entries)
      throws ServicesAvailabilityException;

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
  void privilegedUpdateTableETags(String appName, DbHandle dbHandleName, String tableId,
                                  String schemaETag, String lastDataETag)
      throws ServicesAvailabilityException;

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
  void privilegedUpdateTableLastSyncTime(String appName, DbHandle dbHandleName, String tableId)
      throws ServicesAvailabilityException;

  /**
   * @param appName
   * @param dbHandleName
   * @param tableId
   * @param rowId
   * @return the sync state of the row (use SyncState.valueOf() to reconstruct), or null if the
   * row does not exist.
   */
  String getSyncState(String appName, DbHandle dbHandleName, String tableId, String rowId)
      throws ServicesAvailabilityException;

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
  void privilegedUpdateRowETagAndSyncState(String appName, DbHandle dbHandleName, String tableId,
                                           String rowId, String rowETag, String syncState)
      throws ServicesAvailabilityException;

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
  UserTable getRowsWithId(String appName, DbHandle dbHandleName, String tableId,
                          OrderedColumns orderedColumns, String rowId)
      throws ServicesAvailabilityException;

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
  UserTable privilegedGetRowsWithId(String appName, DbHandle dbHandleName, String tableId,
                                    OrderedColumns orderedColumns, String rowId)
      throws ServicesAvailabilityException;

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
  UserTable getMostRecentRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                   OrderedColumns orderedColumns, String rowId)
      throws ServicesAvailabilityException;

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
  UserTable privilegedPerhapsPlaceRowIntoConflictWithId(String appName, DbHandle dbHandleName,
                                                        String tableId,
                                                        OrderedColumns orderedColumns,
                                                        ContentValues cvValues, String rowId)
      throws ServicesAvailabilityException;

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
  UserTable privilegedInsertRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                      OrderedColumns orderedColumns, ContentValues cvValues,
                                      String rowId, boolean asCsvRequestedChange)
      throws ServicesAvailabilityException;

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
  UserTable insertCheckpointRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                      OrderedColumns orderedColumns, ContentValues cvValues,
                                      String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException;

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
  UserTable insertRowWithId(String appName, DbHandle dbHandleName, String tableId,
                            OrderedColumns orderedColumns, ContentValues cvValues, String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException;

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
  UserTable deleteAllCheckpointRowsWithId(String appName, DbHandle dbHandleName, String tableId,
                                          OrderedColumns orderedColumns, String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException;

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
  UserTable deleteLastCheckpointRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                          OrderedColumns orderedColumns, String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException;

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
  UserTable privilegedDeleteRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                      OrderedColumns orderedColumns, String rowId)
      throws ServicesAvailabilityException;

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
  UserTable deleteRowWithId(String appName, DbHandle dbHandleName, String tableId,
                            OrderedColumns orderedColumns, String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException;

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
  UserTable saveAsIncompleteMostRecentCheckpointRowWithId(String appName, DbHandle dbHandleName,
                                                          String tableId,
                                                          OrderedColumns orderedColumns,
                                                          String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException;

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
  UserTable saveAsCompleteMostRecentCheckpointRowWithId(String appName, DbHandle dbHandleName,
                                                        String tableId,
                                                        OrderedColumns orderedColumns, String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException;

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
  UserTable updateRowWithId(String appName, DbHandle dbHandleName, String tableId,
                            OrderedColumns orderedColumns, ContentValues cvValues, String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException;

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
  UserTable changeRowFilterWithId(String appName, DbHandle dbHandleName, String tableId,
                                  OrderedColumns orderedColumns, String defaultAccess,
                                  String rowOwner, String groupReadOnly, String groupModify,
                                  String groupPrivileged, String rowId)
      throws IllegalArgumentException, ActionNotAuthorizedException, ServicesAvailabilityException;

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
  void resolveServerConflictWithDeleteRowWithId(String appName, DbHandle dbHandleName,
                                                String tableId, String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException;

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
  void resolveServerConflictTakeLocalRowWithId(String appName, DbHandle dbHandleName,
                                               String tableId, String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException;

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
  void resolveServerConflictTakeLocalRowPlusServerDeltasWithId(String appName,
                                                               DbHandle dbHandleName,
                                                               String tableId,
                                                               ContentValues cvValues, String rowId)
      throws ActionNotAuthorizedException, ServicesAvailabilityException;

  /**
   * Resolve the server conflict by taking the server changes.  This may delete the local row.
   *
   * @param appName
   * @param dbHandleName
   * @param tableId
   * @param rowId
   */
  void resolveServerConflictTakeServerRowWithId(String appName, DbHandle dbHandleName,
                                                String tableId, String rowId)
      throws ServicesAvailabilityException;

  /**
   * Remove app and table level manifests. Invoked when we select reset configuration
   * and the initialization task is executed.
   *
   * @param appName
   * @param dbHandleName
   */
  void deleteAppAndTableLevelManifestSyncETags(String appName, DbHandle dbHandleName)
      throws ServicesAvailabilityException;

  /**
   * Forget the document ETag values for the given tableId on all servers.
   * Used when deleting a table. Exposed mainly for integration testing.
   *
   * @param appName
   * @param dbHandleName
   * @param tableId
   */
  void deleteAllSyncETagsForTableId(String appName, DbHandle dbHandleName, String tableId)
      throws ServicesAvailabilityException;

  /**
   * Forget the document ETag values for everything except the specified Uri.
   * Call this when the server URI we are syncing against has changed.
   *
   * @param appName
   * @param dbHandleName
   * @param verifiedUri  (e.g., https://opendatakit-tablesdemo.appspot.com)
   */
  void deleteAllSyncETagsExceptForServer(String appName, DbHandle dbHandleName, String verifiedUri)
      throws ServicesAvailabilityException;

  /**
   * Forget the document ETag values for everything under the specified Uri.
   *
   * @param appName
   * @param dbHandleName
   * @param verifiedUri  (e.g., https://opendatakit-tablesdemo.appspot.com)
   */
  void deleteAllSyncETagsUnderServer(String appName, DbHandle dbHandleName, String verifiedUri)
      throws ServicesAvailabilityException;

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
  String getFileSyncETag(String appName, DbHandle dbHandleName, String verifiedUri, String tableId,
                         long modificationTimestamp) throws ServicesAvailabilityException;

  /**
   * Get the document ETag values for the given manifest under the specified Uri.
   *
   * @param appName
   * @param dbHandleName
   * @param verifiedUri  (e.g., https://opendatakit-tablesdemo.appspot.com)
   * @param tableId      (null if an application-level manifest)
   */
  String getManifestSyncETag(String appName, DbHandle dbHandleName, String verifiedUri,
                             String tableId) throws ServicesAvailabilityException;

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
  void updateFileSyncETag(String appName, DbHandle dbHandleName, String verifiedUri, String tableId,
                          long modificationTimestamp, String eTag)
      throws ServicesAvailabilityException;

  /**
   * Update the document ETag values for the given manifest under the specified Uri.
   *
   * @param appName
   * @param dbHandleName
   * @param verifiedUri  (e.g., https://opendatakit-tablesdemo.appspot.com)
   * @param tableId      (null if an application-level manifest)
   * @param eTag
   */
  void updateManifestSyncETag(String appName, DbHandle dbHandleName, String verifiedUri,
                              String tableId, String eTag) throws ServicesAvailabilityException;

}

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
import org.opendatakit.database.queries.BindArgs;
import org.opendatakit.database.queries.QueryBounds;
import org.opendatakit.exception.ActionNotAuthorizedException;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.sqlite.database.sqlite.SQLiteException;

import java.util.List;

/**
 * Interface that is implemented inside ODK Services then wrapped in the Aidl DbChunk
 * and Exception pass-through interface, and later un-wrapped on the client side.
 *
 * @author mitchellsundt@gmail.com
 */

public interface InternalUserDbInterface {
   String getActiveUser(String appName)
       throws IllegalStateException, IllegalArgumentException,
       ServicesAvailabilityException;

   String getRolesList(String appName)
       throws IllegalStateException, IllegalArgumentException,
       ServicesAvailabilityException;

   String getDefaultGroup(String appName)
       throws IllegalStateException, IllegalArgumentException,
       ServicesAvailabilityException;

   String getUsersList(String appName)
       throws IllegalStateException, IllegalArgumentException,
       ServicesAvailabilityException;

   DbHandle openDatabase(String appName)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   void closeDatabase(String appName, DbHandle dbHandleName)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   OrderedColumns createLocalOnlyTableWithColumns(String appName, DbHandle dbHandleName,
                                                  String tableId, ColumnList columns)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   void deleteLocalOnlyTable(String appName, DbHandle dbHandleName, String tableId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   void insertLocalOnlyRow(String appName, DbHandle dbHandleName, String tableId,
                           ContentValues rowValues)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   void updateLocalOnlyRows(String appName, DbHandle dbHandleName, String tableId,
                           ContentValues rowValues, String whereClause, BindArgs bindArgs)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   void deleteLocalOnlyRows(String appName, DbHandle dbHandleName, String tableId,
                           String whereClause, BindArgs bindArgs)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   void privilegedServerTableSchemaETagChanged(String appName, DbHandle dbHandleName,
                                               String tableId, String schemaETag,
                                               String tableInstanceFilesUri)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   String setChoiceList(String appName, DbHandle dbHandleName, String choiceListJSON)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   String getChoiceList(String appName, DbHandle dbHandleName, String choiceListId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   OrderedColumns createOrOpenTableWithColumns(String appName, DbHandle dbHandleName,
                                               String tableId, ColumnList columns)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   OrderedColumns createOrOpenTableWithColumnsAndProperties(String appName, DbHandle dbHandleName,
                                                            String tableId, ColumnList columns,
                                                            List<KeyValueStoreEntry> metaData,
                                                            boolean clear)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   void deleteTableAndAllData(String appName, DbHandle dbHandleName, String tableId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   boolean rescanTableFormDefs(String appName, DbHandle dbHandleName, String tableId)
        throws IllegalStateException, IllegalArgumentException, SQLiteException,
        ServicesAvailabilityException;

   void deleteTableMetadata(String appName, DbHandle dbHandleName, String tableId, String partition,
                            String aspect, String key)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   String[] getAdminColumns()
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   String[] getAllColumnNames(String appName, DbHandle dbHandleName, String tableId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   List<String> getAllTableIds(String appName, DbHandle dbHandleName)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   TableMetaDataEntries getTableMetadata(String appName, DbHandle dbHandleName, String tableId,
                                         String partition, String aspect, String key)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   TableMetaDataEntries getTableMetadataIfChanged(String appName, DbHandle dbHandleName,
                                                  String tableId, String revId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   String[] getExportColumns()
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   TableDefinitionEntry getTableDefinitionEntry(String appName, DbHandle dbHandleName,
                                                String tableId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   TableHealthInfo getTableHealthStatus(String appName, DbHandle dbHandleName, String tableId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   List<TableHealthInfo> getTableHealthStatuses(String appName, DbHandle dbHandleName)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   OrderedColumns getUserDefinedColumns(String appName, DbHandle dbHandleName, String tableId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   boolean hasTableId(String appName, DbHandle dbHandleName, String tableId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   BaseTable simpleQuery(String appName, DbHandle dbHandleName, String sqlCommand,
                         BindArgs bindArgs, QueryBounds sqlQueryBounds, String tableId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   BaseTable privilegedSimpleQuery(String appName, DbHandle dbHandleName, String sqlCommand,
                                   BindArgs bindArgs, QueryBounds sqlQueryBounds, String tableId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   void privilegedExecute(String appName, DbHandle dbHandleName, String sqlCommand,
                          BindArgs bindArgs)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   void replaceTableMetadata(String appName, DbHandle dbHandleName, KeyValueStoreEntry entry)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   void replaceTableMetadataList(String appName, DbHandle dbHandleName, String tableId,
                                 List<KeyValueStoreEntry> entries, boolean clear)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   void replaceTableMetadataSubList(String appName, DbHandle dbHandleName, String tableId,
                                    String partition, String aspect,
                                    List<KeyValueStoreEntry> entries)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   void privilegedUpdateTableETags(String appName, DbHandle dbHandleName, String tableId,
                                   String schemaETag, String lastDataETag)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   void privilegedUpdateTableLastSyncTime(String appName, DbHandle dbHandleName, String tableId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   String getSyncState(String appName, DbHandle dbHandleName, String tableId, String rowId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   void privilegedUpdateRowETagAndSyncState(String appName, DbHandle dbHandleName, String tableId,
                                            String rowId, String rowETag, String syncState)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   BaseTable getRowsWithId(String appName, DbHandle dbHandleName, String tableId, String rowId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   BaseTable privilegedGetRowsWithId(String appName, DbHandle dbHandleName, String tableId,
                                     String rowId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   BaseTable getMostRecentRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                    String rowId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   BaseTable privilegedPerhapsPlaceRowIntoConflictWithId(String appName, DbHandle dbHandleName,
                                                         String tableId, ContentValues cvValues,
                                                         String rowId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   BaseTable privilegedInsertRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                       ContentValues cvValues, String rowId,
                                       boolean asCsvRequestedChange)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   BaseTable insertCheckpointRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                       ContentValues cvValues, String rowId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ActionNotAuthorizedException, ServicesAvailabilityException;

   BaseTable insertRowWithId(String appName, DbHandle dbHandleName, String tableId,
                             ContentValues cvValues, String rowId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ActionNotAuthorizedException, ServicesAvailabilityException;

   BaseTable deleteAllCheckpointRowsWithId(String appName, DbHandle dbHandleName, String tableId,
                                           String rowId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ActionNotAuthorizedException, ServicesAvailabilityException;

   BaseTable deleteLastCheckpointRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                           String rowId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ActionNotAuthorizedException, ServicesAvailabilityException;

   BaseTable privilegedDeleteRowWithId(String appName, DbHandle dbHandleName, String tableId,
                                       String rowId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   BaseTable deleteRowWithId(String appName, DbHandle dbHandleName, String tableId, String rowId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ActionNotAuthorizedException, ServicesAvailabilityException;

   BaseTable saveAsIncompleteMostRecentCheckpointRowWithId(String appName, DbHandle dbHandleName,
                                                           String tableId, String rowId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ActionNotAuthorizedException, ServicesAvailabilityException;

   BaseTable saveAsCompleteMostRecentCheckpointRowWithId(String appName, DbHandle dbHandleName,
                                                         String tableId, String rowId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ActionNotAuthorizedException, ServicesAvailabilityException;

   BaseTable updateRowWithId(String appName, DbHandle dbHandleName, String tableId,
                             ContentValues cvValues, String rowId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ActionNotAuthorizedException, ServicesAvailabilityException;

   void resolveServerConflictWithDeleteRowWithId(String appName, DbHandle dbHandleName,
                                                 String tableId, String rowId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ActionNotAuthorizedException, ServicesAvailabilityException;

   void resolveServerConflictTakeLocalRowWithId(String appName, DbHandle dbHandleName,
                                                String tableId, String rowId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ActionNotAuthorizedException, ServicesAvailabilityException;

   void resolveServerConflictTakeLocalRowPlusServerDeltasWithId(String appName,
                                                                DbHandle dbHandleName,
                                                                String tableId,
                                                                ContentValues cvValues,
                                                                String rowId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ActionNotAuthorizedException, ServicesAvailabilityException;

   void resolveServerConflictTakeServerRowWithId(String appName, DbHandle dbHandleName,
                                                 String tableId, String rowId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   void deleteAppAndTableLevelManifestSyncETags(String appName, DbHandle dbHandleName)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   void deleteAllSyncETagsForTableId(String appName, DbHandle dbHandleName, String tableId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   void deleteAllSyncETagsExceptForServer(String appName, DbHandle dbHandleName, String verifiedUri)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   void deleteAllSyncETagsUnderServer(String appName, DbHandle dbHandleName, String verifiedUri)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   String getFileSyncETag(String appName, DbHandle dbHandleName, String verifiedUri, String tableId,
                          long modificationTimestamp)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   String getManifestSyncETag(String appName, DbHandle dbHandleName, String verifiedUri,
                              String tableId)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   void updateFileSyncETag(String appName, DbHandle dbHandleName, String verifiedUri,
                           String tableId, long modificationTimestamp, String eTag)
       throws IllegalStateException, IllegalArgumentException, SQLiteException,
       ServicesAvailabilityException;

   void updateManifestSyncETag(String appName, DbHandle dbHandleName, String verifiedUri,
                               String tableId, String eTag)
       throws IllegalStateException, IllegalArgumentException,
       SQLiteException, ServicesAvailabilityException;
}

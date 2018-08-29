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
package org.opendatakit.sync.service.logic;

import org.opendatakit.sync.service.entity.ParcelableChangeSetList;
import org.opendatakit.sync.service.entity.ParcelableColumn;
import org.opendatakit.sync.service.entity.ParcelablePrivilegesInfo;
import org.opendatakit.sync.service.entity.ParcelableRowOutcomeList;
import org.opendatakit.sync.service.entity.ParcelableRowResourceList;
import org.opendatakit.sync.service.entity.ParcelableTableDefinitionResource;
import org.opendatakit.sync.service.entity.ParcelableTableResource;
import org.opendatakit.sync.service.entity.ParcelableTableResourceList;
import org.opendatakit.sync.service.entity.ParcelableUserInfoList;
import org.opendatakit.sync.service.logic.FileManifestDocument;
import org.opendatakit.sync.service.logic.CommonFileAttachmentTerms;
import org.opendatakit.database.data.OrderedColumns;
import org.opendatakit.database.data.TypedRow;
import org.opendatakit.sync.service.SyncAttachmentState;
import org.opendatakit.sync.service.SyncProgressState;

import android.net.Uri;

import java.util.List;
import java.util.Map;

/**
 * Synchronizer abstracts synchronization of tables to an external cloud/server.
 *
 * This is a lower-level interface with somewhat atomic interactions with the
 * remote server and the database. Higher level interactions are managed in the
 * various Process... classes.
 *
 * @author the.dylan.price@gmail.com
 * @author sudar.sam@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
interface IAidlSynchronizer {
  /**
   * Verifies that the device's application name is supported by the server.
   *
   * @throws HttpClientWebException
   * @throws IOException
   */
  void verifyServerSupportsAppName();

  /**
   * Returns null OR an object containing:
   * {
   * "roles" : list of the roles and groups assigned to the user,
   * "defaultGroup" : defaultGroup the user belongs to
   * }
   * <p>
   * The server requires that the user be authenticated during this call. By
   * using the local context on all subsequent interactions, we are able to
   * use the negotiated identity when accessing subsequent APIs.
   * <p>
   * The defaultGroup can be null and, if not null, must appear in the list
   * of roles and groups assigned to the user.
   * <p>
   * If null is returned, then the user is either anonymous or the server is old.
   * In either case, the user should be treated as an anonymous user.
   *
   * @return
   * @throws HttpClientWebException
   * @throws IOException
   */
  ParcelablePrivilegesInfo getUserRolesAndDefaultGroup();

  /**
   * If this user is a registered user with Tables Super-user, Administer Tables,
   * or Site Administrator privileges, this returns the list of all users configured
   * on the server and their roles (including ourselves). If this user is a registered
   * user without those privileges, it returns a singleton list with information about
   * this user. If the device is using anonymousUser access to the server, an empty
   * list is returned.
   *
   * @return
   * @throws HttpClientWebException
   * @throws IOException
   */
  ParcelableUserInfoList getUsers();

  /**
   * Construct the URI for fetching the app-level config files manifest.
   *
   * @return
   */
  Uri constructAppLevelFileManifestUri();

  /**
   * Construct the URI for fetching the tableId's config files manifest.
   *
   * @param tableId
   * @return
   */
  Uri constructTableLevelFileManifestUri(in String tableId);

  /**
   * Construct the URI for the Realized Table (tableId & schemaETag)
   *
   * @param tableId
   * @param schemaETag
   * @return
   */
  Uri constructRealizedTableIdUri(in String tableId, in String schemaETag);

  /**
   * Construct the URI for fetching the manifest for the row-level attachments of a given rowId
   *
   * @param serverInstanceFileUri
   * @param rowId
   * @return
   */
  Uri constructInstanceFileManifestUri(in String serverInstanceFileUri, in String rowId);

  /**
   * Get a list of all tables in the server.
   *
   * @param webSafeResumeCursor null or a non-empty string if we are issuing a resume query
   * @return a list of the table resources on the server
   * @throws HttpClientWebException
   * @throws IOException
   */
  ParcelableTableResourceList getTables(in String webSafeResumeCursor);

  /**
   * Get info about the table in the server.
   *
   * @param tableId
   * @return the table resource for this tableId on the server
   * @throws HttpClientWebException
   * @throws IOException
   */
  ParcelableTableResource getTable(in String tableId);

  /**
   * Discover the schema for a table resource.
   *
   * @param tableDefinitionUri
   * @return the table definition
   * @throws HttpClientWebException
   * @throws IOException
   */
  ParcelableTableDefinitionResource getTableDefinition(in String tableDefinitionUri);

  /**
   * Assert that a table with the given id and schema exists on the server.
   *
   * @param tableId    the unique identifier of the table
   * @param schemaETag the current schemaETag for the table
   * @param columns    an array of the columns for this table.
   * @return the TableResource for the table (the server may return different
   * SyncTag values)
   * @throws HttpClientWebException
   * @throws IOException
   */
  ParcelableTableResource createTable(in String tableId, in String schemaETag, in List<ParcelableColumn> columns);

  /**
   * Delete the table with the given id from the server.
   *
   * @param table the realizedTable resource to delete
   * @throws HttpClientWebException
   * @throws IOException
   */
  void deleteTable(in ParcelableTableResource table);

  /**
   * Retrieve the changeSets applied after the changeSet with the specified dataETag
   *
   * @param tableResource
   * @param dataETag
   * @return
   * @throws HttpClientWebException
   * @throws IOException
   */
  ParcelableChangeSetList getChangeSets(in ParcelableTableResource tableResource, in String dataETag);

  /**
   * Retrieve the change set for the indicated dataETag
   *
   * @param tableResource
   * @param dataETag
   * @param activeOnly
   * @param websafeResumeCursor
   * @return
   * @throws HttpClientWebException
   * @throws IOException
   */
  ParcelableRowResourceList getChangeSet(in ParcelableTableResource tableResource, in String dataETag, boolean activeOnly,
                               in String websafeResumeCursor);

  /**
   * Retrieve changes in the server state since the last synchronization.
   *
   * @param tableResource       the TableResource from the server for a tableId
   * @param dataETag            tracks the last dataETag successfully pulled into
   *                            the local data table. Fetches changes after that dataETag.
   * @param websafeResumeCursor either null or a value used to resume a prior query.
   * @param fetchLimit          the number of rows that should be returned in one chunk.
   *                            Used to limit memory usage during sync and presumably improve
   *                            performance.
   * @return an RowResourceList of the changes on the server since that dataETag.
   * @throws HttpClientWebException
   * @throws IOException
   */
  ParcelableRowResourceList getUpdates(in ParcelableTableResource tableResource, in String dataETag,
                             in String websafeResumeCursor, int fetchLimit);

  /**
   * Apply inserts, updates and deletes in a collection up to the server.
   * This depends upon knowing the current dataETag of the server.
   * The dataETag for the changeSet made by this call is returned to the
   * caller via the RowOutcomeList.
   *
   * @param tableResource              the TableResource from the server for a tableId
   * @param orderedColumns
   * @param rowsToInsertUpdateOrDelete
   * @return null if the server's dataETag is different than ours and we need to re-pull server
   * changes. Otherwise, returns RowOutcomeList with the row-by-row results.
   * @throws HttpClientWebException
   * @throws IOException
   */
  ParcelableRowOutcomeList pushLocalRows(in ParcelableTableResource tableResource, in OrderedColumns orderedColumns, in List<String> rowsToInsertUpdateOrDelete);

  /**
   * Request the app-level manifest. This uses a NOT_MODIFIED header to detect
   * not-changed status. However, it does not update that value. The caller is
   * expected to update the ETag after they have made the device match the
   * content reported by the server (or vice-versa on a push).
   *
   * @param lastKnownLocalAppLevelManifestETag the app-level file manifest etag we last knew of.
   * @param serverReportedAppLevelETag         the app-level file manifest etag found in TableResourceList
   * @param pushLocalFiles
   * @return
   * @throws HttpClientWebException
   * @throws IOException
   */
  FileManifestDocument getAppLevelFileManifest(in String lastKnownLocalAppLevelManifestETag, in String serverReportedAppLevelETag,
                                               boolean pushLocalFiles);

  /**
   * Get the config manifest for a given tableId. This uses a NOT_MODIFIED header to detect
   * not-changed status. However, it does not update that value. The caller is
   * expected to update the ETag after they have made the device match the
   * content reported by the server (or vice-versa on a push).
   *
   * @param tableId
   * @param lastKnownLocalTableLevelManifestETag the table-level file manifest etag we last knew.
   * @param serverReportedTableLevelETag         the table-level file manifest etag found in
   *                                             TableResourceList
   * @param pushLocalFiles
   * @return
   * @throws IOException
   * @throws HttpClientWebException
   */
  FileManifestDocument getTableLevelFileManifest(in String tableId, in String lastKnownLocalTableLevelManifestETag,
                                                 in String serverReportedTableLevelETag, boolean pushLocalFiles);

  /**
   * Get the manifest for the row attachments for the given tableId and row (instance) Id.
   * Use the attachmentState and localRowAttachmentHash to qualify the ETag to use.
   * <p>
   * This uses a NOT_MODIFIED header to detect
   * not-changed status. However, it does not update that value. The caller is
   * expected to update the ETag after they have made the device match the
   * content reported by the server (or vice-versa on a push).
   *
   * @param serverInstanceFileUri
   * @param tableId
   * @param instanceId
   * @param attachmentState                    that we are trying to enforce.
   * @param lastKnownLocalRowLevelManifestETag the eTag for the manifest we last had
   * @return
   * @throws HttpClientWebException
   * @throws IOException
   */
  FileManifestDocument getRowLevelFileManifest(in String serverInstanceFileUri, in String tableId, in String instanceId,
                                               in SyncAttachmentState attachmentState, in String lastKnownLocalRowLevelManifestETag);

  /**
   * Download a file from the given Uri and store it in the destFile.
   *
   * @param destFile
   * @param downloadUrl
   * @throws HttpClientWebException
   * @throws IOException
   */
  void downloadFile(in Uri destFile, in Uri downloadUrl);

  /**
   * Delete the given config file on the server.
   *
   * @param localFile
   * @throws HttpClientWebException
   * @throws IOException
   */
  void deleteConfigFile(in Uri localFile);

  /**
   * @param localFile
   * @return
   * @throws HttpClientWebException
   * @throws IOException
   */
  void uploadConfigFile(in Uri localFile);

  /**
   * @param file
   * @param instanceFileUri
   * @throws HttpClientWebException
   * @throws IOException
   */
  void uploadInstanceFile(in Uri file, in Uri instanceFileUri);

  /**
   * @param serverInstanceFileUri
   * @param tableId
   * @param instanceId
   * @param rowpathUri
   * @return
   */
  CommonFileAttachmentTerms createCommonFileAttachmentTerms(in String serverInstanceFileUri, in String tableId, in String instanceId,
                                                            in String rowpathUri);

  /**
   * @param batch
   * @param serverInstanceFileUri
   * @param instanceId
   * @param tableId
   * @throws HttpClientWebException
   * @throws IOException
   */
  void uploadInstanceFileBatch(in List<CommonFileAttachmentTerms> batch, in String serverInstanceFileUri,
                               in String instanceId, in String tableId);

  /**
   * @param filesToDownload
   * @param serverInstanceFileUri
   * @param instanceId
   * @param tableId
   * @throws HttpClientWebException
   * @throws IOException
   */
  void downloadInstanceFileBatch(in List<CommonFileAttachmentTerms> filesToDownload,
                                 in String serverInstanceFileUri, in String instanceId, in String tableId);
}

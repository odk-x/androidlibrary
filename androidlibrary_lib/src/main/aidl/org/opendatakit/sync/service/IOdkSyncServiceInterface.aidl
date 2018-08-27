/*
 * Copyright (C) 2014 University of Washington
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
package org.opendatakit.sync.service;

import org.opendatakit.sync.service.SyncStatus;
import org.opendatakit.sync.service.SyncProgressEvent;
import org.opendatakit.sync.service.SyncAttachmentState;
import org.opendatakit.sync.service.SyncOverallResult;

interface IOdkSyncServiceInterface {

  /**
   * Verify the server settings and obtain the user's permissions (roles) on that server.
   *
   * Returns true if an asynchronous process to confirm the device's server settings was started.
   * Returns false otherwise (e.g., there is already a sync or verify in progress).
   *
   * The actual range of outcomes is a subset of those resulting from a full-on sync.
   */
  boolean verifyServerSettings(in String appName);

  /**
   * Return the current SyncStatus for this appName.
   * NONE -- there is no active or completed sync
   */
  SyncStatus getSyncStatus(in String appName);

  /**
   * Return the most recent sync progress event.
   * If the sync is complete, this will have a SyncProgressState.FINISHED status
   */
  SyncProgressEvent getSyncProgressEvent(in String appName);

  /**
   * Only valid if the SyncStatus is a neither NONE nor SYNCING
   */
  SyncOverallResult getSyncResult(in String appName);

  /**
   * initiate a synchronization action.
   * If there is an already-running or completed-but-not-cleared synchronization action,
   * then this request will be ignored (and it will return false).
   *
   * @return true if the action was begun.
   *         false if an earlier sync is in progress (and this request was ignored).
   */
  boolean synchronizeWithServer(in String appName, in SyncAttachmentState syncAttachments);
	
  /**
   * initiate a push of the local config to the server followed by a synchronization action.
   * If there is an already-running or completed-but-not-cleared synchronization action,
   * then this request will be ignored (and it will return false).
   *
   * @return true if the action was begun.
   *         false if an earlier sync is in progress (and this request was ignored).
   */
  boolean resetServer(in String appName, in SyncAttachmentState syncAttachments);

  /**
   * If there is a completed sync for this appName, clear it.
   * You must clear any recently-completed sync before you can initiate a new action.
   */
  boolean clearAppSynchronizer(String appName);
}

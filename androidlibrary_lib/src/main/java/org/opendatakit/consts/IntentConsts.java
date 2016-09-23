/*
 * Copyright (C) 2015 University of Washington
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

package org.opendatakit.consts;

public class IntentConsts {
  
  public static final String INTENT_KEY_APP_NAME = "appName";
  public static final String INTENT_KEY_TABLE_ID = "tableId";
  public static final String INTENT_KEY_INSTANCE_ID = "instanceId";
  // for rowpath and/or configpath
  public static final String INTENT_KEY_URI_FRAGMENT = "uriFragment";
  public static final String INTENT_KEY_CONTENT_TYPE = "contentType";
  public static final String INTENT_KEY_SAVED_URI = "savedUri";

  public static final String INTENT_KEY_SETTINGS_IN_ADMIN_MODE = "adminMode";
  public static final String INTENT_KEY_SETTINGS_ADMIN_ENABLED = "adminEnabled";
  /**
   * Intent Extras:
   * <ol><li>INTENT_KEY_APP_NAME</li>
   * <li>INTENT_KEY_TABLE_ID</li>
   * <li>INTENT_KEY_INSTANCE_ID (optional)</li></ol>
   */
  public class ResolveCheckpoint {
    public static final String APPLICATION_NAME = "org.opendatakit.services";
    public static final String ACTIVITY_NAME =
        "org.opendatakit.services.resolve.checkpoint.CheckpointResolutionActivity";
  }

  /**
   * Intent Extras:
   * <ol><li>INTENT_KEY_APP_NAME</li>
   * <li>INTENT_KEY_TABLE_ID</li>
   * <li>INTENT_KEY_INSTANCE_ID (optional)</li></ol>
   */
  public class ResolveConflict {
    public static final String APPLICATION_NAME = "org.opendatakit.services";
    public static final String ACTIVITY_NAME =
        "org.opendatakit.services.resolve.conflict.ConflictResolutionActivity";
  }

  /**
   * Intent Extras:
   * <ol><li>INTENT_KEY_APP_NAME</li></ol>
   */
  public class Sync {
    public static final String APPLICATION_NAME = "org.opendatakit.services";
    public static final String ACTIVITY_NAME =
        "org.opendatakit.services.sync.activities.SyncActivity";
    public static final String SYNC_SERVICE_PACKAGE = "org.opendatakit.services";
    public static final String SYNC_SERVICE_CLASS = "org.opendatakit.services.sync.service.OdkSyncService";
  }

  /**
   * Intent Extras:
   * <ol><li>INTENT_KEY_APP_NAME</li></ol>
   */
  public class AppProperties {
    public static final String APPLICATION_NAME = "org.opendatakit.services";
    public static final String ACTIVITY_NAME =
        "org.opendatakit.services.preferences.activities.AppPropertiesActivity";
  }

  public class Database {
    public static final String DATABASE_SERVICE_PACKAGE = "org.opendatakit.services";
    public static final String DATABASE_SERVICE_CLASS = "org.opendatakit.services.database.service.OdkDatabaseService";
  }

  public class Survey {
    public static final String SURVEY_PACKAGE_NAME = "org.opendatakit.survey";
    public static final String SURVEY_MAIN_MENU_ACTIVITY_COMPONENT_NAME = "org.opendatakit.survey"
        + ".activities.SplashScreenActivity";
  }
}

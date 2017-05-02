/*
 * Copyright (C) 2017 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opendatakit.provider;

import android.net.Uri;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.opendatakit.activities.IAppAwareActivity;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.utilities.ODKFileUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Utility class to construct a well-formed Survey URI for launching survey.
 *
 * @author mitchellsundt@gmail.com
 */

public class FormsProviderUtils {
   private static final String TAG = "FormsProviderUtils";

   public static final String URL_ENCODED_SPACE = "%20";

   /**
    * A url-style query param that is used when constructing a survey intent.
    * It specifies the instance id that should be opened. An unrecognized
    * instanceId will add a new instance with that id.
    */
   private static final String URI_SURVEY_QUERY_PARAM_INSTANCE_ID = "instanceId";
   /**
    * A url-style query param that encodes to what position within a form Survey
    * should open. Can be ignored.
    */
   private static final String URI_SURVEY_QUERY_PARAM_SCREEN_PATH = "screenPath";

   /**
    * @param appName required.
    * @param tableId required.
    * @param formId  may be null. If null, screenPath and elementKeyToStringifiedValue must be null
    * @param instanceId may be null.
    * @param screenPath may be null.
    * @param elementKeyToValueMap may be null or empty. Contents are elementKey -to-
    *                                        value for that elementKey. Used to
    *                                        initialized field values and session variables.
    * @return URI for this survey and its arguments.
    */
   public static String constructSurveyUri(String appName, String tableId, String formId,
       String instanceId, String screenPath, Map<String,Object> elementKeyToValueMap)  {
      if (tableId == null) {
         WebLogger.getLogger(appName).e(TAG, "constructSurveyUri: tableId cannot be null returning.");
         return null;
      }
      if (formId == null && screenPath != null) {
         WebLogger.getLogger(appName).e(TAG, "constructSurveyUri: screenPath cannot be "
             + "specified if formId is null. returning.");
         return null;
      }
      if (formId == null &&
          (elementKeyToValueMap != null &&
           !elementKeyToValueMap.isEmpty())) {
         WebLogger.getLogger(appName).e(TAG, "constructSurveyUri: jsonMap cannot be "
             + "specified if formId is null. returning.");
         return null;
      }

      // if no formId is specified, let ODK Survey choose the default form to use on this tableId.
      if ( formId == null ) {
         formId = "";
      }

      Uri uri = Uri.withAppendedPath(
          Uri.withAppendedPath(
              Uri.withAppendedPath(FormsProviderAPI.CONTENT_URI, appName),
              tableId), formId);

      String uriStr = uri.toString() + "/" + "#";
      // Now we need to add our fragment parameters (client-side parsing).
      try {
         String continueStr = "";

         if ( instanceId != null && instanceId.length() != 0) {
            uriStr += URI_SURVEY_QUERY_PARAM_INSTANCE_ID + "=" + URLEncoder
                .encode(instanceId, ApiConstants.UTF8_ENCODE);
            continueStr = "&";
         }
         if (screenPath != null && screenPath.length() != 0) {
            uriStr += continueStr + URI_SURVEY_QUERY_PARAM_SCREEN_PATH + "="
                + URLEncoder.encode(screenPath, ApiConstants.UTF8_ENCODE);
            continueStr = "&";
         }
         if (elementKeyToValueMap != null && !elementKeyToValueMap.isEmpty()) {
            // We'll add all the entries to the URI as key-value pairs.
            // Use a StringBuilder in case we have a lot of these.
            // We'll assume we already have parameters added to the frame. This is
            // reasonable because this URI call thus far insists on an instanceId,
            // so
            // we know there will be at least that parameter.
            StringBuilder stringBuilder = new StringBuilder(uriStr);
            for (Map.Entry<String, Object> objEntry : elementKeyToValueMap.entrySet()) {
               // First add the ampersand
               stringBuilder.append(continueStr);
               continueStr = "&";
               stringBuilder.append(URLEncoder.encode(objEntry.getKey(), ApiConstants.UTF8_ENCODE));
               stringBuilder.append("=");
               // JSON stringify the value then URL encode it.
               // We've got to replace the plus with %20, which is what js's
               // decodeURIComponent expects.
               String escapedValue = URLEncoder.encode(
                   ODKFileUtils.mapper.writeValueAsString(objEntry.getValue()),
                   ApiConstants.UTF8_ENCODE)
                   .replace("+", URL_ENCODED_SPACE);
               stringBuilder.append(escapedValue);
            }
            uriStr = stringBuilder.toString();
         }
      } catch (UnsupportedEncodingException e) {
         WebLogger.getLogger(appName).printStackTrace(e);
         throw new IllegalArgumentException("error escaping URI parameters");
      } catch (JsonProcessingException e) {
         WebLogger.getLogger(appName).printStackTrace(e);
         throw new IllegalArgumentException("error escaping elementKeyToValueMap parameter");
      }
      WebLogger.getLogger(appName).d(TAG, "constructSurveyUri: " + uriStr);
      return uriStr;
   }
}

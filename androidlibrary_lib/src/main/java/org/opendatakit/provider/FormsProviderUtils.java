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
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.utilities.ODKFileUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Utility class to construct a well-formed Survey URI for launching survey.
 *
 * @author mitchellsundt@gmail.com
 */
@SuppressWarnings("WeakerAccess")
public final class FormsProviderUtils {
   private static final String TAG = FormsProviderUtils.class.getSimpleName();

   /**
    * Do not instantiate this class
    */
   private FormsProviderUtils() {
   }

   public static class ParsedFragment {
      public final String instanceId;
      public final String screenPath;
      public final String auxillaryHash;

      ParsedFragment(String instanceId, String screenPath, String auxillaryHash) {
         this.instanceId = instanceId;
         this.screenPath = screenPath;
         this.auxillaryHash = auxillaryHash;
      }
   }

   static final String URL_ENCODED_SPACE = "%20";

   /**
    * A url-style query param that is used when constructing a survey intent.
    * It specifies the instance id that should be opened. An unrecognized
    * instanceId will add a new instance with that id.
    */
   static final String URI_SURVEY_QUERY_PARAM_INSTANCE_ID = "instanceId";
   /**
    * A url-style query param that encodes to what position within a form Survey
    * should open. Can be ignored.
    */
   static final String URI_SURVEY_QUERY_PARAM_SCREEN_PATH = "screenPath";

   /**
    * Safely encode a value known to be a string for inclusion in the auxillaryHash
    * @param unquotedStringValue an unencoded string
    * @return that string but urlencoded
    */
   public static String encodeFragmentUnquotedStringValue(String unquotedStringValue)
       throws UnsupportedEncodingException {

      return URLEncoder.encode(unquotedStringValue, ApiConstants.UTF8_ENCODE).replace("+",
          URL_ENCODED_SPACE);
   }

   /**
    * Safely encode an object value for inclusion in the auxillaryHash
    * @param value an object to encode
    * @return that object but json encoded then urlencoded
    */
   public static String encodeFragmentObjectValue(Object value)
       throws JsonProcessingException, UnsupportedEncodingException {

      String jsonValue = ODKFileUtils.mapper.writeValueAsString(value);

      return encodeFragmentUnquotedStringValue(jsonValue);
   }

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
      if (formId == null && elementKeyToValueMap != null &&
       !elementKeyToValueMap.isEmpty()) {
         WebLogger.getLogger(appName).e(TAG, "constructSurveyUri: jsonMap cannot be "
             + "specified if formId is null. returning.");
         return null;
      }

      // if no formId is specified, let ODK Survey choose the default form to use on this tableId.
      if ( formId == null ) {
         formId = "_";
      }

      Uri uri = Uri.withAppendedPath(
          Uri.withAppendedPath(
              Uri.withAppendedPath(FormsProviderAPI.CONTENT_URI, appName),
              tableId), formId);

      String uriStr = uri + "/" + "#";
      // Now we need to add our fragment parameters (client-side parsing).
      try {
         String continueStr = "";

         if ( instanceId != null && !instanceId.isEmpty()) {
            uriStr += URI_SURVEY_QUERY_PARAM_INSTANCE_ID + "=" +
                encodeFragmentUnquotedStringValue(instanceId);
            continueStr = "&";
         }
         if (screenPath != null && !screenPath.isEmpty()) {
            uriStr += continueStr + URI_SURVEY_QUERY_PARAM_SCREEN_PATH + "="
                + encodeFragmentUnquotedStringValue(screenPath);
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
               stringBuilder.append(encodeFragmentUnquotedStringValue(objEntry.getKey()));
               stringBuilder.append("=");
               // JSON stringify the value then URL encode it.
               // We've got to replace the plus with %20, which is what js's
               // decodeURIComponent expects.
               String escapedValue = encodeFragmentObjectValue(objEntry.getValue());
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

   public static ParsedFragment parseUri(Uri uri) throws UnsupportedEncodingException {
      String instanceId = null;
      String screenPath = null;
      String auxillaryHash = null;

      // NOTE: uri.getFragment() un-escapes everything before returning the string
      // which is EXACTLY the wrong thing to do.
      String uriString = uri.toString();
      String fragment = "";
      int idxHash = uriString.indexOf("#");
      if ( idxHash >= 0 ) {
         fragment = uriString.substring(idxHash + 1);
      }
      if (!fragment.isEmpty()) {
         // and process the fragment to find the instanceId, screenPath and other
         // kv pairs
         String[] pargs = fragment.split("&");
         boolean first = true;
         StringBuilder b = new StringBuilder();
         int i;
         for (i = 0; i < pargs.length; ++i) {
            String[] keyValue = pargs[i].split("=");
            if ("instanceId".equals(keyValue[0])) {
               if (keyValue.length == 2) {
                  instanceId = URLDecoder.decode(keyValue[1], ApiConstants.UTF8_ENCODE);
               }
            } else if ("screenPath".equals(keyValue[0])) {
               if (keyValue.length == 2) {
                  screenPath = URLDecoder.decode(keyValue[1], ApiConstants.UTF8_ENCODE);
               }
            } else {
               // Ignore it if keyValue[0] is refId or formPath
               if (!("refId".equals(keyValue[0]) || "formPath".equals(keyValue[0]))) {
                  if (!first) {
                     b.append("&");
                  }
                  first = false;
                  b.append(pargs[i]);
               }
            }
         }
         String aux = b.toString();
         if (aux.length() != 0) {
            auxillaryHash = aux;
         }
      }

      return new ParsedFragment(instanceId, screenPath, auxillaryHash);
   }
}

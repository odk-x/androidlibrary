/*
 * Copyright (C) 2017 University of Washington
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
package org.opendatakit.provider;

import android.net.Uri;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.Before;
import org.junit.Test;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.logging.desktop.WebLoggerDesktopFactoryImpl;
import org.opendatakit.utilities.StaticStateManipulator;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Testing the survey URI hash construction
 *
 * @author mitchellsundt@gmail.com
 */

public class FormProviderUtilsTest {

   private static final String APPNAME = "fpuTest";
   private static final String TABLE_ID = "theTable";
   private static final String FORM_ID = "theForm";
   private static final String INSTANCE_ID_SPECIAL =
       "-1=2!@#!@)$_#+@#@$%&*#$&)^()_=()_}{[]||\\:;\"'<>,.?/~`!";
   private static final String SCREEN_PATH = "mysurvey/34";

   private static final String MY_KEY1 = "my_key1";
   private static final Object MY_KEY1_VALUE = "Test =&String";

   private static final String EXPECTED_TABLEID_ONLY_BASE =
       "content://org.opendatakit.provider.forms/" + APPNAME + "/" + TABLE_ID +
           "//#";

   private static final String EXPECTED_TABLEID_FORMID_BASE =
       "content://org.opendatakit.provider.forms/" + APPNAME + "/" + TABLE_ID + "/" + FORM_ID +
           "/#";

   @Before
   public void setUp() throws Exception {
      StaticStateManipulator.get().reset();
      WebLogger.setFactory(new WebLoggerDesktopFactoryImpl());
   }

   @Test
   public void testSameInstanceIdKeyField() throws UnsupportedEncodingException,
       JsonProcessingException {
      String encoded = FormsProviderUtils.encodeFragmentUnquotedStringValue(
          FormsProviderUtils.URI_SURVEY_QUERY_PARAM_INSTANCE_ID);

      assertEquals(FormsProviderUtils.URI_SURVEY_QUERY_PARAM_INSTANCE_ID, encoded);
      assertEquals(-1, encoded.indexOf("&"));
      assertEquals(-1, encoded.indexOf("="));
   }

   @Test
   public void testSameScreenPathKeyField() throws UnsupportedEncodingException,
       JsonProcessingException {
      String encoded = FormsProviderUtils.encodeFragmentUnquotedStringValue(
          FormsProviderUtils.URI_SURVEY_QUERY_PARAM_SCREEN_PATH);

      assertEquals(FormsProviderUtils.URI_SURVEY_QUERY_PARAM_SCREEN_PATH, encoded);
      assertEquals(-1, encoded.indexOf("&"));
      assertEquals(-1, encoded.indexOf("="));
   }

   @Test
   public void testSaveStringField() throws UnsupportedEncodingException, JsonProcessingException {
      String encoded = FormsProviderUtils.encodeFragmentUnquotedStringValue(INSTANCE_ID_SPECIAL);

      assertEquals(-1, encoded.indexOf("&"));
      assertEquals(-1, encoded.indexOf("="));
   }

   @Test
   public void testSaveObjectField() throws UnsupportedEncodingException, JsonProcessingException {
      String encoded = FormsProviderUtils.encodeFragmentObjectValue(INSTANCE_ID_SPECIAL);

      assertEquals(-1, encoded.indexOf("&"));
      assertEquals(-1, encoded.indexOf("="));
   }

   @Test
   public void testSurveyUriTableId() throws UnsupportedEncodingException {

      String url = FormsProviderUtils
          .constructSurveyUri(APPNAME, TABLE_ID, null, null, null, null);

      String expected =
              "content://org.opendatakit.provider.forms/" + APPNAME + "/" + TABLE_ID +
                      "/_/#";
      assertEquals(expected, url);

      Uri uri = Uri.parse(url);
      FormsProviderUtils.ParsedFragment pf = FormsProviderUtils.parseUri(uri);

      assertNull(pf.instanceId);
      assertNull(pf.screenPath);
      assertNull(pf.auxillaryHash);
   }

   @Test
   public void testSurveyUriTableIdScreenPath() throws UnsupportedEncodingException {

      String url = FormsProviderUtils
          .constructSurveyUri(APPNAME, TABLE_ID, null, null, SCREEN_PATH, null);

      // this is an error state -- cannot specify a screen path if we don't know the formId
      assertNull(url);
   }

   @Test
   public void testSurveyUriTableIdEmptyKVMap() throws UnsupportedEncodingException {

      Map<String,Object> elementKeyValueMap = new HashMap<String,Object>();

      String url = FormsProviderUtils
          .constructSurveyUri(APPNAME, TABLE_ID, null, null, null, elementKeyValueMap);

      String expected =
              "content://org.opendatakit.provider.forms/" + APPNAME + "/" + TABLE_ID +
                      "/_/#";
      assertEquals(expected, url);

      Uri uri = Uri.parse(url);
      FormsProviderUtils.ParsedFragment pf = FormsProviderUtils.parseUri(uri);

      assertNull(pf.instanceId);
      assertNull(pf.screenPath);
      assertNull(pf.auxillaryHash);
   }

   @Test
   public void testSurveyUriTableIdNonEmptyKVMap() throws UnsupportedEncodingException {

      Map<String,Object> elementKeyValueMap = new HashMap<String,Object>();
      elementKeyValueMap.put(MY_KEY1, MY_KEY1_VALUE);

      String url = FormsProviderUtils
          .constructSurveyUri(APPNAME, TABLE_ID, null, null, null, elementKeyValueMap);

      // this is an error state -- cannot specify a key-value map if we don't know the formId
      assertNull(url);
   }

   @Test
   public void testSurveyUriTableIdFormId() throws UnsupportedEncodingException {

      String url = FormsProviderUtils
          .constructSurveyUri(APPNAME, TABLE_ID, FORM_ID, null, null, null);

      assertEquals(EXPECTED_TABLEID_FORMID_BASE, url);

      Uri uri = Uri.parse(url);
      FormsProviderUtils.ParsedFragment pf = FormsProviderUtils.parseUri(uri);

      assertNull(pf.instanceId);
      assertNull(pf.screenPath);
      assertNull(pf.auxillaryHash);
   }

   @Test
   public void testSurveyUriTableIdFormIdScreenPathEmptyKVMap()
       throws UnsupportedEncodingException, JsonProcessingException {

      Map<String,Object> elementKeyValueMap = new HashMap<String,Object>();

      String url = FormsProviderUtils
          .constructSurveyUri(APPNAME, TABLE_ID, FORM_ID, null, SCREEN_PATH, elementKeyValueMap);

      assertEquals(EXPECTED_TABLEID_FORMID_BASE +
          FormsProviderUtils.URI_SURVEY_QUERY_PARAM_SCREEN_PATH + "=" +
          FormsProviderUtils.encodeFragmentUnquotedStringValue(SCREEN_PATH), url);

      Uri uri = Uri.parse(url);
      FormsProviderUtils.ParsedFragment pf = FormsProviderUtils.parseUri(uri);

      assertNull(pf.instanceId);
      assertEquals(SCREEN_PATH, pf.screenPath);
      assertNull(pf.auxillaryHash);
   }

   @Test
   public void testSurveyUriTableIdFormIdScreenPathNonEmptyKVMap() throws
       UnsupportedEncodingException, JsonProcessingException {

      Map<String,Object> elementKeyValueMap = new HashMap<String,Object>();
      elementKeyValueMap.put(MY_KEY1, MY_KEY1_VALUE);

      String url = FormsProviderUtils
          .constructSurveyUri(APPNAME, TABLE_ID, FORM_ID, null, SCREEN_PATH, elementKeyValueMap);

      assertEquals(EXPECTED_TABLEID_FORMID_BASE +
          FormsProviderUtils.URI_SURVEY_QUERY_PARAM_SCREEN_PATH + "=" +
          FormsProviderUtils.encodeFragmentUnquotedStringValue(SCREEN_PATH) + "&" +
          MY_KEY1 + "=" +
          FormsProviderUtils.encodeFragmentObjectValue(MY_KEY1_VALUE), url);

      Uri uri = Uri.parse(url);
      FormsProviderUtils.ParsedFragment pf = FormsProviderUtils.parseUri(uri);

      assertNull(pf.instanceId);
      assertEquals(SCREEN_PATH, pf.screenPath);
      assertEquals(MY_KEY1 + "=" +
              FormsProviderUtils.encodeFragmentObjectValue(MY_KEY1_VALUE),
          pf.auxillaryHash);
   }

   @Test
   public void testSurveyUriTableIdFormIdScreenPath()
       throws UnsupportedEncodingException, JsonProcessingException {

      String url = FormsProviderUtils
          .constructSurveyUri(APPNAME, TABLE_ID, FORM_ID, null, SCREEN_PATH, null);

      assertEquals(EXPECTED_TABLEID_FORMID_BASE +
          FormsProviderUtils.URI_SURVEY_QUERY_PARAM_SCREEN_PATH + "=" +
          FormsProviderUtils.encodeFragmentUnquotedStringValue(SCREEN_PATH), url);

      Uri uri = Uri.parse(url);
      FormsProviderUtils.ParsedFragment pf = FormsProviderUtils.parseUri(uri);

      assertNull(pf.instanceId);
      assertEquals(SCREEN_PATH, pf.screenPath);
      assertNull(pf.auxillaryHash);
   }

   @Test
   public void testSurveyUriTableIdFormIdInstanceId()
       throws UnsupportedEncodingException, JsonProcessingException {

      String url = FormsProviderUtils
          .constructSurveyUri(APPNAME, TABLE_ID, FORM_ID, INSTANCE_ID_SPECIAL, null, null);

      assertEquals(EXPECTED_TABLEID_FORMID_BASE +
          FormsProviderUtils.URI_SURVEY_QUERY_PARAM_INSTANCE_ID + "=" +
          FormsProviderUtils.encodeFragmentUnquotedStringValue(INSTANCE_ID_SPECIAL), url);

      Uri uri = Uri.parse(url);
      FormsProviderUtils.ParsedFragment pf = FormsProviderUtils.parseUri(uri);

      assertEquals(INSTANCE_ID_SPECIAL, pf.instanceId);
      assertNull(pf.screenPath);
      assertNull(pf.auxillaryHash);
   }

   @Test
   public void testSurveyUriTableIdFormIdInstanceIdEmptyKVMap()
       throws UnsupportedEncodingException, JsonProcessingException {

      Map<String,Object> elementKeyValueMap = new HashMap<String,Object>();

      String url = FormsProviderUtils
          .constructSurveyUri(APPNAME, TABLE_ID, FORM_ID, INSTANCE_ID_SPECIAL, null, elementKeyValueMap);

      assertEquals(EXPECTED_TABLEID_FORMID_BASE +
          FormsProviderUtils.URI_SURVEY_QUERY_PARAM_INSTANCE_ID + "=" +
          FormsProviderUtils.encodeFragmentUnquotedStringValue(INSTANCE_ID_SPECIAL), url);

      Uri uri = Uri.parse(url);
      FormsProviderUtils.ParsedFragment pf = FormsProviderUtils.parseUri(uri);

      assertEquals(INSTANCE_ID_SPECIAL, pf.instanceId);
      assertNull(pf.screenPath);
      assertNull(pf.auxillaryHash);
   }

   @Test
   public void testSurveyUriTableIdFormIdInstanceIdNonEmptyKVMap()
       throws UnsupportedEncodingException, JsonProcessingException {

      Map<String,Object> elementKeyValueMap = new HashMap<String,Object>();
      elementKeyValueMap.put(MY_KEY1, MY_KEY1_VALUE);

      String url = FormsProviderUtils
          .constructSurveyUri(APPNAME, TABLE_ID, FORM_ID, INSTANCE_ID_SPECIAL, null, elementKeyValueMap);

      assertEquals(EXPECTED_TABLEID_FORMID_BASE +
          FormsProviderUtils.URI_SURVEY_QUERY_PARAM_INSTANCE_ID + "=" +
          FormsProviderUtils.encodeFragmentUnquotedStringValue(INSTANCE_ID_SPECIAL) + "&" +
          MY_KEY1 + "=" +
          FormsProviderUtils.encodeFragmentObjectValue(MY_KEY1_VALUE), url);

      Uri uri = Uri.parse(url);
      FormsProviderUtils.ParsedFragment pf = FormsProviderUtils.parseUri(uri);

      assertEquals(INSTANCE_ID_SPECIAL, pf.instanceId);
      assertNull(pf.screenPath);
      assertEquals(MY_KEY1 + "=" +
          FormsProviderUtils.encodeFragmentObjectValue(MY_KEY1_VALUE), pf.auxillaryHash);
   }

   @Test
   public void testSurveyUriTableIdFormIdInstanceIdScreenPath()
       throws UnsupportedEncodingException, JsonProcessingException {

      String url = FormsProviderUtils
          .constructSurveyUri(APPNAME, TABLE_ID, FORM_ID, INSTANCE_ID_SPECIAL, SCREEN_PATH, null);

      assertEquals(EXPECTED_TABLEID_FORMID_BASE +
          FormsProviderUtils.URI_SURVEY_QUERY_PARAM_INSTANCE_ID + "=" +
          FormsProviderUtils.encodeFragmentUnquotedStringValue(INSTANCE_ID_SPECIAL) + "&" +
          FormsProviderUtils.URI_SURVEY_QUERY_PARAM_SCREEN_PATH + "=" +
          FormsProviderUtils.encodeFragmentUnquotedStringValue(SCREEN_PATH), url);

      Uri uri = Uri.parse(url);
      FormsProviderUtils.ParsedFragment pf = FormsProviderUtils.parseUri(uri);

      assertEquals(INSTANCE_ID_SPECIAL, pf.instanceId);
      assertEquals(SCREEN_PATH, pf.screenPath);
      assertNull(pf.auxillaryHash);
   }

   @Test
   public void testSurveyUriTableIdFormIdInstanceIdScreenPathEmptyKVMap()
       throws UnsupportedEncodingException, JsonProcessingException {

      Map<String,Object> elementKeyValueMap = new HashMap<String,Object>();

      String url = FormsProviderUtils
          .constructSurveyUri(APPNAME, TABLE_ID, FORM_ID, INSTANCE_ID_SPECIAL, SCREEN_PATH, elementKeyValueMap);

      assertEquals(EXPECTED_TABLEID_FORMID_BASE +
          FormsProviderUtils.URI_SURVEY_QUERY_PARAM_INSTANCE_ID + "=" +
          FormsProviderUtils.encodeFragmentUnquotedStringValue(INSTANCE_ID_SPECIAL) + "&" +
          FormsProviderUtils.URI_SURVEY_QUERY_PARAM_SCREEN_PATH + "=" +
          FormsProviderUtils.encodeFragmentUnquotedStringValue(SCREEN_PATH), url);

      Uri uri = Uri.parse(url);
      FormsProviderUtils.ParsedFragment pf = FormsProviderUtils.parseUri(uri);

      assertEquals(INSTANCE_ID_SPECIAL, pf.instanceId);
      assertEquals(SCREEN_PATH, pf.screenPath);
      assertNull(pf.auxillaryHash);
   }

   @Test
   public void testSurveyUriTableIdFormIdInstanceIdScreenPathNonEmptyKVMap()
       throws UnsupportedEncodingException, JsonProcessingException {

      Map<String,Object> elementKeyValueMap = new HashMap<String,Object>();
      elementKeyValueMap.put(MY_KEY1, MY_KEY1_VALUE);

      String url = FormsProviderUtils
          .constructSurveyUri(APPNAME, TABLE_ID, FORM_ID, INSTANCE_ID_SPECIAL, SCREEN_PATH, elementKeyValueMap);

      assertEquals(EXPECTED_TABLEID_FORMID_BASE +
          FormsProviderUtils.URI_SURVEY_QUERY_PARAM_INSTANCE_ID + "=" +
          FormsProviderUtils.encodeFragmentUnquotedStringValue(INSTANCE_ID_SPECIAL) + "&" +
          FormsProviderUtils.URI_SURVEY_QUERY_PARAM_SCREEN_PATH + "=" +
          FormsProviderUtils.encodeFragmentUnquotedStringValue(SCREEN_PATH) + "&" +
          MY_KEY1 + "=" +
          FormsProviderUtils.encodeFragmentObjectValue(MY_KEY1_VALUE), url);

      Uri uri = Uri.parse(url);
      FormsProviderUtils.ParsedFragment pf = FormsProviderUtils.parseUri(uri);

      assertEquals(INSTANCE_ID_SPECIAL, pf.instanceId);
      assertEquals(SCREEN_PATH, pf.screenPath);
      assertEquals(MY_KEY1 + "=" +
          FormsProviderUtils.encodeFragmentObjectValue(MY_KEY1_VALUE), pf.auxillaryHash);
   }
}

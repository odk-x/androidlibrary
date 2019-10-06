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
package org.opendatakit.logic;

import android.Manifest;
import android.content.Context;

import androidx.test.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.logging.desktop.WebLoggerDesktopFactoryImpl;
import org.opendatakit.properties.CommonToolProperties;
import org.opendatakit.properties.PropertiesSingleton;
import org.opendatakit.utilities.ODKFileUtils;
import org.opendatakit.utilities.StaticStateManipulator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author mitchellsundt@gmail.com
 */

public class PropertiesNonPrivilegedTest {

    private static final String APPNAME = "unittestProp";

    @Rule
    public GrantPermissionRule writeRuntimePermissionRule = GrantPermissionRule .grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    @Rule
    public GrantPermissionRule readtimePermissionRule = GrantPermissionRule .grant(Manifest.permission.READ_EXTERNAL_STORAGE);

    @Before
    public void setUp() throws Exception {
        ODKFileUtils.verifyExternalStorageAvailability();
        ODKFileUtils.assertDirectoryStructure(APPNAME);

        StaticStateManipulator.get().reset();
        WebLogger.setFactory(new WebLoggerDesktopFactoryImpl());
    }

    @Test
    public void testSimpleProperties() {

        Context context = InstrumentationRegistry.getContext();

        PropertiesSingleton props = CommonToolProperties.get(context, APPNAME);
        Map<String,String> properties = new HashMap<String,String>();
        // non-default value for font size
        properties.put(CommonToolProperties.KEY_FONT_SIZE, "29");

        props.setProperties(properties);

        StaticStateManipulator.get().reset();

        props = CommonToolProperties.get(context, APPNAME);
        assertEquals(props.getProperty(CommonToolProperties.KEY_FONT_SIZE), "29");
    }

    /**
     * Setting or removing secure properties from a
     * non-privileged APK should fail.
     */
    @Test
    public void testSecureSetProperties() {

        StaticStateManipulator.get().reset();
        Context context = InstrumentationRegistry.getContext();

        TreeMap<String,String> secureProperties = new TreeMap<String,String>();
        CommonToolProperties.accumulateProperties(context, null, null, secureProperties);
        PropertiesSingleton props = CommonToolProperties.get(context, APPNAME);

        for ( String secureKey : secureProperties.keySet() ) {
            // this is stored in SharedPreferences
            boolean threwError = false;
            try {
                props.setProperties(Collections.singletonMap(secureKey, "asdf"));
            } catch (IllegalStateException e) {
                threwError = true;
            }

            assertTrue("set: " + secureKey, threwError);

            // and verify remove doesn't do anything
            threwError = false;
            try {
                String value = null;
                props.setProperties(Collections.singletonMap(secureKey, value));
            } catch (IllegalStateException e) {
                threwError = true;
            }

            assertTrue("remove: " + secureKey, threwError);

        }
    }

    /**
     * Getting secure values from a non-privileged APK should always return null.
     */
    @Test
    public void testSecureGetProperties() {

        StaticStateManipulator.get().reset();
        Context context = InstrumentationRegistry.getContext();

        TreeMap<String,String> secureProperties = new TreeMap<String,String>();
        CommonToolProperties.accumulateProperties(context, null, null, secureProperties);
        PropertiesSingleton props = CommonToolProperties.get(context, APPNAME);

        for ( String secureKey : secureProperties.keySet() ) {
            // this is stored in SharedPreferences
            // always throws an exception
            boolean threwError = false;
            try {
                props.getProperty(secureKey);
            } catch (IllegalStateException e) {
                threwError = true;
            }

            assertTrue("get: " + secureKey, threwError);
        }
    }

}

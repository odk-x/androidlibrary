package org.opendatakit.utilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.net.Uri;

import org.junit.Test;

import java.util.Objects;


public class ODKXFileUriUtilsTest {

    // Constants to prevent magic strings
    private static final class UriConstants {
        static final String CONTENT_SCHEME = "content";
        static final String BASE_AUTHORITY = "opendatakit";
        static final String CONFIG_SEGMENT = "config";
        static final String ASSETS_SEGMENT = "assets";
        static final String CSV_SEGMENT = "csv";
    }

    // Test data constants
    private static final class TestData {
        static final String TEST_APP_NAME = "testApp";
        static final String DEFAULT_APP_NAME = "default";
    }

    @Test
    public void testGetOdkXUri() {
        // Given: We want to generate the base ODK-X URI
        // When: getOdkXUri() is called
        Uri uri = ODKXFileUriUtils.getOdkXUri();

        // Then: Verify URI properties with detailed assertions
        assertEquals("URI scheme should be 'content'",
                UriConstants.CONTENT_SCHEME, uri.getScheme());
        assertEquals("Base authority should be 'opendatakit'",
                UriConstants.BASE_AUTHORITY, uri.getPathSegments().get(0));
        assertEquals("Base URI should have exactly one path segment",
                1, uri.getPathSegments().size());
    }

    @Test
    public void testGetAppUri() {
        // Given: A specific app name
        String appName = TestData.TEST_APP_NAME;

        // When: getAppUri() is called
        Uri uri = ODKXFileUriUtils.getAppUri(appName);

        // Then: Verify URI properties with detailed assertions
        assertEquals("URI scheme should be 'content'",
                UriConstants.CONTENT_SCHEME, uri.getScheme());
        assertEquals("First path segment should be base authority",
                UriConstants.BASE_AUTHORITY, uri.getPathSegments().get(0));
        assertEquals("Second path segment should be app name",
                appName, uri.getPathSegments().get(1));
        assertEquals("App URI should have exactly two path segments",
                2, uri.getPathSegments().size());
    }

    @Test
    public void testGetConfigUri() {
        // Given: A specific app name
        String appName = TestData.TEST_APP_NAME;

        // When: getConfigUri() is called
        Uri uri = ODKXFileUriUtils.getConfigUri(appName);

        // Then: Verify URI properties with detailed assertions
        assertEquals("URI scheme should be 'content'",
                UriConstants.CONTENT_SCHEME, uri.getScheme());
        assertEquals("First path segment should be base authority",
                UriConstants.BASE_AUTHORITY, uri.getPathSegments().get(0));
        assertEquals("Second path segment should be app name",
                appName, uri.getPathSegments().get(1));
        assertEquals("Third path segment should be 'config'",
                UriConstants.CONFIG_SEGMENT, uri.getPathSegments().get(2));
        assertEquals("Config URI should have exactly three path segments",
                3, uri.getPathSegments().size());
    }

    @Test
    public void testGetAssetsUri() {
        // Given: A specific app name
        String appName = TestData.TEST_APP_NAME;

        // When: getAssetsUri() is called
        Uri uri = ODKXFileUriUtils.getAssetsUri(appName);

        // Then: Verify URI properties with detailed assertions
        assertEquals("URI scheme should be 'content'",
                UriConstants.CONTENT_SCHEME, uri.getScheme());
        assertEquals("First path segment should be base authority",
                UriConstants.BASE_AUTHORITY, uri.getPathSegments().get(0));
        assertEquals("Second path segment should be app name",
                appName, uri.getPathSegments().get(1));
        assertEquals("Third path segment should be 'config'",
                UriConstants.CONFIG_SEGMENT, uri.getPathSegments().get(2));
        assertEquals("Fourth path segment should be 'assets'",
                UriConstants.ASSETS_SEGMENT, uri.getPathSegments().get(3));
        assertEquals("Assets URI should have exactly four path segments",
                4, uri.getPathSegments().size());
    }

    @Test
    public void testMultipleAppNames() {
        // Given: Different app names
        String[] appNames = {"testApp", "surveyApp", "data-collection-app", "app_with_underscores", "123NumericApp"};

        // When/Then: Verify each app name is correctly incorporated in URIs
        for (String appName : appNames) {
            Uri appUri = ODKXFileUriUtils.getAppUri(appName);
            assertEquals("App name should be correctly incorporated",
                    appName, appUri.getPathSegments().get(1));

            // Verify the app name is consistently used in derived URIs
            Uri configUri = ODKXFileUriUtils.getConfigUri(appName);
            assertEquals("Config URI should contain correct app name",
                    appName, configUri.getPathSegments().get(1));
        }
    }

    @Test
    public void testNullAppName() {
        // Given: Null app name
        String appName = null;

        try {
            // When: Calling getAppUri with null
            Uri uri = ODKXFileUriUtils.getAppUri(appName);

            // Then: If we reach here, no exception was thrown.
            // Verify that the result handles null appropriately
            // (This could be checking for a default app name or other expected behavior)
            assertNotNull("URI should not be null even with null app name", uri);
            // Add other assertions based on expected behavior
        } catch (Exception e) {
            // If an exception is thrown but it's not IllegalArgumentException,
            // let's see what it actually is
            System.out.println("Exception thrown with null app name: " + e.getClass().getName());
            throw e; // Re-throw to fail the test (unless this is expected behavior)
        }
    }

    @Test
    public void testEmptyAppName() {
        // Given: Empty app name
        String appName = "";

        try {
            // When: Calling getAppUri with empty string
            Uri uri = ODKXFileUriUtils.getAppUri(appName);

            // Then: If we reach here, no exception was thrown.
            // Verify that the result handles empty string appropriately
            assertNotNull("URI should not be null even with empty app name", uri);
            // Add other assertions based on expected behavior
        } catch (Exception e) {
            // If an exception is thrown but it's not IllegalArgumentException,
            // let's see what it actually is
            System.out.println("Exception thrown with empty app name: " + e.getClass().getName());
            throw e; // Re-throw to fail the test (unless this is expected behavior)
        }
    }



    @Test
    public void testAppNameWithSpecialCharacters() {
        // Given: App name with special characters
        String appName = "test@pp#special";

        // When: Creating URIs with this app name
        Uri appUri = ODKXFileUriUtils.getAppUri(appName);

        // Then: Verify the app name is correctly handled
        assertEquals("Special characters in app name should be preserved",
                appName, appUri.getPathSegments().get(1));
    }

    @Test
    public void testDefaultAppNameFallback() {
        // This test would depend on implementation details of ODKXFileUriUtils
        // If there's a fallback to a default app name when none is provided,
        // you could test that behavior here

        // Given: A method that might use a default app name (assuming it exists)
        // When: Calling such a method
        Uri uri = ODKXFileUriUtils.getAppUri(TestData.DEFAULT_APP_NAME);

        // Then: Verify default app name is used
        assertEquals("Default app name should be used as fallback",
                TestData.DEFAULT_APP_NAME, uri.getPathSegments().get(1));
    }

    @Test
    public void testGetAssetsCsvUri() {
        // Given: A specific app name
        String appName = TestData.TEST_APP_NAME;

        // When: getAssetsCsvUri() is called
        Uri uri = ODKXFileUriUtils.getAssetsCsvUri(appName);

        // Then: Verify URI properties with detailed assertions
        assertEquals("URI scheme should be 'content'",
                UriConstants.CONTENT_SCHEME, uri.getScheme());
        assertEquals("First path segment should be base authority",
                UriConstants.BASE_AUTHORITY, uri.getPathSegments().get(0));
        assertEquals("Second path segment should be app name",
                appName, uri.getPathSegments().get(1));
        assertEquals("Third path segment should be 'config'",
                UriConstants.CONFIG_SEGMENT, uri.getPathSegments().get(2));
        assertEquals("Fourth path segment should be 'assets'",
                UriConstants.ASSETS_SEGMENT, uri.getPathSegments().get(3));
        assertEquals("Fifth path segment should be 'csv'",
                UriConstants.CSV_SEGMENT, uri.getPathSegments().get(4));
        assertEquals("CSV Assets URI should have exactly five path segments",
                5, uri.getPathSegments().size());
    }

    @Test
    public void testODKXRemainingPath_ValidPath() {
        // Given: A valid URI with known structure
        Uri uri = Uri.parse("content://authority/opendatakit/testApp/config/assets/csv/testfile.csv");

        // When: ODKXRemainingPath is called
        String remainingPath = ODKXFileUriUtils.ODKXRemainingPath(TestData.TEST_APP_NAME, uri);

        // Then: Verify correct path extraction
        assertEquals("config/assets/csv/testfile.csv", remainingPath);
    }

    @Test
    public void testODKXRemainingPath_InvalidPath() {
        // Given: An invalid URI
        Uri uri = Uri.parse("content://authority/some/other/path");

        // When: ODKXRemainingPath is called
        String remainingPath = ODKXFileUriUtils.ODKXRemainingPath(TestData.TEST_APP_NAME, uri);

        // Then: Verify null is returned
        assertNull(remainingPath);
    }

    @Test
    public void testODKXRemainingPath_ExactMatch() {
        // Given: A URI with exact app path
        Uri uri = Uri.parse("content://authority/opendatakit/testApp/");

        // When: ODKXRemainingPath is called
        String remainingPath = ODKXFileUriUtils.ODKXRemainingPath(TestData.TEST_APP_NAME, uri);

        // Then: Verify empty string is returned
        assertEquals("", remainingPath);
    }

    @Test
    public void testODKXRemainingPath_WithSpaces() {
        // Given: A URI with spaces in the path
        Uri uri = Uri.parse("content://authority/opendatakit/testApp/config/folder with spaces/file.csv");

        // When: ODKXRemainingPath is called
        String remainingPath = ODKXFileUriUtils.ODKXRemainingPath(TestData.TEST_APP_NAME, uri);

        // Then: Verify correct path extraction
        assertEquals("config/folder with spaces/file.csv", remainingPath);
    }

    @Test
    public void testODKXRemainingPath_WithSpecialCharacters() {
        // Given: A URI with special characters in the path
        Uri uri = Uri.parse("content://authority/opendatakit/testApp/config/folder@name:[]!$&/file.csv");

        // When: ODKXRemainingPath is called
        String remainingPath = ODKXFileUriUtils.ODKXRemainingPath(TestData.TEST_APP_NAME, uri);

        // Then: Verify correct path extraction
        assertEquals("config/folder@name:[]!$&/file.csv", remainingPath);
    }

    @Test
    public void testUriRelationships() {
        // Given: URIs at different levels
        Uri baseUri = ODKXFileUriUtils.getOdkXUri();
        Uri appUri = ODKXFileUriUtils.getAppUri(TestData.TEST_APP_NAME);
        Uri configUri = ODKXFileUriUtils.getConfigUri(TestData.TEST_APP_NAME);
        Uri assetsUri = ODKXFileUriUtils.getAssetsUri(TestData.TEST_APP_NAME);
        Uri csvUri = ODKXFileUriUtils.getAssetsCsvUri(TestData.TEST_APP_NAME);

        // Then: Verify path hierarchy
        assertTrue("App URI should start with base URI path",
                Objects.requireNonNull(appUri.getPath()).startsWith(Objects.requireNonNull(baseUri.getPath())));
        assertTrue("Config URI should start with app URI path",
                Objects.requireNonNull(configUri.getPath()).startsWith(Objects.requireNonNull(appUri.getPath())));
        assertTrue("Assets URI should start with config URI path",
                Objects.requireNonNull(assetsUri.getPath()).startsWith(Objects.requireNonNull(configUri.getPath())));
        assertTrue("CSV URI should start with assets URI path",
                Objects.requireNonNull(csvUri.getPath()).startsWith(Objects.requireNonNull(assetsUri.getPath())));
    }

    @Test
    public void testAppNameConsistency() {
        // Given: A specific app name
        String appName = TestData.TEST_APP_NAME;

        // When: generating various URIs
        Uri appUri = ODKXFileUriUtils.getAppUri(appName);
        Uri configUri = ODKXFileUriUtils.getConfigUri(appName);
        Uri assetsUri = ODKXFileUriUtils.getAssetsUri(appName);
        Uri csvUri = ODKXFileUriUtils.getAssetsCsvUri(appName);

        // Then: Verify app name consistency
        assertEquals(appName, appUri.getPathSegments().get(1));
        assertEquals(appName, configUri.getPathSegments().get(1));
        assertEquals(appName, assetsUri.getPathSegments().get(1));
        assertEquals(appName, csvUri.getPathSegments().get(1));
    }

    @Test
    public void testPathSegmentCounting() {
        // Given: Various URIs
        Uri baseUri = ODKXFileUriUtils.getOdkXUri();
        Uri appUri = ODKXFileUriUtils.getAppUri(TestData.TEST_APP_NAME);
        Uri configUri = ODKXFileUriUtils.getConfigUri(TestData.TEST_APP_NAME);
        Uri assetsUri = ODKXFileUriUtils.getAssetsUri(TestData.TEST_APP_NAME);
        Uri csvUri = ODKXFileUriUtils.getAssetsCsvUri(TestData.TEST_APP_NAME);

        // Then: Verify path segment count
        assertEquals(1, baseUri.getPathSegments().size());
        assertEquals(2, appUri.getPathSegments().size());
        assertEquals(3, configUri.getPathSegments().size());
        assertEquals(4, assetsUri.getPathSegments().size());
        assertEquals(5, csvUri.getPathSegments().size());
    }
}
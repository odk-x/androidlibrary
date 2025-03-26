package org.opendatakit.utilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.net.Uri;

import org.junit.Test;


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
                appUri.getPath().startsWith(baseUri.getPath()));
        assertTrue("Config URI should start with app URI path",
                configUri.getPath().startsWith(appUri.getPath()));
        assertTrue("Assets URI should start with config URI path",
                assetsUri.getPath().startsWith(configUri.getPath()));
        assertTrue("CSV URI should start with assets URI path",
                csvUri.getPath().startsWith(assetsUri.getPath()));
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
package org.opendatakit.utilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.net.Uri;

import org.junit.Test;

/**
 * Tests for ODKXFileUriUtils
 */
public class ODKXFileUriUtilsTest {

    private static final String TEST_APP_NAME = "testApp";
    private static final String DEFAULT_APP_NAME = "default";

    @Test
    public void testGetOdkXUri() {
        Uri uri = ODKXFileUriUtils.getOdkXUri();

        assertEquals("content", uri.getScheme());
        assertEquals(1, uri.getPathSegments().size());
        assertEquals("opendatakit", uri.getPathSegments().get(0));
    }

    @Test
    public void testGetAppUri() {
        Uri uri = ODKXFileUriUtils.getAppUri(TEST_APP_NAME);

        assertEquals("content", uri.getScheme());
        assertEquals(2, uri.getPathSegments().size());
        assertEquals("opendatakit", uri.getPathSegments().get(0));
        assertEquals(TEST_APP_NAME, uri.getPathSegments().get(1));
    }

    @Test
    public void testGetConfigUri() {
        Uri uri = ODKXFileUriUtils.getConfigUri(TEST_APP_NAME);

        assertEquals("content", uri.getScheme());
        assertEquals(3, uri.getPathSegments().size());
        assertEquals("opendatakit", uri.getPathSegments().get(0));
        assertEquals(TEST_APP_NAME, uri.getPathSegments().get(1));
        assertEquals("config", uri.getPathSegments().get(2));
    }

    @Test
    public void testGetAssetsUri() {
        Uri uri = ODKXFileUriUtils.getAssetsUri(TEST_APP_NAME);

        assertEquals("content", uri.getScheme());
        assertEquals(4, uri.getPathSegments().size());
        assertEquals("opendatakit", uri.getPathSegments().get(0));
        assertEquals(TEST_APP_NAME, uri.getPathSegments().get(1));
        assertEquals("config", uri.getPathSegments().get(2));
        assertEquals("assets", uri.getPathSegments().get(3));
    }

    @Test
    public void testGetAssetsCsvUri() {
        Uri uri = ODKXFileUriUtils.getAssetsCsvUri(TEST_APP_NAME);

        assertEquals("content", uri.getScheme());
        assertEquals(5, uri.getPathSegments().size());
        assertEquals("opendatakit", uri.getPathSegments().get(0));
        assertEquals(TEST_APP_NAME, uri.getPathSegments().get(1));
        assertEquals("config", uri.getPathSegments().get(2));
        assertEquals("assets", uri.getPathSegments().get(3));
        assertEquals("csv", uri.getPathSegments().get(4));
    }

    @Test
    public void testODKXRemainingPath_ValidPath() {
        // Create a mock Uri with a valid path for testing
        Uri uri = Uri.parse("content://authority/opendatakit/testApp/config/assets/csv/testfile.csv");

        String remainingPath = ODKXFileUriUtils.ODKXRemainingPath(TEST_APP_NAME, uri);

        assertEquals("config/assets/csv/testfile.csv", remainingPath);
    }

    @Test
    public void testODKXRemainingPath_InvalidPath() {
        Uri uri = Uri.parse("content://authority/some/other/path");

        String remainingPath = ODKXFileUriUtils.ODKXRemainingPath(TEST_APP_NAME, uri);

        assertNull(remainingPath);
    }

    @Test
    public void testODKXRemainingPath_ExactMatch() {
        Uri uri = Uri.parse("content://authority/opendatakit/testApp/");

        String remainingPath = ODKXFileUriUtils.ODKXRemainingPath(TEST_APP_NAME, uri);

        assertEquals("", remainingPath);
    }

    @Test
    public void testODKXRemainingPath_WithSpaces() {
        Uri uri = Uri.parse("content://authority/opendatakit/testApp/config/folder with spaces/file.csv");

        String remainingPath = ODKXFileUriUtils.ODKXRemainingPath(TEST_APP_NAME, uri);

        assertEquals("config/folder with spaces/file.csv", remainingPath);
    }

    @Test
    public void testODKXRemainingPath_WithSpecialCharacters() {
        Uri uri = Uri.parse("content://authority/opendatakit/testApp/config/folder@name:[]!$&/file.csv");

        String remainingPath = ODKXFileUriUtils.ODKXRemainingPath(TEST_APP_NAME, uri);

        assertEquals("config/folder@name:[]!$&/file.csv", remainingPath);
    }

    @Test
    public void testUriRelationships() {
        Uri baseUri = ODKXFileUriUtils.getOdkXUri();
        Uri appUri = ODKXFileUriUtils.getAppUri(TEST_APP_NAME);
        Uri configUri = ODKXFileUriUtils.getConfigUri(TEST_APP_NAME);
        Uri assetsUri = ODKXFileUriUtils.getAssetsUri(TEST_APP_NAME);
        Uri csvUri = ODKXFileUriUtils.getAssetsCsvUri(TEST_APP_NAME);

        // Check path progression
        String basePath = baseUri.getPath();
        assertTrue(appUri.getPath().startsWith(basePath));
        assertTrue(configUri.getPath().startsWith(appUri.getPath()));
        assertTrue(assetsUri.getPath().startsWith(configUri.getPath()));
        assertTrue(csvUri.getPath().startsWith(assetsUri.getPath()));
    }

    @Test
    public void testAppNameConsistency() {
        // Verify app name is consistently used in all URIs
        Uri appUri = ODKXFileUriUtils.getAppUri(TEST_APP_NAME);
        Uri configUri = ODKXFileUriUtils.getConfigUri(TEST_APP_NAME);
        Uri assetsUri = ODKXFileUriUtils.getAssetsUri(TEST_APP_NAME);
        Uri csvUri = ODKXFileUriUtils.getAssetsCsvUri(TEST_APP_NAME);

        assertEquals(TEST_APP_NAME, appUri.getPathSegments().get(1));
        assertEquals(TEST_APP_NAME, configUri.getPathSegments().get(1));
        assertEquals(TEST_APP_NAME, assetsUri.getPathSegments().get(1));
        assertEquals(TEST_APP_NAME, csvUri.getPathSegments().get(1));
    }

    @Test
    public void testPathSegmentCounting() {
        // Test that each level adds exactly one path segment
        Uri baseUri = ODKXFileUriUtils.getOdkXUri();
        Uri appUri = ODKXFileUriUtils.getAppUri(TEST_APP_NAME);
        Uri configUri = ODKXFileUriUtils.getConfigUri(TEST_APP_NAME);
        Uri assetsUri = ODKXFileUriUtils.getAssetsUri(TEST_APP_NAME);
        Uri csvUri = ODKXFileUriUtils.getAssetsCsvUri(TEST_APP_NAME);

        assertEquals(1, baseUri.getPathSegments().size());
        assertEquals(2, appUri.getPathSegments().size());
        assertEquals(3, configUri.getPathSegments().size());
        assertEquals(4, assetsUri.getPathSegments().size());
        assertEquals(5, csvUri.getPathSegments().size());
    }
}
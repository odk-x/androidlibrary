package org.opendatakit.utilities;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Tests for ODKFileUtils
 */

public class ODKFileUtilsTest {
  static final String appName;
  static final File appDir;
  static {
    appName = "ODKFileUtilsTest";
    appDir = new File(ODKFileUtils.getAppFolder(appName));
  }

  @Test
  public void verifyUriEscapes() {

    // the Uri class doesn't do any fancy encoding for most characters

    File specialFile;
    String frag;
    specialFile = new File(appDir, "space name" );
    frag = ODKFileUtils.asUriFragment(appName, specialFile);
    assertEquals("space%20name", frag);

    specialFile = new File(specialFile, "space name" );
    frag = ODKFileUtils.asUriFragment(appName, specialFile);
    assertEquals("space%20name/space%20name", frag);

    specialFile = new File(appDir, "space name:[]@!$&'()*+,;=" );
    frag = ODKFileUtils.asUriFragment(appName, specialFile);
    assertEquals("space%20name%3A%5B%5D%40!%24%26'()*%2B%2C%3B%3D", frag);

    specialFile = new File(appDir, "space name:[]@!$&'()*+,;=\\foo%2Fbar" );
    frag = ODKFileUtils.asUriFragment(appName, specialFile);
    assertEquals("space%20name%3A%5B%5D%40!%24%26'()*%2B%2C%3B%3D%5Cfoo%252Fbar", frag);

    specialFile = new File(appDir, "space name?" );
    frag = ODKFileUtils.asUriFragment(appName, specialFile);
    assertEquals("space%20name%3F", frag);

    specialFile = new File(appDir, "space name#" );
    frag = ODKFileUtils.asUriFragment(appName, specialFile);
    assertEquals("space%20name%23", frag);
  }

  @Test
  public void decodeUriEscapes() {

    ODKFileUtils.assertDirectoryStructure(appName);
    File specialFile;
    String frag;
    specialFile = ODKFileUtils.getAsFile(appName, appName + "/" + "space%20name");
    frag = specialFile.getName();
    assertEquals("space name", frag);

    specialFile = ODKFileUtils.getAsFile(appName, appName + "/" + "space%20name/space%20name");
    frag = specialFile.getName();
    assertEquals("space name", frag);
    frag = specialFile.getParentFile().getName();
    assertEquals("space name", frag);

    specialFile = ODKFileUtils.getAsFile(appName, appName + "/" + "space name/space name");
    frag = specialFile.getName();
    assertEquals("space name", frag);
    frag = specialFile.getParentFile().getName();
    assertEquals("space name", frag);

    specialFile = ODKFileUtils.getAsFile(appName, appName + "/" + "space name:[]@!$&'()*+,;=" );
    frag = specialFile.getName();
    assertEquals("space name:[]@!$&'()*+,;=", frag);

    specialFile = ODKFileUtils.getAsFile(appName, appName + "/" +
        "space%20name%3A%5B%5D%40!%24%26'()*%2B%2C%3B%3D" );
    frag = specialFile.getName();
    assertEquals("space name:[]@!$&'()*+,;=", frag);

    specialFile = ODKFileUtils.getAsFile(appName, appName + "/" + "space name?" );
    frag = specialFile.getName();
    assertEquals("space name?", frag);

    specialFile = ODKFileUtils.getAsFile(appName, appName + "/" + "space%20name%3F" );
    frag = specialFile.getName();
    assertEquals("space name?", frag);

    specialFile = ODKFileUtils.getAsFile(appName, appName + "/" + "space name#" );
    frag = specialFile.getName();
    assertEquals("space name#", frag);

    specialFile = ODKFileUtils.getAsFile(appName, appName + "/" + "space%20name%23" );
    frag = specialFile.getName();
    assertEquals("space name#", frag);

    specialFile = ODKFileUtils.getAsFile(appName, appName + "/" + "space name:[]@!$&'()*+,;=\\foo//bar" );
    frag = specialFile.getName();
    assertEquals("bar", frag);
    frag = specialFile.getParentFile().getName();
    assertEquals("space name:[]@!$&'()*+,;=\\foo", frag);

    specialFile = ODKFileUtils.getAsFile(appName, appName + "/" + "space name:[]@!$&'%28%29*+,;=\\foo//bar" );
    frag = specialFile.getName();
    assertEquals("bar", frag);
    frag = specialFile.getParentFile().getName();
    assertEquals("space name:[]@!$&'()*+,;=\\foo", frag);

    specialFile = ODKFileUtils.getAsFile(appName, appName + "/" + "space%20name%3A%5B%5D%40!%24%26'()*%2B%2C%3B%3D%5Cfoo%252Fbar" );
    frag = specialFile.getName();
    assertEquals("space name:[]@!$&'()*+,;=\\foo%2Fbar", frag);
  }

  @Test
  public void testSpecialCharactersInFolderName() {
    // Test case with special characters in folder name
    File specialFile = new File(appDir, "special@folder:name");
    String frag = ODKFileUtils.asUriFragment(appName, specialFile);
    assertEquals("special%40folder%3Aname", frag);
  }

  @Test
  public void testEmptyFolderName() {
    // Test case with an empty folder name
    File specialFile = new File(appDir, "");
    String frag = ODKFileUtils.asUriFragment(appName, specialFile);
    assertEquals("", frag);
  }

  @Test
  public void testSpecialCharactersInFileName() {
    // Test case with special characters in file name
    File specialFile = new File(appDir, "file@name.txt");
    String frag = ODKFileUtils.asUriFragment(appName, specialFile);
    assertEquals("file%40name.txt", frag);
  }

  @Test
  public void testMultipleConsecutiveSlashesInPath() {
    // Test case with multiple consecutive slashes in the file path
    File specialFile = new File(appDir, "folder//subfolder//file.txt");
    String frag = ODKFileUtils.asUriFragment(appName, specialFile);
    assertEquals("folder/subfolder/file.txt", frag);
  }

  @Test
  public void testSpecialCharactersInParentFolderName() {
    // Test case with special characters in parent folder name
    File specialFile = new File(appDir, "parent@folder");
    File childFile = new File(specialFile, "childFolder");
    String frag = ODKFileUtils.asUriFragment(appName, childFile);
    assertEquals("parent%40folder/childFolder", frag);
  }

  @Test
  public void testSpecialCharactersInExtension() {
    // Test case with special characters in file extension
    File specialFile = new File(appDir, "file@name.special.txt");
    String frag = ODKFileUtils.asUriFragment(appName, specialFile);
    assertEquals("file%40name.special.txt", frag);
  }

  @Test
  public void testMixedCaseFolderName() {
    // Test case with mixed case folder name
    File specialFile = new File(appDir, "MiXeDCaSeFoLdEr");
    String frag = ODKFileUtils.asUriFragment(appName, specialFile);
    assertEquals("MiXeDCaSeFoLdEr", frag);
  }

  @Test
  public void testSpecialCharactersInPath() {
    // Test case with special characters in the file path
    File specialFile = new File(appDir, "folder@subfolder/file.txt");
    String frag = ODKFileUtils.asUriFragment(appName, specialFile);
    assertEquals("folder%40subfolder/file.txt", frag);
  }

  @Test
  public void testSpecialCharactersInParentFolderAndFileName() {
    // Test case with special characters in parent folder name and file name
    File specialFile = new File(appDir, "parent@folder");
    File childFile = new File(specialFile, "file@name.txt");
    String frag = ODKFileUtils.asUriFragment(appName, childFile);
    assertEquals("parent%40folder/file%40name.txt", frag);
  }
}

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
}

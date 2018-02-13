/*
 * Copyright (C) 2017 University of Washington
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

package org.opendatakit.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.opendatakit.logging.WebLogger;

import java.io.File;

/**
 * Isolate bitmap resizing to its own utility class.
 *
 * Used in DrawActivity in survey
 *
 * @author mitchellsundt@gmail.com
 */
@SuppressWarnings("unused WeakerAccess")
public final class BitmapUtils {
  private static final String TAG = BitmapUtils.class.getSimpleName();

  private BitmapUtils() {
  }

  /**
   * Resizes a bitmap to fit on a particular display, used in survey.activities.DrawActivity
   * @param appName the app name, used for logging
   * @param f the file that contains the bitmap to resize
   * @param screenHeight the height of the screen that the bitmap needs to fit into
   * @param screenWidth the width of the screen that the bitmap needs to fit into
   * @return a scaled bitmap of the requested file
   */
  public static Bitmap getBitmapScaledToDisplay(String appName, File f, int screenHeight, int screenWidth) {
    // Determine image size of f
    BitmapFactory.Options o = new BitmapFactory.Options();
    o.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(f.getAbsolutePath(), o);

    int heightScale = o.outHeight / screenHeight;
    int widthScale = o.outWidth / screenWidth;

    // Powers of 2 work faster, sometimes, according to the doc.
    // We're just doing closest size that still fills the screen.
    int scale = Math.max(widthScale, heightScale);

    // get bitmap with scale ( < 1 is the same as 1)
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inSampleSize = scale;
    Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
    if (b != null) {
      WebLogger.getLogger(appName).i(TAG,
          "Screen is " + screenHeight + "x" + screenWidth + ".  Image has been scaled down by "
              + scale + " to " + b.getHeight() + "x" + b.getWidth());
    }
    return b;
  }
}

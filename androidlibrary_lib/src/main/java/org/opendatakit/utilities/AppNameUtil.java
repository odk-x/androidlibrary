package org.opendatakit.utilities;

import android.app.Activity;
import org.opendatakit.activities.IAppAwareActivity;

/**
 * Created by wrb on 2/8/2018.
 */

public final class AppNameUtil {

   public static String getAppNameFromActivity(Activity activity) {
      // get the appName from the ODK app aware infrastructure
      if (activity instanceof IAppAwareActivity) {
         return ((IAppAwareActivity) activity).getAppName();
      } else {
         throw new RuntimeException("The activity that fragnment is attaching to is "
             + "NOT an IAppAwareActivity");
      }
   }

}

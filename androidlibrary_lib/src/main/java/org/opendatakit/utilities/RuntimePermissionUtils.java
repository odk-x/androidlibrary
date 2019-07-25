package org.opendatakit.utilities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import org.opendatakit.androidlibrary.R;

public class RuntimePermissionUtils {
  public static final String[] FINE_AND_COARSE_LOCATION = new String[]{
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION
  };

  public static boolean checkSelfAnyPermission(@NonNull Activity activity,
                                               @NonNull String... permissions) {
    for (String permission : permissions) {
      if (ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
        return true;
      }
    }

    return false;
  }

  public static boolean checkSelfAllPermission(@NonNull Activity activity,
                                               @NonNull String... permissions) {
    for (String permission : permissions) {
      if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }

    return true;
  }

  public static boolean checkPackageAnyPermission(@NonNull Context context,
                                              @NonNull String pkgName,
                                              @NonNull String... permissions) {
    PackageManager pm = context.getPackageManager();

    for (String permission : permissions) {
      if (pm.checkPermission(permission, pkgName) == PackageManager.PERMISSION_GRANTED) {
        return true;
      }
    }

    return false;
  }

  public static boolean checkPackageAllPermission(@NonNull Context context,
                                                  @NonNull String pkgName,
                                                  @NonNull String... permissions) {
    PackageManager pm = context.getPackageManager();

    for (String permission : permissions) {
      if (pm.checkPermission(permission, pkgName) != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }

    return true;
  }

  public static boolean shouldShowAnyPermissionRationale(@NonNull Activity activity,
                                                         @NonNull String... permissions) {
    for (String permission : permissions) {
      if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
        return true;
      }
    }

    return false;
  }

  public static AlertDialog.Builder createPermissionRationaleDialog(@NonNull final Activity context,
                                                                    final int requestCode,
                                                                    @NonNull final String... permissions) {
    return new AlertDialog.Builder(context)
        .setTitle(R.string.permission_rational_title)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            ActivityCompat.requestPermissions(context, permissions, requestCode);
          }
        })
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
          }
        });
  }

  public static void handleRequestPermissionsResult(int requestCode,
                                                    @NonNull String[] permissions,
                                                    @NonNull int[] grantResults,
                                                    @NonNull Activity context,
                                                    @StringRes int message) {
    if (grantResults.length > 0) {
      // if any of them is granted, do not show rationale
      for (int grantResult : grantResults) {
        if (grantResult != PackageManager.PERMISSION_DENIED) {
          return;
        }
      }

      if (shouldShowAnyPermissionRationale(context, permissions)) {
        createPermissionRationaleDialog(context, requestCode, permissions)
            .setMessage(message)
            .show();
      }
    }
  }
}

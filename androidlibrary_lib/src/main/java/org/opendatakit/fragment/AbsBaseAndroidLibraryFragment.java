package org.opendatakit.fragment;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import org.opendatakit.activities.IAppAwareActivity;
import org.opendatakit.application.ToolAwareApplication;

/**
 * Like AbsBaseFragment but for android library instead of android common, uses IAppAwareActivity
 * instead of BaseActivity, ToolAwareApplication instead of CommonApplication
 */

public abstract class AbsBaseAndroidLibraryFragment extends Fragment {
  public IAppAwareActivity getAppActivity() {
    Activity act = getActivity();
    if (act instanceof IAppAwareActivity) {
      return (IAppAwareActivity) act;
    }
    throw new IllegalStateException("Bad activity");
  }

  public String getAppName() {
    return getAppActivity().getAppName();
  }

  public ToolAwareApplication getToolApplication() {
    Application app = getActivity().getApplication();
    if (app instanceof ToolAwareApplication) {
      return (ToolAwareApplication) app;
    }
    throw new IllegalStateException("Bad app");
  }
}

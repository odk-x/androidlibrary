package org.opendatakit.test_utils;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import org.opendatakit.activities.IAppAwareActivity;

public class TestActivity extends AppCompatActivity implements IAppAwareActivity {
    public static final String TEST_APP_NAME = "TEST_APP_NAME";

    @Override
    public String getAppName() {
        return TEST_APP_NAME;
    }

    @Override
    public Context getApplicationContext() {
        return getApplication().getApplicationContext();
    }
}

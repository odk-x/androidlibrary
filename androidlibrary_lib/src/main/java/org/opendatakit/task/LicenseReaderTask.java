/*
 * Copyright (C) 2014 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.task;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import org.opendatakit.androidlibrary.R;
import org.opendatakit.listener.LicenseReaderListener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LicenseReaderTask {

  private final ExecutorService executorService = Executors.newSingleThreadExecutor();
  private final Handler handler = new Handler(Looper.getMainLooper());
  private Application appContext;
  private LicenseReaderListener lrl;
  private String appName;
  private volatile String mResult;


 //execute() Starts the background task to read the license file
  public void execute() {
    executorService.execute(() -> {
      StringBuilder interimResult = new StringBuilder();
      String result = null;

      try {
        InputStream licenseInputStream = appContext.getResources().openRawResource(R.raw.license);
        InputStreamReader licenseInputStreamReader = new InputStreamReader(licenseInputStream);
        BufferedReader r = new BufferedReader(licenseInputStreamReader);

        String line;
        while ((line = r.readLine()) != null) {
          interimResult.append(line).append("\n");
        }

        r.close();
        licenseInputStreamReader.close();
        licenseInputStream.close();

        result = interimResult.toString();
      } catch (Exception e) {
        e.printStackTrace();
      }

      String finalResult = result;
      handler.post(() -> {
        mResult = finalResult;
        if (lrl != null) {
          lrl.readLicenseComplete(finalResult);
        }
      });
    });
  }

  //cancel() Stops the background task and notifies the listener (if any)
  public void cancel() {
    synchronized (this) {
      executorService.shutdownNow();
      handler.post(() -> {
        // can be null if cancelled before task executes
        if (lrl != null) {
          lrl.readLicenseComplete(mResult);
        }
      });
    }
  }

  public String getResult() {
    return mResult;
  }

  public void setLicenseReaderListener(LicenseReaderListener listener) {
    synchronized (this) {
      lrl = listener;
    }
  }

  public void clearLicenseReaderListener(LicenseReaderListener listener) {
    synchronized (this) {
      if (lrl == listener) {
        lrl = null;
      }
    }
  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    synchronized (this) {
      this.appName = appName;
    }
  }

  public Application getApplication() {
    return appContext;
  }

  public void setApplication(Application appContext) {
    synchronized (this) {
      this.appContext = appContext;
    }
  }
}

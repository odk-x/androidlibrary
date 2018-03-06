/*
 * Copyright (C) 2016 University of Washington
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

package org.opendatakit.sync.service.logic;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.net.URI;

/**
 * Extracted from AggregateSynchronizer during refactor.
 *
 * @author mitchellsundt@gmail.com
 */
public final class CommonFileAttachmentTerms implements Parcelable {
  public String rowPathUri;
  public File localFile;
  public URI instanceFileDownloadUri;

  public CommonFileAttachmentTerms() {
  }

  public CommonFileAttachmentTerms(Parcel in) {
    rowPathUri = in.readString();
    localFile = (File) in.readSerializable();
    instanceFileDownloadUri = (URI) in.readSerializable();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(rowPathUri);
    dest.writeSerializable(localFile);
    dest.writeSerializable(instanceFileDownloadUri);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<CommonFileAttachmentTerms> CREATOR = new Creator<CommonFileAttachmentTerms>() {
    @Override
    public CommonFileAttachmentTerms createFromParcel(Parcel in) {
      return new CommonFileAttachmentTerms(in);
    }

    @Override
    public CommonFileAttachmentTerms[] newArray(int size) {
      return new CommonFileAttachmentTerms[size];
    }
  };
}

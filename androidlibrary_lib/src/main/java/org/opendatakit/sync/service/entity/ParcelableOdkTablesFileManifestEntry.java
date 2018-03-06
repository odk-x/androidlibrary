package org.opendatakit.sync.service.entity;

import org.opendatakit.aggregate.odktables.rest.entity.OdkTablesFileManifestEntry;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableOdkTablesFileManifestEntry extends OdkTablesFileManifestEntry implements Parcelable {
  protected ParcelableOdkTablesFileManifestEntry(Parcel in) {
    filename = in.readString();
    contentLength = in.readLong();
    contentType = in.readString();
    md5hash = in.readString();
    downloadUrl = in.readString();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(filename);
    dest.writeLong(contentLength);
    dest.writeString(contentType);
    dest.writeString(md5hash);
    dest.writeString(downloadUrl);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<ParcelableOdkTablesFileManifestEntry> CREATOR = new Creator<ParcelableOdkTablesFileManifestEntry>() {
    @Override
    public ParcelableOdkTablesFileManifestEntry createFromParcel(Parcel in) {
      return new ParcelableOdkTablesFileManifestEntry(in);
    }

    @Override
    public ParcelableOdkTablesFileManifestEntry[] newArray(int size) {
      return new ParcelableOdkTablesFileManifestEntry[size];
    }
  };
}

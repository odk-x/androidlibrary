package org.opendatakit.sync.service.entity;

import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowResource;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableRowResource extends RowResource implements Parcelable {
  public ParcelableRowResource(Row row) {
    super(row);
  }

  protected ParcelableRowResource(Parcel in) {
    super(ParcelableRow.CREATOR.createFromParcel(in));
    setSelfUri(in.readString());
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    writeToParcel(this, dest, flags);
  }

  public static void writeToParcel(RowResource rowResource, Parcel dest, int flags) {
    ParcelableRow.writeToParcel(rowResource, dest, flags);
    dest.writeString(rowResource.getSelfUri());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<ParcelableRowResource> CREATOR = new Creator<ParcelableRowResource>() {
    @Override
    public ParcelableRowResource createFromParcel(Parcel in) {
      return new ParcelableRowResource(in);
    }

    @Override
    public ParcelableRowResource[] newArray(int size) {
      return new ParcelableRowResource[size];
    }
  };
}

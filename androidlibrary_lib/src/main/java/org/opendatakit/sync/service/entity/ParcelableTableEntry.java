package org.opendatakit.sync.service.entity;

import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableTableEntry extends TableEntry implements Parcelable {
  protected ParcelableTableEntry(Parcel in) {
    super(
        in.readString(),
        in.readString(),
        in.readString()
    );
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(getTableId());
    dest.writeString(getDataETag());
    dest.writeString(getSchemaETag());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<ParcelableTableEntry> CREATOR = new Creator<ParcelableTableEntry>() {
    @Override
    public ParcelableTableEntry createFromParcel(Parcel in) {
      return new ParcelableTableEntry(in);
    }

    @Override
    public ParcelableTableEntry[] newArray(int size) {
      return new ParcelableTableEntry[size];
    }
  };
}

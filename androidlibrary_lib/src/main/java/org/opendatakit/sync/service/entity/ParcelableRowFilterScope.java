package org.opendatakit.sync.service.entity;

import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableRowFilterScope extends RowFilterScope implements Parcelable {
  public ParcelableRowFilterScope(Access access, String rowOwner, String groupReadOnly, String groupModify, String groupPrivileged) {
    super(access, rowOwner, groupReadOnly, groupModify, groupPrivileged);
  }

  protected ParcelableRowFilterScope(Parcel in) {
    super(
        Access.valueOf(in.readString()),
        in.readString(),
        in.readString(),
        in.readString(),
        in.readString()
    );
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    writeToParcel(this, dest, flags);
  }

  public static void writeToParcel(RowFilterScope scope, Parcel dest, int flags) {
    dest.writeString(scope.getDefaultAccess().name());
    dest.writeString(scope.getRowOwner());
    dest.writeString(scope.getGroupReadOnly());
    dest.writeString(scope.getGroupModify());
    dest.writeString(scope.getGroupPrivileged());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<ParcelableRowFilterScope> CREATOR = new Creator<ParcelableRowFilterScope>() {
    @Override
    public ParcelableRowFilterScope createFromParcel(Parcel in) {
      return new ParcelableRowFilterScope(in);
    }

    @Override
    public ParcelableRowFilterScope[] newArray(int size) {
      return new ParcelableRowFilterScope[size];
    }
  };
}

package org.opendatakit.sync.service.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;

public class ParcelableTableResource extends TableResource implements Parcelable {
  public ParcelableTableResource(TableEntry entry) {
    super(entry);
  }

  protected ParcelableTableResource(Parcel in) {
    super(ParcelableTableEntry.CREATOR.createFromParcel(in));

    setSelfUri(in.readString());
    setDefinitionUri(in.readString());
    setDataUri(in.readString());
    setInstanceFilesUri(in.readString());
    setDiffUri(in.readString());
    setAclUri(in.readString());
    setTableLevelManifestETag(in.readString());
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    ParcelableTableEntry.writeToParcel(this, dest, flags);

    dest.writeString(getSelfUri());
    dest.writeString(getDefinitionUri());
    dest.writeString(getDataUri());
    dest.writeString(getInstanceFilesUri());
    dest.writeString(getDiffUri());
    dest.writeString(getAclUri());
    dest.writeString(getTableLevelManifestETag());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<ParcelableTableResource> CREATOR = new Creator<ParcelableTableResource>() {
    @Override
    public ParcelableTableResource createFromParcel(Parcel in) {
      return new ParcelableTableResource(in);
    }

    @Override
    public ParcelableTableResource[] newArray(int size) {
      return new ParcelableTableResource[size];
    }
  };
}

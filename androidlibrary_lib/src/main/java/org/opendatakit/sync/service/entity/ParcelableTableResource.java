package org.opendatakit.sync.service.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;

public class ParcelableTableResource extends TableResource implements Parcelable {
  public ParcelableTableResource(TableEntry entry) {
    super(entry);
  }

  public ParcelableTableResource(TableResource tableResource) {
    super(new TableEntry(tableResource.getTableId(), tableResource.getDataETag(), tableResource.getSchemaETag()));

    setSelfUri(tableResource.getSelfUri());
    setDefinitionUri(tableResource.getDefinitionUri());
    setDataUri(tableResource.getDataUri());
    setInstanceFilesUri(tableResource.getInstanceFilesUri());
    setDiffUri(tableResource.getDiffUri());
    setAclUri(tableResource.getAclUri());
    setTableLevelManifestETag(tableResource.getTableLevelManifestETag());
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
    writeToParcel(this, dest, flags);
  }

  public static void writeToParcel(TableResource tableResource, Parcel dest, int flags) {
    ParcelableTableEntry.writeToParcel(tableResource, dest, flags);

    dest.writeString(tableResource.getSelfUri());
    dest.writeString(tableResource.getDefinitionUri());
    dest.writeString(tableResource.getDataUri());
    dest.writeString(tableResource.getInstanceFilesUri());
    dest.writeString(tableResource.getDiffUri());
    dest.writeString(tableResource.getAclUri());
    dest.writeString(tableResource.getTableLevelManifestETag());
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

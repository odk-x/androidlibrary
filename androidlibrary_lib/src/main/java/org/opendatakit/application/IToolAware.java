package org.opendatakit.application;

public interface IToolAware {
  /**
   * Return the resource id for the friendly name of this application.
   *
   * @return
   */
  int getApkDisplayNameResourceId();

  String getToolName();

  String getVersionCodeString();

  String getVersionDetail();

  String getVersionedToolName();
}

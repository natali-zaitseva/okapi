/*
 * Copyright (c) 2015-2017, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */
package org.folio.okapi.bean;

/**
 * List of Permissions (and permission sets) belonging to a module. Used as a
 * parameter in the system request to initialize the permission module when a a
 * module is being enabled for a tenant.
 *
 * @author heikki
 */
public class PermissionList {
  String moduleId; // The module that own these permissions.
  Permission[] perms;

  public PermissionList() {
  }

  public PermissionList(PermissionList other) {
    this.moduleId = other.moduleId;
    this.perms = other.perms.clone();
  }

  public String getModuleId() {
    return moduleId;
  }

  public void setModuleId(String moduleId) {
    this.moduleId = moduleId;
  }

  public Permission[] getPerms() {
    return perms;
  }

  public void setPerms(Permission[] perms) {
    this.perms = perms;
  }

}

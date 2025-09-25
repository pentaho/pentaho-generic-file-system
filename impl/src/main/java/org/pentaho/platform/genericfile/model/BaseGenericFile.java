/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.genericfile.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.genericfile.model.IGenericFile;
import org.pentaho.platform.api.genericfile.model.IGenericFileMetadata;

import java.util.Date;
import java.util.List;

public class BaseGenericFile implements IGenericFile {
  private String provider;
  private String name;
  private String path;
  private String parentPath;
  private String type;
  private Date modifiedDate;
  private boolean canEdit;
  private Date deletedDate;
  private String deletedBy;
  private boolean canDelete;
  private List<IGenericFile> originalLocation;
  private String title;
  private String description;
  private String owner;
  private Date createdDate;
  private String creatorId;
  private long fileSize;
  private IGenericFileMetadata metadata;

  @NonNull
  @Override
  public String getProvider() {
    return provider;
  }

  public void setProvider( String provider ) {
    this.provider = provider;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName( String name ) {
    this.name = name;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public void setPath( String path ) {
    this.path = path;
  }

  @Override
  public String getParentPath() {
    return parentPath;
  }

  @Override
  public void setParentPath( String parentPath ) {
    this.parentPath = parentPath;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public void setType( String type ) {
    this.type = type;
  }

  @Override
  public Date getModifiedDate() {
    return modifiedDate;
  }

  @Override
  public void setModifiedDate( Date modifiedDate ) {
    this.modifiedDate = modifiedDate;
  }

  @Override
  public boolean isCanEdit() {
    return canEdit;
  }

  @Override
  public void setCanEdit( boolean canEdit ) {
    this.canEdit = canEdit;
  }

  @Override
  public List<IGenericFile> getOriginalLocation() {
    return originalLocation;
  }

  @Override
  public void setOriginalLocation( List<IGenericFile> originalLocation ) {
    this.originalLocation = originalLocation;
  }

  @Override
  public Date getDeletedDate() {
    return deletedDate;
  }

  @Override
  public void setDeletedDate( Date deletedDate ) {
    this.deletedDate = deletedDate;
  }

  @Override
  public String getDeletedBy() {
    return deletedBy;
  }

  @Override
  public void setDeletedBy( String deletedBy ) {
    this.deletedBy = deletedBy;
  }

  @Override
  public boolean isCanDelete() {
    return canDelete;
  }

  @Override
  public void setCanDelete( boolean canDelete ) {
    this.canDelete = canDelete;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public void setTitle( String title ) {
    this.title = title;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription( String description ) {
    this.description = description;
  }

  @Override
  public String getOwner() {
    return owner;
  }

  @Override
  public void setOwner( String owner ) {
    this.owner = owner;
  }

  @Override
  public Date getCreatedDate() {
    return createdDate;
  }

  @Override
  public void setCreatedDate( Date createdDate ) {
    this.createdDate = createdDate;
  }

  @Override
  public String getCreatorId() {
    return creatorId;
  }

  @Override
  public void setCreatorId( String creatorId ) {
    this.creatorId = creatorId;
  }

  @Override
  public long getFileSize() {
    return fileSize;
  }

  @Override
  public void setFileSize( long fileSize ) {
    this.fileSize = fileSize;
  }

  @Override
  public IGenericFileMetadata getMetadata() {
    return metadata;
  }

  @Override
  public void setMetadata( IGenericFileMetadata metadata ) {
    this.metadata = metadata;
  }
}

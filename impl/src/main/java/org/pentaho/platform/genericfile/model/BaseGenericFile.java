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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  private Map<String, Object> customProperties = new HashMap<>();

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

  public void setName( String name ) {
    this.name = name;
  }

  @Override
  public String getPath() {
    return path;
  }

  public void setPath( String path ) {
    this.path = path;
  }

  @Override
  public String getParentPath() {
    return parentPath;
  }

  public void setParentPath( String parentPath ) {
    this.parentPath = parentPath;
  }

  @Override
  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }

  @Override
  public Date getModifiedDate() {
    return modifiedDate;
  }

  public void setModifiedDate( Date modifiedDate ) {
    this.modifiedDate = modifiedDate;
  }

  @Override
  public boolean isCanEdit() {
    return canEdit;
  }

  public void setCanEdit( boolean canEdit ) {
    this.canEdit = canEdit;
  }

  @Override
  public List<IGenericFile> getOriginalLocation() {
    return originalLocation;
  }

  public void setOriginalLocation( List<IGenericFile> originalLocation ) {
    this.originalLocation = originalLocation;
  }

  @Override
  public Date getDeletedDate() {
    return deletedDate;
  }

  public void setDeletedDate( Date deletedDate ) {
    this.deletedDate = deletedDate;
  }

  @Override
  public String getDeletedBy() {
    return deletedBy;
  }

  public void setDeletedBy( String deletedBy ) {
    this.deletedBy = deletedBy;
  }

  @Override
  public boolean isCanDelete() {
    return canDelete;
  }

  public void setCanDelete( boolean canDelete ) {
    this.canDelete = canDelete;
  }

  @Override
  public String getTitle() {
    return title;
  }

  public void setTitle( String title ) {
    this.title = title;
  }

  @Override
  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  @Override
  public String getOwner() {
    return owner;
  }

  public void setOwner( String owner ) {
    this.owner = owner;
  }

  @Override
  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate( Date createdDate ) {
    this.createdDate = createdDate;
  }

  @Override
  public String getCreatorId() {
    return creatorId;
  }

  public void setCreatorId( String creatorId ) {
    this.creatorId = creatorId;
  }

  @Override
  public long getFileSize() {
    return fileSize;
  }

  public void setFileSize( long fileSize ) {
    this.fileSize = fileSize;
  }

  @Override
  public Map<String, Object> getCustomProperties() {
    return customProperties;
  }

  public void setCustomProperties( Map<String, Object> customProperties ) {
    this.customProperties = customProperties;
  }

  public Object getCustomProperty( String key ) {
    return customProperties.get( key );
  }

  public void addCustomProperty( String key, Object value ) {
    this.customProperties.put( key, value );
  }
}

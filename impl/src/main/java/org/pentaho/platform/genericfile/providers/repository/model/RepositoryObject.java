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


package org.pentaho.platform.genericfile.providers.repository.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.genericfile.model.IGenericFile;
import org.pentaho.platform.genericfile.model.BaseGenericFile;
import org.pentaho.platform.genericfile.providers.repository.RepositoryFileProvider;

import java.util.Date;

public abstract class RepositoryObject extends BaseGenericFile implements IGenericFile {
  private String objectId;
  private String extension;
  private String repository;
  private boolean hidden;
  private Date createdDate;
  private String creatorId;
  private long fileSize;
  private boolean schedulable;

  @NonNull
  @Override
  public String getProvider() {
    return RepositoryFileProvider.TYPE;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId( String objectId ) {
    this.objectId = objectId;
  }

  public String getExtension() {
    return extension;
  }

  public void setExtension( String extension ) {
    this.extension = extension;
  }

  public String getRepository() {
    return repository;
  }

  public void setRepository( String repository ) {
    this.repository = repository;
  }

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden( boolean hidden ) {
    this.hidden = hidden;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate( Date createdDate ) {
    this.createdDate = createdDate;
  }

  public String getCreatorId() {
    return creatorId;
  }

  public void setCreatorId( String creatorId ) {
    this.creatorId = creatorId;
  }

  public long getFileSize() {
    return fileSize;
  }

  public void setFileSize( long fileSize ) {
    this.fileSize = fileSize;
  }

  public boolean isSchedulable() {
    return schedulable;
  }

  public void setSchedulable( boolean schedulable ) {
    this.schedulable = schedulable;
  }
}

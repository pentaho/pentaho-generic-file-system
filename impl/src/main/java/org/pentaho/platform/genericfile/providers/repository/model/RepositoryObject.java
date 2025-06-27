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

public abstract class RepositoryObject extends BaseGenericFile implements IGenericFile {
  private static final String HIDDEN_PROPERTY = "hidden";
  private static final String SCHEDULABLE_PROPERTY = "schedulable";

  private String objectId;
  private String extension;
  private String repository;

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
    return Boolean.parseBoolean( String.valueOf( getCustomProperty( HIDDEN_PROPERTY ) ) );
  }

  public void setHidden( boolean hidden ) {
    addCustomProperty( HIDDEN_PROPERTY, hidden );
  }

  public boolean isSchedulable() {
    return Boolean.parseBoolean( String.valueOf( getCustomProperty( SCHEDULABLE_PROPERTY ) ) );
  }

  public void setSchedulable( boolean schedulable ) {
    addCustomProperty( SCHEDULABLE_PROPERTY, schedulable );
  }
}

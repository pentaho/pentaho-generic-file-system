/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2025 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.platform.genericfile.model;

import org.pentaho.platform.api.genericfile.model.IGenericFileMetadata;

public class BaseGenericFileMetadata implements IGenericFileMetadata {
  private String key;
  private String value;

  public BaseGenericFileMetadata( String key, String value ) {
    this.key = key;
    this.value = value;
  }

  @Override
  public String getKey() {
    return key;
  }

  public void setKey( String key ) {
    this.key = key;
  }

  @Override
  public String getValue() {
    return value;
  }

  public void setValue( String value ) {
    this.value = value;
  }
}

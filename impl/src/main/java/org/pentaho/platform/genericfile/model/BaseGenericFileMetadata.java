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

import org.pentaho.platform.api.genericfile.model.IGenericFileMetadata;

import java.util.HashMap;
import java.util.Map;

public class BaseGenericFileMetadata implements IGenericFileMetadata {
  private Map<String, String> metadata = new HashMap<>();

  @Override
  public Map<String, String> getMetadata() {
    return metadata;
  }

  @Override
  public void setMetadata( Map<String, String> metadata ) {
    this.metadata = metadata;
  }

  @Override
  public void addMetadatum( String key, String value ) {
    metadata.put( key, value );
  }
}

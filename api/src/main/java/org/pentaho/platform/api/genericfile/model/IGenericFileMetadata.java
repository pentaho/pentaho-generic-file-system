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

package org.pentaho.platform.api.genericfile.model;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@code IGenericFileMetadata} interface contains the necessary information for returning a
 * {@code IGenericFile}'s metadata.
 */
public interface IGenericFileMetadata {
  /**
   * Gets the metadata map.
   *
   * @return the metadata map. The map can be modified directly.
   */
  Map<String, String> getMetadata();

  /**
   * Sets the metadata map. Any existing metadata is replaced.
   *
   * @param metadata the metadata map to set
   */
  void setMetadata( Map<String, String> metadata );

  /**
   * Adds a single metadatum to the metadata map.
   *
   * @param key   the metadata key
   * @param value the metadata value
   */
  default void addMetadatum( String key, String value ) {
    Map<String, String> metadata = getMetadata();

    if ( metadata == null ) {
      metadata = new HashMap<>();
      setMetadata( metadata );
    }

    metadata.put( key, value );
  }
}

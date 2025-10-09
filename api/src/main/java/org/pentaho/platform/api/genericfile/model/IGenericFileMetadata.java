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

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Map;

/**
 * The {@code IGenericFileMetadata} interface contains the necessary information for returning a
 * {@code IGenericFile}'s metadata.
 */
public interface IGenericFileMetadata {
  /**
   * Gets the metadata map.
   *
   * @return the metadata map. The map can be modified directly. Never {@code null}.
   */
  @NonNull
  Map<String, String> getMetadata();

  /**
   * Sets the metadata map. Any existing metadata is replaced. The provided map is used directly, not copied.
   *
   * @param metadata the metadata map to set. Must not be {@code null}.
   */
  void setMetadata( @NonNull Map<String, String> metadata );

  /**
   * Adds a single metadatum to the metadata map. If the key already exists, the value is replaced.
   *
   * @param key   the metadata key.
   * @param value the metadata value.
   */
  default void addMetadatum( String key, String value ) {
    getMetadata().put( key, value );
  }
}

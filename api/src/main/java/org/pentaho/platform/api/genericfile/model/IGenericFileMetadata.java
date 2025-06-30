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

package org.pentaho.platform.api.genericfile.model;

/**
 * The {@code IGenericFileMetadata} interface contains the necessary information for returning a
 * {@code IGenericFile}'s metadata.
 */
public interface IGenericFileMetadata {
  /**
   * Gets the key of the metadata.
   *
   * @return the key of the metadata
   */
  String getKey();

  /**
   * Gets the value of the metadata.
   *
   * @return the value of the metadata
   */
  String getValue();
}

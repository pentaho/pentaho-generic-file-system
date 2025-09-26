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

@SuppressWarnings( "unused" )
public interface IGenericFolder extends IGenericFile {
  /**
   * Gets the type of generic file. For this specific case, always returns {@code TYPE_FOLDER}.
   *
   * @return {@code TYPE_FOLDER}
   */
  @Override
  default String getType() {
    return TYPE_FOLDER;
  }

  boolean isCanAddChildren();

  default boolean isHasChildren() {
    return true;
  }
}

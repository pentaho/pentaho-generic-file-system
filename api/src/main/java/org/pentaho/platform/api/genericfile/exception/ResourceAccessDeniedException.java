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

package org.pentaho.platform.api.genericfile.exception;

import org.pentaho.platform.api.genericfile.GenericFilePath;

/**
 * The exception class thrown when the user of the current session does not have the correct role-based access
 * security for some resource.
 */
public class ResourceAccessDeniedException extends OperationFailedException {
  private final transient GenericFilePath path;

  public ResourceAccessDeniedException() {
    super();
    this.path = null;
  }

  public ResourceAccessDeniedException( String message ) {
    super( message );
    this.path = null;
  }

  public ResourceAccessDeniedException( Throwable cause ) {
    super( cause );
    this.path = null;
  }

  public ResourceAccessDeniedException( String message, Throwable cause ) {
    super( message, cause );
    this.path = null;
  }

  public ResourceAccessDeniedException( GenericFilePath path ) {
    super();
    this.path = path;
  }

  public ResourceAccessDeniedException( String message, GenericFilePath path ) {
    super( message );
    this.path = path;
  }

  public ResourceAccessDeniedException( String message, GenericFilePath path, Throwable cause ) {
    super( message, cause );
    this.path = path;
  }

  public GenericFilePath getPath() {
    return path;
  }
}

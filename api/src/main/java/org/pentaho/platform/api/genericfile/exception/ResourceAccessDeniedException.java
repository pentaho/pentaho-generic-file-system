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

package org.pentaho.platform.api.genericfile.exception;

import org.pentaho.platform.api.genericfile.GenericFilePath;

/**
 * The exception class thrown when the user of the current session does not have the correct role-based access
 * security for some resource.
 */
public class ResourceAccessDeniedException extends OperationWithPathFailedException {
  public ResourceAccessDeniedException() {
    super();
  }

  public ResourceAccessDeniedException( String message ) {
    super( message );
  }

  public ResourceAccessDeniedException( Throwable cause ) {
    super( cause );
  }

  public ResourceAccessDeniedException( String message, Throwable cause ) {
    super( message, cause );
  }

  public ResourceAccessDeniedException( GenericFilePath path ) {
    super( path );
  }

  public ResourceAccessDeniedException( String message, GenericFilePath path ) {
    super( message, path );
  }

  public ResourceAccessDeniedException( String message, GenericFilePath path, Throwable cause ) {
    super( message, path, cause );
  }
}

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
 * The exception class thrown when a generic file assumed to exist, does not or the user does not have READ/WRITE
 * access needed to perform some operation of the
 * {@link org.pentaho.platform.api.genericfile.IGenericFileService IGenericFileService interface},
 * to not allow a user without read permissions to even know if a file exists.
 */
public class NotFoundException extends OperationWithPathFailedException {
  public NotFoundException() {
    super();
  }

  public NotFoundException( String message ) {
    super( message );
  }

  public NotFoundException( Throwable cause ) {
    super( cause );
  }

  public NotFoundException( String message, Throwable cause ) {
    super( message, cause );
  }

  public NotFoundException( GenericFilePath path ) {
    super( path );
  }

  public NotFoundException( String message, GenericFilePath path ) {
    super( message, path );
  }

  public NotFoundException( String message, GenericFilePath path, Throwable cause ) {
    super( message, path, cause );
  }
}

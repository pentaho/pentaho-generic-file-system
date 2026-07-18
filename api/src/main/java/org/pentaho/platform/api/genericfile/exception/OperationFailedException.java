/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.api.genericfile.exception;

/**
 * The base class for exceptions thrown by operations of the
 * {@link org.pentaho.platform.api.genericfile.IGenericFileService IGenericFileService interface}.
 * <p>
 * Exception instances of this class can be thrown when none of the more specific exception subclasses of this one
 * applies.
 */
public class OperationFailedException extends Exception {

  public OperationFailedException() {
    super();
  }

  public OperationFailedException( String message ) {
    super( message );
  }

  public OperationFailedException( Throwable cause ) {
    super( cause );
  }

  public OperationFailedException( String message, Throwable cause ) {
    super( message, cause );
  }
}

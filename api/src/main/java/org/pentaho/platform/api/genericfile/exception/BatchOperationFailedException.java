/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/
package org.pentaho.platform.api.genericfile.exception;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.genericfile.GenericFilePath;

import java.util.HashMap;
import java.util.Map;

public class BatchOperationFailedException extends OperationFailedException {
  private final transient Map<GenericFilePath, Exception> failedFiles;

  public BatchOperationFailedException( String message ) {
    super( message );
    this.failedFiles = new HashMap<>();
  }

  public void addFailedPath( @NonNull GenericFilePath path, Exception e ) {
    this.failedFiles.put( path, e );
    this.addSuppressed( e );
  }

  public Map<GenericFilePath, Exception> getFailedFiles() {
    return failedFiles;
  }
}

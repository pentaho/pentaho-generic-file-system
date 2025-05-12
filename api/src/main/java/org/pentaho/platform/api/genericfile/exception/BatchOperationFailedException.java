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

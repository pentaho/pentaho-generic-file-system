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

package org.pentaho.platform.genericfile.decorators;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.genericfile.GenericFilePath;
import org.pentaho.platform.api.genericfile.GetFileOptions;
import org.pentaho.platform.api.genericfile.GetTreeOptions;
import org.pentaho.platform.api.genericfile.IGenericFileDecorator;
import org.pentaho.platform.api.genericfile.IGenericFileService;
import org.pentaho.platform.api.genericfile.exception.OperationFailedException;
import org.pentaho.platform.api.genericfile.model.IGenericFile;
import org.pentaho.platform.api.genericfile.model.IGenericFileMetadata;
import org.pentaho.platform.api.genericfile.model.IGenericFileTree;
import org.pentaho.platform.util.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A composite implementation of {@link IGenericFileDecorator} that delegates to a list of other
 * {@code IGenericFileDecorator} instances.
 * Design choice: all OperationFailedException instances are caught and only logged (never propagated)
 * despite the interface declaring throws. Callers must not rely on decoration failures being surfaced.
 */
@SuppressWarnings( { "RedundantThrows", "unused", "ClassCanBeRecord" } )
public class CompositeGenericFileDecorator implements IGenericFileDecorator {
  private final List<IGenericFileDecorator> fileDecorators;

  public CompositeGenericFileDecorator( List<IGenericFileDecorator> fileDecorators ) {
    Objects.requireNonNull( fileDecorators );

    if ( fileDecorators.isEmpty() ) {
      throw new IllegalArgumentException();
    }

    this.fileDecorators = new ArrayList<>( fileDecorators );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void decorateFileMetadata( @NonNull IGenericFileMetadata fileMetadata,
                                    @NonNull GenericFilePath path,
                                    @NonNull IGenericFileService service ) throws OperationFailedException {
    Objects.requireNonNull( fileMetadata );
    Objects.requireNonNull( path );
    Objects.requireNonNull( service );

    for ( IGenericFileDecorator fileDecorator : fileDecorators ) {
      try {
        fileDecorator.decorateFileMetadata( fileMetadata, path, service );
      } catch ( OperationFailedException e ) {
        Logger.error( this.getClass().getName(), "Error decorating a file metadata.", e );
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void decorateFile( @NonNull IGenericFile file,
                            @NonNull IGenericFileService service,
                            @NonNull GetFileOptions options ) throws OperationFailedException {
    Objects.requireNonNull( file );
    Objects.requireNonNull( service );
    Objects.requireNonNull( options );

    for ( IGenericFileDecorator fileDecorator : fileDecorators ) {
      try {
        fileDecorator.decorateFile( file, service, options );
      } catch ( OperationFailedException e ) {
        Logger.error( this.getClass().getName(), "Error decorating a file.", e );
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void decorateTree( @NonNull IGenericFileTree fileTree,
                            @NonNull IGenericFileService service,
                            @NonNull GetTreeOptions options ) throws OperationFailedException {
    Objects.requireNonNull( fileTree );
    Objects.requireNonNull( service );
    Objects.requireNonNull( options );

    for ( IGenericFileDecorator fileDecorator : fileDecorators ) {
      try {
        fileDecorator.decorateTree( fileTree, service, options );
      } catch ( OperationFailedException e ) {
        Logger.error( this.getClass().getName(), "Error decorating a tree.", e );
      }
    }
  }
}

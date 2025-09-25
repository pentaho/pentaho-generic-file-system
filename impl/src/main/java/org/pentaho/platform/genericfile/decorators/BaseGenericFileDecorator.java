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
import edu.umd.cs.findbugs.annotations.Nullable;
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

import java.util.Objects;

/**
 * Base decorator implementing the template algorithm.
 * Design choice: all OperationFailedException instances are caught and only logged (never propagated)
 * despite the interface declaring throws. Callers must not rely on decoration failures being surfaced.
 * Subclasses should override hook methods only ( *Core / decorateTreeNode ).
 */
@SuppressWarnings( { "RedundantThrows", "unused" } )
public abstract class BaseGenericFileDecorator implements IGenericFileDecorator {
  /**
   * Decorate file metadata given the file path.
   *
   * @param fileMetadata the file metadata to decorate
   * @param path         the file path
   * @param service      the file service
   * @throws OperationFailedException if an error occurs during decoration
   */
  @Override
  public final void decorateFileMetadata( @NonNull IGenericFileMetadata fileMetadata,
                                          @NonNull GenericFilePath path,
                                          @NonNull IGenericFileService service ) throws OperationFailedException {
    Objects.requireNonNull( fileMetadata );
    Objects.requireNonNull( path );
    Objects.requireNonNull( service );

    try {
      decorateFileMetadataCore( fileMetadata, path, service );
    } catch ( OperationFailedException e ) {
      Logger.error( getClass().getName(), "Error decorating file metadata at path=" + path + ". Decoration skipped.",
        e );
    }
  }

  /**
   * Decorate file metadata given the file.
   *
   * @param fileMetadata the file metadata to decorate
   * @param file         the file
   * @param service      the file service
   * @throws OperationFailedException if an error occurs during decoration
   */
  protected void decorateFileMetadataCore( @NonNull IGenericFileMetadata fileMetadata,
                                           @NonNull IGenericFile file,
                                           @NonNull IGenericFileService service ) throws OperationFailedException {
    decorateFileMetadataCore( fileMetadata, GenericFilePath.parseRequired( file.getPath() ), service );
  }

  /**
   * Hook to decorate file metadata. Default implementation is a no-op.
   *
   * @param fileMetadata the file metadata to decorate
   * @param path         the file path
   * @param service      the file service
   * @throws OperationFailedException if an error occurs during decoration
   */
  protected void decorateFileMetadataCore( @NonNull IGenericFileMetadata fileMetadata,
                                           @NonNull GenericFilePath path,
                                           @NonNull IGenericFileService service ) throws OperationFailedException {
    // no-op by default
  }

  /**
   * Decorate a file. If metadata decoration is requested (in the options), it is done first.
   *
   * @param file    the file to decorate
   * @param service the file service
   * @param options the get file options
   * @throws OperationFailedException if an error occurs during decoration
   */
  @Override
  public final void decorateFile( @NonNull IGenericFile file,
                                  @NonNull IGenericFileService service,
                                  @NonNull GetFileOptions options ) throws OperationFailedException {
    Objects.requireNonNull( file );
    Objects.requireNonNull( service );
    Objects.requireNonNull( options );

    try {
      if ( options.isIncludeMetadata() ) {
        IGenericFileMetadata metadata = file.getMetadata();

        if ( metadata != null ) {
          decorateFileMetadataCore( metadata, file, service );
        }
      }
    } catch ( OperationFailedException e ) {
      Logger.error( getClass().getName(),
        "Error decorating file metadata at path=" + file.getPath() + ". Decoration skipped.", e );
    }

    try {
      decorateFileCore( file, service, options );
    } catch ( OperationFailedException e ) {
      Logger.error( getClass().getName(), "Error decorating file at path=" + file.getPath() + ". Decoration skipped.",
        e );
    }
  }

  /**
   * Mandatory hook to decorate a file (excluding metadata). The metadata decoration is handled previously and at
   * this point the file already has the decorated metadata. Default implementation is a no-op.
   *
   * @param file    the file to decorate
   * @param service the file service
   * @param options the get file options
   * @throws OperationFailedException if an error occurs during decoration
   */
  protected void decorateFileCore( @NonNull IGenericFile file,
                                   @NonNull IGenericFileService service,
                                   @NonNull GetFileOptions options ) throws OperationFailedException {
    // no-op by default
  }

  /**
   * Decorate a file tree recursively. Each node is decorated by calling
   * {@link #decorateTreeNode(IGenericFile, IGenericFileTree, IGenericFileService, GetTreeOptions)}.
   *
   * @param fileTree the file tree to decorate
   * @param service  the file service
   * @param options  the get tree options
   * @throws OperationFailedException if an error occurs during decoration
   */
  @Override
  public final void decorateTree( @NonNull IGenericFileTree fileTree,
                                  @NonNull IGenericFileService service,
                                  @NonNull GetTreeOptions options ) throws OperationFailedException {
    Objects.requireNonNull( fileTree );
    Objects.requireNonNull( service );
    Objects.requireNonNull( options );

    try {
      decorateTreeRecursively( fileTree, service, options );
    } catch ( OperationFailedException e ) {
      Logger.error( getClass().getName(), "Error decorating tree root. Partial tree may be undecorated.", e );
    }
  }

  /**
   * Recursively decorate a file tree node and its children.
   *
   * @param fileTree the file tree node to decorate
   * @param service  the file service
   * @param options  the get tree options
   * @throws OperationFailedException if an error occurs during decoration
   */
  private void decorateTreeRecursively( @Nullable IGenericFileTree fileTree,
                                        @NonNull IGenericFileService service,
                                        @NonNull GetTreeOptions options ) throws OperationFailedException {
    if ( fileTree == null ) {
      return;
    }

    decorateTreeNode( fileTree.getFile(), fileTree, service, options );

    if ( fileTree.getChildren() != null ) {
      for ( IGenericFileTree child : fileTree.getChildren() ) {
        decorateTreeRecursively( child, service, options );
      }
    }
  }

  /**
   * Maps tree options to file options. Extend if new flags are added.
   *
   * @param treeOptions the tree options
   * @return the derived file options
   */
  @NonNull
  protected GetFileOptions deriveFileOptions( @NonNull GetTreeOptions treeOptions ) {
    Objects.requireNonNull( treeOptions );

    GetFileOptions fileOptions = new GetFileOptions();
    fileOptions.setIncludeMetadata( treeOptions.isIncludeMetadata() );
    return fileOptions;
  }

  /**
   * Hook to decorate a tree node. Default delegates to file decoration.
   * Note: Do not call super + decorateFile again (super already performs file decoration).
   * Subclasses should catch and handle their own exceptions if they want finer granularity than the framework logging.
   *
   * @param file     the file to decorate
   * @param fileTree the file tree node
   * @param service  the file service
   * @param options  the get tree options
   * @throws OperationFailedException if an error occurs during decoration
   */
  @SuppressWarnings( "unused" )
  protected void decorateTreeNode( @NonNull IGenericFile file,
                                   @NonNull IGenericFileTree fileTree,
                                   @NonNull IGenericFileService service,
                                   @NonNull GetTreeOptions options ) throws OperationFailedException {
    decorateFile( file, service, deriveFileOptions( options ) );
  }
}

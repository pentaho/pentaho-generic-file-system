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


package org.pentaho.platform.genericfile;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.genericfile.GenericFilePath;
import org.pentaho.platform.api.genericfile.GenericFilePermission;
import org.pentaho.platform.api.genericfile.GetTreeOptions;
import org.pentaho.platform.api.genericfile.IGenericFileProvider;
import org.pentaho.platform.api.genericfile.IGenericFileService;
import org.pentaho.platform.api.genericfile.exception.BatchOperationFailedException;
import org.pentaho.platform.api.genericfile.exception.InvalidGenericFileProviderException;
import org.pentaho.platform.api.genericfile.exception.NotFoundException;
import org.pentaho.platform.api.genericfile.exception.OperationFailedException;
import org.pentaho.platform.api.genericfile.model.IGenericFile;
import org.pentaho.platform.api.genericfile.model.IGenericFileContentWrapper;
import org.pentaho.platform.api.genericfile.model.IGenericFileTree;
import org.pentaho.platform.genericfile.model.BaseGenericFile;
import org.pentaho.platform.genericfile.model.BaseGenericFileTree;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class DefaultGenericFileService implements IGenericFileService {

  @VisibleForTesting
  static final String MULTIPLE_PROVIDER_ROOT_PROVIDER = "combined";
  @VisibleForTesting
  static final String MULTIPLE_PROVIDER_ROOT_NAME = "root";

  private final List<IGenericFileProvider<?>> fileProviders;

  public DefaultGenericFileService( @NonNull List<IGenericFileProvider<?>> fileProviders )
    throws InvalidGenericFileProviderException {

    Objects.requireNonNull( fileProviders );

    if ( fileProviders.isEmpty() ) {
      throw new InvalidGenericFileProviderException();
    }

    // Create defensive copy to disallow external modification (and be sure there's always >= 1 provider).
    this.fileProviders = new ArrayList<>( fileProviders );
  }

  public void clearTreeCache() {
    for ( IGenericFileProvider<?> fileProvider : fileProviders ) {
      try {
        fileProvider.clearTreeCache();
      } catch ( OperationFailedException e ) {
        // Clear as many as possible. Still, log each failure.
        e.printStackTrace();
      }
    }
  }

  @NonNull
  @Override
  public List<IGenericFileTree> getRootTrees( @NonNull GetTreeOptions options ) throws OperationFailedException {
    List<IGenericFileTree> rootTrees = new ArrayList<>();

    boolean oneProviderSucceeded = false;
    Exception firstProviderException = null;

    for ( IGenericFileProvider<?> fileProvider : fileProviders ) {
      try {
        rootTrees.addAll( fileProvider.getRootTrees( options ) );
        oneProviderSucceeded = true;
      } catch ( Exception e ) {
        if ( firstProviderException == null ) {
          firstProviderException = e;
        }

        // Continue, collecting providers that work. But still log failed ones, JIC.
        e.printStackTrace();
      }
    }

    if ( firstProviderException != null && !oneProviderSucceeded ) {
      // All providers failed. Opting to throw the error of the first failed one to the caller.
      if ( firstProviderException instanceof OperationFailedException ) {
        throw (OperationFailedException) firstProviderException;
      } else {
        throw new OperationFailedException( firstProviderException );
      }
    }

    return rootTrees;
  }

  @NonNull
  public IGenericFileTree getTree( @NonNull GetTreeOptions options ) throws OperationFailedException {

    Objects.requireNonNull( options );

    if ( isSingleProviderMode() ) {
      return fileProviders.get( 0 ).getTree( options );
    }

    return options.getBasePath() == null
      ? getTreeFromRoot( options )
      : getSubTree( options.getBasePath(), options );
  }

  @VisibleForTesting
  boolean isSingleProviderMode() {
    return fileProviders.size() == 1;
  }

  @NonNull
  private IGenericFileTree getTreeFromRoot( @NonNull GetTreeOptions options ) throws OperationFailedException {
    BaseGenericFileTree rootTree = createMultipleProviderTreeRoot();
    Exception firstProviderException = null;

    for ( IGenericFileProvider<?> fileProvider : fileProviders ) {
      try {
        rootTree.addChild( fileProvider.getTree( options ) );
      } catch ( Exception e ) {
        if ( firstProviderException == null ) {
          firstProviderException = e;
        }

        // Continue, collecting providers that work. But still log failed ones, JIC.
        e.printStackTrace();
      }
    }

    if ( firstProviderException != null && rootTree.getChildren() == null ) {
      // All providers failed. Opting to throw the error of the first failed one to the caller.
      if ( firstProviderException instanceof OperationFailedException ) {
        throw (OperationFailedException) firstProviderException;
      } else {
        throw new OperationFailedException( firstProviderException );
      }
    }

    return rootTree;
  }

  @NonNull
  private static BaseGenericFileTree createMultipleProviderTreeRoot() {
    // Note that the absolute root has a null path.
    BaseGenericFile entity = new BaseGenericFile();
    entity.setName( MULTIPLE_PROVIDER_ROOT_NAME );
    entity.setProvider( MULTIPLE_PROVIDER_ROOT_PROVIDER );
    entity.setType( IGenericFile.TYPE_FOLDER );

    return new BaseGenericFileTree( entity );
  }

  @NonNull
  private IGenericFileTree getSubTree( @NonNull GenericFilePath basePath, @NonNull GetTreeOptions options )
    throws OperationFailedException {

    // In multi-provider mode, and fetching a subtree based on basePath, the parent path is the parent path of basePath.
    return getOwnerFileProvider( basePath ).getTree( options );
  }

  public boolean doesFolderExist( @NonNull GenericFilePath path ) throws OperationFailedException {
    return getOwnerFileProvider( path ).doesFolderExist( path );
  }

  public boolean createFolder( @NonNull GenericFilePath path ) throws OperationFailedException {
    return getOwnerFileProvider( path ).createFolder( path );
  }

  @Override
  @NonNull
  public IGenericFileContentWrapper getFileContentWrapper( @NonNull GenericFilePath path )
    throws OperationFailedException {
    return getOwnerFileProvider( path ).getFileContentWrapper( path );
  }

  @Override
  @NonNull
  public IGenericFile getFile( @NonNull GenericFilePath path ) throws OperationFailedException {
    return getOwnerFileProvider( path ).getFile( path );
  }

  private IGenericFileProvider<?> getOwnerFileProvider( @NonNull GenericFilePath path ) throws NotFoundException {
    return fileProviders.stream()
      .filter( fileProvider -> fileProvider.owns( path ) )
      .findFirst()
      .orElseThrow( () -> new NotFoundException( String.format( "Path not found '%s'.", path ) ) );
  }

  @Override
  public boolean hasAccess( @NonNull GenericFilePath path, @NonNull EnumSet<GenericFilePermission> permissions )
    throws OperationFailedException {
    return getOwnerFileProvider( path ).hasAccess( path, permissions );
  }

  @Override
  @NonNull
  public List<IGenericFile> getDeletedFiles() throws OperationFailedException {
    List<IGenericFile> deletedFiles = new ArrayList<>();

    boolean oneProviderSucceeded = false;
    Exception firstProviderException = null;

    for ( IGenericFileProvider<?> fileProvider : fileProviders ) {
      try {
        deletedFiles.addAll( fileProvider.getDeletedFiles() );
        oneProviderSucceeded = true;
      } catch ( Exception e ) {
        if ( firstProviderException == null ) {
          firstProviderException = e;
        }

        // Continue, collecting providers that work. But still log failed ones, JIC.
        e.printStackTrace();
      }
    }

    if ( firstProviderException != null && !oneProviderSucceeded ) {
      // All providers failed. Opting to throw the error of the first failed one to the caller.
      if ( firstProviderException instanceof OperationFailedException ) {
        throw (OperationFailedException) firstProviderException;
      } else {
        throw new OperationFailedException( firstProviderException );
      }
    }

    return deletedFiles;
  }

  @Override
  public void deleteFilesPermanently( @NonNull List<GenericFilePath> paths ) throws OperationFailedException {
    BatchOperationFailedException batchException = null;

    for ( GenericFilePath path : paths ) {
      try {
        deleteFilePermanently( path );
      } catch ( OperationFailedException e ) {
        if ( batchException == null ) {
          batchException =
            new BatchOperationFailedException( "Error(s) occurred during permanent deletion." );
        }

        batchException.addFailedPath( path, e );
      }
    }

    if ( batchException != null ) {
      throw batchException;
    }
  }

  @Override
  public void deleteFilePermanently( @NonNull GenericFilePath path ) throws OperationFailedException {
    getOwnerFileProvider( path ).deleteFilePermanently( path );
  }

  @Override
  public void deleteFiles( @NonNull List<GenericFilePath> paths, boolean permanent ) throws OperationFailedException {
    BatchOperationFailedException batchException = null;

    for ( GenericFilePath path : paths ) {
      try {
        deleteFile( path, permanent );
      } catch ( OperationFailedException e ) {
        if ( batchException == null ) {
          batchException =
            new BatchOperationFailedException( "Error(s) occurred during deletion." );
        }

        batchException.addFailedPath( path, e );
      }
    }

    if ( batchException != null ) {
      throw batchException;
    }
  }

  @Override
  public void deleteFile( @NonNull GenericFilePath path, boolean permanent ) throws OperationFailedException {
    getOwnerFileProvider( path ).deleteFile( path, permanent );
  }

  @Override
  public void restoreFiles( @NonNull List<GenericFilePath> paths ) throws OperationFailedException {
    BatchOperationFailedException batchException = null;

    for ( GenericFilePath path : paths ) {
      try {
        restoreFile( path );
      } catch ( OperationFailedException e ) {
        if ( batchException == null ) {
          batchException =
            new BatchOperationFailedException( "Error(s) occurred while attempting to restore." );
        }

        batchException.addFailedPath( path, e );
      }
    }

    if ( batchException != null ) {
      throw batchException;
    }
  }

  @Override
  public void restoreFile( @NonNull GenericFilePath path ) throws OperationFailedException {
    getOwnerFileProvider( path ).restoreFile( path );
  }

  @Override
  public void renameFile( @NonNull GenericFilePath path, @NonNull String newName ) throws OperationFailedException {
    getOwnerFileProvider( path ).renameFile( path, newName );
  }

  @Override
  public IGenericFile getFileProperties( @NonNull GenericFilePath path ) throws OperationFailedException {
    return getOwnerFileProvider( path ).getFileProperties( path );
  }

  @Override
  public IGenericFileContentWrapper downloadFile( @NonNull GenericFilePath path ) throws OperationFailedException {
    return getOwnerFileProvider( path ).downloadFile( path );
  }
}

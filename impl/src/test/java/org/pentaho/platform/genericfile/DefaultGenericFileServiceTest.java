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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.pentaho.platform.api.genericfile.GenericFilePath;
import org.pentaho.platform.api.genericfile.GetTreeOptions;
import org.pentaho.platform.api.genericfile.IGenericFileProvider;
import org.pentaho.platform.api.genericfile.exception.BatchOperationFailedException;
import org.pentaho.platform.api.genericfile.exception.InvalidGenericFileProviderException;
import org.pentaho.platform.api.genericfile.exception.InvalidPathException;
import org.pentaho.platform.api.genericfile.exception.NotFoundException;
import org.pentaho.platform.api.genericfile.exception.OperationFailedException;
import org.pentaho.platform.api.genericfile.model.IGenericFile;
import org.pentaho.platform.api.genericfile.model.IGenericFileContentWrapper;
import org.pentaho.platform.api.genericfile.model.IGenericFileTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultGenericFileServiceTest {
  // region Construction Tests
  @Test
  void testThrowsInvalidGenericFileProviderExceptionIfCreatedWithEmptyProviderList() {
    List<IGenericFileProvider<?>> providers = new ArrayList<>();
    assertThrows( InvalidGenericFileProviderException.class, () -> new DefaultGenericFileService( providers ) );
  }

  @Test
  void testCanBeCreatedWithASingleProvider() throws InvalidGenericFileProviderException {
    IGenericFileProvider<?> providerMock = mock( IGenericFileProvider.class );

    DefaultGenericFileService service = new DefaultGenericFileService( Collections.singletonList( providerMock ) );

    assertTrue( service.isSingleProviderMode() );
  }

  @Test
  void testCanBeCreatedWithTwoProviders() throws InvalidGenericFileProviderException {
    IGenericFileProvider<?> provider1Mock = mock( IGenericFileProvider.class );
    IGenericFileProvider<?> provider2Mock = mock( IGenericFileProvider.class );

    DefaultGenericFileService service = new DefaultGenericFileService( Arrays.asList( provider1Mock, provider2Mock ) );
    assertFalse( service.isSingleProviderMode() );
  }
  // endregion

  private static class MultipleProviderUseCase {
    public final IGenericFileProvider<?> provider1Mock;
    public final IGenericFileProvider<?> provider2Mock;
    public final DefaultGenericFileService service;

    public MultipleProviderUseCase() throws InvalidGenericFileProviderException {
      provider1Mock = mock( IGenericFileProvider.class );

      provider2Mock = mock( IGenericFileProvider.class );

      service = new DefaultGenericFileService( Arrays.asList( provider1Mock, provider2Mock ) );
    }
  }

  // region getTree()
  private static class GetTreeMultipleProviderUseCase extends MultipleProviderUseCase {
    public final IGenericFileTree tree1Mock;
    public final IGenericFileTree tree2Mock;
    public final GetTreeOptions optionsMock;

    public GetTreeMultipleProviderUseCase() throws OperationFailedException, InvalidGenericFileProviderException {
      tree1Mock = mock( IGenericFileTree.class );
      doReturn( tree1Mock ).when( provider1Mock ).getTree( any( GetTreeOptions.class ) );

      tree2Mock = mock( IGenericFileTree.class );
      doReturn( tree2Mock ).when( provider2Mock ).getTree( any( GetTreeOptions.class ) );

      optionsMock = mock( GetTreeOptions.class );
    }
  }

  @Test
  void testGetTreeWithSingleProviderReturnsProviderTreeDirectly()
    throws InvalidGenericFileProviderException, OperationFailedException {
    IGenericFileProvider<?> providerMock = mock( IGenericFileProvider.class );

    IGenericFileTree treeMock = mock( IGenericFileTree.class );
    when( providerMock.getTree( any( GetTreeOptions.class ) ) ).thenReturn( treeMock );

    DefaultGenericFileService service = new DefaultGenericFileService( Collections.singletonList( providerMock ) );
    GetTreeOptions optionsMock = mock( GetTreeOptions.class );

    IGenericFileTree resultTree = service.getTree( optionsMock );

    assertEquals( treeMock, resultTree );
  }

  @Test
  void testGetTreeWithMultipleProvidersAndNullBasePathAggregatesProviderTrees()
    throws InvalidGenericFileProviderException, OperationFailedException {
    GetTreeMultipleProviderUseCase useCase = new GetTreeMultipleProviderUseCase();

    IGenericFileTree aggregateTree = useCase.service.getTree( useCase.optionsMock );

    // ---

    assertNotNull( aggregateTree );
    assertNotSame( useCase.tree1Mock, aggregateTree );
    assertNotSame( useCase.tree2Mock, aggregateTree );

    // Test Aggregate Root File
    IGenericFile aggregateRoot = aggregateTree.getFile();
    assertNotNull( aggregateRoot );
    assertEquals( DefaultGenericFileService.MULTIPLE_PROVIDER_ROOT_NAME, aggregateRoot.getName() );
    assertEquals( DefaultGenericFileService.MULTIPLE_PROVIDER_ROOT_PROVIDER, aggregateRoot.getProvider() );
    assertTrue( aggregateRoot.isFolder() );

    // Test Aggregate Tree Children
    assertEquals( Arrays.asList( useCase.tree1Mock, useCase.tree2Mock ), aggregateTree.getChildren() );
  }

  @Test
  void testGetTreeWithMultipleProvidersAndNullBasePathIgnoresFailedProviders()
    throws OperationFailedException, InvalidGenericFileProviderException {
    GetTreeMultipleProviderUseCase useCase = new GetTreeMultipleProviderUseCase();

    doThrow( mock( OperationFailedException.class ) )
      .when( useCase.provider1Mock )
      .getTree( any( GetTreeOptions.class ) );

    IGenericFileTree aggregateTree = useCase.service.getTree( useCase.optionsMock );

    // ---

    assertNotNull( aggregateTree );
    assertEquals( Collections.singletonList( useCase.tree2Mock ), aggregateTree.getChildren() );
  }

  @Test
  void testGetTreeWithMultipleProvidersAndNullBasePathThrowsFirstExceptionIfAllFailed()
    throws OperationFailedException, InvalidGenericFileProviderException {
    GetTreeMultipleProviderUseCase useCase = new GetTreeMultipleProviderUseCase();

    OperationFailedException ex1 = mock( OperationFailedException.class );
    doThrow( ex1 )
      .when( useCase.provider1Mock )
      .getTree( any( GetTreeOptions.class ) );

    OperationFailedException ex2 = mock( OperationFailedException.class );
    doThrow( ex2 )
      .when( useCase.provider2Mock )
      .getTree( any( GetTreeOptions.class ) );

    try {
      useCase.service.getTree( useCase.optionsMock );
      fail();
    } catch ( OperationFailedException ex ) {
      assertSame( ex1, ex );
    }
  }

  @Test
  void testGetTreeWithMultipleProvidersAndUnknownProviderBasePathThrowsNotFoundException()
    throws OperationFailedException, InvalidGenericFileProviderException {
    GetTreeMultipleProviderUseCase useCase = new GetTreeMultipleProviderUseCase();

    doReturn( false ).when( useCase.provider1Mock ).owns( any( GenericFilePath.class ) );
    doReturn( false ).when( useCase.provider2Mock ).owns( any( GenericFilePath.class ) );

    doReturn( mock( GenericFilePath.class ) ).when( useCase.optionsMock ).getBasePath();

    assertThrows( NotFoundException.class, () -> useCase.service.getTree( useCase.optionsMock ) );
  }

  @Test
  void testGetTreeWithMultipleProvidersAndKnownProviderBasePathReturnsProviderSubtree()
    throws OperationFailedException, InvalidGenericFileProviderException {
    GetTreeMultipleProviderUseCase useCase = new GetTreeMultipleProviderUseCase();

    doReturn( false ).when( useCase.provider1Mock ).owns( any( GenericFilePath.class ) );
    doReturn( true ).when( useCase.provider2Mock ).owns( any( GenericFilePath.class ) );

    doReturn( mock( GenericFilePath.class ) ).when( useCase.optionsMock ).getBasePath();

    IGenericFileTree resultTree = useCase.service.getTree( useCase.optionsMock );

    assertSame( useCase.tree2Mock, resultTree );
    verify( useCase.provider2Mock, times( 1 ) ).getTree( useCase.optionsMock );
    verify( useCase.provider1Mock, never() ).getTree( useCase.optionsMock );
  }
  // endregion

  // region getRootTrees()
  private static class GetRootTreesMultipleProviderUseCase extends MultipleProviderUseCase {
    public final IGenericFileTree tree1Mock;
    public final IGenericFileTree tree2Mock;
    public final IGenericFileTree tree3Mock;
    public final IGenericFileTree tree4Mock;

    public final GetTreeOptions optionsMock;

    public GetRootTreesMultipleProviderUseCase() throws OperationFailedException, InvalidGenericFileProviderException {
      tree1Mock = mock( IGenericFileTree.class );
      tree2Mock = mock( IGenericFileTree.class );
      doReturn( List.of( tree1Mock, tree2Mock ) ).when( provider1Mock ).getRootTrees( any( GetTreeOptions.class ) );

      tree3Mock = mock( IGenericFileTree.class );
      tree4Mock = mock( IGenericFileTree.class );
      doReturn( List.of( tree3Mock, tree4Mock ) ).when( provider2Mock ).getRootTrees( any( GetTreeOptions.class ) );

      optionsMock = mock( GetTreeOptions.class );
    }
  }

  @Test
  void testGetTreeWithMultipleProvidersAggregatesAllProvidersRootTrees()
    throws InvalidGenericFileProviderException, OperationFailedException {
    GetRootTreesMultipleProviderUseCase useCase = new GetRootTreesMultipleProviderUseCase();

    List<IGenericFileTree> rootTrees = useCase.service.getRootTrees( useCase.optionsMock );

    // ---

    assertNotNull( rootTrees );
    assertEquals( 4, rootTrees.size() );
    assertSame( useCase.tree1Mock, rootTrees.get( 0 ) );
    assertSame( useCase.tree2Mock, rootTrees.get( 1 ) );
    assertSame( useCase.tree3Mock, rootTrees.get( 2 ) );
    assertSame( useCase.tree4Mock, rootTrees.get( 3 ) );
  }


  @Test
  void testGetTreeWithMultipleProvidersIgnoresFailedProviders()
    throws InvalidGenericFileProviderException, OperationFailedException {
    GetRootTreesMultipleProviderUseCase useCase = new GetRootTreesMultipleProviderUseCase();

    doThrow( mock( OperationFailedException.class ) )
      .when( useCase.provider1Mock )
      .getRootTrees( any( GetTreeOptions.class ) );

    List<IGenericFileTree> rootTrees = useCase.service.getRootTrees( useCase.optionsMock );

    // ---

    assertNotNull( rootTrees );
    assertEquals( 2, rootTrees.size() );
    assertSame( useCase.tree3Mock, rootTrees.get( 0 ) );
    assertSame( useCase.tree4Mock, rootTrees.get( 1 ) );
  }
  // endregion

  // region getFile
  private static class GetFileMultipleProviderUseCase extends MultipleProviderUseCase {
    public final IGenericFile file1Mock;
    public final IGenericFile file2Mock;

    public GetFileMultipleProviderUseCase() throws Exception {
      file1Mock = mock( IGenericFile.class );
      doReturn( file1Mock ).when( provider1Mock ).getFile( any( GenericFilePath.class ) );

      file2Mock = mock( IGenericFile.class );
      doReturn( file2Mock ).when( provider2Mock ).getFile( any( GenericFilePath.class ) );
    }
  }

  @Test
  void testGetFile() throws Exception {
    GetFileMultipleProviderUseCase useCase = new GetFileMultipleProviderUseCase();

    doReturn( false ).when( useCase.provider1Mock ).owns( any( GenericFilePath.class ) );
    doReturn( true ).when( useCase.provider2Mock ).owns( any( GenericFilePath.class ) );

    IGenericFile resultFile = useCase.service.getFile( mock( GenericFilePath.class ) );

    assertSame( useCase.file2Mock, resultFile );
    verify( useCase.provider2Mock, times( 1 ) ).getFile( any() );
    verify( useCase.provider1Mock, never() ).getFile( any() );
  }
  // endregion

  // region getDeletedFiles()
  private static class GetDeletedFilesMultipleProviderUseCase extends MultipleProviderUseCase {
    public final IGenericFile file1Mock;
    public final IGenericFile file2Mock;
    public final IGenericFile file3Mock;
    public final IGenericFile file4Mock;

    public GetDeletedFilesMultipleProviderUseCase()
      throws OperationFailedException, InvalidGenericFileProviderException {
      file1Mock = mock( IGenericFile.class );
      file2Mock = mock( IGenericFile.class );
      doReturn( List.of( file1Mock, file2Mock ) ).when( provider1Mock ).getDeletedFiles();

      file3Mock = mock( IGenericFile.class );
      file4Mock = mock( IGenericFile.class );
      doReturn( List.of( file3Mock, file4Mock ) ).when( provider2Mock ).getDeletedFiles();
    }
  }

  @Test
  void testGetDeletedFilesWithMultipleProvidersAggregatesAllProvidersDeletedFiles()
    throws InvalidGenericFileProviderException, OperationFailedException {
    GetDeletedFilesMultipleProviderUseCase useCase = new GetDeletedFilesMultipleProviderUseCase();

    List<IGenericFile> rootTrees = useCase.service.getDeletedFiles();

    // ---

    assertNotNull( rootTrees );
    assertEquals( 4, rootTrees.size() );
    assertSame( useCase.file1Mock, rootTrees.get( 0 ) );
    assertSame( useCase.file2Mock, rootTrees.get( 1 ) );
    assertSame( useCase.file3Mock, rootTrees.get( 2 ) );
    assertSame( useCase.file4Mock, rootTrees.get( 3 ) );
  }

  @Test
  void testGetDeletedFilesWithMultipleProvidersIgnoresFailedProviders()
    throws InvalidGenericFileProviderException, OperationFailedException {
    GetDeletedFilesMultipleProviderUseCase useCase = new GetDeletedFilesMultipleProviderUseCase();

    doThrow( mock( OperationFailedException.class ) )
      .when( useCase.provider1Mock )
      .getDeletedFiles();

    List<IGenericFile> rootTrees = useCase.service.getDeletedFiles();

    // ---

    assertNotNull( rootTrees );
    assertEquals( 2, rootTrees.size() );
    assertSame( useCase.file3Mock, rootTrees.get( 0 ) );
    assertSame( useCase.file4Mock, rootTrees.get( 1 ) );
  }
  // endregion

  // region deleteFilePermanently
  private static class DeleteFilesPermanentlyMultipleProviderUseCase extends MultipleProviderUseCase {
    public final GenericFilePath path1;
    public final GenericFilePath path2;

    public DeleteFilesPermanentlyMultipleProviderUseCase() throws InvalidGenericFileProviderException {
      path1 = mock( GenericFilePath.class );
      path2 = mock( GenericFilePath.class );

      doReturn( true ).when( provider1Mock ).owns( path1 );
      doReturn( false ).when( provider1Mock ).owns( path2 );

      doReturn( false ).when( provider2Mock ).owns( path1 );
      doReturn( true ).when( provider2Mock ).owns( path2 );
    }
  }

  @Test
  void testDeleteFilesPermanentlySuccess() throws Exception {
    DeleteFilesPermanentlyMultipleProviderUseCase useCase = new DeleteFilesPermanentlyMultipleProviderUseCase();

    useCase.service.deleteFilesPermanently( Arrays.asList( useCase.path1, useCase.path2 ) );

    verify( useCase.provider1Mock ).deleteFilePermanently( useCase.path1 );
    verify( useCase.provider2Mock ).deleteFilePermanently( useCase.path2 );
  }

  @Test
  void testDeleteFilePermanentlyPathNotFound() throws Exception {
    DeleteFilesPermanentlyMultipleProviderUseCase useCase = new DeleteFilesPermanentlyMultipleProviderUseCase();

    doReturn( false ).when( useCase.provider1Mock ).owns( useCase.path1 );

    BatchOperationFailedException exception = assertThrows( BatchOperationFailedException.class,
      () -> useCase.service.deleteFilesPermanently( Collections.singletonList( useCase.path1 ) ) );

    Map<GenericFilePath, Exception> failedFiles = exception.getFailedFiles();

    assertEquals( "Error(s) occurred during permanent deletion.", exception.getMessage() );
    assertNotNull( failedFiles );
    assertFalse( failedFiles.isEmpty() );
    assertEquals( 1, failedFiles.size() );
    assertTrue( failedFiles.containsKey( useCase.path1 ) );
    assertEquals( "Path not found '" + useCase.path1 + "'.", failedFiles.get( useCase.path1 ).getMessage() );
    assertEquals( NotFoundException.class, exception.getSuppressed()[ 0 ].getClass() );
    verify( useCase.provider1Mock, never() ).deleteFilePermanently( any( GenericFilePath.class ) );
  }

  @Test
  void testDeleteFilePermanentlyInvalidPath() throws Exception {
    DeleteFilesPermanentlyMultipleProviderUseCase useCase = new DeleteFilesPermanentlyMultipleProviderUseCase();

    doThrow( InvalidPathException.class ).when( useCase.provider1Mock ).deleteFilePermanently( useCase.path1 );

    InvalidPathException exception =
      assertThrows( InvalidPathException.class, () -> useCase.service.deleteFilePermanently( useCase.path1 ) );

    assertEquals( InvalidPathException.class, exception.getClass() );
  }

  @Test
  void testDeleteFilesPermanentlyException() throws Exception {
    DeleteFilesPermanentlyMultipleProviderUseCase useCase = new DeleteFilesPermanentlyMultipleProviderUseCase();

    doThrow( new OperationFailedException( "Deletion failed." ) ).when( useCase.provider1Mock )
      .deleteFilePermanently( useCase.path1 );

    BatchOperationFailedException exception = assertThrows( BatchOperationFailedException.class,
      () -> useCase.service.deleteFilesPermanently( Arrays.asList( useCase.path1, useCase.path2 ) )
    );

    Map<GenericFilePath, Exception> failedFiles = exception.getFailedFiles();

    assertEquals( "Error(s) occurred during permanent deletion.", exception.getMessage() );
    assertNotNull( failedFiles );
    assertFalse( failedFiles.isEmpty() );
    assertEquals( 1, failedFiles.size() );
    assertTrue( failedFiles.containsKey( useCase.path1 ) );
    assertEquals( "Deletion failed.", failedFiles.get( useCase.path1 ).getMessage() );
    verify( useCase.provider1Mock ).deleteFilePermanently( useCase.path1 );
    verify( useCase.provider2Mock ).deleteFilePermanently( useCase.path2 );
  }
  // endregion

  // region deleteFile
  private static class DeleteFilesMultipleProviderUseCase extends MultipleProviderUseCase {
    public final GenericFilePath path1;
    public final GenericFilePath path2;

    public DeleteFilesMultipleProviderUseCase() throws InvalidGenericFileProviderException {
      path1 = mock( GenericFilePath.class );
      path2 = mock( GenericFilePath.class );

      doReturn( true ).when( provider1Mock ).owns( path1 );
      doReturn( false ).when( provider1Mock ).owns( path2 );

      doReturn( false ).when( provider2Mock ).owns( path1 );
      doReturn( true ).when( provider2Mock ).owns( path2 );
    }
  }

  @ParameterizedTest
  @ValueSource( booleans = { true, false } )
  void testDeleteFilesSuccess( boolean permanent ) throws Exception {
    DeleteFilesMultipleProviderUseCase useCase = new DeleteFilesMultipleProviderUseCase();

    useCase.service.deleteFiles( Arrays.asList( useCase.path1, useCase.path2 ), permanent );

    verify( useCase.provider1Mock ).deleteFile( useCase.path1, permanent );
    verify( useCase.provider2Mock ).deleteFile( useCase.path2, permanent );
  }

  @ParameterizedTest
  @ValueSource( booleans = { true, false } )
  void testDeleteFilePathNotFound( boolean permanent ) throws Exception {
    DeleteFilesMultipleProviderUseCase useCase = new DeleteFilesMultipleProviderUseCase();

    doReturn( false ).when( useCase.provider1Mock ).owns( useCase.path1 );

    BatchOperationFailedException exception = assertThrows( BatchOperationFailedException.class,
      () -> useCase.service.deleteFiles( Collections.singletonList( useCase.path1 ), permanent ) );

    Map<GenericFilePath, Exception> failedFiles = exception.getFailedFiles();

    assertEquals( "Error(s) occurred during deletion.", exception.getMessage() );
    assertNotNull( failedFiles );
    assertFalse( failedFiles.isEmpty() );
    assertEquals( 1, failedFiles.size() );
    assertTrue( failedFiles.containsKey( useCase.path1 ) );
    assertEquals( "Path not found '" + useCase.path1 + "'.", failedFiles.get( useCase.path1 ).getMessage() );
    verify( useCase.provider1Mock, never() ).deleteFile( any( GenericFilePath.class ), eq( permanent ) );
  }

  @ParameterizedTest
  @ValueSource( booleans = { true, false } )
  void testDeleteFilesException( boolean permanent ) throws Exception {
    DeleteFilesMultipleProviderUseCase useCase = new DeleteFilesMultipleProviderUseCase();

    doThrow( new OperationFailedException( "Deletion failed." ) ).when( useCase.provider1Mock )
      .deleteFile( useCase.path1, permanent );

    BatchOperationFailedException exception = assertThrows( BatchOperationFailedException.class, () ->
      useCase.service.deleteFiles( Arrays.asList( useCase.path1, useCase.path2 ), permanent )
    );

    Map<GenericFilePath, Exception> failedFiles = exception.getFailedFiles();

    assertEquals( "Error(s) occurred during deletion.", exception.getMessage() );
    assertNotNull( failedFiles );
    assertFalse( failedFiles.isEmpty() );
    assertEquals( 1, failedFiles.size() );
    assertTrue( failedFiles.containsKey( useCase.path1 ) );
    assertEquals( "Deletion failed.", failedFiles.get( useCase.path1 ).getMessage() );
    verify( useCase.provider1Mock ).deleteFile( useCase.path1, permanent );
    verify( useCase.provider2Mock ).deleteFile( useCase.path2, permanent );
  }
  // endregion

  // region restoreFile
  private static class RestoreFilesMultipleProviderUseCase extends MultipleProviderUseCase {
    public final GenericFilePath path1;
    public final GenericFilePath path2;

    public RestoreFilesMultipleProviderUseCase() throws InvalidGenericFileProviderException {
      path1 = mock( GenericFilePath.class );
      path2 = mock( GenericFilePath.class );

      doReturn( true ).when( provider1Mock ).owns( path1 );
      doReturn( false ).when( provider1Mock ).owns( path2 );

      doReturn( false ).when( provider2Mock ).owns( path1 );
      doReturn( true ).when( provider2Mock ).owns( path2 );
    }
  }

  @Test
  void testRestoreFilesSuccess() throws Exception {
    RestoreFilesMultipleProviderUseCase useCase = new RestoreFilesMultipleProviderUseCase();

    useCase.service.restoreFiles( Arrays.asList( useCase.path1, useCase.path2 ) );

    verify( useCase.provider1Mock ).restoreFile( useCase.path1 );
    verify( useCase.provider2Mock ).restoreFile( useCase.path2 );
  }

  @Test
  void testRestoreFilePathNotFound() throws Exception {
    RestoreFilesMultipleProviderUseCase useCase = new RestoreFilesMultipleProviderUseCase();

    doReturn( false ).when( useCase.provider1Mock ).owns( useCase.path1 );

    BatchOperationFailedException exception = assertThrows( BatchOperationFailedException.class,
      () -> useCase.service.restoreFiles( Collections.singletonList( useCase.path1 ) ) );

    Map<GenericFilePath, Exception> failedFiles = exception.getFailedFiles();

    assertEquals( "Error(s) occurred while attempting to restore.", exception.getMessage() );
    assertNotNull( failedFiles );
    assertFalse( failedFiles.isEmpty() );
    assertEquals( 1, failedFiles.size() );
    assertTrue( failedFiles.containsKey( useCase.path1 ) );
    assertEquals( "Path not found '" + useCase.path1 + "'.", failedFiles.get( useCase.path1 ).getMessage() );
    verify( useCase.provider1Mock, never() ).restoreFile( any( GenericFilePath.class ) );
  }

  @Test
  void testRestoreFilesException() throws Exception {
    RestoreFilesMultipleProviderUseCase useCase = new RestoreFilesMultipleProviderUseCase();

    doThrow( new OperationFailedException( "Restore failed." ) ).when( useCase.provider1Mock )
      .restoreFile( useCase.path1 );

    BatchOperationFailedException exception = assertThrows( BatchOperationFailedException.class, () ->
      useCase.service.restoreFiles( Arrays.asList( useCase.path1, useCase.path2 ) )
    );

    Map<GenericFilePath, Exception> failedFiles = exception.getFailedFiles();

    assertEquals( "Error(s) occurred while attempting to restore.", exception.getMessage() );
    assertNotNull( failedFiles );
    assertFalse( failedFiles.isEmpty() );
    assertEquals( 1, failedFiles.size() );
    assertTrue( failedFiles.containsKey( useCase.path1 ) );
    assertEquals( "Restore failed.", failedFiles.get( useCase.path1 ).getMessage() );
    verify( useCase.provider1Mock ).restoreFile( useCase.path1 );
    verify( useCase.provider2Mock ).restoreFile( useCase.path2 );
  }
  // endregion

  // region renameFile
  private static class RenameFilesMultipleProviderUseCase extends MultipleProviderUseCase {
    public final GenericFilePath path1;
    public final GenericFilePath path2;

    public RenameFilesMultipleProviderUseCase() throws InvalidGenericFileProviderException {
      path1 = mock( GenericFilePath.class );
      path2 = mock( GenericFilePath.class );

      doReturn( true ).when( provider1Mock ).owns( path1 );
      doReturn( false ).when( provider1Mock ).owns( path2 );

      doReturn( false ).when( provider2Mock ).owns( path1 );
      doReturn( true ).when( provider2Mock ).owns( path2 );
    }
  }

  @Test
  void testRenameFilesSuccess() throws Exception {
    RenameFilesMultipleProviderUseCase useCase = new RenameFilesMultipleProviderUseCase();

    useCase.service.renameFile( useCase.path1, "newName1" );
    useCase.service.renameFile( useCase.path2, "newName2" );

    verify( useCase.provider1Mock ).renameFile( useCase.path1, "newName1" );
    verify( useCase.provider2Mock ).renameFile( useCase.path2, "newName2" );
  }

  @Test
  void testRenameFilePathNotFound() throws Exception {
    RenameFilesMultipleProviderUseCase useCase = new RenameFilesMultipleProviderUseCase();

    doReturn( false ).when( useCase.provider1Mock ).owns( useCase.path1 );

    NotFoundException exception = assertThrows( NotFoundException.class,
      () -> useCase.service.renameFile( useCase.path1, "newName" ) );

    assertEquals( "Path not found '" + useCase.path1 + "'.", exception.getMessage() );
    verify( useCase.provider1Mock, never() ).renameFile( any( GenericFilePath.class ), any() );
  }

  @Test
  void testRenameFileException() throws Exception {
    RenameFilesMultipleProviderUseCase useCase = new RenameFilesMultipleProviderUseCase();

    doThrow( new OperationFailedException( "Rename failed." ) ).when( useCase.provider1Mock )
      .renameFile( useCase.path1, "newName" );

    OperationFailedException exception = assertThrows( OperationFailedException.class,
      () -> useCase.service.renameFile( useCase.path1, "newName" ) );

    assertEquals( "Rename failed.", exception.getMessage() );
    verify( useCase.provider1Mock ).renameFile( useCase.path1, "newName" );
  }
  // endregion

  // region getFileProperties
  private static class GetFilePropertiesMultipleProviderUseCase extends MultipleProviderUseCase {
    public final GenericFilePath path1;
    public final GenericFilePath path2;

    public GetFilePropertiesMultipleProviderUseCase() throws InvalidGenericFileProviderException {
      path1 = mock( GenericFilePath.class );
      path2 = mock( GenericFilePath.class );

      doReturn( true ).when( provider1Mock ).owns( path1 );
      doReturn( false ).when( provider1Mock ).owns( path2 );

      doReturn( false ).when( provider2Mock ).owns( path1 );
      doReturn( true ).when( provider2Mock ).owns( path2 );
    }
  }

  @Test
  void testGetFilePropertiesSuccess() throws Exception {
    GetFilePropertiesMultipleProviderUseCase useCase = new GetFilePropertiesMultipleProviderUseCase();

    IGenericFile mockFile1 = mock( IGenericFile.class );
    IGenericFile mockFile2 = mock( IGenericFile.class );
    doReturn( mockFile1 ).when( useCase.provider1Mock ).getFileProperties( useCase.path1 );
    doReturn( mockFile2 ).when( useCase.provider2Mock ).getFileProperties( useCase.path2 );

    IGenericFile result1 = useCase.service.getFileProperties( useCase.path1 );
    IGenericFile result2 = useCase.service.getFileProperties( useCase.path2 );

    assertEquals( mockFile1, result1 );
    assertEquals( mockFile2, result2 );
    verify( useCase.provider1Mock ).getFileProperties( useCase.path1 );
    verify( useCase.provider2Mock ).getFileProperties( useCase.path2 );
  }

  @Test
  void testGetFilePropertiesPathNotFound() throws Exception {
    GetFilePropertiesMultipleProviderUseCase useCase = new GetFilePropertiesMultipleProviderUseCase();

    doReturn( false ).when( useCase.provider1Mock ).owns( useCase.path1 );

    NotFoundException exception = assertThrows( NotFoundException.class,
      () -> useCase.service.getFileProperties( useCase.path1 ) );

    assertEquals( "Path not found '" + useCase.path1 + "'.", exception.getMessage() );
    verify( useCase.provider1Mock, never() ).getFileProperties( any( GenericFilePath.class ) );
  }

  @Test
  void testGetFilePropertiesException() throws Exception {
    GetFilePropertiesMultipleProviderUseCase useCase = new GetFilePropertiesMultipleProviderUseCase();

    doThrow( new OperationFailedException( "Get file properties failed." ) ).when( useCase.provider1Mock )
      .getFileProperties( useCase.path1 );

    OperationFailedException exception = assertThrows( OperationFailedException.class,
      () -> useCase.service.getFileProperties( useCase.path1 ) );

    assertEquals( "Get file properties failed.", exception.getMessage() );
    verify( useCase.provider1Mock ).getFileProperties( useCase.path1 );
  }
  // endregion

  // region getRootProperties
  private static class GetRootPropertiesMultipleProviderUseCase extends MultipleProviderUseCase {
    public final GenericFilePath path1;
    public final GenericFilePath path2;

    public GetRootPropertiesMultipleProviderUseCase() throws InvalidGenericFileProviderException {
      path1 = mock( GenericFilePath.class );
      path2 = mock( GenericFilePath.class );

      doReturn( true ).when( provider1Mock ).owns( path1 );
      doReturn( false ).when( provider1Mock ).owns( path2 );

      doReturn( false ).when( provider2Mock ).owns( path1 );
      doReturn( true ).when( provider2Mock ).owns( path2 );
    }
  }

  @Test
  void testGetRootPropertiesSuccess() throws Exception {
    GetRootPropertiesMultipleProviderUseCase useCase = new GetRootPropertiesMultipleProviderUseCase();

    IGenericFile root1 = mock( IGenericFile.class );
    IGenericFile root2 = mock( IGenericFile.class );
    doReturn( root1 ).when( useCase.provider1Mock ).getRootProperties();
    doReturn( root2 ).when( useCase.provider2Mock ).getRootProperties();

    List<IGenericFile> result = useCase.service.getRootProperties();

    assertTrue( result.contains( root1 ) );
    assertTrue( result.contains( root2 ) );
    verify( useCase.provider1Mock ).getRootProperties();
    verify( useCase.provider2Mock ).getRootProperties();
  }

  @Test
  void testGetRootPropertiesException() throws Exception {
    GetRootPropertiesMultipleProviderUseCase useCase = new GetRootPropertiesMultipleProviderUseCase();

    IGenericFile root2 = mock( IGenericFile.class );
    doThrow( new OperationFailedException( "Root properties failed." ) ).when( useCase.provider1Mock )
      .getRootProperties();
    doReturn( root2 ).when( useCase.provider2Mock ).getRootProperties();

    List<IGenericFile> result = useCase.service.getRootProperties();

    assertFalse( result.isEmpty() );
    assertTrue( result.contains( root2 ) );
    verify( useCase.provider1Mock ).getRootProperties();
    verify( useCase.provider2Mock ).getRootProperties();
  }
  // endregion

  // region downloadFile
  private static class DownloadFileMultipleProviderUseCase extends MultipleProviderUseCase {
    public final GenericFilePath path1;
    public final GenericFilePath path2;

    public DownloadFileMultipleProviderUseCase() throws InvalidGenericFileProviderException {
      path1 = mock( GenericFilePath.class );
      path2 = mock( GenericFilePath.class );

      doReturn( true ).when( provider1Mock ).owns( path1 );
      doReturn( false ).when( provider1Mock ).owns( path2 );

      doReturn( false ).when( provider2Mock ).owns( path1 );
      doReturn( true ).when( provider2Mock ).owns( path2 );
    }
  }

  @Test
  void testDownloadFileSuccess() throws Exception {
    DownloadFileMultipleProviderUseCase useCase = new DownloadFileMultipleProviderUseCase();

    IGenericFileContentWrapper mockWrapper1 = mock( IGenericFileContentWrapper.class );
    IGenericFileContentWrapper mockWrapper2 = mock( IGenericFileContentWrapper.class );
    doReturn( mockWrapper1 ).when( useCase.provider1Mock ).downloadFile( useCase.path1 );
    doReturn( mockWrapper2 ).when( useCase.provider2Mock ).downloadFile( useCase.path2 );

    IGenericFileContentWrapper result1 = useCase.service.downloadFile( useCase.path1 );
    IGenericFileContentWrapper result2 = useCase.service.downloadFile( useCase.path2 );

    assertEquals( mockWrapper1, result1 );
    assertEquals( mockWrapper2, result2 );
    verify( useCase.provider1Mock ).downloadFile( useCase.path1 );
    verify( useCase.provider2Mock ).downloadFile( useCase.path2 );
  }

  @Test
  void testDownloadFilePathNotFound() throws Exception {
    DownloadFileMultipleProviderUseCase useCase = new DownloadFileMultipleProviderUseCase();

    doReturn( false ).when( useCase.provider1Mock ).owns( useCase.path1 );

    NotFoundException exception = assertThrows( NotFoundException.class,
      () -> useCase.service.downloadFile( useCase.path1 ) );

    assertEquals( "Path not found '" + useCase.path1 + "'.", exception.getMessage() );
    verify( useCase.provider1Mock, never() ).downloadFile( any( GenericFilePath.class ) );
  }

  @Test
  void testDownloadFileException() throws Exception {
    DownloadFileMultipleProviderUseCase useCase = new DownloadFileMultipleProviderUseCase();

    doThrow( new OperationFailedException( "Download failed." ) ).when( useCase.provider1Mock )
      .downloadFile( useCase.path1 );

    OperationFailedException exception = assertThrows( OperationFailedException.class,
      () -> useCase.service.downloadFile( useCase.path1 ) );

    assertEquals( "Download failed.", exception.getMessage() );
    verify( useCase.provider1Mock ).downloadFile( useCase.path1 );
  }
  // endregion
}

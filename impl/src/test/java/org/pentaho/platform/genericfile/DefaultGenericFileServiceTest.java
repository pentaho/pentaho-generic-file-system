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
import org.pentaho.platform.api.genericfile.model.IGenericFileContent;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
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

    // Test Aggregate Tree's Children
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

    assertEquals( "Error(s) occurred while attempting to restore files.", exception.getMessage() );
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

    assertEquals( "Error(s) occurred while attempting to restore files.", exception.getMessage() );
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

  // region copyFile
  private static class CopyFilesMultipleProviderUseCase extends MultipleProviderUseCase {
    public final GenericFilePath path1;
    public final GenericFilePath path2;
    public final GenericFilePath destPath;

    public CopyFilesMultipleProviderUseCase() throws InvalidGenericFileProviderException {
      path1 = mock( GenericFilePath.class );
      path2 = mock( GenericFilePath.class );
      destPath = mock( GenericFilePath.class );

      doReturn( true ).when( provider1Mock ).owns( path1 );
      doReturn( false ).when( provider1Mock ).owns( path2 );

      doReturn( false ).when( provider2Mock ).owns( path1 );
      doReturn( true ).when( provider2Mock ).owns( path2 );
    }
  }

  @Test
  void testCopyFilesSuccess() throws Exception {
    CopyFilesMultipleProviderUseCase useCase = new CopyFilesMultipleProviderUseCase();

    useCase.service.copyFiles( Arrays.asList( useCase.path1, useCase.path2 ), useCase.destPath );

    verify( useCase.provider1Mock ).copyFile( useCase.path1, useCase.destPath );
    verify( useCase.provider2Mock ).copyFile( useCase.path2, useCase.destPath );
  }

  @Test
  void testCopyFilePathNotFound() throws Exception {
    CopyFilesMultipleProviderUseCase useCase = new CopyFilesMultipleProviderUseCase();

    doReturn( false ).when( useCase.provider1Mock ).owns( useCase.path1 );

    BatchOperationFailedException exception = assertThrows( BatchOperationFailedException.class,
      () -> useCase.service.copyFiles( Collections.singletonList( useCase.path1 ), useCase.destPath ) );

    Map<GenericFilePath, Exception> failedFiles = exception.getFailedFiles();

    assertEquals( "Error(s) occurred while attempting to copy files.", exception.getMessage() );
    assertNotNull( failedFiles );
    assertFalse( failedFiles.isEmpty() );
    assertEquals( 1, failedFiles.size() );
    assertTrue( failedFiles.containsKey( useCase.path1 ) );
    assertEquals( "Path not found '" + useCase.path1 + "'.", failedFiles.get( useCase.path1 ).getMessage() );
    verify( useCase.provider1Mock, never() ).copyFile( any( GenericFilePath.class ), any( GenericFilePath.class ) );
  }

  @Test
  void testCopyFilesException() throws Exception {
    CopyFilesMultipleProviderUseCase useCase = new CopyFilesMultipleProviderUseCase();

    doThrow( new OperationFailedException( "Copy failed." ) ).when( useCase.provider1Mock )
      .copyFile( useCase.path1, useCase.destPath );

    BatchOperationFailedException exception = assertThrows( BatchOperationFailedException.class,
      () -> useCase.service.copyFiles( Arrays.asList( useCase.path1, useCase.path2 ), useCase.destPath ) );

    Map<GenericFilePath, Exception> failedFiles = exception.getFailedFiles();

    assertEquals( "Error(s) occurred while attempting to copy files.", exception.getMessage() );
    assertNotNull( failedFiles );
    assertFalse( failedFiles.isEmpty() );
    assertEquals( 1, failedFiles.size() );
    assertTrue( failedFiles.containsKey( useCase.path1 ) );
    assertEquals( "Copy failed.", exception.getFailedFiles().get( useCase.path1 ).getMessage() );
    verify( useCase.provider1Mock ).copyFile( useCase.path1, useCase.destPath );
    verify( useCase.provider2Mock ).copyFile( useCase.path2, useCase.destPath );
  }
  // endregion

  // region moveFile
  private static class MoveFilesMultipleProviderUseCase extends MultipleProviderUseCase {
    public final GenericFilePath path1;
    public final GenericFilePath path2;
    public final GenericFilePath destPath;

    public MoveFilesMultipleProviderUseCase() throws InvalidGenericFileProviderException {
      path1 = mock( GenericFilePath.class );
      path2 = mock( GenericFilePath.class );
      destPath = mock( GenericFilePath.class );

      doReturn( true ).when( provider1Mock ).owns( path1 );
      doReturn( false ).when( provider1Mock ).owns( path2 );

      doReturn( false ).when( provider2Mock ).owns( path1 );
      doReturn( true ).when( provider2Mock ).owns( path2 );
    }
  }

  @Test
  void testMoveFilesSuccess() throws Exception {
    MoveFilesMultipleProviderUseCase useCase = new MoveFilesMultipleProviderUseCase();

    useCase.service.moveFiles( Arrays.asList( useCase.path1, useCase.path2 ), useCase.destPath );

    verify( useCase.provider1Mock ).moveFile( useCase.path1, useCase.destPath );
    verify( useCase.provider2Mock ).moveFile( useCase.path2, useCase.destPath );
  }

  @Test
  void testMoveFilePathNotFound() throws Exception {
    MoveFilesMultipleProviderUseCase useCase = new MoveFilesMultipleProviderUseCase();

    doReturn( false ).when( useCase.provider1Mock ).owns( useCase.path1 );

    BatchOperationFailedException exception = assertThrows( BatchOperationFailedException.class,
      () -> useCase.service.moveFiles( Collections.singletonList( useCase.path1 ), useCase.destPath ) );

    Map<GenericFilePath, Exception> failedFiles = exception.getFailedFiles();

    assertEquals( "Error(s) occurred while attempting to move files.", exception.getMessage() );
    assertNotNull( failedFiles );
    assertFalse( failedFiles.isEmpty() );
    assertEquals( 1, failedFiles.size() );
    assertTrue( failedFiles.containsKey( useCase.path1 ) );
    assertEquals( "Path not found '" + useCase.path1 + "'.", failedFiles.get( useCase.path1 ).getMessage() );
    verify( useCase.provider1Mock, never() ).moveFile( any( GenericFilePath.class ), any( GenericFilePath.class ) );
  }

  @Test
  void testMoveFilesException() throws Exception {
    MoveFilesMultipleProviderUseCase useCase = new MoveFilesMultipleProviderUseCase();

    doThrow( new OperationFailedException( "Move failed." ) ).when( useCase.provider1Mock )
      .moveFile( useCase.path1, useCase.destPath );

    BatchOperationFailedException exception = assertThrows( BatchOperationFailedException.class,
      () -> useCase.service.moveFiles( Arrays.asList( useCase.path1, useCase.path2 ), useCase.destPath ) );

    Map<GenericFilePath, Exception> failedFiles = exception.getFailedFiles();

    assertEquals( "Error(s) occurred while attempting to move files.", exception.getMessage() );
    assertNotNull( failedFiles );
    assertFalse( failedFiles.isEmpty() );
    assertEquals( 1, failedFiles.size() );
    assertTrue( failedFiles.containsKey( useCase.path1 ) );
    assertEquals( "Move failed.", exception.getFailedFiles().get( useCase.path1 ).getMessage() );
    verify( useCase.provider1Mock ).moveFile( useCase.path1, useCase.destPath );
    verify( useCase.provider2Mock ).moveFile( useCase.path2, useCase.destPath );
  }
  // endregion

  // region getFileContent
  private static class GetFileContentMultipleProviderUseCase extends MultipleProviderUseCase {
    public final GenericFilePath path1;
    public final GenericFilePath path2;

    public GetFileContentMultipleProviderUseCase() throws InvalidGenericFileProviderException {
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
  void testGetFileContentSuccess( boolean compressed ) throws Exception {
    GetFileContentMultipleProviderUseCase useCase = new GetFileContentMultipleProviderUseCase();
    IGenericFileContent content1 = mock( IGenericFileContent.class );
    IGenericFileContent content2 = mock( IGenericFileContent.class );

    doReturn( content1 ).when( useCase.provider1Mock ).getFileContent( useCase.path1, compressed );
    doReturn( content2 ).when( useCase.provider2Mock ).getFileContent( useCase.path2, compressed );

    assertSame( content1, useCase.service.getFileContent( useCase.path1, compressed ) );
    assertSame( content2, useCase.service.getFileContent( useCase.path2, compressed ) );
    verify( useCase.provider1Mock ).getFileContent( useCase.path1, compressed );
    verify( useCase.provider2Mock ).getFileContent( useCase.path2, compressed );
  }

  @ParameterizedTest
  @ValueSource( booleans = { true, false } )
  void testGetFileContentPathNotFound( boolean compressed ) throws Exception {
    GetFileContentMultipleProviderUseCase useCase = new GetFileContentMultipleProviderUseCase();
    doReturn( false ).when( useCase.provider1Mock ).owns( useCase.path1 );

    NotFoundException ex =
      assertThrows( NotFoundException.class, () -> useCase.service.getFileContent( useCase.path1, compressed ) );
    assertEquals( "Path not found '" + useCase.path1 + "'.", ex.getMessage() );
    verify( useCase.provider1Mock, never() ).getFileContent( any(), anyBoolean() );
  }

  @ParameterizedTest
  @ValueSource( booleans = { true, false } )
  void testGetFileContentException( boolean compressed ) throws Exception {
    GetFileContentMultipleProviderUseCase useCase = new GetFileContentMultipleProviderUseCase();
    doThrow( new OperationFailedException( "Read failed." ) ).when( useCase.provider1Mock )
      .getFileContent( useCase.path1, compressed );

    OperationFailedException ex =
      assertThrows( OperationFailedException.class, () -> useCase.service.getFileContent( useCase.path1, compressed ) );
    assertEquals( "Read failed.", ex.getMessage() );
    verify( useCase.provider1Mock ).getFileContent( useCase.path1, compressed );
  }
  // endregion

  // region createFolder
  private static class CreateFolderMultipleProviderUseCase extends MultipleProviderUseCase {
    public final GenericFilePath path1;
    public final GenericFilePath path2;

    public CreateFolderMultipleProviderUseCase() throws InvalidGenericFileProviderException {
      path1 = mock( GenericFilePath.class );
      path2 = mock( GenericFilePath.class );

      doReturn( true ).when( provider1Mock ).owns( path1 );
      doReturn( false ).when( provider1Mock ).owns( path2 );

      doReturn( false ).when( provider2Mock ).owns( path1 );
      doReturn( true ).when( provider2Mock ).owns( path2 );
    }
  }

  @Test
  void testCreateFolderSuccess() throws Exception {
    CreateFolderMultipleProviderUseCase useCase = new CreateFolderMultipleProviderUseCase();
    doReturn( true ).when( useCase.provider1Mock ).createFolder( useCase.path1 );
    doReturn( true ).when( useCase.provider2Mock ).createFolder( useCase.path2 );

    assertTrue( useCase.service.createFolder( useCase.path1 ) );
    assertTrue( useCase.service.createFolder( useCase.path2 ) );
    verify( useCase.provider1Mock ).createFolder( useCase.path1 );
    verify( useCase.provider2Mock ).createFolder( useCase.path2 );
  }

  @Test
  void testCreateFolderPathNotFound() throws Exception {
    CreateFolderMultipleProviderUseCase useCase = new CreateFolderMultipleProviderUseCase();
    doReturn( false ).when( useCase.provider1Mock ).owns( useCase.path1 );

    NotFoundException ex = assertThrows( NotFoundException.class, () -> useCase.service.createFolder( useCase.path1 ) );
    assertEquals( "Path not found '" + useCase.path1 + "'.", ex.getMessage() );
    verify( useCase.provider1Mock, never() ).createFolder( any() );
  }

  @Test
  void testCreateFolderOperationFailed() throws Exception {
    CreateFolderMultipleProviderUseCase useCase = new CreateFolderMultipleProviderUseCase();
    doReturn( false ).when( useCase.provider1Mock ).createFolder( useCase.path1 );

    assertFalse( useCase.service.createFolder( useCase.path1 ) );
    verify( useCase.provider1Mock ).createFolder( useCase.path1 );
  }

  @Test
  void testCreateFolderThrowsException() throws Exception {
    CreateFolderMultipleProviderUseCase useCase = new CreateFolderMultipleProviderUseCase();
    doThrow( new OperationFailedException( "Create failed." ) ).when( useCase.provider1Mock )
      .createFolder( useCase.path1 );

    OperationFailedException ex =
      assertThrows( OperationFailedException.class, () -> useCase.service.createFolder( useCase.path1 ) );
    assertEquals( "Create failed.", ex.getMessage() );
    verify( useCase.provider1Mock ).createFolder( useCase.path1 );
  }
  // endregion
}

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


package org.pentaho.platform.genericfile.providers.repository;

import com.google.common.net.MediaType;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.genericfile.GenericFilePath;
import org.pentaho.platform.api.genericfile.GetTreeOptions;
import org.pentaho.platform.api.genericfile.exception.AccessControlException;
import org.pentaho.platform.api.genericfile.exception.InvalidPathException;
import org.pentaho.platform.api.genericfile.exception.NotFoundException;
import org.pentaho.platform.api.genericfile.exception.OperationFailedException;
import org.pentaho.platform.api.genericfile.model.IGenericFile;
import org.pentaho.platform.api.genericfile.model.IGenericFileContentWrapper;
import org.pentaho.platform.api.genericfile.model.IGenericFileTree;
import org.pentaho.platform.api.genericfile.model.IGenericFolder;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileTreeDto;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.genericfile.messages.Messages;
import org.pentaho.platform.genericfile.providers.repository.model.RepositoryObject;
import org.pentaho.platform.plugin.services.importexport.ExportHandler;
import org.pentaho.platform.util.RepositoryPathEncoder;
import org.pentaho.platform.web.http.api.resources.services.FileService;
import org.pentaho.platform.web.http.api.resources.utils.SystemUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.pentaho.platform.genericfile.providers.repository.RepositoryFileProvider.ROOT_PATH;
import static org.pentaho.platform.util.RepositoryPathEncoder.encodeRepositoryPath;

/**
 * Tests for the {@link RepositoryFileProvider} class.
 */
@SuppressWarnings( "DataFlowIssue" )
class RepositoryFileProviderTest {
  static final String ENCODED_ROOT_PATH = RepositoryPathEncoder.encodeRepositoryPath( ROOT_PATH );
  static final String ALL_FILTER = "*";

  // region Helpers and Sample Structures

  /**
   * A sample native repository tree structure.
   * <p>
   * /
   * /home
   * /public
   * /public/testFile1
   * /public/testFolder2
   */
  static class NativeDtoRepositoryScenario {
    @NonNull
    public final RepositoryFileTreeDto rootTree;
    @NonNull
    public final RepositoryFileDto rootFolder;
    @NonNull
    public final RepositoryFileTreeDto homeTree;
    @NonNull
    public final RepositoryFileDto homeFolder;
    @NonNull
    public final RepositoryFileTreeDto publicTree;
    @NonNull
    public final RepositoryFileDto publicFolder;
    @NonNull
    public final RepositoryFileDto testFile1;
    @NonNull
    public final RepositoryFileDto testFolder2;
    @NonNull
    public final RepositoryFileDto testDeletedFile3;

    public NativeDtoRepositoryScenario() {
      rootFolder = createNativeFileDto( ROOT_PATH, "", true );
      rootTree = createNativeTreeDto( rootFolder );

      // ---
      // /home
      homeFolder = createNativeFileDto( "/home", "home", true );
      homeTree = createNativeTreeDto( homeFolder );

      // ---
      // /public
      publicFolder = createNativeFileDto( "/public", "public", true );
      publicTree = createNativeTreeDto( publicFolder );

      testFile1 = createSampleTestFile1();
      testFolder2 = createSampleTestFolder2();

      publicTree.setChildren( Arrays.asList(
        createNativeTreeDto( testFile1 ),
        createNativeTreeDto( testFolder2 )
      ) );

      rootTree.setChildren( Arrays.asList( homeTree, publicTree ) );

      testDeletedFile3 = createSampleTestDeletedFile3();
    }

    @NonNull
    private static RepositoryFileDto createSampleTestFile1() {
      RepositoryFileDto testFile1 = createNativeFileDto( "/public/testFile1", "testFile1", false );
      testFile1.setHidden( true );

      String elapsedMilliseconds = "100";
      testFile1.setLastModifiedDate( elapsedMilliseconds );

      testFile1.setId( "Test File 1 Id" );
      testFile1.setTitle( "Test File 1 title" );
      testFile1.setDescription( "Test File 1 description" );

      return testFile1;
    }

    @NonNull
    private static RepositoryFileDto createSampleTestFolder2() {
      RepositoryFileDto testFolder2 = createNativeFileDto( "/public/testFolder2", "testFolder2", true );

      String elapsedMilliseconds = "200";
      testFolder2.setLastModifiedDate( elapsedMilliseconds );

      testFolder2.setId( "Test Folder 2 Id" );
      testFolder2.setTitle( "Test Folder 2 title" );
      testFolder2.setDescription( "Test Folder 2 description" );

      return testFolder2;
    }

    @NonNull
    private static RepositoryFileDto createSampleTestDeletedFile3() {
      RepositoryFileDto testDeletedFile3 =
        createNativeFileDto( "/home/userA/.trash/pho:1234/deletedFile3", "deletedFile3", false );
      testDeletedFile3.setOriginalParentFolderPath( "/public" );
      testDeletedFile3.setCreatorId( "userB" );

      String elapsedMilliseconds = "300";
      testDeletedFile3.setLastModifiedDate( elapsedMilliseconds );

      String deletedMilliseconds = "400";
      testDeletedFile3.setDeletedDate( deletedMilliseconds );

      testDeletedFile3.setId( "Test Deleted File 3 Id" );
      testDeletedFile3.setTitle( "Test Deleted File 3 title" );
      testDeletedFile3.setDescription( "Test Deleted File 3 description" );

      return testDeletedFile3;
    }
  }

  @NonNull
  static RepositoryFileTreeDto createNativeTreeDto( @NonNull RepositoryFileDto nativeFile ) {
    RepositoryFileTreeDto nativeTree = new RepositoryFileTreeDto();
    nativeTree.setFile( nativeFile );
    return nativeTree;
  }

  @NonNull
  private static RepositoryFileDto createNativeFileDto( String path, String name, boolean isFolder ) {
    RepositoryFileDto nativeFile = new RepositoryFileDto();
    nativeFile.setName( name );
    nativeFile.setPath( path );
    nativeFile.setFolder( isFolder );

    String numberOfMilliseconds = "0";
    nativeFile.setCreatedDate( numberOfMilliseconds );

    return nativeFile;
  }

  @NonNull
  private static org.pentaho.platform.api.repository2.unified.RepositoryFile createNativeFile( String path,
                                                                                               String name,
                                                                                               boolean isFolder ) {
    Date createdDate = new Date( 100 );
    Date lastModeDate = new Date( 200 );
    Date lockDate = new Date();
    return new org.pentaho.platform.api.repository2.unified.RepositoryFile(
      "12345", name, isFolder, false, false, false, "versionId", path, createdDate,
      lastModeDate,
      false, "lockOwner", "lockMessage", lockDate, "en_US", name + " title", name + " description",
      null, null, 4096, name + "creatorId", null
    );
  }

  /**
   * Represents the result structure of a root tree operation for {@link NativeDtoRepositoryScenario}.
   */
  static class RepositoryValidatedScenario {
    @NonNull
    public final IGenericFileTree rootTree;
    @NonNull
    public final IGenericFolder rootFolder;
    @NonNull
    public final IGenericFileTree homeTree;
    @NonNull
    public final IGenericFolder homeFolder;
    @NonNull
    public final IGenericFileTree publicTree;
    @NonNull
    public final IGenericFolder publicFolder;
    @NonNull
    public final IGenericFile testFile1;
    @NonNull
    public final IGenericFolder testFolder2;

    public RepositoryValidatedScenario( @NonNull IGenericFileTree tree ) {
      assertNotNull( tree );
      rootTree = tree;
      rootFolder = assertRootFolder( tree.getFile() );

      assertEquals( ROOT_PATH, rootFolder.getPath() );

      // Check that the children of the home subtree are now part of the root tree.
      List<IGenericFileTree> rootChildren = tree.getChildren();
      assertNotNull( rootChildren );
      assertEquals( 2, rootChildren.size() );

      // ---
      // /home
      assertNotNull( rootChildren.get( 0 ) );
      homeTree = rootChildren.get( 0 );
      homeFolder = assertGenericFolder( homeTree.getFile() );
      assertEquals( "/home", homeFolder.getPath() );
      assertEquals( "home", homeFolder.getName() );
      assertEquals( ROOT_PATH, homeFolder.getParentPath() );

      // ---
      // /public

      assertNotNull( rootChildren.get( 1 ) );
      publicTree = rootChildren.get( 1 );
      publicFolder = assertPublicTree( publicTree );

      List<IGenericFileTree> publicChildren = publicTree.getChildren();
      assertNotNull( publicChildren );
      assertEquals( 2, publicChildren.size() );

      IGenericFileTree testTree1 = publicChildren.get( 0 );
      assertNotNull( testTree1 );
      assertNotNull( testTree1.getFile() );
      testFile1 = testTree1.getFile();

      IGenericFileTree testTree2 = publicChildren.get( 1 );
      assertNotNull( testTree2 );
      assertNotNull( testTree2.getFile() );
      testFolder2 = assertGenericFolder( testTree2.getFile() );
    }
  }

  @NonNull
  RepositoryValidatedScenario assertRepositoryTree( IGenericFileTree tree ) {
    return new RepositoryValidatedScenario( tree );
  }

  private static @NonNull IGenericFolder assertPublicTree( IGenericFileTree publicTree ) {
    IGenericFolder publicFolder = assertGenericFolder( publicTree.getFile() );

    assertEquals( "/public", publicFolder.getPath() );
    assertEquals( "public", publicFolder.getName() );
    assertEquals( ROOT_PATH, publicFolder.getParentPath() );
    assertRegularCapabilities( publicFolder );

    return publicFolder;
  }

  @NonNull
  static IGenericFolder assertGenericFolder( IGenericFile file ) {
    assertNotNull( file );
    assertTrue( file.isFolder() );
    assertInstanceOf( IGenericFolder.class, file );
    return (IGenericFolder) file;
  }

  @NonNull
  static IGenericFolder assertRootFolder( IGenericFile file ) {
    IGenericFolder folder = assertGenericFolder( file );

    assertEquals( ROOT_PATH, file.getPath() );
    assertEquals( ROOT_PATH, file.getName() );
    assertNull( file.getParentPath() );
    assertEquals( Messages.getString( "GenericFileRepository.REPOSITORY_FOLDER_DISPLAY" ), file.getTitle() );

    assertFalse( folder.isCanDelete() );
    assertFalse( folder.isCanEdit() );
    assertFalse( folder.isCanAddChildren() );
    return folder;
  }

  static void assertRegularCapabilities( IGenericFile file ) {
    assertTrue( file.isCanDelete() );
    assertTrue( file.isCanEdit() );

    if ( file.isFolder() ) {
      IGenericFolder folder = (IGenericFolder) file;
      assertTrue( folder.isCanAddChildren() );
    }
  }

  @NonNull
  private RepositoryFileAcl createMockFileOwner( String owner ) {
    RepositoryFileAcl acl = mock( RepositoryFileAcl.class );

    RepositoryFileSid ownerSid = mock( RepositoryFileSid.class );
    doReturn( owner )
      .when( ownerSid )
      .getName();

    doReturn( ownerSid )
      .when( acl )
      .getOwner();

    return acl;
  }
  // endregion

  // region getTree
  @Test
  void testGetTreeThrowsNotFoundExceptionIfBasePathNotOwned() throws OperationFailedException {
    RepositoryFileProvider repositoryProvider =
      new RepositoryFileProvider( mock( IUnifiedRepository.class ), mock( FileService.class ) );

    GetTreeOptions options = new GetTreeOptions();
    options.setBasePath( "scheme://path" );
    options.setMaxDepth( 1 );

    assertThrows( NotFoundException.class, () -> repositoryProvider.getTree( options ) );
  }

  @Test
  void testGetTreeDelegatesToFileServiceDoGetTree() throws OperationFailedException {

    NativeDtoRepositoryScenario scenario = new NativeDtoRepositoryScenario();

    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );

    FileService fileServiceMock = mock( FileService.class );
    doReturn( scenario.rootTree )
      .when( fileServiceMock )
      .doGetTree( any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean() );

    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

    GetTreeOptions options = new GetTreeOptions();
    options.setBasePath( ROOT_PATH );
    options.setMaxDepth( 2 );

    IGenericFileTree tree = repositoryProvider.getTree( options );

    assertRepositoryTree( tree );

    verify( fileServiceMock, times( 1 ) )
      .doGetTree( ENCODED_ROOT_PATH, 2, ALL_FILTER, false, false, false );
  }

  @Test
  void testGetTreeDefaultsBasePathToRepositoryRoot() throws OperationFailedException {
    NativeDtoRepositoryScenario scenario = new NativeDtoRepositoryScenario();

    FileService fileServiceMock = mock( FileService.class );
    doReturn( scenario.rootTree )
      .when( fileServiceMock )
      .doGetTree( any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean() );

    RepositoryFileProvider repositoryProvider =
      new RepositoryFileProvider( mock( IUnifiedRepository.class ), fileServiceMock );

    GetTreeOptions options = new GetTreeOptions();
    options.setBasePath( (GenericFilePath) null );
    options.setMaxDepth( 1 );

    repositoryProvider.getTree( options );

    verify( fileServiceMock, times( 1 ) )
      .doGetTree( eq( ENCODED_ROOT_PATH ), anyInt(), anyString(), anyBoolean(), anyBoolean(), anyBoolean() );
  }

  @Test
  void testGetTreeRespectsNullChildrenList() throws OperationFailedException {
    NativeDtoRepositoryScenario scenario = new NativeDtoRepositoryScenario();

    // Check initial structure has null children list for /home.
    assertNull( scenario.homeTree.getChildren() );

    FileService fileServiceMock = mock( FileService.class );
    doReturn( scenario.rootTree )
      .when( fileServiceMock )
      .doGetTree( any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean() );

    RepositoryFileProvider repositoryProvider =
      new RepositoryFileProvider( mock( IUnifiedRepository.class ), fileServiceMock );

    GetTreeOptions options = new GetTreeOptions();
    options.setBasePath( ROOT_PATH );
    options.setMaxDepth( 1 );

    IGenericFileTree tree = repositoryProvider.getTree( options );

    RepositoryValidatedScenario validatedScenario = assertRepositoryTree( tree );
    assertNull( validatedScenario.homeTree.getChildren() );
  }

  @Test
  void testGetTreeRespectsEmptyChildrenList() throws OperationFailedException {
    NativeDtoRepositoryScenario scenario = new NativeDtoRepositoryScenario();

    // Set empty list to children of /home.
    scenario.homeTree.setChildren( Collections.emptyList() );

    FileService fileServiceMock = mock( FileService.class );
    doReturn( scenario.rootTree )
      .when( fileServiceMock )
      .doGetTree( any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean() );

    RepositoryFileProvider repositoryProvider =
      new RepositoryFileProvider( mock( IUnifiedRepository.class ), fileServiceMock );

    GetTreeOptions options = new GetTreeOptions();
    options.setBasePath( ROOT_PATH );
    options.setMaxDepth( 1 );

    IGenericFileTree tree = repositoryProvider.getTree( options );

    RepositoryValidatedScenario validatedScenario = assertRepositoryTree( tree );
    assertNotNull( validatedScenario.homeTree.getChildren() );
    assertTrue( validatedScenario.homeTree.getChildren().isEmpty() );
  }

  @Test
  void testGetTreeTestFile1HasExpectedProperties() throws OperationFailedException {
    NativeDtoRepositoryScenario scenario = new NativeDtoRepositoryScenario();

    FileService fileServiceMock = mock( FileService.class );
    doReturn( scenario.rootTree )
      .when( fileServiceMock )
      .doGetTree( any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean() );

    RepositoryFileProvider repositoryProvider =
      new RepositoryFileProvider( mock( IUnifiedRepository.class ), fileServiceMock );

    GetTreeOptions options = new GetTreeOptions();
    options.setBasePath( ROOT_PATH );
    options.setMaxDepth( 1 );

    IGenericFileTree tree = repositoryProvider.getTree( options );

    RepositoryValidatedScenario validatedScenario = assertRepositoryTree( tree );

    RepositoryObject testFile1 = (RepositoryObject) validatedScenario.testFile1;
    assertEquals( "/public/testFile1", testFile1.getPath() );
    assertEquals( "/public", testFile1.getParentPath() );
    assertEquals( "testFile1", testFile1.getName() );
    assertEquals( "Test File 1 Id", testFile1.getObjectId() );
    assertEquals( "Test File 1 title", testFile1.getTitle() );
    assertEquals( "Test File 1 description", testFile1.getDescription() );
    assertTrue( testFile1.isHidden() );
    assertEquals( new Date( 100 ), testFile1.getModifiedDate() );
  }

  @Test
  void testGetTreeTestFolder2HasExpectedProperties() throws OperationFailedException {
    NativeDtoRepositoryScenario scenario = new NativeDtoRepositoryScenario();

    FileService fileServiceMock = mock( FileService.class );
    doReturn( scenario.rootTree )
      .when( fileServiceMock )
      .doGetTree( any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean() );

    RepositoryFileProvider repositoryProvider =
      new RepositoryFileProvider( mock( IUnifiedRepository.class ), fileServiceMock );

    GetTreeOptions options = new GetTreeOptions();
    options.setBasePath( ROOT_PATH );
    options.setMaxDepth( 1 );

    IGenericFileTree tree = repositoryProvider.getTree( options );

    RepositoryValidatedScenario validatedScenario = assertRepositoryTree( tree );

    RepositoryObject testFolder2 = (RepositoryObject) validatedScenario.testFolder2;
    assertEquals( "/public/testFolder2", testFolder2.getPath() );
    assertEquals( "/public", testFolder2.getParentPath() );
    assertEquals( "testFolder2", testFolder2.getName() );
    assertEquals( "Test Folder 2 Id", testFolder2.getObjectId() );
    assertEquals( "Test Folder 2 title", testFolder2.getTitle() );
    assertEquals( "Test Folder 2 description", testFolder2.getDescription() );
    assertFalse( testFolder2.isHidden() );
    assertEquals( new Date( 200 ), testFolder2.getModifiedDate() );
  }

  @Test
  void testGetSubTreeRootNodeHasExpectedProperties() throws OperationFailedException {

    NativeDtoRepositoryScenario scenario = new NativeDtoRepositoryScenario();

    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );

    FileService fileServiceMock = mock( FileService.class );
    doReturn( scenario.publicTree )
      .when( fileServiceMock )
      .doGetTree( any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean() );

    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

    GetTreeOptions options = new GetTreeOptions();
    options.setBasePath( scenario.publicFolder.getPath() );
    options.setMaxDepth( 1 );

    IGenericFileTree tree = repositoryProvider.getTree( options );

    assertPublicTree( tree );
  }
  // endregion

  // region getRootTrees
  @Test
  void testGetRootTreesDelegatesToGetTreeCoreReturnsSingleRootTree() throws OperationFailedException {
    NativeDtoRepositoryScenario scenario = new NativeDtoRepositoryScenario();

    FileService fileServiceMock = mock( FileService.class );
    doReturn( scenario.rootTree )
      .when( fileServiceMock )
      .doGetTree( any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean() );

    RepositoryFileProvider repositoryProvider =
      new RepositoryFileProvider( mock( IUnifiedRepository.class ), fileServiceMock );

    GetTreeOptions options = new GetTreeOptions();
    options.setBasePath( (GenericFilePath) null );
    options.setMaxDepth( 1 );

    List<IGenericFileTree> rootTrees = repositoryProvider.getRootTrees( options );
    assertNotNull( rootTrees );
    assertEquals( 1, rootTrees.size() );

    assertRepositoryTree( rootTrees.get( 0 ) );

    // Call again to make sure we're using getTreeCore / no cache.
    repositoryProvider.getRootTrees( options );

    // Must have called backend twice!
    verify( fileServiceMock, times( 2 ) )
      .doGetTree( eq( ENCODED_ROOT_PATH ), anyInt(), anyString(), anyBoolean(), anyBoolean(), anyBoolean() );
  }
  // endregion

  // region getFile
  @Test
  void testGetFileThrowsNotFoundExceptionIfPathNotOwned() {
    RepositoryFileProvider repositoryProvider =
      new RepositoryFileProvider( mock( IUnifiedRepository.class ), mock( FileService.class ) );

    assertThrows( NotFoundException.class,
      () -> repositoryProvider.getFile( GenericFilePath.parse( "scheme://path" ) ) );
  }

  @Test
  void testGetFileThrowsNotFoundExceptionIfPathNotFound() {

    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    doReturn( null )
      .when( repositoryMock )
      .getFile( "/path" );

    RepositoryFileProvider repositoryProvider =
      new RepositoryFileProvider( repositoryMock, mock( FileService.class ) );

    assertThrows( NotFoundException.class, () -> repositoryProvider.getFile( GenericFilePath.parse( "/path" ) ) );
  }

  @Test
  void testGetFileRootHasExpectedProperties() throws OperationFailedException {
    org.pentaho.platform.api.repository2.unified.RepositoryFile nativeFile = createNativeFile( ROOT_PATH, "", true );

    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    doReturn( nativeFile )
      .when( repositoryMock )
      .getFile( ROOT_PATH );

    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, mock( FileService.class ) );

    IGenericFile file = repositoryProvider.getFile( GenericFilePath.parse( ROOT_PATH ) );

    assertRootFolder( file );
  }

  @Test
  void testGetFileRegularHasExpectedProperties() throws OperationFailedException {
    org.pentaho.platform.api.repository2.unified.RepositoryFile nativeFile =
      createNativeFile( "/public/testFile1", "testFile1", false );

    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    doReturn( nativeFile )
      .when( repositoryMock )
      .getFile( nativeFile.getPath() );

    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, mock( FileService.class ) );

    IGenericFile file = repositoryProvider.getFile( GenericFilePath.parse( nativeFile.getPath() ) );

    assertEquals( "/public/testFile1", file.getPath() );
    assertEquals( "/public", file.getParentPath() );
    assertEquals( "testFile1", file.getName() );
    assertEquals( "testFile1 title", file.getTitle() );
    assertEquals( "testFile1 description", file.getDescription() );

    assertEquals( new Date( 200 ), file.getModifiedDate() );

    assertRegularCapabilities( file );
  }
  // endregion

  // region getDeletedFiles
  @Test
  void getDeletedFilesTestDeletedFile3HasExpectedProperties() {
    NativeDtoRepositoryScenario scenario = new NativeDtoRepositoryScenario();

    FileService fileServiceMock = mock( FileService.class );
    doReturn( Collections.singletonList( scenario.testDeletedFile3 ) )
      .when( fileServiceMock )
      .doGetDeletedFiles();

    String expectedOwner = "userA";
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    doReturn( createMockFileOwner( expectedOwner ) )
      .when( repositoryMock )
      .getAcl( scenario.testDeletedFile3.getId() );

    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

    List<IGenericFile> deletedFiles = repositoryProvider.getDeletedFiles();

    assertNotNull( deletedFiles );
    assertEquals( 1, deletedFiles.size() );

    IGenericFile deletedFile = deletedFiles.get( 0 );
    assertEquals( scenario.testDeletedFile3.getPath(), deletedFile.getPath() );
    assertEquals( scenario.testDeletedFile3.getCreatorId(), deletedFile.getDeletedBy() );
    assertEquals( expectedOwner, deletedFile.getOwner() );

    Date expectedDeletedDate = new Date( Integer.parseInt( scenario.testDeletedFile3.getDeletedDate() ) );
    assertEquals( expectedDeletedDate, deletedFile.getDeletedDate() );

    List<IGenericFile> originalLocations = deletedFile.getOriginalLocation();
    assertEquals( 2, originalLocations.size() );

    assertEquals( ROOT_PATH, originalLocations.get( 0 ).getPath() );
    assertEquals( "/public", originalLocations.get( 1 ).getPath() );
  }
  // endregion

  // region deleteFilePermanently
  @Test
  void testDeleteFilePermanentlySuccess() throws Exception {
    GenericFilePath path = GenericFilePath.parse( "/home/admin/.trash/pho:8b69da2b-2a10-4a82-89bc-a376e52d5482"
      + "/PAZReport.xanalyzer" );

    FileService fileServiceMock = mock( FileService.class );
    doNothing().when( fileServiceMock ).doDeleteFilesPermanent( any() );
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

    repositoryProvider.deleteFilePermanently( path );

    verify( fileServiceMock, times( 1 ) ).doDeleteFilesPermanent( repositoryProvider.getTrashFileId( path ) );
  }

  @Test
  void testDeleteFilePermanentlyInvalidPath() throws Exception {
    GenericFilePath path = GenericFilePath.parse( "/home/admin/pho:8b69da2b-2a10-4a82-89bc-a376e52d5482"
      + "/PAZReport.xanalyzer" );

    FileService fileServiceMock = mock( FileService.class );
    doNothing().when( fileServiceMock ).doDeleteFilesPermanent( any() );
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

    NotFoundException exception = assertThrows( NotFoundException.class, () ->
      repositoryProvider.deleteFilePermanently( path )
    );

    assertEquals( "The path does not correspond to a deleted file.", exception.getMessage() );
    verify( fileServiceMock, never() ).doDeleteFilesPermanent( anyString() );
  }

  @Test
  void testDeleteFilePermanentlyOperationFailed() throws Exception {
    GenericFilePath path = GenericFilePath.parse( "/home/admin/.trash/pho:8b69da2b-2a10-4a82-89bc-a376e52d5482"
      + "/PAZReport.xanalyzer" );

    FileService fileServiceMock = mock( FileService.class );
    doNothing().when( fileServiceMock ).doDeleteFilesPermanent( any() );
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

    doThrow( new OperationFailedException() ).when( fileServiceMock ).doDeleteFilesPermanent( any() );

    assertThrows( OperationFailedException.class, () -> repositoryProvider.deleteFilePermanently( path ) );

    verify( fileServiceMock ).doDeleteFilesPermanent( repositoryProvider.getTrashFileId( path ) );
  }

  @Test
  void testGetTrashFileIdValidPath() throws Exception {
    testGetTrashFileIdValidPath(
      "/home/admin/.trash/pho:8b69da2b-2a10-4a82-89bc-a376e52d5483/report/PAZReport.xanalyzer",
      "8b69da2b-2a10-4a82-89bc-a376e52d5483" );
  }

  @Test
  void testGetTrashFileIdValidPathWithColon() throws Exception {
    testGetTrashFileIdValidPath( "/home/admin/.trash/pho:8b69da2b-2a10-4a82-89bc-a376e52d5482/PAZReport.xanalyzer",
      "8b69da2b-2a10-4a82-89bc-a376e52d5482" );
  }

  @Test
  void testGetTrashFileIdInvalidPathWithoutColon() throws Exception {
    testGetTrashFileIdInvalidPath( "/home/admin/.trash/8b69da2b-2a10-4a82-89bc-a376e52d5482/PAZReport.xanalyzer",
      InvalidPathException.class );
  }

  @Test
  void testGetTrashFileIdInvalidPathNoTrash() throws Exception {
    testGetTrashFileIdInvalidPath( "/home/admin/pho:8b69da2b-2a10-4a82-89bc-a376e52d5482/PAZReport.xanalyzer",
      NotFoundException.class );
  }

  @Test
  void testGetTrashFileIdInvalidPathNoTrashNoId() throws Exception {
    testGetTrashFileIdInvalidPath( "/home/admin/PAZReport.xanalyzer", NotFoundException.class );
  }

  @Test
  void testGetFileIdInvalidPathNoIdNoTrashFile() throws Exception {
    testGetTrashFileIdInvalidPath( "/home/admin/.trash/", NotFoundException.class );
  }

  @Test
  void testGetTrashFileIdInvalidPathRoot() throws Exception {
    testGetTrashFileIdInvalidPath( "/", NotFoundException.class );
  }

  @Test
  void testGetTrashFileIdInvalidPathNoTrashNoColon() throws Exception {
    testGetTrashFileIdInvalidPath( "/home/admin/8b69da2b-2a10-4a82-89bc-a376e52d5482/PAZReport.xanalyzer",
      NotFoundException.class );
  }

  private <T extends Throwable> void testGetTrashFileIdInvalidPath( String pathString, Class<T> exceptionClass )
    throws Exception {
    GenericFilePath path = GenericFilePath.parse( pathString );

    FileService fileServiceMock = mock( FileService.class );
    doNothing().when( fileServiceMock ).doDeleteFilesPermanent( any() );
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

    T exception = assertThrows( exceptionClass, () -> repositoryProvider.getTrashFileId( path ) );

    if ( exception instanceof NotFoundException ) {
      assertEquals( "The path does not correspond to a deleted file.", exception.getMessage() );
    } else if ( exception instanceof InvalidPathException ) {
      assertEquals( "File ID not found in the path.", exception.getMessage() );
    }
  }

  private void testGetTrashFileIdValidPath( String pathString, String id ) throws Exception {
    GenericFilePath path = GenericFilePath.parse( pathString );

    FileService fileServiceMock = mock( FileService.class );
    doNothing().when( fileServiceMock ).doDeleteFilesPermanent( any() );
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

    String fileId = repositoryProvider.getTrashFileId( path );
    assertEquals( id, fileId );
  }
  // endregion

  // region deleteFile
  @ParameterizedTest
  @ValueSource( booleans = { true, false } )
  void testDeleteFileSuccess( boolean permanent ) throws Exception {
    String fileId = "8b69da2b-2a10-4a82-89bc-a376e52d5482";
    GenericFilePath path = GenericFilePath.parse( "/home/admin/8b69da2b-2a10-4a82-89bc-a376e52d5482"
      + "/PAZReport.xanalyzer" );

    FileService fileServiceMock = mock( FileService.class );

    if ( permanent ) {
      doNothing().when( fileServiceMock ).doDeleteFilesPermanent( any() );
    } else {
      doNothing().when( fileServiceMock ).doDeleteFiles( any() );
    }

    doReturn( createSampleFile( fileId ) ).when( fileServiceMock ).doGetProperties( any() );
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

    repositoryProvider.deleteFile( path, permanent );

    if ( permanent ) {
      verify( fileServiceMock, never() ).doDeleteFiles( fileId );
      verify( fileServiceMock, times( 1 ) ).doDeleteFilesPermanent( fileId );
    } else {
      verify( fileServiceMock, times( 1 ) ).doDeleteFiles( fileId );
      verify( fileServiceMock, never() ).doDeleteFilesPermanent( fileId );
    }
  }

  @ParameterizedTest
  @ValueSource( booleans = { true, false } )
  void testDeleteFileOperationFailed( boolean permanent ) throws Exception {
    String fileId = "8b69da2b-2a10-4a82-89bc-a376e52d5482";
    GenericFilePath path = GenericFilePath.parse( "/home/admin/8b69da2b-2a10-4a82-89bc-a376e52d5482"
      + "/PAZReport.xanalyzer" );

    FileService fileServiceMock = mock( FileService.class );

    if ( permanent ) {
      doThrow( new OperationFailedException() ).when( fileServiceMock ).doDeleteFilesPermanent( any() );
    } else {
      doThrow( new OperationFailedException() ).when( fileServiceMock ).doDeleteFiles( any() );
    }

    doReturn( createSampleFile( fileId ) ).when( fileServiceMock ).doGetProperties( any() );
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

    assertThrows( OperationFailedException.class, () -> repositoryProvider.deleteFile( path, permanent ) );

    if ( permanent ) {
      verify( fileServiceMock, never() ).doDeleteFiles( fileId );
      verify( fileServiceMock ).doDeleteFilesPermanent( fileId );
    } else {
      verify( fileServiceMock ).doDeleteFiles( fileId );
      verify( fileServiceMock, never() ).doDeleteFilesPermanent( fileId );
    }
  }

  @ParameterizedTest
  @ValueSource( booleans = { true, false } )
  void testDeleteFileNotFound( boolean permanent ) throws Exception {
    GenericFilePath path = GenericFilePath.parse( "/home/admin/nonexistent-file.xanalyzer" );

    FileService fileServiceMock = mock( FileService.class );
    doReturn( null ).when( fileServiceMock ).doGetProperties( any() );
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

    assertThrows( OperationFailedException.class, () -> repositoryProvider.deleteFile( path, permanent ) );

    verify( fileServiceMock, never() ).doDeleteFiles( anyString() );
    verify( fileServiceMock, never() ).doDeleteFilesPermanent( anyString() );
  }

  @NonNull
  private static RepositoryFileDto createSampleFile( String id ) {
    RepositoryFileDto file = createNativeFileDto( "/public/fileToDelete", "fileToDelete", false );
    file.setHidden( false );
    file.setId( id );

    return file;
  }
  // endregion

  // region restoreFile
  @Test
  void testRestoreFileSuccess() throws Exception {
    GenericFilePath path = GenericFilePath.parse( "/home/admin/.trash/pho:8b69da2b-2a10-4a82-89bc-a376e52d5482"
      + "/PAZReport.xanalyzer" );

    FileService fileServiceMock = mock( FileService.class );
    doNothing().when( fileServiceMock ).doRestoreFiles( any() );
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

    repositoryProvider.restoreFile( path );

    verify( fileServiceMock, times( 1 ) ).doRestoreFiles( repositoryProvider.getTrashFileId( path ) );
  }

  @Test
  void testRestoreFileInvalidPath() throws Exception {
    GenericFilePath path = GenericFilePath.parse( "/home/admin/pho:8b69da2b-2a10-4a82-89bc-a376e52d5482"
      + "/PAZReport.xanalyzer" );

    FileService fileServiceMock = mock( FileService.class );
    doNothing().when( fileServiceMock ).doRestoreFiles( any() );
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

    NotFoundException exception = assertThrows( NotFoundException.class, () ->
      repositoryProvider.restoreFile( path )
    );

    assertEquals( "The path does not correspond to a deleted file.", exception.getMessage() );
    verify( fileServiceMock, never() ).doRestoreFiles( anyString() );
  }

  @Test
  void testRestoreFileOperationFailed() throws Exception {
    GenericFilePath path = GenericFilePath.parse( "/home/admin/.trash/pho:8b69da2b-2a10-4a82-89bc-a376e52d5482"
      + "/PAZReport.xanalyzer" );

    FileService fileServiceMock = mock( FileService.class );
    doNothing().when( fileServiceMock ).doRestoreFiles( any() );
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

    doThrow( new InternalError() ).when( fileServiceMock ).doRestoreFiles( any() );

    assertThrows( OperationFailedException.class, () -> repositoryProvider.restoreFile( path ) );

    verify( fileServiceMock ).doRestoreFiles( repositoryProvider.getTrashFileId( path ) );
  }
  // endregion

  // region renameFile
  @Test
  void testRenameFileSuccess() throws Exception {
    String fileId = "8b69da2b-2a10-4a82-89bc-a376e52d5482";
    GenericFilePath path = GenericFilePath.parse( "/home/admin/" + fileId + "/PAZReport.xanalyzer" );
    String newName = "renamed.xanalyzer";

    FileService fileServiceMock = mock( FileService.class );
    doReturn( true ).when( fileServiceMock ).isValidFileName( newName );
    doReturn( true ).when( fileServiceMock ).doRename( encodeRepositoryPath( path.toString() ), newName );
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

    repositoryProvider.renameFile( path, newName );

    verify( fileServiceMock, times( 1 ) ).doRename( encodeRepositoryPath( path.toString() ), newName );
  }

  @Test
  void testRenameFileErrorThrowsException() throws Exception {
    String fileId = "8b69da2b-2a10-4a82-89bc-a376e52d5482";
    GenericFilePath path = GenericFilePath.parse( "/home/admin/" + fileId + "/PAZReport.xanalyzer" );
    String newName = "renamed.xanalyzer";

    FileService fileServiceMock = mock( FileService.class );
    doReturn( true ).when( fileServiceMock ).isValidFileName( newName );
    doThrow( new RuntimeException( "rename failed" ) ).when( fileServiceMock )
      .doRename( encodeRepositoryPath( path.toString() ), newName );
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

    OperationFailedException exception = assertThrows( OperationFailedException.class,
      () -> repositoryProvider.renameFile( path, newName ) );

    assertEquals( "rename failed", exception.getCause().getMessage() );
    verify( fileServiceMock ).doRename( encodeRepositoryPath( path.toString() ), newName );
  }

  @Test
  void testRenameFileOperationFailed() throws Exception {
    String fileId = "8b69da2b-2a10-4a82-89bc-a376e52d5482";
    GenericFilePath path = GenericFilePath.parse( "/home/admin/" + fileId + "/PAZReport.xanalyzer" );
    String newName = "renamed.xanalyzer";

    FileService fileServiceMock = mock( FileService.class );
    doReturn( true ).when( fileServiceMock ).isValidFileName( newName );
    doReturn( false ).when( fileServiceMock ).doRename( encodeRepositoryPath( path.toString() ), newName );
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

    OperationFailedException exception = assertThrows( OperationFailedException.class,
      () -> repositoryProvider.renameFile( path, newName ) );

    assertEquals( String.format( "Failed to rename file '%s' to '%s'.", path, newName ), exception.getMessage() );
    verify( fileServiceMock ).doRename( encodeRepositoryPath( path.toString() ), newName );
  }

  @Test
  void testRenameFileInvalidName() throws Exception {
    String fileId = "8b69da2b-2a10-4a82-89bc-a376e52d5482";
    GenericFilePath path = GenericFilePath.parse( "/home/admin/" + fileId + "/PAZReport.xanalyzer" );
    String newName = "bad/name";

    FileService fileServiceMock = mock( FileService.class );
    doReturn( false ).when( fileServiceMock ).isValidFileName( newName );
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

    OperationFailedException exception =
      assertThrows( OperationFailedException.class, () -> repositoryProvider.renameFile( path, newName ) );

    assertInstanceOf( IllegalArgumentException.class, exception.getCause() );
    assertEquals( "Invalid name to rename the file: " + newName, exception.getCause().getMessage() );
    verify( fileServiceMock, never() ).doRename( encodeRepositoryPath( path.toString() ), newName );
  }
  // endregion

  // region getFileProperties
  @Test
  void testGetFilePropertiesSuccess() throws Exception {
    String fileId = "8b69da2b-2a10-4a82-89bc-a376e52d5482";
    GenericFilePath path = GenericFilePath.parse( "/home/admin/" + fileId + "/PAZReport.xanalyzer" );
    RepositoryFileDto mockFile = createSampleFile( fileId );

    FileService fileServiceMock = mock( FileService.class );
    doReturn( mockFile ).when( fileServiceMock ).doGetProperties( any() );
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

    IGenericFile result = repositoryProvider.getFileProperties( path );

    assertNotNull( result );
    assertEquals( mockFile.getPath(), result.getPath() );
    verify( fileServiceMock ).doGetProperties( encodeRepositoryPath( path.toString() ) );
  }

  @Test
  void testGetFilePropertiesNotFound() throws Exception {
    GenericFilePath path = GenericFilePath.parse( "/home/admin/nonexistent-file.xanalyzer" );

    FileService fileServiceMock = mock( FileService.class );
    doReturn( null ).when( fileServiceMock ).doGetProperties( any() );
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

    OperationFailedException exception = assertThrows( OperationFailedException.class,
      () -> repositoryProvider.getFileProperties( path ) );

    assertNotNull( exception.getCause() );
    verify( fileServiceMock ).doGetProperties( any() );
  }

  @Test
  void testGetFilePropertiesException() throws Exception {
    GenericFilePath path =
      GenericFilePath.parse( "/home/admin/8b69da2b-2a10-4a82-89bc-a376e52d5482/PAZReport.xanalyzer" );

    FileService fileServiceMock = mock( FileService.class );
    doThrow( new RuntimeException( "get file properties failed" ) ).when( fileServiceMock ).doGetProperties( any() );
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

    OperationFailedException exception = assertThrows( OperationFailedException.class,
      () -> repositoryProvider.getFileProperties( path ) );

    assertEquals( "get file properties failed", exception.getCause().getMessage() );
    verify( fileServiceMock ).doGetProperties( any() );
  }
  // endregion

  // region getRootProperties
  @Test
  void testGetRootPropertiesSuccess() throws Exception {
    RepositoryFileDto rootFile = createSampleFile( "fileId" );
    FileService fileServiceMock = mock( FileService.class );
    doReturn( rootFile ).when( fileServiceMock ).doGetRootProperties();
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

    IGenericFile result = repositoryProvider.getRootProperties();

    assertNotNull( result );
    assertEquals( rootFile.getPath(), result.getPath() );
    verify( fileServiceMock ).doGetRootProperties();
  }

  @Test
  void testGetRootPropertiesException() {
    FileService fileServiceMock = mock( FileService.class );
    doThrow( new RuntimeException( "root properties failed" ) ).when( fileServiceMock ).doGetRootProperties();
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

    OperationFailedException exception =
      assertThrows( OperationFailedException.class, repositoryProvider::getRootProperties );

    assertEquals( "root properties failed", exception.getCause().getMessage() );
    verify( fileServiceMock ).doGetRootProperties();
  }
  // endregion

  // region downloadFile
  @Test
  void testDownloadFileSuccess() throws Exception {
    String fileId = "8b69da2b-2a10-4a82-89bc-a376e52d5482";
    String fileName = "PAZReport.xanalyzer";
    String pathString = "/home/admin/" + fileId + "/" + fileName;
    GenericFilePath path = GenericFilePath.parse( pathString );

    try ( MockedStatic<SystemUtils> systemUtilsMock = mockStatic( SystemUtils.class );
          MockedStatic<PentahoSessionHolder> sessionHolderMock = mockStatic( PentahoSessionHolder.class ) ) {
      systemUtilsMock.when( () -> SystemUtils.canDownload( any() ) ).thenReturn( true );

      IPentahoSession pentahoSessionMock = mock( IPentahoSession.class );
      doReturn( "sessionName" ).when( pentahoSessionMock ).getName();
      sessionHolderMock.when( PentahoSessionHolder::getSession ).thenReturn( pentahoSessionMock );

      FileService fileServiceMock = mock( FileService.class );
      doReturn( true ).when( fileServiceMock ).isPathValid( any() );

      org.pentaho.platform.api.repository2.unified.RepositoryFile nativeFile =
        createNativeFile( pathString, fileName, false );

      IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
      doReturn( repositoryMock ).when( fileServiceMock ).getRepository();
      doReturn( nativeFile ).when( repositoryMock ).getFile( any() );

      RepositoryFileProvider repositoryProvider =
        Mockito.spy( new RepositoryFileProvider( repositoryMock, fileServiceMock ) );
      ExportHandler exportHandlerMock = mock( ExportHandler.class );
      doReturn( exportHandlerMock ).when( repositoryProvider ).getDownloadExportHandler();

      IGenericFileContentWrapper result = repositoryProvider.downloadFile( path );

      assertNotNull( result );
      assertEquals( fileName + ".zip", result.getFileName() );
      assertEquals( MediaType.ZIP.toString(), result.getMimeType() );
      assertNotNull( result.getInputStream() );
    }
  }

  @Test
  void testDownloadFolderSuccess() throws Exception {
    String fileId = "8b69da2b-2a10-4a82-89bc-a376e52d5482";
    String fileName = "report";
    String pathString = "/home/admin/" + fileId + "/" + fileName;
    GenericFilePath path = GenericFilePath.parse( pathString );

    try ( MockedStatic<SystemUtils> systemUtilsMock = mockStatic( SystemUtils.class );
          MockedStatic<PentahoSessionHolder> sessionHolderMock = mockStatic( PentahoSessionHolder.class ) ) {
      systemUtilsMock.when( () -> SystemUtils.canDownload( any() ) ).thenReturn( true );

      IPentahoSession pentahoSessionMock = mock( IPentahoSession.class );
      doReturn( "sessionName" ).when( pentahoSessionMock ).getName();
      sessionHolderMock.when( PentahoSessionHolder::getSession ).thenReturn( pentahoSessionMock );

      FileService fileServiceMock = mock( FileService.class );
      doReturn( true ).when( fileServiceMock ).isPathValid( any() );

      org.pentaho.platform.api.repository2.unified.RepositoryFile nativeFile =
        createNativeFile( pathString, fileName, true );

      IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
      doReturn( repositoryMock ).when( fileServiceMock ).getRepository();
      doReturn( nativeFile ).when( repositoryMock ).getFile( any() );

      RepositoryFileProvider repositoryProvider =
        Mockito.spy( new RepositoryFileProvider( repositoryMock, fileServiceMock ) );
      ExportHandler exportHandlerMock = mock( ExportHandler.class );
      doReturn( exportHandlerMock ).when( repositoryProvider ).getDownloadExportHandler();

      IGenericFileContentWrapper result = repositoryProvider.downloadFile( path );

      assertNotNull( result );
      assertEquals( fileName + ".zip", result.getFileName() );
      assertEquals( MediaType.ZIP.toString(), result.getMimeType() );
      assertNotNull( result.getInputStream() );
    }
  }

  @Test
  void testDownloadFileNotAllowed() throws Exception {
    String fileId = "8b69da2b-2a10-4a82-89bc-a376e52d5482";
    String fileName = "PAZReport.xanalyzer";
    String pathString = "/home/admin/" + fileId + "/" + fileName;
    GenericFilePath path = GenericFilePath.parse( pathString );

    try ( MockedStatic<SystemUtils> systemUtilsMock = mockStatic( SystemUtils.class ) ) {
      systemUtilsMock.when( () -> SystemUtils.canDownload( any() ) ).thenReturn( false );

      FileService fileServiceMock = mock( FileService.class );
      IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
      RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

      assertThrows( AccessControlException.class, () -> repositoryProvider.downloadFile( path ) );
    }
  }

  @Test
  void testDownloadFileInvalidPath() throws Exception {
    String fileId = "8b69da2b-2a10-4a82-89bc-a376e52d5482";
    String fileName = "PAZReport.xanalyzer";
    String pathString = "/home/admin/" + fileId + "/" + fileName;
    GenericFilePath path = GenericFilePath.parse( pathString );

    try ( MockedStatic<SystemUtils> systemUtilsMock = mockStatic( SystemUtils.class ) ) {
      systemUtilsMock.when( () -> SystemUtils.canDownload( any() ) ).thenReturn( true );

      FileService fileServiceMock = mock( FileService.class );
      doReturn( false ).when( fileServiceMock ).isPathValid( any() );

      IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
      RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

      assertThrows( InvalidPathException.class, () -> repositoryProvider.downloadFile( path ) );
    }
  }

  @Test
  void testDownloadFileRepositoryFileNotFound() throws Exception {
    String fileId = "8b69da2b-2a10-4a82-89bc-a376e52d5482";
    String fileName = "PAZReport.xanalyzer";
    String pathString = "/home/admin/" + fileId + "/" + fileName;
    GenericFilePath path = GenericFilePath.parse( pathString );

    try ( MockedStatic<SystemUtils> systemUtilsMock = mockStatic( SystemUtils.class ) ) {
      systemUtilsMock.when( () -> SystemUtils.canDownload( any() ) ).thenReturn( true );

      FileService fileServiceMock = mock( FileService.class );
      doReturn( true ).when( fileServiceMock ).isPathValid( any() );

      IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
      doReturn( null ).when( repositoryMock ).getFile( any() );
      doReturn( repositoryMock ).when( fileServiceMock ).getRepository();

      RepositoryFileProvider repositoryProvider = new RepositoryFileProvider( repositoryMock, fileServiceMock );

      assertThrows( NotFoundException.class, () -> repositoryProvider.downloadFile( path ) );
    }
  }

  @Test
  void testDownloadFileExportException() throws Exception {
    String fileId = "8b69da2b-2a10-4a82-89bc-a376e52d5482";
    String fileName = "PAZReport.xanalyzer";
    String pathString = "/home/admin/" + fileId + "/" + fileName;
    GenericFilePath path = GenericFilePath.parse( pathString );

    try ( MockedStatic<SystemUtils> systemUtilsMock = mockStatic( SystemUtils.class ) ) {
      systemUtilsMock.when( () -> SystemUtils.canDownload( any() ) ).thenReturn( true );

      FileService fileServiceMock = mock( FileService.class );
      doReturn( true ).when( fileServiceMock ).isPathValid( any() );

      org.pentaho.platform.api.repository2.unified.RepositoryFile nativeFile =
        createNativeFile( pathString, fileName, false );

      IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
      doReturn( repositoryMock ).when( fileServiceMock ).getRepository();
      doReturn( nativeFile ).when( repositoryMock ).getFile( any() );

      RepositoryFileProvider repositoryProvider =
        Mockito.spy( new RepositoryFileProvider( repositoryMock, fileServiceMock ) );
      doThrow( new org.pentaho.platform.api.importexport.ExportException( "export failed" ) ).when( repositoryProvider )
        .getDownloadStream( any() );

      assertThrows( OperationFailedException.class, () -> repositoryProvider.downloadFile( path ) );
    }
  }
  // endregion
}

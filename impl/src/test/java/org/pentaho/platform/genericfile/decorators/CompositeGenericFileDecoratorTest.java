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

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.pentaho.platform.api.genericfile.GenericFilePath;
import org.pentaho.platform.api.genericfile.GetFileOptions;
import org.pentaho.platform.api.genericfile.GetTreeOptions;
import org.pentaho.platform.api.genericfile.IGenericFileDecorator;
import org.pentaho.platform.api.genericfile.IGenericFileService;
import org.pentaho.platform.api.genericfile.exception.OperationFailedException;
import org.pentaho.platform.api.genericfile.model.IGenericFile;
import org.pentaho.platform.api.genericfile.model.IGenericFileMetadata;
import org.pentaho.platform.api.genericfile.model.IGenericFileTree;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CompositeGenericFileDecoratorTest {
  // region constructor
  @Test
  void testCanBeCreatedWithEmptyDecoratorList() {
    List<IGenericFileDecorator> decoratorList = Collections.emptyList();

    new CompositeGenericFileDecorator( decoratorList );
  }

  @Test
  void testCanBeCreatedWithSingleDecorator() {
    IGenericFileDecorator mockDecorator = mock( IGenericFileDecorator.class );

    CompositeGenericFileDecorator decorator =
      new CompositeGenericFileDecorator( Collections.singletonList( mockDecorator ) );
    assertNotNull( decorator );
  }

  @Test
  void testCanBeCreatedWithMultipleDecorators() {
    IGenericFileDecorator decorator1 = mock( IGenericFileDecorator.class );
    IGenericFileDecorator decorator2 = mock( IGenericFileDecorator.class );

    CompositeGenericFileDecorator decorator =
      new CompositeGenericFileDecorator( Arrays.asList( decorator1, decorator2 ) );
    assertNotNull( decorator );
  }

  @Test
  void testThrowsNullPointerExceptionWithNullDecoratorList() {
    assertThrows( NullPointerException.class, () -> new CompositeGenericFileDecorator( null ) );
  }
  // endregion

  // region decorateFileMetadata
  @Test
  void testDecorateFileMetadataCallsAllDecoratorsInOrder() throws OperationFailedException {
    IGenericFileDecorator decorator1 = mock( IGenericFileDecorator.class );
    IGenericFileDecorator decorator2 = mock( IGenericFileDecorator.class );
    IGenericFileDecorator decorator3 = mock( IGenericFileDecorator.class );

    CompositeGenericFileDecorator composite =
      new CompositeGenericFileDecorator( Arrays.asList( decorator1, decorator2, decorator3 ) );

    IGenericFileMetadata metadata = mock( IGenericFileMetadata.class );
    GenericFilePath path = mock( GenericFilePath.class );
    IGenericFileService service = mock( IGenericFileService.class );

    composite.decorateFileMetadata( metadata, path, service );

    InOrder inOrder = inOrder( decorator1, decorator2, decorator3 );
    inOrder.verify( decorator1 ).decorateFileMetadata( metadata, path, service );
    inOrder.verify( decorator2 ).decorateFileMetadata( metadata, path, service );
    inOrder.verify( decorator3 ).decorateFileMetadata( metadata, path, service );
  }

  @Test
  void testDecorateFileMetadataContinuesOnException() throws OperationFailedException {
    IGenericFileDecorator decorator1 = mock( IGenericFileDecorator.class );
    IGenericFileDecorator decorator2 = mock( IGenericFileDecorator.class );
    IGenericFileDecorator decorator3 = mock( IGenericFileDecorator.class );

    IGenericFileMetadata metadata = mock( IGenericFileMetadata.class );
    GenericFilePath path = mock( GenericFilePath.class );
    IGenericFileService service = mock( IGenericFileService.class );

    doThrow( new OperationFailedException( "Decorator 2 failed" ) ).when( decorator2 )
      .decorateFileMetadata( metadata, path, service );

    CompositeGenericFileDecorator composite = new CompositeGenericFileDecorator(
      Arrays.asList( decorator1, decorator2, decorator3 ) );

    composite.decorateFileMetadata( metadata, path, service );

    verify( decorator1 ).decorateFileMetadata( metadata, path, service );
    verify( decorator2 ).decorateFileMetadata( metadata, path, service );
    verify( decorator3 ).decorateFileMetadata( metadata, path, service );
  }

  @SuppressWarnings( "ConstantConditions" )
  @Test
  void testDecorateFileMetadataWithNullParameters() {
    IGenericFileDecorator mockDecorator = mock( IGenericFileDecorator.class );
    CompositeGenericFileDecorator decorator = new CompositeGenericFileDecorator(
      Collections.singletonList( mockDecorator ) );

    assertThrows( NullPointerException.class, () ->
      decorator.decorateFileMetadata( null, mock( GenericFilePath.class ), mock( IGenericFileService.class ) ) );
    assertThrows( NullPointerException.class, () ->
      decorator.decorateFileMetadata( mock( IGenericFileMetadata.class ), null, mock( IGenericFileService.class ) ) );
    assertThrows( NullPointerException.class, () ->
      decorator.decorateFileMetadata( mock( IGenericFileMetadata.class ), mock( GenericFilePath.class ), null ) );
  }
  // endregion

  // region decorateFile
  @Test
  void testDecorateFileCallsAllDecoratorsInOrder() throws OperationFailedException {
    IGenericFileDecorator decorator1 = mock( IGenericFileDecorator.class );
    IGenericFileDecorator decorator2 = mock( IGenericFileDecorator.class );
    IGenericFileDecorator decorator3 = mock( IGenericFileDecorator.class );

    CompositeGenericFileDecorator composite = new CompositeGenericFileDecorator(
      Arrays.asList( decorator1, decorator2, decorator3 ) );

    IGenericFile file = mock( IGenericFile.class );
    IGenericFileService service = mock( IGenericFileService.class );
    GetFileOptions options = mock( GetFileOptions.class );

    composite.decorateFile( file, service, options );

    InOrder inOrder = inOrder( decorator1, decorator2, decorator3 );
    inOrder.verify( decorator1 ).decorateFile( file, service, options );
    inOrder.verify( decorator2 ).decorateFile( file, service, options );
    inOrder.verify( decorator3 ).decorateFile( file, service, options );
  }

  @Test
  void testDecorateFileContinuesOnException() throws OperationFailedException {
    IGenericFileDecorator decorator1 = mock( IGenericFileDecorator.class );
    IGenericFileDecorator decorator2 = mock( IGenericFileDecorator.class );
    IGenericFileDecorator decorator3 = mock( IGenericFileDecorator.class );

    IGenericFile file = mock( IGenericFile.class );
    IGenericFileService service = mock( IGenericFileService.class );
    GetFileOptions options = mock( GetFileOptions.class );

    doThrow( new OperationFailedException( "Decorator 1 failed" ) ).when( decorator1 )
      .decorateFile( file, service, options );

    CompositeGenericFileDecorator composite = new CompositeGenericFileDecorator(
      Arrays.asList( decorator1, decorator2, decorator3 ) );

    composite.decorateFile( file, service, options );

    verify( decorator1 ).decorateFile( file, service, options );
    verify( decorator2 ).decorateFile( file, service, options );
    verify( decorator3 ).decorateFile( file, service, options );
  }

  @SuppressWarnings( "ConstantConditions" )
  @Test
  void testDecorateFileWithNullParameters() {
    IGenericFileDecorator mockDecorator = mock( IGenericFileDecorator.class );
    CompositeGenericFileDecorator decorator = new CompositeGenericFileDecorator(
      Collections.singletonList( mockDecorator ) );

    assertThrows( NullPointerException.class, () ->
      decorator.decorateFile( null, mock( IGenericFileService.class ), mock( GetFileOptions.class ) ) );
    assertThrows( NullPointerException.class, () ->
      decorator.decorateFile( mock( IGenericFile.class ), null, mock( GetFileOptions.class ) ) );
    assertThrows( NullPointerException.class, () ->
      decorator.decorateFile( mock( IGenericFile.class ), mock( IGenericFileService.class ), null ) );
  }
  // endregion

  // region decorateTree
  @Test
  void testDecorateTreeCallsAllDecoratorsInOrder() throws OperationFailedException {
    IGenericFileDecorator decorator1 = mock( IGenericFileDecorator.class );
    IGenericFileDecorator decorator2 = mock( IGenericFileDecorator.class );
    IGenericFileDecorator decorator3 = mock( IGenericFileDecorator.class );

    CompositeGenericFileDecorator composite = new CompositeGenericFileDecorator(
      Arrays.asList( decorator1, decorator2, decorator3 ) );

    IGenericFileTree fileTree = mock( IGenericFileTree.class );
    IGenericFileService service = mock( IGenericFileService.class );
    GetTreeOptions options = mock( GetTreeOptions.class );

    composite.decorateTree( fileTree, service, options );

    InOrder inOrder = inOrder( decorator1, decorator2, decorator3 );
    inOrder.verify( decorator1 ).decorateTree( fileTree, service, options );
    inOrder.verify( decorator2 ).decorateTree( fileTree, service, options );
    inOrder.verify( decorator3 ).decorateTree( fileTree, service, options );
  }

  @Test
  void testDecorateTreeDoNotContinuesOnException() throws OperationFailedException {
    IGenericFileDecorator decorator1 = mock( IGenericFileDecorator.class );
    IGenericFileDecorator decorator2 = mock( IGenericFileDecorator.class );
    IGenericFileDecorator decorator3 = mock( IGenericFileDecorator.class );

    IGenericFileTree fileTree = mock( IGenericFileTree.class );
    IGenericFileService service = mock( IGenericFileService.class );
    GetTreeOptions options = mock( GetTreeOptions.class );

    doThrow( new OperationFailedException( "Decorator 3 failed" ) ).when( decorator3 )
      .decorateTree( fileTree, service, options );

    CompositeGenericFileDecorator composite = new CompositeGenericFileDecorator(
      Arrays.asList( decorator1, decorator2, decorator3 ) );

    composite.decorateTree( fileTree, service, options );

    verify( decorator1 ).decorateTree( fileTree, service, options );
    verify( decorator2 ).decorateTree( fileTree, service, options );
    verify( decorator3 ).decorateTree( fileTree, service, options );
  }

  @SuppressWarnings( "ConstantConditions" )
  @Test
  void testDecorateTreeWithNullParameters() {
    IGenericFileDecorator mockDecorator = mock( IGenericFileDecorator.class );
    CompositeGenericFileDecorator decorator = new CompositeGenericFileDecorator(
      Collections.singletonList( mockDecorator ) );

    assertThrows( NullPointerException.class, () ->
      decorator.decorateTree( null, mock( IGenericFileService.class ), mock( GetTreeOptions.class ) ) );
    assertThrows( NullPointerException.class, () ->
      decorator.decorateTree( mock( IGenericFileTree.class ), null, mock( GetTreeOptions.class ) ) );
    assertThrows( NullPointerException.class, () ->
      decorator.decorateTree( mock( IGenericFileTree.class ), mock( IGenericFileService.class ), null ) );
  }
  // endregion
}

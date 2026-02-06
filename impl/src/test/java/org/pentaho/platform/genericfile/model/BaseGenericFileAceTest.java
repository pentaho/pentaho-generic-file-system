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

package org.pentaho.platform.genericfile.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pentaho.platform.api.genericfile.GenericFilePermission;
import org.pentaho.platform.api.genericfile.GenericFileSid;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseGenericFileAceTest {
  private BaseGenericFileAce ace;

  @BeforeEach
  void setUp() {
    ace = new BaseGenericFileAce();
  }

  // region Constructor Tests
  @Test
  void testConstructor_initializesEmptyPermissions() {
    assertNotNull( ace.getPermissions() );
    assertTrue( ace.getPermissions().isEmpty() );
  }

  @Test
  void testConstructor_initializesNullRecipient() {
    assertNull( ace.getRecipient() );
  }

  @Test
  void testConstructor_initializesNullRecipientType() {
    assertNull( ace.getRecipientType() );
  }
  // endregion

  // region Recipient Tests
  @Test
  void testSetRecipient_setsRecipientSuccessfully() {
    ace.setRecipient( "user1" );
    assertEquals( "user1", ace.getRecipient() );
  }

  @Test
  void testSetRecipient_acceptsNull() {
    ace.setRecipient( "user1" );
    ace.setRecipient( null );
    assertNull( ace.getRecipient() );
  }

  @Test
  void testSetRecipient_replacesExistingRecipient() {
    ace.setRecipient( "user1" );
    ace.setRecipient( "user2" );
    assertEquals( "user2", ace.getRecipient() );
  }
  // endregion

  // region RecipientType Tests
  @Test
  void testSetRecipientType_setsRecipientTypeSuccessfully() {
    ace.setRecipientType( GenericFileSid.USER );
    assertEquals( GenericFileSid.USER, ace.getRecipientType() );
  }

  @Test
  void testSetRecipientType_acceptsNull() {
    ace.setRecipientType( GenericFileSid.ROLE );
    ace.setRecipientType( null );
    assertNull( ace.getRecipientType() );
  }
  // endregion

  // region addPermission Tests
  @Test
  void testAddPermission_addsPermissionSuccessfully() {
    ace.addPermission( GenericFilePermission.READ );
    assertEquals( 1, ace.getPermissions().size() );
    assertEquals( GenericFilePermission.READ, ace.getPermissions().get( 0 ) );
  }

  @Test
  void testAddPermission_addsMultiplePermissions() {
    ace.addPermission( GenericFilePermission.READ );
    ace.addPermission( GenericFilePermission.WRITE );
    ace.addPermission( GenericFilePermission.DELETE );

    assertEquals( 3, ace.getPermissions().size() );
    assertEquals( GenericFilePermission.READ, ace.getPermissions().get( 0 ) );
    assertEquals( GenericFilePermission.WRITE, ace.getPermissions().get( 1 ) );
    assertEquals( GenericFilePermission.DELETE, ace.getPermissions().get( 2 ) );
  }

  @Test
  void testAddPermission_throwsNullPointerException() {
    assertThrows( NullPointerException.class, () -> ace.addPermission( null ) );
  }

  @Test
  void testAddPermission_maintainsOrder() {
    ace.addPermission( GenericFilePermission.READ );
    ace.addPermission( GenericFilePermission.WRITE );
    ace.addPermission( GenericFilePermission.DELETE );

    assertEquals( GenericFilePermission.READ, ace.getPermissions().get( 0 ) );
    assertEquals( GenericFilePermission.WRITE, ace.getPermissions().get( 1 ) );
    assertEquals( GenericFilePermission.DELETE, ace.getPermissions().get( 2 ) );
  }

  @Test
  void testAddPermission_afterSetPermissions() {
    List<GenericFilePermission> initialPermissions = new ArrayList<>();
    initialPermissions.add( GenericFilePermission.READ );
    ace.setPermissions( initialPermissions );

    ace.addPermission( GenericFilePermission.WRITE );

    assertEquals( 2, ace.getPermissions().size() );
    assertEquals( GenericFilePermission.READ, ace.getPermissions().get( 0 ) );
    assertEquals( GenericFilePermission.WRITE, ace.getPermissions().get( 1 ) );
  }

  @Test
  void testAddPermission_canAddSamePermissionMultipleTimes() {
    ace.addPermission( GenericFilePermission.READ );
    ace.addPermission( GenericFilePermission.READ );

    assertEquals( 2, ace.getPermissions().size() );
    assertEquals( GenericFilePermission.READ, ace.getPermissions().get( 0 ) );
    assertEquals( GenericFilePermission.READ, ace.getPermissions().get( 1 ) );
  }

  @Test
  void testAddPermission_withManyPermissions() {
    for ( int i = 0; i < 100; i++ ) {
      ace.addPermission( GenericFilePermission.READ );
    }

    assertEquals( 100, ace.getPermissions().size() );
  }
  // endregion

  // region setPermissions Tests
  @Test
  void testSetPermissions_setsPermissionsSuccessfully() {
    List<GenericFilePermission> permissions = new ArrayList<>();
    permissions.add( GenericFilePermission.READ );
    permissions.add( GenericFilePermission.WRITE );

    ace.setPermissions( permissions );

    assertEquals( 2, ace.getPermissions().size() );
    assertSame( permissions, ace.getPermissions() );
  }

  @Test
  void testSetPermissions_throwsNullPointerException() {
    assertThrows( NullPointerException.class, () -> ace.setPermissions( null ) );
  }

  @Test
  void testSetPermissions_replacesExistingPermissions() {
    List<GenericFilePermission> firstPermissions = new ArrayList<>();
    firstPermissions.add( GenericFilePermission.READ );
    ace.setPermissions( firstPermissions );

    List<GenericFilePermission> newPermissions = new ArrayList<>();
    newPermissions.add( GenericFilePermission.WRITE );
    newPermissions.add( GenericFilePermission.DELETE );

    ace.setPermissions( newPermissions );

    assertEquals( 2, ace.getPermissions().size() );
    assertEquals( GenericFilePermission.WRITE, ace.getPermissions().get( 0 ) );
    assertEquals( GenericFilePermission.DELETE, ace.getPermissions().get( 1 ) );
  }

  @Test
  void testSetPermissions_acceptsEmptyList() {
    List<GenericFilePermission> emptyList = new ArrayList<>();

    ace.setPermissions( emptyList );

    assertNotNull( ace.getPermissions() );
    assertTrue( ace.getPermissions().isEmpty() );
  }

  @Test
  void testGetPermissions_returnsNonNull() {
    assertNotNull( ace.getPermissions() );
  }

  @Test
  void testGetPermissions_returnsSameInstanceAfterSet() {
    List<GenericFilePermission> permissions = new ArrayList<>();
    ace.setPermissions( permissions );

    assertSame( permissions, ace.getPermissions() );
  }
  // endregion

  // region Integration Tests
  @Test
  void testCompleteAceSetup() {
    ace.setRecipient( "user1" );
    ace.setRecipientType( GenericFileSid.USER );
    ace.addPermission( GenericFilePermission.READ );
    ace.addPermission( GenericFilePermission.WRITE );

    assertEquals( "user1", ace.getRecipient() );
    assertEquals( GenericFileSid.USER, ace.getRecipientType() );
    assertEquals( 2, ace.getPermissions().size() );
    assertEquals( GenericFilePermission.READ, ace.getPermissions().get( 0 ) );
    assertEquals( GenericFilePermission.WRITE, ace.getPermissions().get( 1 ) );
  }
  // endregion
}


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
import org.pentaho.platform.api.genericfile.GenericFilePrincipalType;
import org.pentaho.platform.api.genericfile.model.IGenericFileAce;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class BaseGenericFileAclTest {
  private BaseGenericFileAcl acl;

  @BeforeEach
  void setUp() {
    acl = new BaseGenericFileAcl();
  }

  // region Constructor Tests
  @Test
  void testConstructor_initializesEmptyEntries() {
    assertNotNull( acl.getEntries() );
    assertTrue( acl.getEntries().isEmpty() );
  }

  @Test
  void testConstructor_initializesNullOwner() {
    assertNull( acl.getOwner() );
  }

  @Test
  void testConstructor_initializesNullOwnerType() {
    assertNull( acl.getOwnerType() );
  }

  @Test
  void testConstructor_initializesFalseEntriesInheriting() {
    assertFalse( acl.isEntriesInheriting() );
  }
  // endregion

  // region Owner Tests
  @Test
  void testSetOwner_setsOwnerSuccessfully() {
    acl.setOwner( "admin" );

    assertEquals( "admin", acl.getOwner() );
  }

  @Test
  void testSetOwner_acceptsNull() {
    acl.setOwner( "admin" );
    acl.setOwner( null );

    assertNull( acl.getOwner() );
  }

  @Test
  void testSetOwner_replacesExistingOwner() {
    acl.setOwner( "admin" );
    acl.setOwner( "user1" );

    assertEquals( "user1", acl.getOwner() );
  }
  // endregion

  // region OwnerType Tests
  @Test
  void testSetOwnerType_setsOwnerTypeSuccessfully() {
    acl.setOwnerType( GenericFilePrincipalType.USER );

    assertEquals( GenericFilePrincipalType.USER, acl.getOwnerType() );
  }

  @Test
  void testSetOwnerType_acceptsNull() {
    acl.setOwnerType( GenericFilePrincipalType.ROLE );
    acl.setOwnerType( null );

    assertNull( acl.getOwnerType() );
  }

  @Test
  void testSetOwnerType_replacesExistingOwnerType() {
    acl.setOwnerType( GenericFilePrincipalType.USER );
    acl.setOwnerType( GenericFilePrincipalType.ROLE );

    assertEquals( GenericFilePrincipalType.ROLE, acl.getOwnerType() );
  }

  @Test
  void testSetOwnerType_supportsAllEnumValues() {
    for ( GenericFilePrincipalType sidType : GenericFilePrincipalType.values() ) {
      acl.setOwnerType( sidType );
      assertEquals( sidType, acl.getOwnerType() );
    }
  }
  // endregion

  // region EntriesInheriting Tests
  @Test
  void testSetEntriesInheriting_setsToTrue() {
    acl.setEntriesInheriting( true );

    assertTrue( acl.isEntriesInheriting() );
  }

  @Test
  void testSetEntriesInheriting_setsToFalse() {
    acl.setEntriesInheriting( true );
    acl.setEntriesInheriting( false );

    assertFalse( acl.isEntriesInheriting() );
  }

  @Test
  void testSetEntriesInheriting_togglesValue() {
    assertFalse( acl.isEntriesInheriting() );

    acl.setEntriesInheriting( true );
    assertTrue( acl.isEntriesInheriting() );

    acl.setEntriesInheriting( false );
    assertFalse( acl.isEntriesInheriting() );
  }
  // endregion

  // region Entries Tests
  @Test
  void testSetEntries_setsEntriesSuccessfully() {
    List<IGenericFileAce> entries = new ArrayList<>();
    IGenericFileAce ace1 = mock( IGenericFileAce.class );
    IGenericFileAce ace2 = mock( IGenericFileAce.class );
    entries.add( ace1 );
    entries.add( ace2 );

    acl.setEntries( entries );

    assertEquals( 2, acl.getEntries().size() );
    assertSame( ace1, acl.getEntries().get( 0 ) );
    assertSame( ace2, acl.getEntries().get( 1 ) );
  }

  @Test
  void testSetEntries_replacesExistingEntries() {
    List<IGenericFileAce> firstEntries = new ArrayList<>();
    firstEntries.add( mock( IGenericFileAce.class ) );
    acl.setEntries( firstEntries );

    List<IGenericFileAce> newEntries = new ArrayList<>();
    IGenericFileAce newAce = mock( IGenericFileAce.class );
    newEntries.add( newAce );

    acl.setEntries( newEntries );

    assertEquals( 1, acl.getEntries().size() );
    assertSame( newAce, acl.getEntries().get( 0 ) );
  }

  @Test
  void testSetEntries_throwsNullPointerException() {
    assertThrows( NullPointerException.class, () -> acl.setEntries( null ) );
  }

  @Test
  void testSetEntries_acceptsEmptyList() {
    List<IGenericFileAce> emptyList = new ArrayList<>();

    acl.setEntries( emptyList );

    assertNotNull( acl.getEntries() );
    assertTrue( acl.getEntries().isEmpty() );
  }

  @Test
  void testGetEntries_returnsNonNull() {
    assertNotNull( acl.getEntries() );
  }

  @Test
  void testGetEntries_returnsSameInstanceAfterSet() {
    List<IGenericFileAce> entries = new ArrayList<>();
    acl.setEntries( entries );

    assertSame( entries, acl.getEntries() );
  }
  // endregion

  // region addEntry Tests
  @Test
  void testAddEntry_addsEntrySuccessfully() {
    IGenericFileAce ace = mock( IGenericFileAce.class );

    acl.addEntry( ace );

    assertEquals( 1, acl.getEntries().size() );
    assertSame( ace, acl.getEntries().get( 0 ) );
  }

  @Test
  void testAddEntry_addsMultipleEntries() {
    IGenericFileAce ace1 = mock( IGenericFileAce.class );
    IGenericFileAce ace2 = mock( IGenericFileAce.class );
    IGenericFileAce ace3 = mock( IGenericFileAce.class );

    acl.addEntry( ace1 );
    acl.addEntry( ace2 );
    acl.addEntry( ace3 );

    assertEquals( 3, acl.getEntries().size() );
    assertSame( ace1, acl.getEntries().get( 0 ) );
    assertSame( ace2, acl.getEntries().get( 1 ) );
    assertSame( ace3, acl.getEntries().get( 2 ) );
  }

  @Test
  void testAddEntry_throwsNullPointerException() {
    assertThrows( NullPointerException.class, () -> acl.addEntry( null ) );
  }

  @Test
  void testAddEntry_maintainsOrder() {
    IGenericFileAce first = mock( IGenericFileAce.class );
    IGenericFileAce second = mock( IGenericFileAce.class );
    IGenericFileAce third = mock( IGenericFileAce.class );

    acl.addEntry( first );
    acl.addEntry( second );
    acl.addEntry( third );

    assertEquals( first, acl.getEntries().get( 0 ) );
    assertEquals( second, acl.getEntries().get( 1 ) );
    assertEquals( third, acl.getEntries().get( 2 ) );
  }

  @Test
  void testAddEntry_afterSetEntries() {
    List<IGenericFileAce> initialEntries = new ArrayList<>();
    IGenericFileAce initialAce = mock( IGenericFileAce.class );
    initialEntries.add( initialAce );

    acl.setEntries( initialEntries );

    IGenericFileAce newAce = mock( IGenericFileAce.class );
    acl.addEntry( newAce );

    assertEquals( 2, acl.getEntries().size() );
    assertSame( initialAce, acl.getEntries().get( 0 ) );
    assertSame( newAce, acl.getEntries().get( 1 ) );
  }

  @Test
  void testAddEntry_canAddSameEntryMultipleTimes() {
    IGenericFileAce ace = mock( IGenericFileAce.class );

    acl.addEntry( ace );
    acl.addEntry( ace );

    assertEquals( 2, acl.getEntries().size() );
    assertSame( ace, acl.getEntries().get( 0 ) );
    assertSame( ace, acl.getEntries().get( 1 ) );
  }

  @Test
  void testAddEntry_withManyEntries() {
    for ( int i = 0; i < 100; i++ ) {
      acl.addEntry( mock( IGenericFileAce.class ) );
    }

    assertEquals( 100, acl.getEntries().size() );
  }
  // endregion

  // region Integration Tests
  @Test
  void testCompleteAclSetup() {
    acl.setOwner( "admin" );
    acl.setOwnerType( GenericFilePrincipalType.USER );
    acl.setEntriesInheriting( true );

    IGenericFileAce ace1 = mock( IGenericFileAce.class );
    IGenericFileAce ace2 = mock( IGenericFileAce.class );
    acl.addEntry( ace1 );
    acl.addEntry( ace2 );

    assertEquals( "admin", acl.getOwner() );
    assertEquals( GenericFilePrincipalType.USER, acl.getOwnerType() );
    assertTrue( acl.isEntriesInheriting() );
    assertEquals( 2, acl.getEntries().size() );
  }
  // endregion
}


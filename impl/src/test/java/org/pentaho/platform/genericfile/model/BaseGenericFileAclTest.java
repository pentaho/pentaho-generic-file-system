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

import org.junit.jupiter.api.Test;
import org.pentaho.platform.api.genericfile.GenericFilePrincipalType;
import org.pentaho.platform.api.genericfile.model.IGenericFileAce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@SuppressWarnings( "DataFlowIssue" )
class BaseGenericFileAclTest {
  // region Constructor Tests
  @Test
  void testConstructorWithValidParameters() {
    String owner = "admin";
    GenericFilePrincipalType ownerType = GenericFilePrincipalType.USER;
    boolean entriesInheriting = true;
    String tenantPath = "/pentaho/tenant1";
    List<IGenericFileAce> entries = new ArrayList<>();

    BaseGenericFileAcl acl = new BaseGenericFileAcl( owner, ownerType, entriesInheriting, tenantPath, entries );

    assertEquals( owner, acl.getOwner() );
    assertEquals( ownerType, acl.getOwnerType() );
    assertTrue( acl.isEntriesInheriting() );
    assertEquals( tenantPath, acl.getTenantPath() );
    assertEquals( entries, acl.getEntries() );
  }

  @Test
  void testConstructorWithNullTenantPath() {
    String owner = "admin";
    GenericFilePrincipalType ownerType = GenericFilePrincipalType.USER;
    List<IGenericFileAce> entries = new ArrayList<>();

    BaseGenericFileAcl acl = new BaseGenericFileAcl( owner, ownerType, false, null, entries );

    assertEquals( owner, acl.getOwner() );
    assertEquals( ownerType, acl.getOwnerType() );
    assertFalse( acl.isEntriesInheriting() );
    assertNull( acl.getTenantPath() );
    assertEquals( entries, acl.getEntries() );
  }

  @Test
  void testConstructorWithNullOwner() {
    GenericFilePrincipalType ownerType = GenericFilePrincipalType.USER;
    List<IGenericFileAce> entries = new ArrayList<>();

    assertThrows( NullPointerException.class,
      () -> new BaseGenericFileAcl( null, ownerType, false, "/tenant", entries ) );
  }

  @Test
  void testConstructorWithNullOwnerType() {
    String owner = "admin";
    List<IGenericFileAce> entries = new ArrayList<>();

    assertThrows( NullPointerException.class,
      () -> new BaseGenericFileAcl( owner, null, false, "/tenant", entries ) );
  }

  @Test
  void testConstructorWithNullEntries() {
    String owner = "admin";
    GenericFilePrincipalType ownerType = GenericFilePrincipalType.USER;

    assertThrows( NullPointerException.class,
      () -> new BaseGenericFileAcl( owner, ownerType, false, "/tenant", null ) );
  }
  // endregion

  // region Owner Tests
  @Test
  void testGetOwner() {
    String owner = "testUser";
    BaseGenericFileAcl acl = new BaseGenericFileAcl( owner, GenericFilePrincipalType.USER, false,
      null, Collections.emptyList() );

    assertEquals( owner, acl.getOwner() );
  }

  @Test
  void testGetOwnerWithDifferentValues() {
    List<String> owners = List.of( "admin", "user1", "role1", "testOwner123" );

    for ( String owner : owners ) {
      BaseGenericFileAcl acl = new BaseGenericFileAcl( owner, GenericFilePrincipalType.USER, false,
        null, Collections.emptyList() );
      assertEquals( owner, acl.getOwner() );
    }
  }
  // endregion

  // region OwnerType Tests
  @Test
  void testGetOwnerTypeUser() {
    BaseGenericFileAcl acl = new BaseGenericFileAcl( "admin", GenericFilePrincipalType.USER, false,
      null, Collections.emptyList() );

    assertEquals( GenericFilePrincipalType.USER, acl.getOwnerType() );
  }

  @Test
  void testGetOwnerTypeRole() {
    BaseGenericFileAcl acl = new BaseGenericFileAcl( "admin", GenericFilePrincipalType.ROLE, false,
      null, Collections.emptyList() );

    assertEquals( GenericFilePrincipalType.ROLE, acl.getOwnerType() );
  }

  @Test
  void testGetOwnerTypeAllEnumValues() {
    for ( GenericFilePrincipalType type : GenericFilePrincipalType.values() ) {
      BaseGenericFileAcl acl = new BaseGenericFileAcl( "admin", type, false, null, Collections.emptyList() );
      assertEquals( type, acl.getOwnerType() );
    }
  }
  // endregion

  // region EntriesInheriting Tests
  @Test
  void testIsEntriesInheritingTrue() {
    BaseGenericFileAcl acl = new BaseGenericFileAcl( "admin", GenericFilePrincipalType.USER, true,
      null, Collections.emptyList() );

    assertTrue( acl.isEntriesInheriting() );
  }

  @Test
  void testIsEntriesInheritingFalse() {
    BaseGenericFileAcl acl = new BaseGenericFileAcl( "admin", GenericFilePrincipalType.USER, false,
      null, Collections.emptyList() );

    assertFalse( acl.isEntriesInheriting() );
  }
  // endregion

  // region TenantPath Tests
  @Test
  void testGetTenantPath() {
    String tenantPath = "/pentaho/tenant1";
    BaseGenericFileAcl acl = new BaseGenericFileAcl( "admin", GenericFilePrincipalType.USER, false,
      tenantPath, Collections.emptyList() );

    assertEquals( tenantPath, acl.getTenantPath() );
  }

  @Test
  void testGetTenantPathNull() {
    BaseGenericFileAcl acl = new BaseGenericFileAcl( "admin", GenericFilePrincipalType.USER, false,
      null, Collections.emptyList() );

    assertNull( acl.getTenantPath() );
  }
  // endregion

  // region Entries Tests
  @Test
  void testGetEntriesEmpty() {
    List<IGenericFileAce> entries = Collections.emptyList();
    BaseGenericFileAcl acl = new BaseGenericFileAcl( "admin", GenericFilePrincipalType.USER, false,
      null, entries );

    assertEquals( entries, acl.getEntries() );
    assertTrue( acl.getEntries().isEmpty() );
  }

  @Test
  void testGetEntriesSingle() {
    IGenericFileAce ace = mock( IGenericFileAce.class );
    List<IGenericFileAce> entries = List.of( ace );
    BaseGenericFileAcl acl = new BaseGenericFileAcl( "admin", GenericFilePrincipalType.USER, false,
      null, entries );

    assertEquals( entries, acl.getEntries() );
    assertEquals( 1, acl.getEntries().size() );
  }

  @Test
  void testGetEntriesMultiple() {
    IGenericFileAce ace1 = mock( IGenericFileAce.class );
    IGenericFileAce ace2 = mock( IGenericFileAce.class );
    IGenericFileAce ace3 = mock( IGenericFileAce.class );
    List<IGenericFileAce> entries = List.of( ace1, ace2, ace3 );
    BaseGenericFileAcl acl = new BaseGenericFileAcl( "admin", GenericFilePrincipalType.USER, false,
      null, entries );

    assertEquals( entries, acl.getEntries() );
    assertEquals( 3, acl.getEntries().size() );
  }

  @Test
  void testGetEntriesReturnsSameInstance() {
    List<IGenericFileAce> entries = new ArrayList<>();
    entries.add( mock( IGenericFileAce.class ) );
    BaseGenericFileAcl acl = new BaseGenericFileAcl( "admin", GenericFilePrincipalType.USER, false,
      null, entries );

    assertEquals( entries, acl.getEntries() );

    // Verify it's the same instance
    for ( int i = 0; i < entries.size(); i++ ) {
      assertEquals( entries.get( i ), acl.getEntries().get( i ) );
    }
  }
  // endregion

  // region Integration Tests
  @Test
  void testCompleteAclSetup() {
    String owner = "admin";
    GenericFilePrincipalType ownerType = GenericFilePrincipalType.USER;
    boolean entriesInheriting = true;
    String tenantPath = "/pentaho/tenant1";
    IGenericFileAce ace1 = mock( IGenericFileAce.class );
    IGenericFileAce ace2 = mock( IGenericFileAce.class );
    List<IGenericFileAce> entries = List.of( ace1, ace2 );

    BaseGenericFileAcl acl = new BaseGenericFileAcl( owner, ownerType, entriesInheriting, tenantPath, entries );

    assertEquals( owner, acl.getOwner() );
    assertEquals( ownerType, acl.getOwnerType() );
    assertTrue( acl.isEntriesInheriting() );
    assertEquals( tenantPath, acl.getTenantPath() );
    assertEquals( 2, acl.getEntries().size() );
    assertEquals( ace1, acl.getEntries().get( 0 ) );
    assertEquals( ace2, acl.getEntries().get( 1 ) );
  }

  @Test
  void testAclWithRoleOwner() {
    BaseGenericFileAcl acl = new BaseGenericFileAcl( "adminRole", GenericFilePrincipalType.ROLE, false,
      "/tenant", Collections.emptyList() );

    assertEquals( "adminRole", acl.getOwner() );
    assertEquals( GenericFilePrincipalType.ROLE, acl.getOwnerType() );
    assertEquals( "/tenant", acl.getTenantPath() );
    assertTrue( acl.getEntries().isEmpty() );
  }

  @Test
  void testAclWithManyEntries() {
    List<IGenericFileAce> entries = new ArrayList<>();

    for ( int i = 0; i < 100; i++ ) {
      entries.add( mock( IGenericFileAce.class ) );
    }

    BaseGenericFileAcl acl = new BaseGenericFileAcl( "admin", GenericFilePrincipalType.USER, true,
      "/tenant", entries );

    assertEquals( 100, acl.getEntries().size() );
    assertTrue( acl.isEntriesInheriting() );
    assertEquals( "/tenant", acl.getTenantPath() );
  }
  // endregion
}

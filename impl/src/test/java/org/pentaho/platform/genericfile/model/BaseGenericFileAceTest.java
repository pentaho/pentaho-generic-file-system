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
import org.pentaho.platform.api.genericfile.GenericFilePermission;
import org.pentaho.platform.api.genericfile.GenericFilePrincipalType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings( "DataFlowIssue" )
class BaseGenericFileAceTest {
  // region Constructor Tests
  @Test
  void testConstructorWithValidParameters() {
    String recipient = "user1";
    GenericFilePrincipalType recipientType = GenericFilePrincipalType.USER;
    String tenantPath = "/pentaho/tenant1";
    boolean modifiable = true;
    List<GenericFilePermission> permissions = List.of( GenericFilePermission.READ );

    BaseGenericFileAce ace = new BaseGenericFileAce( recipient, recipientType, tenantPath, modifiable, permissions );

    assertEquals( recipient, ace.getRecipient() );
    assertEquals( recipientType, ace.getRecipientType() );
    assertEquals( tenantPath, ace.getTenantPath() );
    assertTrue( ace.isModifiable() );
    assertEquals( permissions, ace.getPermissions() );
  }

  @Test
  void testConstructorWithNullTenantPath() {
    String recipient = "user1";
    GenericFilePrincipalType recipientType = GenericFilePrincipalType.USER;
    List<GenericFilePermission> permissions = List.of( GenericFilePermission.READ );

    BaseGenericFileAce ace = new BaseGenericFileAce( recipient, recipientType, null, false, permissions );

    assertEquals( recipient, ace.getRecipient() );
    assertEquals( recipientType, ace.getRecipientType() );
    assertNull( ace.getTenantPath() );
    assertFalse( ace.isModifiable() );
    assertEquals( permissions, ace.getPermissions() );
  }

  @Test
  void testConstructorWithNullRecipient() {
    GenericFilePrincipalType recipientType = GenericFilePrincipalType.USER;
    List<GenericFilePermission> permissions = List.of( GenericFilePermission.READ );

    assertThrows( NullPointerException.class,
      () -> new BaseGenericFileAce( null, recipientType, "/tenant", true, permissions ) );
  }

  @Test
  void testConstructorWithNullRecipientType() {
    String recipient = "user1";
    List<GenericFilePermission> permissions = List.of( GenericFilePermission.READ );

    assertThrows( NullPointerException.class,
      () -> new BaseGenericFileAce( recipient, null, "/tenant", true, permissions ) );
  }

  @Test
  void testConstructorWithNullPermissions() {
    String recipient = "user1";
    GenericFilePrincipalType recipientType = GenericFilePrincipalType.USER;

    assertThrows( NullPointerException.class,
      () -> new BaseGenericFileAce( recipient, recipientType, "/tenant", true, null ) );
  }
  // endregion

  // region Recipient Tests
  @Test
  void testGetRecipient() {
    String recipient = "testUser";
    BaseGenericFileAce ace = new BaseGenericFileAce( recipient, GenericFilePrincipalType.USER,
      null, false, Collections.emptyList() );

    assertEquals( recipient, ace.getRecipient() );
  }

  @Test
  void testGetRecipientWithDifferentValues() {
    List<String> recipients = List.of( "user1", "admin", "role1", "testRecipient123" );

    for ( String recipient : recipients ) {
      BaseGenericFileAce ace = new BaseGenericFileAce( recipient, GenericFilePrincipalType.USER,
        null, false, Collections.emptyList() );
      assertEquals( recipient, ace.getRecipient() );
    }
  }

  @Test
  void testGetRecipientWithSpecialCharacters() {
    String recipient = "user@domain.com";
    BaseGenericFileAce ace = new BaseGenericFileAce( recipient, GenericFilePrincipalType.USER,
      null, false, Collections.emptyList() );

    assertEquals( recipient, ace.getRecipient() );
  }
  // endregion

  // region RecipientType Tests
  @Test
  void testGetRecipientTypeUser() {
    BaseGenericFileAce ace = new BaseGenericFileAce( "user1", GenericFilePrincipalType.USER,
      null, false, Collections.emptyList() );

    assertEquals( GenericFilePrincipalType.USER, ace.getRecipientType() );
  }

  @Test
  void testGetRecipientTypeRole() {
    BaseGenericFileAce ace = new BaseGenericFileAce( "role1", GenericFilePrincipalType.ROLE,
      null, false, Collections.emptyList() );

    assertEquals( GenericFilePrincipalType.ROLE, ace.getRecipientType() );
  }

  @Test
  void testGetRecipientTypeAllEnumValues() {
    for ( GenericFilePrincipalType type : GenericFilePrincipalType.values() ) {
      BaseGenericFileAce ace = new BaseGenericFileAce( "recipient", type, null, false, Collections.emptyList() );
      assertEquals( type, ace.getRecipientType() );
    }
  }
  // endregion

  // region TenantPath Tests
  @Test
  void testGetTenantPath() {
    String tenantPath = "/pentaho/tenant1";
    BaseGenericFileAce ace = new BaseGenericFileAce( "user1", GenericFilePrincipalType.USER,
      tenantPath, false, Collections.emptyList() );

    assertEquals( tenantPath, ace.getTenantPath() );
  }

  @Test
  void testGetTenantPathNull() {
    BaseGenericFileAce ace = new BaseGenericFileAce( "user1", GenericFilePrincipalType.USER,
      null, false, Collections.emptyList() );

    assertNull( ace.getTenantPath() );
  }
  // endregion

  // region Modifiable Tests
  @Test
  void testIsModifiableTrue() {
    BaseGenericFileAce ace = new BaseGenericFileAce( "user1", GenericFilePrincipalType.USER,
      null, true, Collections.emptyList() );

    assertTrue( ace.isModifiable() );
  }

  @Test
  void testIsModifiableFalse() {
    BaseGenericFileAce ace = new BaseGenericFileAce( "user1", GenericFilePrincipalType.USER,
      null, false, Collections.emptyList() );

    assertFalse( ace.isModifiable() );
  }
  // endregion

  // region Permissions Tests
  @Test
  void testGetPermissionsEmpty() {
    List<GenericFilePermission> permissions = Collections.emptyList();
    BaseGenericFileAce ace = new BaseGenericFileAce( "user1", GenericFilePrincipalType.USER,
      null, false, permissions );

    assertEquals( permissions, ace.getPermissions() );
    assertTrue( ace.getPermissions().isEmpty() );
  }

  @Test
  void testGetPermissionsSingle() {
    List<GenericFilePermission> permissions = List.of( GenericFilePermission.READ );
    BaseGenericFileAce ace = new BaseGenericFileAce( "user1", GenericFilePrincipalType.USER,
      null, false, permissions );

    assertEquals( permissions, ace.getPermissions() );
    assertEquals( 1, ace.getPermissions().size() );
    assertEquals( GenericFilePermission.READ, ace.getPermissions().get( 0 ) );
  }

  @Test
  void testGetPermissionsMultiple() {
    List<GenericFilePermission> permissions = List.of(
      GenericFilePermission.READ,
      GenericFilePermission.WRITE,
      GenericFilePermission.DELETE
    );
    BaseGenericFileAce ace = new BaseGenericFileAce( "user1", GenericFilePrincipalType.USER,
      null, false, permissions );

    assertEquals( permissions, ace.getPermissions() );
    assertEquals( 3, ace.getPermissions().size() );
    assertEquals( GenericFilePermission.READ, ace.getPermissions().get( 0 ) );
    assertEquals( GenericFilePermission.WRITE, ace.getPermissions().get( 1 ) );
    assertEquals( GenericFilePermission.DELETE, ace.getPermissions().get( 2 ) );
  }

  @Test
  void testGetPermissionsAllEnumValues() {
    List<GenericFilePermission> permissions = new ArrayList<>();
    Collections.addAll( permissions, GenericFilePermission.values() );

    BaseGenericFileAce ace = new BaseGenericFileAce( "user1", GenericFilePrincipalType.USER,
      null, false, permissions );

    assertEquals( GenericFilePermission.values().length, ace.getPermissions().size() );

    for ( int i = 0; i < GenericFilePermission.values().length; i++ ) {
      assertEquals( GenericFilePermission.values()[ i ], ace.getPermissions().get( i ) );
    }
  }

  @Test
  void testGetPermissionsReturnsSameInstance() {
    List<GenericFilePermission> permissions = new ArrayList<>();
    permissions.add( GenericFilePermission.READ );
    permissions.add( GenericFilePermission.WRITE );
    BaseGenericFileAce ace = new BaseGenericFileAce( "user1", GenericFilePrincipalType.USER,
      null, false, permissions );

    assertEquals( permissions, ace.getPermissions() );

    for ( int i = 0; i < permissions.size(); i++ ) {
      assertEquals( permissions.get( i ), ace.getPermissions().get( i ) );
    }
  }

  @Test
  void testGetPermissionsDuplicates() {
    List<GenericFilePermission> permissions = List.of(
      GenericFilePermission.READ,
      GenericFilePermission.READ,
      GenericFilePermission.WRITE
    );
    BaseGenericFileAce ace = new BaseGenericFileAce( "user1", GenericFilePrincipalType.USER,
      null, false, permissions );

    assertEquals( 3, ace.getPermissions().size() );
    assertEquals( GenericFilePermission.READ, ace.getPermissions().get( 0 ) );
    assertEquals( GenericFilePermission.READ, ace.getPermissions().get( 1 ) );
    assertEquals( GenericFilePermission.WRITE, ace.getPermissions().get( 2 ) );
  }
  // endregion

  // region Integration Tests
  @Test
  void testCompleteAceSetup() {
    String recipient = "user1";
    GenericFilePrincipalType recipientType = GenericFilePrincipalType.USER;
    String tenantPath = "/pentaho/tenant1";
    boolean modifiable = true;
    List<GenericFilePermission> permissions = List.of(
      GenericFilePermission.READ,
      GenericFilePermission.WRITE
    );

    BaseGenericFileAce ace = new BaseGenericFileAce( recipient, recipientType, tenantPath, modifiable, permissions );

    assertEquals( recipient, ace.getRecipient() );
    assertEquals( recipientType, ace.getRecipientType() );
    assertEquals( tenantPath, ace.getTenantPath() );
    assertTrue( ace.isModifiable() );
    assertEquals( 2, ace.getPermissions().size() );
    assertEquals( GenericFilePermission.READ, ace.getPermissions().get( 0 ) );
    assertEquals( GenericFilePermission.WRITE, ace.getPermissions().get( 1 ) );
  }

  @Test
  void testAceWithRoleRecipient() {
    BaseGenericFileAce ace = new BaseGenericFileAce( "adminRole", GenericFilePrincipalType.ROLE,
      "/tenant", false,
      List.of( GenericFilePermission.READ, GenericFilePermission.WRITE, GenericFilePermission.DELETE ) );

    assertEquals( "adminRole", ace.getRecipient() );
    assertEquals( GenericFilePrincipalType.ROLE, ace.getRecipientType() );
    assertEquals( "/tenant", ace.getTenantPath() );
    assertFalse( ace.isModifiable() );
    assertEquals( 3, ace.getPermissions().size() );
  }

  @Test
  void testAceWithAllPermissions() {
    List<GenericFilePermission> allPermissions = new ArrayList<>();
    Collections.addAll( allPermissions, GenericFilePermission.values() );

    BaseGenericFileAce ace = new BaseGenericFileAce( "user1", GenericFilePrincipalType.USER,
      null, true, allPermissions );

    assertEquals( GenericFilePermission.values().length, ace.getPermissions().size() );
  }

  @Test
  void testAceWithSpecialRecipientName() {
    String recipient = "user-123@domain.co.uk";
    BaseGenericFileAce ace = new BaseGenericFileAce( recipient, GenericFilePrincipalType.USER,
      "/tenant", true, List.of( GenericFilePermission.READ ) );

    assertEquals( recipient, ace.getRecipient() );
    assertEquals( 1, ace.getPermissions().size() );
  }

  @Test
  void testMultipleAceInstancesAreIndependent() {
    List<GenericFilePermission> permissions1 = List.of( GenericFilePermission.READ );
    List<GenericFilePermission> permissions2 = List.of( GenericFilePermission.WRITE, GenericFilePermission.DELETE );

    BaseGenericFileAce ace1 = new BaseGenericFileAce( "user1", GenericFilePrincipalType.USER,
      "/tenant1", true, permissions1 );
    BaseGenericFileAce ace2 = new BaseGenericFileAce( "user2", GenericFilePrincipalType.ROLE,
      "/tenant2", false, permissions2 );

    assertEquals( "user1", ace1.getRecipient() );
    assertEquals( "user2", ace2.getRecipient() );
    assertEquals( "/tenant1", ace1.getTenantPath() );
    assertEquals( "/tenant2", ace2.getTenantPath() );
    assertTrue( ace1.isModifiable() );
    assertFalse( ace2.isModifiable() );
    assertEquals( 1, ace1.getPermissions().size() );
    assertEquals( 2, ace2.getPermissions().size() );
    assertEquals( GenericFilePermission.READ, ace1.getPermissions().get( 0 ) );
    assertEquals( GenericFilePermission.WRITE, ace2.getPermissions().get( 0 ) );
  }
  // endregion
}

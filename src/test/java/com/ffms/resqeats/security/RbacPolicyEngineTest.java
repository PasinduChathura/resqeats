package com.ffms.resqeats.security;

import com.ffms.resqeats.exception.security.AccessDeniedException;
import com.ffms.resqeats.exception.security.InsufficientRoleException;
import com.ffms.resqeats.security.context.ResqeatsSecurityContext;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import com.ffms.resqeats.security.rbac.RbacPolicyEngine;
import com.ffms.resqeats.user.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RBAC Policy Engine.
 * 
 * Tests:
 * - Role-based access checks
 * - Merchant scope access
 * - Outlet scope access
 * - User data access
 * - Cross-tenant access prevention
 */
@DisplayName("RBAC Policy Engine Tests")
class RbacPolicyEngineTest {

    private RbacPolicyEngine rbacEngine;

    @BeforeEach
    void setup() {
        rbacEngine = new RbacPolicyEngine();
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    private void setContext(UserRole role, UUID merchantId, UUID outletId) {
        ResqeatsSecurityContext context = ResqeatsSecurityContext.builder()
                .userId(UUID.randomUUID())
                .role(role)
                .merchantId(merchantId)
                .outletId(outletId)
                .anonymous(false)
                .correlationId("test")
                .build();
        SecurityContextHolder.setContext(context);
    }

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("Should pass when authenticated")
        void shouldPassWhenAuthenticated() {
            setContext(UserRole.USER, null, null);
            assertDoesNotThrow(() -> rbacEngine.requireAuthenticated());
        }

        @Test
        @DisplayName("Should fail when not authenticated")
        void shouldFailWhenNotAuthenticated() {
            SecurityContextHolder.setContext(ResqeatsSecurityContext.anonymous("test"));
            assertThrows(AccessDeniedException.class, () -> rbacEngine.requireAuthenticated());
        }
    }

    @Nested
    @DisplayName("Role Requirement Tests")
    class RoleRequirementTests {

        @Test
        @DisplayName("SUPER_ADMIN should pass all role checks")
        void superAdminShouldPassAllRoleChecks() {
            setContext(UserRole.SUPER_ADMIN, null, null);
            
            assertDoesNotThrow(() -> rbacEngine.requireRole(UserRole.SUPER_ADMIN));
            assertDoesNotThrow(() -> rbacEngine.requireRole(UserRole.ADMIN));
            assertDoesNotThrow(() -> rbacEngine.requireRole(UserRole.MERCHANT));
            assertDoesNotThrow(() -> rbacEngine.requireRole(UserRole.OUTLET_USER));
            assertDoesNotThrow(() -> rbacEngine.requireRole(UserRole.USER));
            assertDoesNotThrow(() -> rbacEngine.requireSuperAdmin());
            assertDoesNotThrow(() -> rbacEngine.requireAdmin());
        }

        @Test
        @DisplayName("ADMIN should fail SUPER_ADMIN check")
        void adminShouldFailSuperAdminCheck() {
            setContext(UserRole.ADMIN, null, null);
            
            assertThrows(InsufficientRoleException.class, () -> rbacEngine.requireSuperAdmin());
            assertDoesNotThrow(() -> rbacEngine.requireAdmin());
            assertDoesNotThrow(() -> rbacEngine.requireMerchant());
        }

        @Test
        @DisplayName("MERCHANT should fail ADMIN check")
        void merchantShouldFailAdminCheck() {
            setContext(UserRole.MERCHANT, UUID.randomUUID(), null);
            
            assertThrows(InsufficientRoleException.class, () -> rbacEngine.requireAdmin());
            assertDoesNotThrow(() -> rbacEngine.requireMerchant());
            assertDoesNotThrow(() -> rbacEngine.requireOutletUser());
        }

        @Test
        @DisplayName("USER should fail elevated role checks")
        void userShouldFailElevatedRoleChecks() {
            setContext(UserRole.USER, null, null);
            
            assertThrows(InsufficientRoleException.class, () -> rbacEngine.requireSuperAdmin());
            assertThrows(InsufficientRoleException.class, () -> rbacEngine.requireAdmin());
            assertThrows(InsufficientRoleException.class, () -> rbacEngine.requireMerchant());
            assertThrows(InsufficientRoleException.class, () -> rbacEngine.requireOutletUser());
        }
    }

    @Nested
    @DisplayName("Merchant Access Tests")
    class MerchantAccessTests {

        @Test
        @DisplayName("ADMIN should access any merchant")
        void adminShouldAccessAnyMerchant() {
            setContext(UserRole.ADMIN, null, null);
            UUID anyMerchantId = UUID.randomUUID();
            
            assertDoesNotThrow(() -> rbacEngine.requireMerchantAccess(anyMerchantId));
        }

        @Test
        @DisplayName("MERCHANT should access own merchant")
        void merchantShouldAccessOwnMerchant() {
            UUID merchantId = UUID.randomUUID();
            setContext(UserRole.MERCHANT, merchantId, null);
            
            assertDoesNotThrow(() -> rbacEngine.requireMerchantAccess(merchantId));
        }

        @Test
        @DisplayName("MERCHANT should NOT access other merchant")
        void merchantShouldNotAccessOtherMerchant() {
            UUID ownMerchantId = UUID.randomUUID();
            UUID otherMerchantId = UUID.randomUUID();
            setContext(UserRole.MERCHANT, ownMerchantId, null);
            
            assertThrows(AccessDeniedException.class, 
                    () -> rbacEngine.requireMerchantAccess(otherMerchantId));
        }

        @Test
        @DisplayName("USER should NOT access merchant APIs")
        void userShouldNotAccessMerchantApis() {
            setContext(UserRole.USER, null, null);
            UUID merchantId = UUID.randomUUID();
            
            assertThrows(AccessDeniedException.class, 
                    () -> rbacEngine.requireMerchantAccess(merchantId));
        }
    }

    @Nested
    @DisplayName("Outlet Access Tests")
    class OutletAccessTests {

        @Test
        @DisplayName("ADMIN should access any outlet")
        void adminShouldAccessAnyOutlet() {
            setContext(UserRole.ADMIN, null, null);
            UUID outletId = UUID.randomUUID();
            UUID outletMerchantId = UUID.randomUUID();
            
            assertDoesNotThrow(() -> rbacEngine.requireOutletAccess(outletId, outletMerchantId));
        }

        @Test
        @DisplayName("MERCHANT should access outlets they own")
        void merchantShouldAccessOwnedOutlets() {
            UUID merchantId = UUID.randomUUID();
            UUID outletId = UUID.randomUUID();
            setContext(UserRole.MERCHANT, merchantId, null);
            
            assertDoesNotThrow(() -> rbacEngine.requireOutletAccess(outletId, merchantId));
        }

        @Test
        @DisplayName("MERCHANT should NOT access outlets of other merchants")
        void merchantShouldNotAccessOtherMerchantsOutlets() {
            UUID ownMerchantId = UUID.randomUUID();
            UUID otherMerchantId = UUID.randomUUID();
            UUID outletId = UUID.randomUUID();
            setContext(UserRole.MERCHANT, ownMerchantId, null);
            
            assertThrows(AccessDeniedException.class, 
                    () -> rbacEngine.requireOutletAccess(outletId, otherMerchantId));
        }

        @Test
        @DisplayName("OUTLET_USER should access assigned outlet")
        void outletUserShouldAccessAssignedOutlet() {
            UUID merchantId = UUID.randomUUID();
            UUID outletId = UUID.randomUUID();
            setContext(UserRole.OUTLET_USER, merchantId, outletId);
            
            assertDoesNotThrow(() -> rbacEngine.requireOutletAccess(outletId, merchantId));
        }

        @Test
        @DisplayName("OUTLET_USER should NOT access other outlets")
        void outletUserShouldNotAccessOtherOutlets() {
            UUID merchantId = UUID.randomUUID();
            UUID assignedOutletId = UUID.randomUUID();
            UUID otherOutletId = UUID.randomUUID();
            setContext(UserRole.OUTLET_USER, merchantId, assignedOutletId);
            
            assertThrows(AccessDeniedException.class, 
                    () -> rbacEngine.requireOutletAccess(otherOutletId, merchantId));
        }
    }

    @Nested
    @DisplayName("User Data Access Tests")
    class UserDataAccessTests {

        @Test
        @DisplayName("ADMIN should access any user's data")
        void adminShouldAccessAnyUsersData() {
            setContext(UserRole.ADMIN, null, null);
            UUID targetUserId = UUID.randomUUID();
            
            assertDoesNotThrow(() -> rbacEngine.requireUserAccess(targetUserId));
        }

        @Test
        @DisplayName("User should access own data")
        void userShouldAccessOwnData() {
            UUID userId = UUID.randomUUID();
            ResqeatsSecurityContext context = ResqeatsSecurityContext.builder()
                    .userId(userId)
                    .role(UserRole.USER)
                    .anonymous(false)
                    .correlationId("test")
                    .build();
            SecurityContextHolder.setContext(context);
            
            assertDoesNotThrow(() -> rbacEngine.requireUserAccess(userId));
        }

        @Test
        @DisplayName("User should NOT access other user's data")
        void userShouldNotAccessOtherUsersData() {
            UUID userId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();
            ResqeatsSecurityContext context = ResqeatsSecurityContext.builder()
                    .userId(userId)
                    .role(UserRole.USER)
                    .anonymous(false)
                    .correlationId("test")
                    .build();
            SecurityContextHolder.setContext(context);
            
            assertThrows(AccessDeniedException.class, 
                    () -> rbacEngine.requireUserAccess(otherUserId));
        }
    }

    @Nested
    @DisplayName("Helper Method Tests")
    class HelperMethodTests {

        @Test
        @DisplayName("isResourceOwner should work correctly")
        void isResourceOwnerShouldWorkCorrectly() {
            UUID userId = UUID.randomUUID();
            ResqeatsSecurityContext context = ResqeatsSecurityContext.builder()
                    .userId(userId)
                    .role(UserRole.USER)
                    .anonymous(false)
                    .correlationId("test")
                    .build();
            SecurityContextHolder.setContext(context);
            
            assertTrue(rbacEngine.isResourceOwner(userId));
            assertFalse(rbacEngine.isResourceOwner(UUID.randomUUID()));
            assertFalse(rbacEngine.isResourceOwner(null));
        }

        @Test
        @DisplayName("canAccessMerchant should work correctly")
        void canAccessMerchantShouldWorkCorrectly() {
            UUID merchantId = UUID.randomUUID();
            setContext(UserRole.MERCHANT, merchantId, null);
            
            assertTrue(rbacEngine.canAccessMerchant(merchantId));
            assertFalse(rbacEngine.canAccessMerchant(UUID.randomUUID()));
            assertFalse(rbacEngine.canAccessMerchant(null));
        }

        @Test
        @DisplayName("canAccessOutlet should work correctly for OUTLET_USER")
        void canAccessOutletShouldWorkCorrectlyForOutletUser() {
            UUID merchantId = UUID.randomUUID();
            UUID outletId = UUID.randomUUID();
            setContext(UserRole.OUTLET_USER, merchantId, outletId);
            
            assertTrue(rbacEngine.canAccessOutlet(outletId));
            assertFalse(rbacEngine.canAccessOutlet(UUID.randomUUID()));
        }
    }
}

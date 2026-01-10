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

    private void setContext(UserRole role, Long merchantId, Long outletId) {
        ResqeatsSecurityContext context = ResqeatsSecurityContext.builder()
                .userId(100L)
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
            setContext(UserRole.CUSTOMER_USER, null, null);
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
            assertDoesNotThrow(() -> rbacEngine.requireRole(UserRole.MERCHANT_USER));
            assertDoesNotThrow(() -> rbacEngine.requireRole(UserRole.OUTLET_USER));
            assertDoesNotThrow(() -> rbacEngine.requireRole(UserRole.CUSTOMER_USER));
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
            setContext(UserRole.MERCHANT_USER, 200L, null);
            
            assertThrows(InsufficientRoleException.class, () -> rbacEngine.requireAdmin());
            assertDoesNotThrow(() -> rbacEngine.requireMerchant());
            assertDoesNotThrow(() -> rbacEngine.requireOutletUser());
        }

        @Test
        @DisplayName("USER should fail elevated role checks")
        void userShouldFailElevatedRoleChecks() {
            setContext(UserRole.CUSTOMER_USER, null, null);
            
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
            Long anyMerchantId = 300L;
            
            assertDoesNotThrow(() -> rbacEngine.requireMerchantAccess(anyMerchantId));
        }

        @Test
        @DisplayName("MERCHANT should access own merchant")
        void merchantShouldAccessOwnMerchant() {
            Long merchantId = 400L;
            setContext(UserRole.MERCHANT_USER, merchantId, null);
            
            assertDoesNotThrow(() -> rbacEngine.requireMerchantAccess(merchantId));
        }

        @Test
        @DisplayName("MERCHANT should NOT access other merchant")
        void merchantShouldNotAccessOtherMerchant() {
            Long ownMerchantId = 500L;
            Long otherMerchantId = 501L;
            setContext(UserRole.MERCHANT_USER, ownMerchantId, null);
            
            assertThrows(AccessDeniedException.class, 
                    () -> rbacEngine.requireMerchantAccess(otherMerchantId));
        }

        @Test
        @DisplayName("USER should NOT access merchant APIs")
        void userShouldNotAccessMerchantApis() {
            setContext(UserRole.CUSTOMER_USER, null, null);
            Long merchantId = 600L;
            
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
            Long outletId = 700L;
            Long outletMerchantId = 701L;
            
            assertDoesNotThrow(() -> rbacEngine.requireOutletAccess(outletId, outletMerchantId));
        }

        @Test
        @DisplayName("MERCHANT should access outlets they own")
        void merchantShouldAccessOwnedOutlets() {
            Long merchantId = 800L;
            Long outletId = 801L;
            setContext(UserRole.MERCHANT_USER, merchantId, null);
            
            assertDoesNotThrow(() -> rbacEngine.requireOutletAccess(outletId, merchantId));
        }

        @Test
        @DisplayName("MERCHANT should NOT access outlets of other merchants")
        void merchantShouldNotAccessOtherMerchantsOutlets() {
            Long ownMerchantId = 900L;
            Long otherMerchantId = 901L;
            Long outletId = 902L;
            setContext(UserRole.MERCHANT_USER, ownMerchantId, null);
            
            assertThrows(AccessDeniedException.class, 
                    () -> rbacEngine.requireOutletAccess(outletId, otherMerchantId));
        }

        @Test
        @DisplayName("OUTLET_USER should access assigned outlet")
        void outletUserShouldAccessAssignedOutlet() {
            Long merchantId = 1000L;
            Long outletId = 1001L;
            setContext(UserRole.OUTLET_USER, merchantId, outletId);
            
            assertDoesNotThrow(() -> rbacEngine.requireOutletAccess(outletId, merchantId));
        }

        @Test
        @DisplayName("OUTLET_USER should NOT access other outlets")
        void outletUserShouldNotAccessOtherOutlets() {
            Long merchantId = 1100L;
            Long assignedOutletId = 1101L;
            Long otherOutletId = 1102L;
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
            Long targetUserId = 1200L;
            
            assertDoesNotThrow(() -> rbacEngine.requireUserAccess(targetUserId));
        }

        @Test
        @DisplayName("User should access own data")
        void userShouldAccessOwnData() {
            Long userId = 1300L;
            ResqeatsSecurityContext context = ResqeatsSecurityContext.builder()
                    .userId(userId)
                    .role(UserRole.CUSTOMER_USER)
                    .anonymous(false)
                    .correlationId("test")
                    .build();
            SecurityContextHolder.setContext(context);
            
            assertDoesNotThrow(() -> rbacEngine.requireUserAccess(userId));
        }

        @Test
        @DisplayName("User should NOT access other user's data")
        void userShouldNotAccessOtherUsersData() {
            Long userId = 1400L;
            Long otherUserId = 1401L;
            ResqeatsSecurityContext context = ResqeatsSecurityContext.builder()
                    .userId(userId)
                .role(UserRole.CUSTOMER_USER)
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
            Long userId = 1500L;
            ResqeatsSecurityContext context = ResqeatsSecurityContext.builder()
                    .userId(userId)
                    .role(UserRole.CUSTOMER_USER)
                    .anonymous(false)
                    .correlationId("test")
                    .build();
            SecurityContextHolder.setContext(context);
            
            assertTrue(rbacEngine.isResourceOwner(userId));
            assertFalse(rbacEngine.isResourceOwner(1501L));
            assertFalse(rbacEngine.isResourceOwner(null));
        }

        @Test
        @DisplayName("canAccessMerchant should work correctly")
        void canAccessMerchantShouldWorkCorrectly() {
            Long merchantId = 1600L;
            setContext(UserRole.MERCHANT_USER, merchantId, null);
            
            assertTrue(rbacEngine.canAccessMerchant(merchantId));
            assertFalse(rbacEngine.canAccessMerchant(1601L));
            assertFalse(rbacEngine.canAccessMerchant(null));
        }

        @Test
        @DisplayName("canAccessOutlet should work correctly for OUTLET_USER")
        void canAccessOutletShouldWorkCorrectlyForOutletUser() {
            Long merchantId = 1700L;
            Long outletId = 1701L;
            setContext(UserRole.OUTLET_USER, merchantId, outletId);
            
            assertTrue(rbacEngine.canAccessOutlet(outletId));
            assertFalse(rbacEngine.canAccessOutlet(1702L));
        }
    }
}

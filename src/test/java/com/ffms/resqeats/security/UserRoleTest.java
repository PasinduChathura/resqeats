package com.ffms.resqeats.security;

import com.ffms.resqeats.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserRole enum.
 * 
 * Tests:
 * - Hierarchy levels
 * - Authority checks
 * - Audit requirements
 * - Tenant scope requirements
 */
@DisplayName("UserRole Enum Tests")
class UserRoleTest {

    @Nested
    @DisplayName("Hierarchy Level Tests")
    class HierarchyLevelTests {

        @Test
        @DisplayName("SUPER_ADMIN should have highest level")
        void superAdminShouldHaveHighestLevel() {
            assertEquals(5, UserRole.SUPER_ADMIN.getHierarchyLevel());
            assertTrue(UserRole.SUPER_ADMIN.getHierarchyLevel() > UserRole.ADMIN.getHierarchyLevel());
        }

        @Test
        @DisplayName("ADMIN should be between SUPER_ADMIN and MERCHANT")
        void adminShouldBeBetweenSuperAdminAndMerchant() {
            assertEquals(4, UserRole.ADMIN.getHierarchyLevel());
            assertTrue(UserRole.ADMIN.getHierarchyLevel() < UserRole.SUPER_ADMIN.getHierarchyLevel());
            assertTrue(UserRole.ADMIN.getHierarchyLevel() > UserRole.MERCHANT_USER.getHierarchyLevel());
        }

        @Test
        @DisplayName("MERCHANT should be between ADMIN and OUTLET_USER")
        void merchantShouldBeBetweenAdminAndOutletUser() {
            assertEquals(3, UserRole.MERCHANT_USER.getHierarchyLevel());
            assertTrue(UserRole.MERCHANT_USER.getHierarchyLevel() < UserRole.ADMIN.getHierarchyLevel());
            assertTrue(UserRole.MERCHANT_USER.getHierarchyLevel() > UserRole.OUTLET_USER.getHierarchyLevel());
        }

        @Test
        @DisplayName("OUTLET_USER should be between MERCHANT and USER")
        void outletUserShouldBeBetweenMerchantAndUser() {
            assertEquals(2, UserRole.OUTLET_USER.getHierarchyLevel());
            assertTrue(UserRole.OUTLET_USER.getHierarchyLevel() < UserRole.MERCHANT_USER.getHierarchyLevel());
            assertTrue(UserRole.OUTLET_USER.getHierarchyLevel() > UserRole.CUSTOMER_USER.getHierarchyLevel());
        }

        @Test
        @DisplayName("USER should have lowest level")
        void userShouldHaveLowestLevel() {
            assertEquals(1, UserRole.CUSTOMER_USER.getHierarchyLevel());
            for (UserRole role : UserRole.values()) {
                if (role != UserRole.CUSTOMER_USER) {
                    assertTrue(UserRole.CUSTOMER_USER.getHierarchyLevel() < role.getHierarchyLevel(),
                            "USER should have lower level than " + role);
                }
            }
        }
    }

    @Nested
    @DisplayName("Authority Check Tests")
    class AuthorityCheckTests {

        @Test
        @DisplayName("hasAuthorityOver should work correctly")
        void hasAuthorityOverShouldWorkCorrectly() {
            // SUPER_ADMIN has authority over all
            assertTrue(UserRole.SUPER_ADMIN.hasAuthorityOver(UserRole.SUPER_ADMIN));
            assertTrue(UserRole.SUPER_ADMIN.hasAuthorityOver(UserRole.ADMIN));
            assertTrue(UserRole.SUPER_ADMIN.hasAuthorityOver(UserRole.MERCHANT_USER));
            assertTrue(UserRole.SUPER_ADMIN.hasAuthorityOver(UserRole.OUTLET_USER));
            assertTrue(UserRole.SUPER_ADMIN.hasAuthorityOver(UserRole.CUSTOMER_USER));

            // ADMIN has authority over MERCHANT and below
            assertFalse(UserRole.ADMIN.hasAuthorityOver(UserRole.SUPER_ADMIN));
            assertTrue(UserRole.ADMIN.hasAuthorityOver(UserRole.ADMIN));
            assertTrue(UserRole.ADMIN.hasAuthorityOver(UserRole.MERCHANT_USER));
            assertTrue(UserRole.ADMIN.hasAuthorityOver(UserRole.OUTLET_USER));
            assertTrue(UserRole.ADMIN.hasAuthorityOver(UserRole.CUSTOMER_USER));

            // MERCHANT has authority over OUTLET_USER and below
            assertFalse(UserRole.MERCHANT_USER.hasAuthorityOver(UserRole.ADMIN));
            assertTrue(UserRole.MERCHANT_USER.hasAuthorityOver(UserRole.MERCHANT_USER));
            assertTrue(UserRole.MERCHANT_USER.hasAuthorityOver(UserRole.OUTLET_USER));
            assertTrue(UserRole.MERCHANT_USER.hasAuthorityOver(UserRole.CUSTOMER_USER));

            // USER only has authority over itself
            assertFalse(UserRole.CUSTOMER_USER.hasAuthorityOver(UserRole.OUTLET_USER));
            assertTrue(UserRole.CUSTOMER_USER.hasAuthorityOver(UserRole.CUSTOMER_USER));
        }

        @Test
        @DisplayName("isAtLeast should work correctly")
        void isAtLeastShouldWorkCorrectly() {
            // ADMIN is at least ADMIN
            assertTrue(UserRole.ADMIN.isAtLeast(UserRole.ADMIN));
            assertTrue(UserRole.ADMIN.isAtLeast(UserRole.MERCHANT_USER));
            assertTrue(UserRole.ADMIN.isAtLeast(UserRole.CUSTOMER_USER));
            assertFalse(UserRole.ADMIN.isAtLeast(UserRole.SUPER_ADMIN));

            // MERCHANT is at least MERCHANT
            assertTrue(UserRole.MERCHANT_USER.isAtLeast(UserRole.MERCHANT_USER));
            assertTrue(UserRole.MERCHANT_USER.isAtLeast(UserRole.CUSTOMER_USER));
            assertFalse(UserRole.MERCHANT_USER.isAtLeast(UserRole.ADMIN));
        }
    }

    @Nested
    @DisplayName("Audit Requirement Tests")
    class AuditRequirementTests {

        @Test
        @DisplayName("Only SUPER_ADMIN and ADMIN should require audit")
        void onlySuperAdminAndAdminShouldRequireAudit() {
            assertTrue(UserRole.SUPER_ADMIN.requiresAudit());
            assertTrue(UserRole.ADMIN.requiresAudit());
            assertFalse(UserRole.MERCHANT_USER.requiresAudit());
            assertFalse(UserRole.OUTLET_USER.requiresAudit());
            assertFalse(UserRole.CUSTOMER_USER.requiresAudit());
        }
    }

    @Nested
    @DisplayName("Tenant Scope Tests")
    class TenantScopeTests {

        @Test
        @DisplayName("MERCHANT and OUTLET_USER should have tenant scope")
        void merchantAndOutletUserShouldHaveTenantScope() {
            assertFalse(UserRole.SUPER_ADMIN.hasTenantScope());
            assertFalse(UserRole.ADMIN.hasTenantScope());
            assertTrue(UserRole.MERCHANT_USER.hasTenantScope());
            assertTrue(UserRole.OUTLET_USER.hasTenantScope());
            assertFalse(UserRole.CUSTOMER_USER.hasTenantScope());
        }

        @Test
        @DisplayName("Only SUPER_ADMIN and ADMIN should have global access")
        void onlySuperAdminAndAdminShouldHaveGlobalAccess() {
            assertTrue(UserRole.SUPER_ADMIN.hasGlobalAccess());
            assertTrue(UserRole.ADMIN.hasGlobalAccess());
            assertFalse(UserRole.MERCHANT_USER.hasGlobalAccess());
            assertFalse(UserRole.OUTLET_USER.hasGlobalAccess());
            assertFalse(UserRole.CUSTOMER_USER.hasGlobalAccess());
        }
    }
}

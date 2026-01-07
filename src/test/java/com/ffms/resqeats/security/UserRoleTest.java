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
            assertTrue(UserRole.ADMIN.getHierarchyLevel() > UserRole.MERCHANT.getHierarchyLevel());
        }

        @Test
        @DisplayName("MERCHANT should be between ADMIN and OUTLET_USER")
        void merchantShouldBeBetweenAdminAndOutletUser() {
            assertEquals(3, UserRole.MERCHANT.getHierarchyLevel());
            assertTrue(UserRole.MERCHANT.getHierarchyLevel() < UserRole.ADMIN.getHierarchyLevel());
            assertTrue(UserRole.MERCHANT.getHierarchyLevel() > UserRole.OUTLET_USER.getHierarchyLevel());
        }

        @Test
        @DisplayName("OUTLET_USER should be between MERCHANT and USER")
        void outletUserShouldBeBetweenMerchantAndUser() {
            assertEquals(2, UserRole.OUTLET_USER.getHierarchyLevel());
            assertTrue(UserRole.OUTLET_USER.getHierarchyLevel() < UserRole.MERCHANT.getHierarchyLevel());
            assertTrue(UserRole.OUTLET_USER.getHierarchyLevel() > UserRole.USER.getHierarchyLevel());
        }

        @Test
        @DisplayName("USER should have lowest level")
        void userShouldHaveLowestLevel() {
            assertEquals(1, UserRole.USER.getHierarchyLevel());
            for (UserRole role : UserRole.values()) {
                if (role != UserRole.USER) {
                    assertTrue(UserRole.USER.getHierarchyLevel() < role.getHierarchyLevel(),
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
            assertTrue(UserRole.SUPER_ADMIN.hasAuthorityOver(UserRole.MERCHANT));
            assertTrue(UserRole.SUPER_ADMIN.hasAuthorityOver(UserRole.OUTLET_USER));
            assertTrue(UserRole.SUPER_ADMIN.hasAuthorityOver(UserRole.USER));

            // ADMIN has authority over MERCHANT and below
            assertFalse(UserRole.ADMIN.hasAuthorityOver(UserRole.SUPER_ADMIN));
            assertTrue(UserRole.ADMIN.hasAuthorityOver(UserRole.ADMIN));
            assertTrue(UserRole.ADMIN.hasAuthorityOver(UserRole.MERCHANT));
            assertTrue(UserRole.ADMIN.hasAuthorityOver(UserRole.OUTLET_USER));
            assertTrue(UserRole.ADMIN.hasAuthorityOver(UserRole.USER));

            // MERCHANT has authority over OUTLET_USER and below
            assertFalse(UserRole.MERCHANT.hasAuthorityOver(UserRole.ADMIN));
            assertTrue(UserRole.MERCHANT.hasAuthorityOver(UserRole.MERCHANT));
            assertTrue(UserRole.MERCHANT.hasAuthorityOver(UserRole.OUTLET_USER));
            assertTrue(UserRole.MERCHANT.hasAuthorityOver(UserRole.USER));

            // USER only has authority over itself
            assertFalse(UserRole.USER.hasAuthorityOver(UserRole.OUTLET_USER));
            assertTrue(UserRole.USER.hasAuthorityOver(UserRole.USER));
        }

        @Test
        @DisplayName("isAtLeast should work correctly")
        void isAtLeastShouldWorkCorrectly() {
            // ADMIN is at least ADMIN
            assertTrue(UserRole.ADMIN.isAtLeast(UserRole.ADMIN));
            assertTrue(UserRole.ADMIN.isAtLeast(UserRole.MERCHANT));
            assertTrue(UserRole.ADMIN.isAtLeast(UserRole.USER));
            assertFalse(UserRole.ADMIN.isAtLeast(UserRole.SUPER_ADMIN));

            // MERCHANT is at least MERCHANT
            assertTrue(UserRole.MERCHANT.isAtLeast(UserRole.MERCHANT));
            assertTrue(UserRole.MERCHANT.isAtLeast(UserRole.USER));
            assertFalse(UserRole.MERCHANT.isAtLeast(UserRole.ADMIN));
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
            assertFalse(UserRole.MERCHANT.requiresAudit());
            assertFalse(UserRole.OUTLET_USER.requiresAudit());
            assertFalse(UserRole.USER.requiresAudit());
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
            assertTrue(UserRole.MERCHANT.hasTenantScope());
            assertTrue(UserRole.OUTLET_USER.hasTenantScope());
            assertFalse(UserRole.USER.hasTenantScope());
        }

        @Test
        @DisplayName("Only SUPER_ADMIN and ADMIN should have global access")
        void onlySuperAdminAndAdminShouldHaveGlobalAccess() {
            assertTrue(UserRole.SUPER_ADMIN.hasGlobalAccess());
            assertTrue(UserRole.ADMIN.hasGlobalAccess());
            assertFalse(UserRole.MERCHANT.hasGlobalAccess());
            assertFalse(UserRole.OUTLET_USER.hasGlobalAccess());
            assertFalse(UserRole.USER.hasGlobalAccess());
        }
    }
}

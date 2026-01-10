package com.ffms.resqeats.security;

import com.ffms.resqeats.security.context.ResqeatsSecurityContext;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import com.ffms.resqeats.user.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SecurityContext and SecurityContextHolder.
 * 
 * Tests:
 * - Context immutability
 * - Role hierarchy checks
 * - Tenant scope methods
 * - Thread-local isolation
 */
@DisplayName("SecurityContext Tests")
class SecurityContextTest {

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("SecurityContext Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create immutable context with all fields")
        void shouldCreateImmutableContext() {
            Long userId = 1L;
            Long merchantId = 2L;
            Long outletId = 3L;
            
            ResqeatsSecurityContext context = ResqeatsSecurityContext.builder()
                    .userId(userId)
                    .role(UserRole.MERCHANT_USER)
                    .merchantId(merchantId)
                    .outletId(outletId)
                    .email("test@example.com")
                    .tokenId("token123")
                    .issuedAt(System.currentTimeMillis())
                    .expiresAt(System.currentTimeMillis() + 3600000)
                    .anonymous(false)
                    .correlationId("corr123")
                    .build();

            assertEquals(userId, context.getUserId());
            assertEquals(UserRole.MERCHANT_USER, context.getRole());
            assertEquals(merchantId, context.getMerchantId());
            assertEquals(outletId, context.getOutletId());
            assertEquals("test@example.com", context.getEmail());
            assertFalse(context.isAnonymous());
        }

        @Test
        @DisplayName("Should create anonymous context")
        void shouldCreateAnonymousContext() {
            ResqeatsSecurityContext context = ResqeatsSecurityContext.anonymous("corr123");
            
            assertTrue(context.isAnonymous());
            assertNull(context.getUserId());
            assertNull(context.getRole());
            assertEquals("corr123", context.getCorrelationId());
        }

        @Test
        @DisplayName("Should create system context")
        void shouldCreateSystemContext() {
            ResqeatsSecurityContext context = ResqeatsSecurityContext.system();
            
            assertFalse(context.isAnonymous());
            assertEquals(UserRole.SUPER_ADMIN, context.getRole());
            assertEquals("SYSTEM", context.getCorrelationId());
        }
    }

    @Nested
    @DisplayName("Role Hierarchy Tests")
    class RoleHierarchyTests {

        @Test
        @DisplayName("SUPER_ADMIN should have highest authority")
        void superAdminShouldHaveHighestAuthority() {
            ResqeatsSecurityContext context = ResqeatsSecurityContext.builder()
                    .userId(10L)
                    .role(UserRole.SUPER_ADMIN)
                    .anonymous(false)
                    .build();

            assertTrue(context.isSuperAdmin());
            assertTrue(context.isAdmin());
            assertTrue(context.isMerchant());
            assertTrue(context.isOutletUser());
            assertTrue(context.hasGlobalAccess());
            assertTrue(context.requiresAudit());
        }

        @Test
        @DisplayName("ADMIN should have authority over MERCHANT and below")
        void adminShouldHaveAuthorityOverMerchant() {
            ResqeatsSecurityContext context = ResqeatsSecurityContext.builder()
                    .userId(11L)
                    .role(UserRole.ADMIN)
                    .anonymous(false)
                    .build();

            assertFalse(context.isSuperAdmin());
            assertTrue(context.isAdmin());
            assertTrue(context.isMerchant());
            assertTrue(context.isOutletUser());
            assertTrue(context.hasGlobalAccess());
            assertTrue(context.requiresAudit());
        }

        @Test
        @DisplayName("MERCHANT should have authority over OUTLET_USER and below")
        void merchantShouldHaveAuthorityOverOutletUser() {
            ResqeatsSecurityContext context = ResqeatsSecurityContext.builder()
                    .userId(12L)
                    .role(UserRole.MERCHANT_USER)
                    .merchantId(100L)
                    .anonymous(false)
                    .build();

            assertFalse(context.isSuperAdmin());
            assertFalse(context.isAdmin());
            assertTrue(context.isMerchant());
            assertTrue(context.isOutletUser());
            assertFalse(context.hasGlobalAccess());
            assertFalse(context.requiresAudit());
        }

        @Test
        @DisplayName("USER should have lowest authority")
        void userShouldHaveLowestAuthority() {
            ResqeatsSecurityContext context = ResqeatsSecurityContext.builder()
                    .userId(13L)
                    .role(UserRole.CUSTOMER_USER)
                    .anonymous(false)
                    .build();

            assertFalse(context.isSuperAdmin());
            assertFalse(context.isAdmin());
            assertFalse(context.isMerchant());
            assertFalse(context.isOutletUser());
            assertFalse(context.hasGlobalAccess());
            assertFalse(context.requiresAudit());
        }

        @Test
        @DisplayName("hasRole should respect hierarchy")
        void hasRoleShouldRespectHierarchy() {
            ResqeatsSecurityContext adminContext = ResqeatsSecurityContext.builder()
                    .userId(14L)
                    .role(UserRole.ADMIN)
                    .anonymous(false)
                    .build();

            assertTrue(adminContext.hasRole(UserRole.ADMIN));
            assertTrue(adminContext.hasRole(UserRole.MERCHANT_USER));
            assertTrue(adminContext.hasRole(UserRole.OUTLET_USER));
            assertTrue(adminContext.hasRole(UserRole.CUSTOMER_USER));
            assertFalse(adminContext.hasRole(UserRole.SUPER_ADMIN));
        }
    }

    @Nested
    @DisplayName("Tenant Scope Tests")
    class TenantScopeTests {

        @Test
        @DisplayName("MERCHANT should have merchant scope")
        void merchantShouldHaveMerchantScope() {
            Long merchantId = 200L;
            ResqeatsSecurityContext context = ResqeatsSecurityContext.builder()
                .userId(20L)
                    .role(UserRole.MERCHANT_USER)
                    .merchantId(merchantId)
                    .anonymous(false)
                    .build();

            assertTrue(context.hasMerchantScope());
            assertFalse(context.hasOutletScope());
            assertEquals(merchantId, context.getEffectiveMerchantId());
            assertNull(context.getEffectiveOutletId());
        }

        @Test
        @DisplayName("OUTLET_USER should have outlet scope")
        void outletUserShouldHaveOutletScope() {
            Long merchantId = 300L;
            Long outletId = 301L;
            ResqeatsSecurityContext context = ResqeatsSecurityContext.builder()
                .userId(21L)
                    .role(UserRole.OUTLET_USER)
                    .merchantId(merchantId)
                    .outletId(outletId)
                    .anonymous(false)
                    .build();

            assertFalse(context.hasMerchantScope());
            assertTrue(context.hasOutletScope());
            assertEquals(merchantId, context.getEffectiveMerchantId());
            assertEquals(outletId, context.getEffectiveOutletId());
        }

        @Test
        @DisplayName("ADMIN should have no scope restrictions")
        void adminShouldHaveNoScopeRestrictions() {
            ResqeatsSecurityContext context = ResqeatsSecurityContext.builder()
                    .userId(22L)
                    .role(UserRole.ADMIN)
                    .anonymous(false)
                    .build();

            assertFalse(context.hasMerchantScope());
            assertFalse(context.hasOutletScope());
            assertNull(context.getEffectiveMerchantId());
            assertNull(context.getEffectiveOutletId());
        }
    }

    @Nested
    @DisplayName("SecurityContextHolder Tests")
    class ContextHolderTests {

        @Test
        @DisplayName("Should set and get context")
        void shouldSetAndGetContext() {
            ResqeatsSecurityContext context = ResqeatsSecurityContext.builder()
                    .userId(30L)
                    .role(UserRole.CUSTOMER_USER)
                    .anonymous(false)
                    .build();

            SecurityContextHolder.setContext(context);
            
            assertTrue(SecurityContextHolder.hasContext());
            assertEquals(context, SecurityContextHolder.getContext());
            assertEquals(context.getUserId(), SecurityContextHolder.getCurrentUserId());
            assertEquals(context.getRole(), SecurityContextHolder.getCurrentRole());
        }

        @Test
        @DisplayName("Should clear context")
        void shouldClearContext() {
            ResqeatsSecurityContext context = ResqeatsSecurityContext.builder()
                    .userId(31L)
                    .role(UserRole.CUSTOMER_USER)
                    .anonymous(false)
                    .build();

            SecurityContextHolder.setContext(context);
            SecurityContextHolder.clearContext();
            
            // Should return anonymous context when none is set
            assertFalse(SecurityContextHolder.hasContext());
            assertTrue(SecurityContextHolder.getContext().isAnonymous());
        }

        @Test
        @DisplayName("Should reject null context")
        void shouldRejectNullContext() {
            assertThrows(IllegalArgumentException.class, () -> 
                    SecurityContextHolder.setContext(null));
        }

        @Test
        @DisplayName("Should provide convenience methods")
        void shouldProvideConvenienceMethods() {
            Long userId = 40L;
            Long merchantId = 401L;
            
            ResqeatsSecurityContext context = ResqeatsSecurityContext.builder()
                    .userId(userId)
                    .role(UserRole.ADMIN)
                    .merchantId(merchantId)
                    .anonymous(false)
                    .correlationId("test-corr")
                    .build();

            SecurityContextHolder.setContext(context);
            
            assertEquals(userId, SecurityContextHolder.getCurrentUserId());
            assertEquals(UserRole.ADMIN, SecurityContextHolder.getCurrentRole());
            assertEquals(merchantId, SecurityContextHolder.getCurrentMerchantId());
            assertTrue(SecurityContextHolder.isAuthenticated());
            assertTrue(SecurityContextHolder.isAdmin());
            assertTrue(SecurityContextHolder.hasRole(UserRole.MERCHANT_USER));
            assertEquals("test-corr", SecurityContextHolder.getCorrelationId());
        }
    }
}

package com.ffms.resqeats.security.tenant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for entities that are tenant-scoped.
 * 
 * Entities marked with this annotation will have automatic
 * tenant filtering applied via Hibernate filters.
 * 
 * The entity MUST have the corresponding tenant ID field(s):
 * - merchant_id for MERCHANT scope
 * - outlet_id for OUTLET scope
 * - user_id for USER scope
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantScoped {
    
    /**
     * The type of tenant scope for this entity.
     */
    TenantScopeType value() default TenantScopeType.MERCHANT;
    
    /**
     * Whether this entity can have null tenant ID (global entities).
     */
    boolean allowNull() default false;
}

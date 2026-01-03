-- Fix for PrivilegeType enum error
-- This script updates any NULL or empty type values in the privilege table to 'USER'

-- Update NULL values
UPDATE privilege SET type = 'USER' WHERE type IS NULL;

-- Update empty string values
UPDATE privilege SET type = 'USER' WHERE type = '';

-- Verify all records have valid type values
SELECT id, name, type FROM privilege WHERE type NOT IN ('SUPER_ADMIN', 'USER', 'WORKFLOW', 'ROLE', 'SHOP', 'FOOD', 'ORDER', 'CART', 'PAYMENT', 'NOTIFICATION', 'CUSTOMER');

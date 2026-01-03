package com.ffms.trackable.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component("appUtils")
public class AppUtils {

    public static String getAppUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

    public String getUserResource() {
        return AppConstants.RESOURCE_USER;
    }

    public String getRoleResource() {
        return AppConstants.RESOURCE_ROLE;
    }

    public String getPrivilegeResource() {
        return AppConstants.RESOURCE_PRIVILEGE;
    }

    public String getCustomerResource() {
        return AppConstants.RESOURCE_CUSTOMER;
    }

    public String getWorkflowResource() {
        return AppConstants.RESOURCE_WORKFLOW;
    }

    public String getReadPrivilege() {
        return AppConstants.PRIVILEGE_READ;
    }

    public String getWritePrivilege() {
        return AppConstants.PRIVILEGE_WRITE;
    }

    public String getUpdatePrivilege() {
        return AppConstants.PRIVILEGE_UPDATE;
    }

    public String getDeletePrivilege() {
        return AppConstants.PRIVILEGE_DELETE;
    }

    public String getAdminPrivilege() {
        return AppConstants.PRIVILEGE_ADMIN;
    }
}

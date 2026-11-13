package nl.metafactory.common.security;

import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

import java.util.List;

public final class OpenJCockpitRoles {

    public static final String ADMIN = "openjcockpit-admin";

    public static final String OPERATOR = "openjcockpit-operator";

    public static final String APPROVER = "openjcockpit-approver";

    public static final String VIEWER = "openjcockpit-viewer";

    public static final List<String> ALL = List.of(ADMIN, OPERATOR, APPROVER, VIEWER);

    private static final String ROLE_PREFIX = "ROLE_";

    private OpenJCockpitRoles() {
    }

    public static String authority(String role) {
        return ROLE_PREFIX + role;
    }

    public static RoleHierarchy hierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role(ADMIN).implies(OPERATOR, APPROVER)
                .role(OPERATOR).implies(VIEWER)
                .role(APPROVER).implies(VIEWER)
                .build();
    }
}

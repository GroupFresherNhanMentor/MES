package fpt.qn.mes.auth.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EndpointRoleCatalog {

    public static final String PUBLIC = "PUBLIC";
    public static final String ROLE_PROTECTED = "ROLE_PROTECTED";

    private static final List<String> PUBLIC_OPERATIONS = List.of(
            "POST /api/auth/login",
            "POST /api/auth/refresh");

    private static final List<String> PROTECTED_OPERATIONS = List.of(
            "GET /api/users",
            "GET /api/users/{id}",
            "POST /api/users",
            "PUT /api/users/{id}",
            "PATCH /api/users/{id}/activate",
            "PATCH /api/users/{id}/deactivate",
            "GET /api/users/{userId}/roles",
            "PUT /api/users/{userId}/roles",
            "GET /api/roles",
            "GET /api/roles/{id}",
            "POST /api/roles",
            "PUT /api/roles/{id}",
            "DELETE /api/roles/{id}");

    private EndpointRoleCatalog() {
    }

    public static Map<String, String> operations() {
        Map<String, String> result = new LinkedHashMap<>();
        for (String operation : PUBLIC_OPERATIONS) {
            add(result, operation, PUBLIC);
        }
        for (String operation : PROTECTED_OPERATIONS) {
            add(result, operation, ROLE_PROTECTED);
        }
        return Collections.unmodifiableMap(result);
    }

    public static List<EndpointSpec> specifications() {
        List<EndpointSpec> result = new ArrayList<>();
        for (Map.Entry<String, String> operation : operations().entrySet()) {
            String[] parts = operation.getKey().split(" ", 2);
            boolean mutating = !"GET".equals(parts[0]);
            result.add(new EndpointSpec(
                    parts[0],
                    parts[1],
                    operation.getValue(),
                    Set.of(200, 201, 204, 400, 404, 409, 500),
                    mutating));
        }
        return Collections.unmodifiableList(result);
    }

    public static final class EndpointSpec {

        private final String method;
        private final String path;
        private final String access;
        private final Set<Integer> allowedStatuses;
        private final boolean mutationProbe;

        EndpointSpec(
                String method,
                String path,
                String access,
                Set<Integer> allowedStatuses,
                boolean mutationProbe) {
            this.method = method;
            this.path = path;
            this.access = access;
            this.allowedStatuses = allowedStatuses;
            this.mutationProbe = mutationProbe;
        }

        public String getMethod() {
            return method;
        }

        public String getPath() {
            return path;
        }

        public String getAccess() {
            return access;
        }

        public Set<Integer> getAllowedStatuses() {
            return allowedStatuses;
        }

        public boolean hasMutationProbe() {
            return mutationProbe;
        }

        public String key() {
            return method + " " + path;
        }
    }

    private static void add(Map<String, String> catalog, String operation, String access) {
        String previous = catalog.put(operation, access);
        if (previous != null) {
            throw new IllegalStateException("Duplicate endpoint catalog entry: " + operation);
        }
    }
}

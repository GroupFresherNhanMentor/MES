package fpt.qn.mes.auth.support;

import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EndpointPermissionCatalog {

    public static final String PUBLIC = "PUBLIC";

    private EndpointPermissionCatalog() {
    }

    public static Map<String, String> operations() {
        Map<String, String> result = new LinkedHashMap<>();

        add(result, "POST", "/api/auth/login", PUBLIC);
        add(result, "POST", "/api/auth/refresh", PUBLIC);

        add(result, "GET", "/api/users", "USER_READ");
        add(result, "GET", "/api/users/{id}", "USER_READ");
        add(result, "POST", "/api/users", "USER_CREATE");
        add(result, "PUT", "/api/users/{id}", "USER_UPDATE");
        add(result, "PATCH", "/api/users/{id}/activate", "USER_ACTIVATE");
        add(result, "PATCH", "/api/users/{id}/deactivate", "USER_DEACTIVATE");
        add(result, "GET", "/api/users/{userId}/roles", "USER_ROLE_READ");
        add(result, "PUT", "/api/users/{userId}/roles", "USER_ROLE_ASSIGN");

        add(result, "GET", "/api/roles", "ROLE_READ");
        add(result, "GET", "/api/roles/{id}", "ROLE_READ");
        add(result, "POST", "/api/roles", "ROLE_CREATE");
        add(result, "PUT", "/api/roles/{id}", "ROLE_UPDATE");
        add(result, "DELETE", "/api/roles/{id}", "ROLE_DELETE");
        add(result, "GET", "/api/products", "PRODUCT_READ");
        add(result, "GET", "/api/products/{id}", "PRODUCT_READ");
        add(result, "POST", "/api/products", "PRODUCT_CREATE");
        add(result, "PUT", "/api/products/{id}", "PRODUCT_UPDATE");
        add(result, "PUT", "/api/products/{id}/deactivate", "PRODUCT_DEACTIVATE");
        add(result, "GET", "/api/products/types", "LOOKUP_READ");
        add(result, "GET", "/api/products/statuses", "LOOKUP_READ");

        add(result, "GET", "/api/warehouses", "WAREHOUSE_READ");
        add(result, "GET", "/api/warehouses/{id}", "WAREHOUSE_READ");
        add(result, "POST", "/api/warehouses", "WAREHOUSE_CREATE");
        add(result, "PUT", "/api/warehouses/{id}", "WAREHOUSE_UPDATE");
        add(result, "PUT", "/api/warehouses/{id}/deactivate", "WAREHOUSE_DEACTIVATE");

        add(result, "GET", "/api/warehouses/{warehouseId}/locations", "LOCATION_READ");
        add(result, "GET", "/api/warehouses/{warehouseId}/locations/{id}", "LOCATION_READ");
        add(result, "POST", "/api/warehouses/{warehouseId}/locations", "LOCATION_CREATE");
        add(result, "PUT", "/api/warehouses/{warehouseId}/locations/{id}", "LOCATION_UPDATE");
        add(result, "PUT", "/api/warehouses/{warehouseId}/locations/{id}/deactivate", "LOCATION_DEACTIVATE");

        add(result, "GET", "/api/machines", "MACHINE_READ");
        add(result, "GET", "/api/machines/{id}", "MACHINE_READ");
        add(result, "POST", "/api/machines", "MACHINE_CREATE");
        add(result, "PUT", "/api/machines/{id}", "MACHINE_UPDATE");
        add(result, "PUT", "/api/machines/{id}/deactivate", "MACHINE_DEACTIVATE");
        add(result, "PATCH", "/api/machines/{id}/status", "MACHINE_STATUS_CHANGE");

        add(result, "GET", "/api/production-lines", "PRODUCTION_LINE_READ");
        add(result, "GET", "/api/production-lines/{id}", "PRODUCTION_LINE_READ");
        add(result, "POST", "/api/production-lines", "PRODUCTION_LINE_CREATE");
        add(result, "PUT", "/api/production-lines/{id}", "PRODUCTION_LINE_UPDATE");
        add(result, "PUT", "/api/production-lines/{id}/deactivate", "PRODUCTION_LINE_DEACTIVATE");

        add(result, "GET", "/api/boms", "BOM_READ");
        add(result, "GET", "/api/boms/{id}", "BOM_READ");
        add(result, "POST", "/api/boms", "BOM_CREATE");
        add(result, "POST", "/api/boms/{id}/activate", "BOM_ACTIVATE");
        add(result, "POST", "/api/boms/{id}/new-version", "BOM_CREATE_VERSION");
        add(result, "POST", "/api/boms/{bomId}/items", "BOM_ITEM_ADD");
        add(result, "DELETE", "/api/boms/{bomId}/items/{itemId}", "BOM_ITEM_DELETE");
        add(result, "GET", "/api/boms/statuses", "LOOKUP_READ");

        add(result, "GET", "/api/stock-lots", "STOCK_LOT_READ");
        add(result, "GET", "/api/stock-lots/{id}", "STOCK_LOT_READ");
        add(result, "POST", "/api/stock-lots", "STOCK_LOT_CREATE");
        add(result, "GET", "/api/stock-movements", "STOCK_MOVEMENT_READ");
        add(result, "POST", "/api/stock-movements", "STOCK_MOVEMENT_CREATE");
        add(result, "GET", "/api/stock-balances", "STOCK_BALANCE_READ");
        add(result, "GET", "/api/lot-types", "LOOKUP_READ");
        add(result, "GET", "/api/stock-statuses", "LOOKUP_READ");
        add(result, "GET", "/api/movement-types", "LOOKUP_READ");

        add(result, "GET", "/api/work-orders", "WORK_ORDER_READ");
        add(result, "GET", "/api/work-orders/{id}", "WORK_ORDER_READ");
        add(result, "POST", "/api/work-orders", "WORK_ORDER_CREATE");
        add(result, "PUT", "/api/work-orders/{id}", "WORK_ORDER_UPDATE");
        add(result, "DELETE", "/api/work-orders/{id}", "WORK_ORDER_DELETE");
        add(result, "GET", "/api/work-orders/{workOrderId}/materials", "WORK_ORDER_MATERIAL_READ");
        add(result, "POST", "/api/work-orders/{workOrderId}/materials", "WORK_ORDER_MATERIAL_ADD");
        add(result, "DELETE", "/api/work-orders/{workOrderId}/materials/{materialId}",
                "WORK_ORDER_MATERIAL_DELETE");
        add(result, "GET", "/api/work-orders/{workOrderId}/events", "WORK_ORDER_EVENT_READ");
        add(result, "POST", "/api/work-orders/{workOrderId}/events", "WORK_ORDER_EVENT_ADD");
        add(result, "GET", "/api/work-orders/statuses", "LOOKUP_READ");
        add(result, "GET", "/api/work-orders/priorities", "LOOKUP_READ");
        add(result, "GET", "/api/work-orders/event-types", "LOOKUP_READ");

        add(result, "GET", "/api/quality-inspections", "QUALITY_INSPECTION_READ");
        add(result, "GET", "/api/quality-inspections/{id}", "QUALITY_INSPECTION_READ");
        add(result, "POST", "/api/quality-inspections", "QUALITY_INSPECTION_CREATE");
        add(result, "DELETE", "/api/quality-inspections/{id}", "QUALITY_INSPECTION_DELETE");
        add(result, "GET", "/api/quality-inspections/{inspectionId}/results", "QUALITY_RESULT_READ");
        add(result, "POST", "/api/quality-inspections/{inspectionId}/results", "QUALITY_RESULT_CREATE");
        add(result, "GET", "/api/quality-inspections/statuses", "LOOKUP_READ");
        add(result, "GET", "/api/quality-inspections/defect-types", "LOOKUP_READ");

        add(result, "GET", "/api/maintenance-tickets", "MAINTENANCE_TICKET_READ");
        add(result, "GET", "/api/maintenance-tickets/{id}", "MAINTENANCE_TICKET_READ");
        add(result, "POST", "/api/maintenance-tickets", "MAINTENANCE_TICKET_CREATE");
        add(result, "PUT", "/api/maintenance-tickets/{id}", "MAINTENANCE_TICKET_UPDATE");
        add(result, "DELETE", "/api/maintenance-tickets/{id}", "MAINTENANCE_TICKET_DELETE");
        add(result, "GET", "/api/maintenance-tickets/{ticketId}/downtime", "MACHINE_DOWNTIME_READ");
        add(result, "POST", "/api/maintenance-tickets/{ticketId}/downtime", "MACHINE_DOWNTIME_CREATE");
        add(result, "GET", "/api/maintenance-tickets/types", "LOOKUP_READ");
        add(result, "GET", "/api/maintenance-tickets/statuses", "LOOKUP_READ");
        add(result, "GET", "/api/maintenance-tickets/priorities", "LOOKUP_READ");

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
                    "OPENAPI_REQUEST_SCHEMA",
                    Set.of(200, 201, 204, 400, 404, 409, 500),
                    mutating));
        }
        return Collections.unmodifiableList(result);
    }

    public static final class EndpointSpec {

        private final String method;
        private final String path;
        private final String authority;
        private final String validRequestFixture;
        private final Set<Integer> allowedStatuses;
        private final boolean mutationProbe;

        EndpointSpec(
                String method,
                String path,
                String authority,
                String validRequestFixture,
                Set<Integer> allowedStatuses,
                boolean mutationProbe) {
            this.method = method;
            this.path = path;
            this.authority = authority;
            this.validRequestFixture = validRequestFixture;
            this.allowedStatuses = allowedStatuses;
            this.mutationProbe = mutationProbe;
        }

        public String getMethod() {
            return method;
        }

        public String getPath() {
            return path;
        }

        public String getAuthority() {
            return authority;
        }

        public String getValidRequestFixture() {
            return validRequestFixture;
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

    private static void add(
            Map<String, String> catalog,
            String method,
            String path,
            String authority) {
        String previous = catalog.put(method + " " + path, authority);
        if (previous != null) {
            throw new IllegalStateException("Duplicate endpoint catalog entry: " + method + " " + path);
        }
    }
}

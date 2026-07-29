package fpt.qn.mes.auth.application.permission;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Component;

import fpt.qn.mes.auth.domain.entities.Role;

@Component
public class RolePermissionPolicy {

    private static final Set<String> ADMIN_PERMISSIONS = Set.of(
            "BOM_ACTIVATE",
            "BOM_CREATE",
            "BOM_CREATE_VERSION",
            "BOM_ITEM_ADD",
            "BOM_ITEM_DELETE",
            "BOM_READ",
            "LOCATION_CREATE",
            "LOCATION_DEACTIVATE",
            "LOCATION_READ",
            "LOCATION_UPDATE",
            "LOOKUP_READ",
            "MACHINE_CREATE",
            "MACHINE_DEACTIVATE",
            "MACHINE_DOWNTIME_CREATE",
            "MACHINE_DOWNTIME_READ",
            "MACHINE_READ",
            "MACHINE_STATUS_CHANGE",
            "MACHINE_UPDATE",
            "MAINTENANCE_TICKET_CREATE",
            "MAINTENANCE_TICKET_DELETE",
            "MAINTENANCE_TICKET_READ",
            "MAINTENANCE_TICKET_UPDATE",
            "PRODUCT_CREATE",
            "PRODUCT_DEACTIVATE",
            "PRODUCT_READ",
            "PRODUCT_UPDATE",
            "PRODUCTION_LINE_CREATE",
            "PRODUCTION_LINE_DEACTIVATE",
            "PRODUCTION_LINE_READ",
            "PRODUCTION_LINE_UPDATE",
            "QUALITY_INSPECTION_CREATE",
            "QUALITY_INSPECTION_DELETE",
            "QUALITY_INSPECTION_READ",
            "QUALITY_RESULT_CREATE",
            "QUALITY_RESULT_READ",
            "ROLE_CREATE",
            "ROLE_DELETE",
            "ROLE_READ",
            "ROLE_UPDATE",
            "STOCK_BALANCE_READ",
            "STOCK_LOT_CREATE",
            "STOCK_LOT_READ",
            "STOCK_MOVEMENT_CREATE",
            "STOCK_MOVEMENT_READ",
            "USER_ACTIVATE",
            "USER_CREATE",
            "USER_DEACTIVATE",
            "USER_READ",
            "USER_ROLE_ASSIGN",
            "USER_ROLE_READ",
            "USER_UPDATE",
            "WAREHOUSE_CREATE",
            "WAREHOUSE_DEACTIVATE",
            "WAREHOUSE_READ",
            "WAREHOUSE_UPDATE",
            "WORK_ORDER_CREATE",
            "WORK_ORDER_DELETE",
            "WORK_ORDER_EVENT_ADD",
            "WORK_ORDER_EVENT_READ",
            "WORK_ORDER_MATERIAL_ADD",
            "WORK_ORDER_MATERIAL_DELETE",
            "WORK_ORDER_MATERIAL_READ",
            "WORK_ORDER_READ",
            "WORK_ORDER_UPDATE");

    private static final Map<String, Set<String>> ROLE_PERMISSIONS = Map.ofEntries(
            Map.entry("ADMIN", ADMIN_PERMISSIONS),
            Map.entry("WAREHOUSE_MANAGER", Set.of(
                    "LOOKUP_READ", "PRODUCT_READ", "WAREHOUSE_READ", "LOCATION_READ",
                    "MACHINE_READ", "PRODUCTION_LINE_READ", "LOCATION_CREATE", "LOCATION_UPDATE",
                    "STOCK_LOT_READ", "STOCK_LOT_CREATE", "STOCK_MOVEMENT_READ",
                    "STOCK_MOVEMENT_CREATE", "STOCK_BALANCE_READ", "WORK_ORDER_MATERIAL_READ")),
            Map.entry("PLANNER", Set.of(
                    "LOOKUP_READ", "PRODUCT_READ", "WAREHOUSE_READ", "LOCATION_READ",
                    "MACHINE_READ", "PRODUCTION_LINE_READ", "BOM_READ", "BOM_CREATE",
                    "BOM_ACTIVATE", "BOM_CREATE_VERSION", "BOM_ITEM_ADD", "BOM_ITEM_DELETE",
                    "STOCK_LOT_READ", "STOCK_BALANCE_READ", "WORK_ORDER_READ",
                    "WORK_ORDER_CREATE", "WORK_ORDER_UPDATE", "WORK_ORDER_DELETE",
                    "WORK_ORDER_MATERIAL_READ", "WORK_ORDER_MATERIAL_ADD",
                    "WORK_ORDER_MATERIAL_DELETE", "WORK_ORDER_EVENT_READ",
                    "WORK_ORDER_EVENT_ADD", "MAINTENANCE_TICKET_CREATE")),
            Map.entry("OPERATOR", Set.of(
                    "LOOKUP_READ", "PRODUCT_READ", "WAREHOUSE_READ", "LOCATION_READ",
                    "MACHINE_READ", "PRODUCTION_LINE_READ", "BOM_READ", "WORK_ORDER_READ",
                    "WORK_ORDER_EVENT_READ", "WORK_ORDER_EVENT_ADD",
                    "MAINTENANCE_TICKET_CREATE")),
            Map.entry("QC_INSPECTOR", Set.of(
                    "LOOKUP_READ", "PRODUCT_READ", "WAREHOUSE_READ", "LOCATION_READ",
                    "MACHINE_READ", "PRODUCTION_LINE_READ", "BOM_READ",
                    "QUALITY_INSPECTION_READ", "QUALITY_INSPECTION_CREATE",
                    "QUALITY_RESULT_READ", "QUALITY_RESULT_CREATE")),
            Map.entry("MAINTENANCE_ENGINEER", Set.of(
                    "LOOKUP_READ", "PRODUCT_READ", "WAREHOUSE_READ", "LOCATION_READ",
                    "MACHINE_READ", "PRODUCTION_LINE_READ", "MACHINE_UPDATE",
                    "MACHINE_STATUS_CHANGE", "MAINTENANCE_TICKET_READ",
                    "MAINTENANCE_TICKET_CREATE", "MAINTENANCE_TICKET_UPDATE",
                    "MACHINE_DOWNTIME_READ", "MACHINE_DOWNTIME_CREATE")),
            Map.entry("FACTORY_MANAGER", Set.of(
                    "LOOKUP_READ", "BOM_READ", "LOCATION_READ", "MACHINE_DOWNTIME_READ",
                    "MACHINE_READ", "MAINTENANCE_TICKET_READ", "PRODUCT_READ",
                    "PRODUCTION_LINE_READ", "QUALITY_INSPECTION_READ", "QUALITY_RESULT_READ",
                    "STOCK_BALANCE_READ", "STOCK_LOT_READ", "STOCK_MOVEMENT_READ", "WAREHOUSE_READ",
                    "WORK_ORDER_EVENT_READ", "WORK_ORDER_MATERIAL_READ", "WORK_ORDER_READ",
                    "MAINTENANCE_TICKET_CREATE", "MAINTENANCE_TICKET_UPDATE",
                    "MACHINE_DOWNTIME_CREATE")),
            Map.entry("AUDITOR", Set.of(
                    "LOOKUP_READ", "PRODUCT_READ", "WAREHOUSE_READ", "LOCATION_READ",
                    "MACHINE_READ", "PRODUCTION_LINE_READ", "STOCK_LOT_READ",
                    "STOCK_MOVEMENT_READ", "STOCK_BALANCE_READ")));

    public List<String> permissionsForRoles(Collection<String> roles) {
        TreeSet<String> permissions = new TreeSet<>();
        if (roles == null) {
            return List.of();
        }
        for (String role : roles) {
            permissions.addAll(ROLE_PERMISSIONS.getOrDefault(Role.normalizeName(role), Set.of()));
        }
        return List.copyOf(permissions);
    }

    public List<String> permissionsForRole(String role) {
        return permissionsForRoles(List.of(role));
    }

    public Set<String> allApiPermissions() {
        return ADMIN_PERMISSIONS;
    }
}

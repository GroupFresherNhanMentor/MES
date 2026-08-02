package fpt.qn.mes.master.warehouse.application.dto.warehouse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import fpt.qn.mes.master.warehouse.application.dto.warehousestatus.WarehouseStatusResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor @FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseResponse {

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UserInfo {
        private UUID id;
        private String fullName;
        private String username;
    }

    UUID id;
    String code;
    String name;
    String address;
    WarehouseStatusResponse warehouseStatus;
    List<WarehouseManagerResponse> managers;
    Instant createdAt;
    UserInfo createdBy;
    Instant updatedAt;
    UserInfo updatedBy;
}

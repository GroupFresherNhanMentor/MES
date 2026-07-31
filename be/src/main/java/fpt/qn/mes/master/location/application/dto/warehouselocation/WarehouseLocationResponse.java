package fpt.qn.mes.master.location.application.dto.warehouselocation;

import java.time.Instant;
import java.util.UUID;

import fpt.qn.mes.master.location.application.dto.locationstatus.LocationStatusResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor @FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseLocationResponse {

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class WarehouseInfo {
        private UUID id;
        private String code;
        private String name;
        private String address;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UserInfo {
        private UUID id;
        private String fullName;
        private String username;
    }

    UUID id;
    WarehouseInfo warehouse;
    String code;
    String name;
    LocationStatusResponse locationStatus;
    Instant createdAt;
    UserInfo createdBy;
    Instant updatedAt;
    UserInfo updatedBy;
}

package fpt.qn.mes.bom.application.dto.bom;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import fpt.qn.mes.bom.application.dto.bomitem.BomItemResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BomResponse {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserInfo {
        UUID id;
        String fullName;
        String username;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class BomStatusInfo {
        UUID id;
        String name;
    }

    UUID id;
    UUID finishedProductId;
    String finishedProductCode;
    String finishedProductName;
    Integer version;
    BomStatusInfo bomStatus;
    UserInfo createdBy;
    Instant createdAt;
    List<BomItemResponse> items;
}

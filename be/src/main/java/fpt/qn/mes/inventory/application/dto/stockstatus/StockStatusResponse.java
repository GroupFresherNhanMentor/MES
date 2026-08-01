package fpt.qn.mes.inventory.application.dto.stockstatus;

import java.time.Instant;
import java.util.UUID;

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
public class StockStatusResponse {

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

    UUID id;
    String name;
    String description;
    Instant createdAt;
    UserInfo createdBy;
    Instant updatedAt;
    UserInfo updatedBy;
}

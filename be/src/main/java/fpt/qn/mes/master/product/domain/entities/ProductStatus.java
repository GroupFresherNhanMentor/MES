package fpt.qn.mes.master.product.domain.entities;

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
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductStatus {

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UserRef {
        private UUID id;
        private String fullName;
        private String username;
    }

    UUID id;
    String name;
    String description;
    UserRef createdBy;
    UserRef updatedBy;
    Instant createdAt;
    Instant updatedAt;
}

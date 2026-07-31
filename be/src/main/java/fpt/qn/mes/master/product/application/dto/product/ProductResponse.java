package fpt.qn.mes.master.product.application.dto.product;

import java.time.Instant;
import java.util.UUID;

import fpt.qn.mes.master.product.application.dto.productstatus.ProductStatusResponse;
import fpt.qn.mes.master.product.application.dto.producttype.ProductTypeResponse;
import fpt.qn.mes.master.product.application.dto.unitofmeasure.UnitOfMeasureResponse;
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
public class ProductResponse {

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
    String code;
    String name;
    ProductTypeResponse productType;
    UnitOfMeasureResponse unit;
    ProductStatusResponse productStatus;
    String version;
    Instant createdAt;
    UserInfo createdBy;
    Instant updatedAt;
    UserInfo updatedBy;
}

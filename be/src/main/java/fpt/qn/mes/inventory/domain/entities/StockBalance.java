package fpt.qn.mes.inventory.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import fpt.qn.mes.common.exception.DomainException;
import fpt.qn.mes.common.util.UuidV7;
import fpt.qn.mes.inventory.application.exception.InsufficientStockException;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockBalance {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class WarehouseRef {
        UUID id;
        String code;
        String name;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class LocationRef {
        UUID id;
        String code;
        String name;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ProductRef {
        UUID id;
        String code;
        String name;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class LotRef {
        UUID id;
        String lotNumber;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class StockStatusRef {
        UUID id;
        String name;
    }

    UUID id;
    WarehouseRef warehouse;
    LocationRef location;
    ProductRef product;
    LotRef lot;
    StockStatusRef stockStatus;
    BigDecimal quantity;
    Long version;
    Instant createdAt;
    Instant updatedAt;

    public static StockBalance create(UUID warehouseId, UUID locationId, UUID productId,
            UUID lotId, UUID stockStatusId, BigDecimal quantity) {
        return StockBalance.builder()
                .id(UuidV7.generate())
                .warehouse(WarehouseRef.builder().id(warehouseId).build())
                .location(locationId != null ? LocationRef.builder().id(locationId).build() : null)
                .product(ProductRef.builder().id(productId).build())
                .lot(lotId != null ? LotRef.builder().id(lotId).build() : null)
                .stockStatus(stockStatusId != null ? StockStatusRef.builder().id(stockStatusId).build() : null)
                .quantity(quantity)
                .version(1L)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static StockBalance update(StockBalance existing, BigDecimal newQuantity) {
        return StockBalance.builder()
                .id(existing.id)
                .warehouse(existing.warehouse)
                .location(existing.location)
                .product(existing.product)
                .lot(existing.lot)
                .stockStatus(existing.stockStatus)
                .quantity(newQuantity)
                .version(existing.version == null ? 1L : existing.version + 1)
                .createdAt(existing.createdAt)
                .updatedAt(Instant.now())
                .build();
    }

    public UUID getWarehouseId() {
        return warehouse != null ? warehouse.getId() : null;
    }

    public UUID getLocationId() {
        return location != null ? location.getId() : null;
    }

    public UUID getProductId() {
        return product != null ? product.getId() : null;
    }

    public UUID getLotId() {
        return lot != null ? lot.getId() : null;
    }

    public UUID getStockStatusId() {
        return stockStatus != null ? stockStatus.getId() : null;
    }

    public void deductQuantity(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Deduction quantity must be positive");
        }
        if (this.quantity == null || this.quantity.compareTo(amount) < 0) {
            throw new InsufficientStockException("Insufficient stock balance for deduction");
        }
        this.quantity = this.quantity.subtract(amount);
        this.version = this.version != null ? this.version + 1 : 1L;
    }

    public void addQuantity(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Addition quantity must be positive");
        }
        if (this.quantity == null) {
            this.quantity = amount;
        } else {
            this.quantity = this.quantity.add(amount);
        }
        this.version = this.version != null ? this.version + 1 : 1L;
    }
}

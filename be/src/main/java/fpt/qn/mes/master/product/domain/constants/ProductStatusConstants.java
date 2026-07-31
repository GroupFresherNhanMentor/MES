package fpt.qn.mes.master.product.domain.constants;

/**
 * Predefined product status name constants matching database seed data.
 * <p>
 * <b>Usage Example:</b>
 * <pre>{@code
 * ProductStatus active = productStatusRepository.findByName(ProductStatusConstants.ACTIVE)
 *         .orElseThrow(() -> new ProductStatusNotFoundException("ACTIVE status not found"));
 * }</pre>
 */
public final class ProductStatusConstants {

    public static final String ACTIVE   = "ACTIVE";
    public static final String INACTIVE = "INACTIVE";

    private ProductStatusConstants() {
    }
}

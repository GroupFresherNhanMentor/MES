package fpt.qn.mes.master.product.domain.constants;

/**
 * Predefined product type name constants matching database seed data.
 * <p>
 * <b>Usage Example:</b>
 * <pre>{@code
 * ProductType rawMaterial = productTypeRepository.findByName(ProductTypeConstants.RAW_MATERIAL)
 *         .orElseThrow(() -> new ProductTypeNotFoundException("RAW_MATERIAL type not found"));
 * }</pre>
 */
public final class ProductTypeConstants {

    public static final String RAW_MATERIAL  = "RAW_MATERIAL";
    public static final String SEMI_FINISHED = "SEMI_FINISHED";
    public static final String FINISHED_GOOD = "FINISHED_GOOD";
    public static final String CONSUMABLE    = "CONSUMABLE";
    public static final String SPARE_PART    = "SPARE_PART";

    private ProductTypeConstants() {
    }
}

package fpt.qn.mes.master.product.domain.constants;

/**
 * Predefined unit of measure name constants matching database seed data.
 * <p>
 * <b>Usage Example:</b>
 * <pre>{@code
 * UnitOfMeasure kg = unitOfMeasureRepository.findByName(UnitOfMeasureConstants.KG)
 *         .orElseThrow(() -> new UnitOfMeasureNotFoundException("KG unit not found"));
 * }</pre>
 */
public final class UnitOfMeasureConstants {

    public static final String PCS  = "PCS";
    public static final String KG   = "KG";
    public static final String G    = "G";
    public static final String L    = "L";
    public static final String ML   = "ML";
    public static final String M    = "M";
    public static final String M2   = "M2";
    public static final String M3   = "M3";
    public static final String BOX  = "BOX";
    public static final String SET  = "SET";
    public static final String ROLL = "ROLL";
    public static final String TON  = "TON";

    private UnitOfMeasureConstants() {
    }
}

package fpt.qn.mes.workorder.domain.constants;

/**
 * Predefined work order status name constants matching database seed data.
 */
public final class WorkOrderStatusConstants {

    public static final String DRAFT = "DRAFT";
    public static final String PLANNED = "PLANNED";
    public static final String READY_TO_PRODUCE = "READY_TO_PRODUCE";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String PAUSED = "PAUSED";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELLED = "CANCELLED";

    private WorkOrderStatusConstants() {
        // Prevent instantiation
    }
}

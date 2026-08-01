package fpt.qn.mes.workorder.support;

import java.util.UUID;

/** Identifiers used by completion integration tests to keep committed fixtures explicit. */
public final class WorkOrderCompletionFixture {
    private WorkOrderCompletionFixture() {
    }

    public static UUID id() {
        return UUID.randomUUID();
    }
}

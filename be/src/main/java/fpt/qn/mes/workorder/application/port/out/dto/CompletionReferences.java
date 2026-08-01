package fpt.qn.mes.workorder.application.port.out.dto;

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
public class CompletionReferences {
    UUID availableStatusId;
    UUID reservedStatusId;
    UUID consumedStatusId;
    UUID scrappedStatusId;
    UUID qualityInspectionStatusId;
    UUID consumeMovementTypeId;
    UUID scrapMovementTypeId;
    UUID releaseMovementTypeId;
    UUID productionOutputMovementTypeId;
    UUID productionLotTypeId;
    UUID pendingInspectionStatusId;
    UUID completeEventTypeId;
    UUID availableMachineStatusId;
}

package fpt.qn.mes.master.machine.application.dto.machine.search;

import java.util.UUID;
import fpt.qn.mes.common.dto.request.BaseSearchRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MachineSearchRequest extends BaseSearchRequest {
    UUID productionLineId;
    String code;
    String name;
    UUID machineStatusId;
}

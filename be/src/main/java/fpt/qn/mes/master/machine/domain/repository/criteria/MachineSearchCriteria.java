package fpt.qn.mes.master.machine.domain.repository.criteria;

import java.util.UUID;

import fpt.qn.mes.common.domainQuery.BaseSearchCriteria;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MachineSearchCriteria extends BaseSearchCriteria {
    UUID productionLineId;
    String code;
    String name;
    UUID machineStatusId;
}

package fpt.qn.mes.inventory.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.repository.BaseDomainRepository;
import fpt.qn.mes.inventory.domain.entities.LotType;
import fpt.qn.mes.inventory.domain.repository.criteria.LotTypeSearchCriteria;

public interface LotTypeRepository extends BaseDomainRepository<LotType, UUID> {

    Optional<UUID> findIdByName(String name);

    List<LotType> search(LotTypeSearchCriteria criteria);

    long count(LotTypeSearchCriteria criteria);
}

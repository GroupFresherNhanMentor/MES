package fpt.qn.mes.master.location.domain.repository;

import java.util.Optional;
import java.util.UUID;
import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.master.location.domain.entities.LocationStatus;
import fpt.qn.mes.master.location.domain.repository.criteria.LocationStatusSearchCriteria;

public interface LocationStatusRepository {
    Optional<LocationStatus> findById(UUID id);
    Optional<LocationStatus> findByName(String name);
    boolean existsById(UUID id);
    boolean existsByName(String name);
    LocationStatus save(LocationStatus status);
    PaginationResult<LocationStatus> search(LocationStatusSearchCriteria criteria);
}

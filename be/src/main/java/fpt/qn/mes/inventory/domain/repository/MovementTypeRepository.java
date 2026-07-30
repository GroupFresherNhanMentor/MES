package fpt.qn.mes.inventory.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.repository.BaseDomainRepository;
import fpt.qn.mes.inventory.domain.constants.MovementTypeConstants;
import fpt.qn.mes.inventory.domain.entities.MovementType;
import fpt.qn.mes.inventory.domain.repository.criteria.MovementTypeSearchCriteria;

public interface MovementTypeRepository extends BaseDomainRepository<MovementType, UUID> {

    /**
     * Find movement type ID by its unique name string.
     * <p>
     * <b>Usage Hint:</b> Pass predefined string constants from {@link MovementTypeConstants}.<br>
     * Example: <code>movementTypeRepository.findIdByName(MovementTypeConstants.QC_RELEASE)</code>
     *
     * @param name Movement type name constant (e.g. {@link MovementTypeConstants#QC_RELEASE})
     * @return Optional containing the UUID if found
     * @see MovementTypeConstants
     */
    Optional<UUID> findIdByName(String name);

    List<MovementType> search(MovementTypeSearchCriteria criteria);

    long count(MovementTypeSearchCriteria criteria);
}

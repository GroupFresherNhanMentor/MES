package fpt.qn.mes.bom.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.bom.application.dto.bomitem.update.UpdateBomItemRequest;
import fpt.qn.mes.bom.application.exception.BomNotFoundException;
import fpt.qn.mes.bom.application.exception.BomStatusNotFoundException;
import fpt.qn.mes.bom.application.exception.InvalidBomStatusException;
import fpt.qn.mes.bom.application.port.in.BomItemUseCase;
import fpt.qn.mes.bom.domain.constants.BomStatusConstants;
import fpt.qn.mes.bom.domain.entities.BomItem;
import fpt.qn.mes.bom.domain.repository.BomItemRepository;
import fpt.qn.mes.bom.domain.repository.BomRepository;
import fpt.qn.mes.bom.domain.repository.BomStatusRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BomItemService implements BomItemUseCase {

    BomRepository bomRepository;
    BomItemRepository bomItemRepository;
    BomStatusRepository bomStatusRepository;

    @Override
    @Transactional
    public void updateBomItems(UUID bomId, List<UpdateBomItemRequest> items) {
        var bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new BomNotFoundException("BOM not found: " + bomId));

        var draftStatus = bomStatusRepository.findByName(BomStatusConstants.DRAFT)
                .orElseThrow(() -> new BomStatusNotFoundException("DRAFT status not found in bom_statuses"));

        if (!draftStatus.getId().equals(bom.getBomStatus().getId())) {
            throw new InvalidBomStatusException("Only DRAFT BOMs can be modified; create a new version instead");
        }

        bomItemRepository.deleteByBomId(bomId);
        for (UpdateBomItemRequest item : items) {
            bomItemRepository.save(BomItem.create(bomId, item.getMaterialProductId(),
                    item.getQuantityPerUnit(), item.getScrapRate()));
        }
    }
}

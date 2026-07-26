package fpt.qn.mes.bom.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.bom.application.dto.BomDto;
import fpt.qn.mes.bom.application.dto.BomItemDto;
import fpt.qn.mes.bom.application.dto.CreateBomItemRequest;
import fpt.qn.mes.bom.application.dto.CreateBomRequest;
import fpt.qn.mes.bom.application.mapper.BomDtoMapper;
import fpt.qn.mes.bom.application.port.in.BomUseCase;
import fpt.qn.mes.bom.domain.repository.BomRepository;
import fpt.qn.mes.common.dto.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BomService implements BomUseCase {

    BomRepository bomRepository;
    BomDtoMapper mapper;

    @Override @Transactional(readOnly = true)
    public PageResponse<BomDto> getBoms(int page, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional(readOnly = true)
    public BomDto getBomById(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public BomDto createBom(CreateBomRequest request, UUID currentUserId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public void deleteBom(UUID id) {}

    @Override @Transactional
    public BomItemDto addBomItem(UUID bomId, CreateBomItemRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public void deleteBomItem(UUID bomId, UUID itemId) {}
}

package fpt.qn.mes.bom.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.bom.application.dto.bomstatus.BomStatusResponse;
import fpt.qn.mes.bom.application.dto.bomstatus.create.CreateBomStatusRequest;
import fpt.qn.mes.bom.application.exception.BomConflictException;
import fpt.qn.mes.bom.application.mapper.BomStatusDtoMapper;
import fpt.qn.mes.bom.application.port.in.BomStatusUseCase;
import fpt.qn.mes.bom.domain.entities.BomStatus;
import fpt.qn.mes.bom.domain.repository.BomStatusRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BomStatusService implements BomStatusUseCase {

    BomStatusRepository bomStatusRepository;
    BomStatusDtoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<BomStatusResponse> getBomStatuses() {
        return bomStatusRepository.findAll().stream().map(s -> mapper.toDto(s)).toList();
    }

    @Override
    @Transactional
    public void createBomStatus(CreateBomStatusRequest request) {
        if (bomStatusRepository.existsByName(request.getName())) {
            throw new BomConflictException("BOM status already exists: " + request.getName());
        }
        bomStatusRepository.save(BomStatus.create(request.getName(), request.getDescription()));
    }
}

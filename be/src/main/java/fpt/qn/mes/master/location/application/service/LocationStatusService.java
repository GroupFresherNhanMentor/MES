package fpt.qn.mes.master.location.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.util.PaginationUtils;
import fpt.qn.mes.master.location.application.dto.locationstatus.LocationStatusResponse;
import fpt.qn.mes.master.location.application.dto.locationstatus.create.CreateLocationStatusRequest;
import fpt.qn.mes.master.location.application.dto.locationstatus.search.LocationStatusSearchRequest;
import fpt.qn.mes.master.location.application.exception.LocationStatusConflictException;
import fpt.qn.mes.master.location.application.mapper.LocationStatusDtoMapper;
import fpt.qn.mes.master.location.application.port.in.LocationStatusUseCase;
import fpt.qn.mes.master.location.domain.entities.LocationStatus;
import fpt.qn.mes.master.location.domain.repository.LocationStatusRepository;
import fpt.qn.mes.master.location.domain.repository.criteria.LocationStatusSearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationStatusService implements LocationStatusUseCase {

    LocationStatusRepository locationstatusRepository;
    LocationStatusDtoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LocationStatusResponse> getLocationStatuses(LocationStatusSearchRequest request) {
        var criteria = LocationStatusSearchCriteria.builder()
                .name(request.getName())
                .page(request.getPage())
                .size(request.getSize())
                .sort(request.getSort())
                .build();
        var result = locationstatusRepository.search(criteria);
        var items = result.getItems().stream().map(s -> mapper.toDto(s)).toList();
        return PageResponse.<LocationStatusResponse>builder()
                .items(items).totalElements(result.getTotal())
                .pageNumber(request.getPage()).pageSize(request.getSize())
                .totalPages(PaginationUtils.calculateTotalPages(result.getTotal(), request.getSize()))
                .build();
    }

    @Override
    @Transactional
    public void createLocationStatus(CreateLocationStatusRequest request) {
        if (locationstatusRepository.existsByName(request.getName())) {
            throw new LocationStatusConflictException("Name already exists: " + request.getName());
        }
        var status = LocationStatus.create(request.getName(), request.getDescription());
        locationstatusRepository.save(status);
    }
}

package fpt.qn.mes.common.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LookupService {

    LookupRepository lookupRepository;

    public List<LookupEntry> getProductTypes() {
        return lookupRepository.findAll("product_types");
    }

    public List<LookupEntry> getProductStatuses() {
        return lookupRepository.findAll("product_statuses");
    }

    public List<LookupEntry> getUnitsOfMeasure() {
        return lookupRepository.findAll("units_of_measure");
    }

    public List<LookupEntry> getWarehouseStatuses() {
        return lookupRepository.findAll("warehouse_statuses");
    }

    public List<LookupEntry> getLocationStatuses() {
        return lookupRepository.findAll("location_statuses");
    }

    public List<LookupEntry> getLineStatuses() {
        return lookupRepository.findAll("line_statuses");
    }

    public List<LookupEntry> getMachineStatuses() {
        return lookupRepository.findAll("machine_statuses");
    }
}

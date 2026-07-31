package fpt.qn.mes.master.location.infrastructure.persistence.locationstatus;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.LocationStatusesRecord;
import fpt.qn.mes.master.location.domain.entities.LocationStatus;

@Component
public class LocationStatusRecordMapper {

    public LocationStatus toDomain(LocationStatusesRecord r) {
        if (r == null || r.getId() == null) return null;
        return LocationStatus.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .build();
    }

    public LocationStatusesRecord toRecord(LocationStatus s) {
        LocationStatusesRecord r = new LocationStatusesRecord();
        r.setId(s.getId());
        r.setName(s.getName());
        r.setDescription(s.getDescription());
        return r;
    }
}

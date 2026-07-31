package fpt.qn.mes.audit.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.audit.application.dto.AuditLogResponse;
import fpt.qn.mes.audit.domain.entities.AuditLog;

@Mapper(componentModel = "spring")
public interface AuditLogDtoMapper {

    AuditLogResponse toDto(AuditLog domain);
}

package fpt.qn.mes.role.application.dto.request;

import java.util.List;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignPermissionsRequest {
    List<UUID> permissionIds;
}

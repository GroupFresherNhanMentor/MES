package fpt.qn.mes.role.domain.entities;

import java.util.List;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role {

    UUID id;
    String name;
    String description;
    List<String> permissionNames;

    public static Role create(String name, String description) {
        throw new UnsupportedOperationException("Not implemented");
    }
}

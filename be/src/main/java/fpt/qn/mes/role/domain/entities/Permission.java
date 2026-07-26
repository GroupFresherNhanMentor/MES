package fpt.qn.mes.role.domain.entities;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Permission {

    UUID id;
    String name;
    String description;

    public static Permission create(String name, String description) {
        throw new UnsupportedOperationException("Not implemented");
    }
}

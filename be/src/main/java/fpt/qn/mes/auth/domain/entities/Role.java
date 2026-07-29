package fpt.qn.mes.auth.domain.entities;

import java.util.List;
import java.util.Locale;
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
        String normalizedName = requireValidName(name);
        return Role.builder()
                .id(UUID.randomUUID())
                .name(normalizedName)
                .description(description)
                .permissionNames(List.of())
                .build();
    }

    public Role update(String newName, String newDescription) {
        return Role.builder()
                .id(id)
                .name(newName == null ? name : requireValidName(newName))
                .description(newDescription)
                .permissionNames(permissionNames == null ? List.of() : List.copyOf(permissionNames))
                .build();
    }

    public Role withPermissionNames(List<String> permissions) {
        return Role.builder()
                .id(id)
                .name(name)
                .description(description)
                .permissionNames(permissions == null ? List.of() : List.copyOf(permissions))
                .build();
    }

    public static String normalizeName(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private static String requireValidName(String value) {
        String normalized = normalizeName(value);
        if (normalized == null || normalized.isBlank() || normalized.length() > 50) {
            throw new fpt.qn.mes.auth.domain.exception.AuthDomainException(
                    "Role name must contain between 1 and 50 characters");
        }
        return normalized;
    }
}

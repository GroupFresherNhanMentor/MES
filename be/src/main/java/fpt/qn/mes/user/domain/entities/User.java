package fpt.qn.mes.user.domain.entities;

import java.time.Instant;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    UUID id;
    String username;
    String passwordHash;
    String fullName;
    Boolean active;
    Instant createdAt;

    public static User create(String username, String passwordHash, String fullName) {
        return User.builder()
                .id(UUID.randomUUID())
                .username(username == null ? null : username.trim())
                .passwordHash(passwordHash)
                .fullName(fullName)
                .active(true)
                .createdAt(Instant.now())
                .build();
    }

    public User updateFullName(String newFullName) {
        return User.builder()
                .id(id)
                .username(username)
                .passwordHash(passwordHash)
                .fullName(newFullName)
                .active(active)
                .createdAt(createdAt)
                .build();
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }
}

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
        throw new UnsupportedOperationException("Not implemented");
    }

    public void activate() {
    }

    public void deactivate() {
    }
}

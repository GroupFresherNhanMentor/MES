package fpt.qn.mes.auth.application.security;

import java.util.List;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CredentialAccount {

    UUID id;
    String username;
    String passwordHash;
    String fullName;
    boolean active;
    List<String> roles;
}


package fpt.qn.mes.master.line.application.dto.line;

import java.time.Instant;
import java.util.UUID;

import fpt.qn.mes.master.line.application.dto.linestatus.LineStatusResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor @FieldDefaults(level = AccessLevel.PRIVATE)
public class LineResponse {

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UserInfo {
        private UUID id;
        private String fullName;
        private String username;
    }

    UUID id;
    String code;
    String name;
    LineStatusResponse lineStatus;
    Instant createdAt;
    UserInfo createdBy;
    Instant updatedAt;
    UserInfo updatedBy;
}

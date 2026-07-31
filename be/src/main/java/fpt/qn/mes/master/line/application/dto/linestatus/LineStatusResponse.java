package fpt.qn.mes.master.line.application.dto.linestatus;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor @FieldDefaults(level = AccessLevel.PRIVATE)
public class LineStatusResponse {
    UUID id;
    String name;
    String description;
}

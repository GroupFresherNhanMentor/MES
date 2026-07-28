package fpt.qn.mes.common.dto.request;

import java.util.ArrayList;
import java.util.List;

import fpt.qn.mes.common.validation.ValidSortFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BaseSearchRequest {

    @Min(0)
    int page = 0;

    @Min(1)
    @Max(100)
    int size = 20;

    @ValidSortFormat
    List<String> sort = new ArrayList<>();
}

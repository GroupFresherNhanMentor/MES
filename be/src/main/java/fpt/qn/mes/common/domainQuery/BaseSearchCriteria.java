package fpt.qn.mes.common.domainQuery;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BaseSearchCriteria {

    @Builder.Default
    int page = 0;

    @Builder.Default
    int size = 20;

    @Builder.Default
    List<String> sort = new ArrayList<>();
}

package fpt.qn.mes.common.domainQuery;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaginationResult<T> {
    long total;
    List<T> items;

    public static <T> PaginationResult<T> of(List<T> items, long total) {
        return PaginationResult.<T>builder()
                .items(items != null ? items : List.of())
                .total(total)
                .build();
    }

    public static <T> PaginationResult<T> of(List<T> items, long total, int page, int size) {
        return PaginationResult.<T>builder()
                .items(items != null ? items : List.of())
                .total(total)
                .build();
    }
}

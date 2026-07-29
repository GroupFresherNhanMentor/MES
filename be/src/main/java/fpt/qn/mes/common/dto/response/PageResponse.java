package fpt.qn.mes.common.dto.response;

import java.util.List;
import java.util.function.Function;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import fpt.qn.mes.common.util.PaginationUtils;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageResponse<T> {
    long totalElements;
    int totalPages;
    int pageNumber;
    int pageSize;
    List<T> items;

    public <U> PageResponse<U> map(Function<T, U> mapper) {
        return PageResponse.<U>builder()
                .items(items.stream().map(mapper).toList())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .build();
    }

    public static <T> PageResponse<T> of(List<T> items, long totalElements, int pageNumber, int pageSize) {
        int totalPages = PaginationUtils.calculateTotalPages(totalElements, pageSize);
        return PageResponse.<T>builder()
                .items(items != null ? items : List.of())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .build();
    }
}

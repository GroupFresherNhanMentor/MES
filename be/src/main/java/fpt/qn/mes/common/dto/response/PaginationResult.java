package fpt.qn.mes.common.dto.response;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaginationResult<T> {
     long total;
     List<T> items;

     public static <T> PaginationResult<T> of(List<T> items, long total, int page, int size) {
         PaginationResult<T> result = new PaginationResult<>();
         result.items = items;
         result.total = total;
         return result;
     }
}

package fpt.qn.mes.common.repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jooq.Field;
import org.jooq.SortField;

public class SortUtils {

    private SortUtils() {}

    public static List<SortField<?>> resolveSorts(List<String> sort, Map<String, Field<?>> sortFields, Field<?> defaultField) {
        if (sort == null || sort.isEmpty()) {
            return List.of(defaultField.desc());
        }
        List<SortField<?>> resolved = sort.stream()
                .map(s -> s.split(",", 2))
                .filter(parts -> sortFields.containsKey(parts[0].trim()))
                .map(parts -> {
                    Field<?> col = sortFields.get(parts[0].trim());
                    boolean asc = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim());
                    return asc ? col.asc() : col.desc();
                })
                .collect(Collectors.toList());
        return resolved.isEmpty() ? List.of(defaultField.desc()) : resolved;
    }
}

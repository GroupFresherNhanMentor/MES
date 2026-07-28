package fpt.qn.mes.common.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LookupRepository {

    DSLContext ctx;

    public List<LookupEntry> findAll(String tableName) {
        return ctx.select(
                        DSL.field(DSL.name(tableName, "id")),
                        DSL.field(DSL.name(tableName, "name")),
                        DSL.field(DSL.name(tableName, "description")))
                .from(DSL.table(DSL.name(tableName)))
                .fetch()
                .stream()
                .map(r -> LookupEntry.builder()
                        .id(r.get(0, UUID.class))
                        .name(r.get(1, String.class))
                        .description(r.get(2, String.class))
                        .build())
                .collect(Collectors.toList());
    }
}

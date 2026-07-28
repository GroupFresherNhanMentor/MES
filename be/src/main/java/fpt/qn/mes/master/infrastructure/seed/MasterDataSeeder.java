package fpt.qn.mes.master.infrastructure.seed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import fpt.qn.mes.common.util.UuidV7;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MasterDataSeeder implements ApplicationRunner {

    DSLContext ctx;
    ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) {
        seed("product-types.json",      "product_types");
        seed("product-statuses.json",   "product_statuses");
        seed("units-of-measure.json",   "units_of_measure");
        seed("warehouse-statuses.json", "warehouse_statuses");
        seed("location-statuses.json",  "location_statuses");
        seed("line-statuses.json",      "line_statuses");
        seed("machine-statuses.json",   "machine_statuses");
        log.info("Master data seeded successfully");
    }

    private void seed(String fileName, String tableName) {
        try {
            Resource resource = new PathMatchingResourcePatternResolver()
                    .getResource("classpath:seed/" + fileName);
            if (!resource.exists()) {
                log.warn("Seed file not found: {}", fileName);
                return;
            }
            try (InputStream is = resource.getInputStream()) {
                List<Map<String, String>> items = objectMapper.readValue(
                        is, new TypeReference<List<Map<String, String>>>() {});
                for (Map<String, String> item : items) {
                    boolean exists = ctx.fetchExists(
                            ctx.selectFrom(DSL.table(DSL.name(tableName)))
                                    .where(DSL.field(DSL.name(tableName, "name")).eq(item.get("name"))));
                    if (!exists) {
                        ctx.insertInto(DSL.table(DSL.name(tableName)))
                                .set(DSL.field(DSL.name(tableName, "id")), UuidV7.generate())
                                .set(DSL.field(DSL.name(tableName, "name")), item.get("name"))
                                .set(DSL.field(DSL.name(tableName, "description")), item.get("description"))
                                .execute();
                        log.debug("Seeded {}: {}", fileName, item.get("name"));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to seed {}: {}", fileName, e.getMessage());
        }
    }
}

package fpt.qn.mes.common.repository;

import java.util.Optional;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.UpdatableRecord;

public abstract class BaseRepository<R extends UpdatableRecord<R>> {

    protected final DSLContext ctx;
    protected final Table<R> table;

    protected BaseRepository(DSLContext ctx, Table<R> table) {
        this.ctx = ctx;
        this.table = table;
    }

    protected Optional<R> fetchById(UUID id) {
        return ctx.selectFrom(table)
                .where(table.field("id", UUID.class).eq(id))
                .fetchOptional();
    }

    protected Optional<R> fetchByIdForUpdate(UUID id) {
        return ctx.selectFrom(table)
                .where(table.field("id", UUID.class).eq(id))
                .forUpdate()
                .fetchOptional();
    }

    protected R create(R record) {
        return ctx.insertInto(table)
                .set(record)
                .returning()
                .fetchOne();
    }

    protected R update(R record) {
        if (!record.touched()) return record;
        return ctx.update(table)
                .set(record)
                .where(table.field("id", UUID.class).eq(record.get("id", UUID.class)))
                .returning()
                .fetchOne();
    }

    protected void hardDeleteById(UUID id) {
        ctx.deleteFrom(table)
                .where(table.field("id", UUID.class).eq(id))
                .execute();
    }

    protected boolean existsById(UUID id) {
        return ctx.fetchExists(
                ctx.selectFrom(table)
                        .where(table.field("id", UUID.class).eq(id))
        );
    }

    protected long count() {
        return ctx.fetchCount(table);
    }

    protected long count(Condition condition) {
        return ctx.fetchCount(table, condition);
    }
}

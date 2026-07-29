package fpt.qn.mes.user.infrastructure.persistence;

import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.UsersRecord;
import fpt.qn.mes.user.domain.entities.User;

@Component
public class UserRecordMapper {

    public User toDomain(UsersRecord r) {
        return User.builder()
                .id(r.getId())
                .username(r.getUsername())
                .passwordHash(r.getPasswordHash())
                .fullName(r.getFullName())
                .active(r.getActive())
                .createdAt(r.getCreatedAt().toInstant())
                .build();
    }

    public UsersRecord toRecord(User u) {
        UsersRecord record = new UsersRecord();
        record.setId(u.getId());
        record.setUsername(u.getUsername());
        record.setPasswordHash(u.getPasswordHash());
        record.setFullName(u.getFullName());
        record.setActive(u.getActive());
        record.setCreatedAt(u.getCreatedAt().atOffset(ZoneOffset.UTC));
        return record;
    }
}

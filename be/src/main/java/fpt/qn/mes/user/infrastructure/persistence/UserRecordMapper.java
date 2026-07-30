package fpt.qn.mes.user.infrastructure.persistence;

import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.UsersRecord;
import fpt.qn.mes.user.domain.entities.User;

@Component
public class UserRecordMapper {

    public User toDomain(UsersRecord r) {
        if (r == null) return null;
        return User.builder()
                .id(r.getId())
                .username(r.getUsername())
                .passwordHash(r.getPasswordHash())
                .fullName(r.getFullName())
                .active(r.getActive())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toInstant() : null)
                .build();
    }

    public UsersRecord toRecord(User u) {
        if (u == null) return null;
        UsersRecord r = new UsersRecord();
        r.setId(u.getId());
        r.setUsername(u.getUsername());
        r.setPasswordHash(u.getPasswordHash());
        r.setFullName(u.getFullName());
        r.setActive(u.getActive());
        if (u.getCreatedAt() != null) {
            r.setCreatedAt(u.getCreatedAt().atOffset(ZoneOffset.UTC));
        }
        return r;
    }
}

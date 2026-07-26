package fpt.qn.mes.user.infrastructure.persistence;

import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.UsersRecord;
import fpt.qn.mes.user.domain.entities.User;

@Component
public class UserRecordMapper {

    public User toDomain(UsersRecord r) {
        return null;
    }

    public UsersRecord toRecord(User u) {
        return null;
    }
}

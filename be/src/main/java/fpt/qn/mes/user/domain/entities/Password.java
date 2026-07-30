package fpt.qn.mes.user.domain.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Password {

    String encodedValue;

    public static Password ofEncoded(String encoded) {
        return new Password(encoded);
    }
}

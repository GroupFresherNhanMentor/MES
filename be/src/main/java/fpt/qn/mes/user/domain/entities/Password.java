package fpt.qn.mes.user.domain.entities;

public record Password(String encodedValue) {
    public static Password ofEncoded(String encoded) {
        return new Password(encoded);
    }
}

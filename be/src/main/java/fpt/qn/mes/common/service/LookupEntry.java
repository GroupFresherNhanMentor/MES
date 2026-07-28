package fpt.qn.mes.common.service;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class LookupEntry {
    UUID id;
    String name;
    String description;
}

package fpt.qn.mes.common.infrastructure.json;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fpt.qn.mes.common.exception.InternalServerErrorException;
import fpt.qn.mes.common.port.out.JsonSerializerPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JacksonJsonSerializerAdapter implements JsonSerializerPort {

    ObjectMapper objectMapper;

    @Override
    public String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException("JSON serialization failed: " + e.getOriginalMessage());
        }
    }
}

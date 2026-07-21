package org.mini_lab.file_upload_service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JacksonUtils {

    private final ObjectMapper objectMapper;

    public <T> T convertFromJson(
            String json,
            TypeReference<T> typeReference
    ) throws JsonProcessingException {
        return objectMapper.readValue(json, typeReference);
    }

    public <T> String convertObjectToJson(T object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }
}

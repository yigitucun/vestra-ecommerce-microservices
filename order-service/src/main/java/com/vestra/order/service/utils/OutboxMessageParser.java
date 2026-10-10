package com.vestra.order.service.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class OutboxMessageParser {

    private final ObjectMapper mapper;

    public <T> T parse(String rawMessage, Class<T> targetType) {
        String innerJson = mapper.readValue(rawMessage, String.class);
        return mapper.readValue(innerJson, targetType);
    }
}

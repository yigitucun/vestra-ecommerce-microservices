package com.vestra.auth.service.service;

import com.vestra.auth.service.entity.OutboxEvent;
import com.vestra.auth.service.repository.OutboxEventRepository;
import com.vestra.common.dto.OutboxEventDTO;
import com.vestra.common.event.payloads.ResetPasswordPayload;
import com.vestra.common.event.payloads.UserRegisteredPayload;
import com.vestra.common.event.types.UserEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository eventRepository;
    private final ObjectMapper objectMapper;

    private void save(OutboxEventDTO event){
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventType(event.eventType())
                .aggregateType(event.aggregateType())
                .aggregateId(event.aggregateId())
                .payload(objectMapper.writeValueAsString(event.payload()))
                .build();
        eventRepository.save(outboxEvent);
    }

    public void registeredEvent(String aggregateId,String fullName,String email){
        OutboxEventDTO eventDTO = OutboxEventDTO.builder()
                .aggregateId(aggregateId)
                .aggregateType("User")
                .eventType(UserEvents.USER_REGISTERED)
                .payload(new UserRegisteredPayload(fullName,email))
                .build();
        save(eventDTO);
    }

    public void forgotPasswordEvent(String aggregateId,String token,String email){
        OutboxEventDTO eventDTO = OutboxEventDTO.builder()
                .aggregateId(aggregateId)
                .aggregateType("User")
                .eventType(UserEvents.PASSWORD_RESET)
                .payload(new ResetPasswordPayload(email,token))
                .build();
        save(eventDTO);
    }



}

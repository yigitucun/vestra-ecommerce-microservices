package com.vestra.notification.service.consumer;

import com.vestra.notification.service.service.notifier.PasswordResetNotifier;
import com.vestra.notification.service.service.notifier.WelcomeUserNotifier;
import com.vestra.notification.service.utils.OutboxMessageParser;
import com.vestra.common.event.payloads.ResetPasswordPayload;
import com.vestra.common.event.payloads.UserRegisteredPayload;
import com.vestra.common.event.types.UserEvents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;


@RequiredArgsConstructor
@Service
@Slf4j
public class NotificationConsumer {

    private final OutboxMessageParser messageParser;
    private final PasswordResetNotifier passwordResetNotifier;
    private final WelcomeUserNotifier welcomeUserNotifier;

    @KafkaListener(topics = "user.events",groupId = "notification-group")
    public void consume(String message, @Header(name = "event_type",required = false) byte[] eventTypeHeader){
        if (eventTypeHeader == null){
            log.warn("Event type header eksik!");
            return;
        }
        String eventType = new String(eventTypeHeader, StandardCharsets.UTF_8);
        try{
            switch (eventType){
                case UserEvents.PASSWORD_RESET -> handlePasswordReset(message);
                case UserEvents.USER_REGISTERED ->  handleWelcomeMail(message);
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }

    private void handlePasswordReset(String message){
        ResetPasswordPayload event = messageParser.parse(message,ResetPasswordPayload.class);
        passwordResetNotifier.notify(event);
    }

    private void handleWelcomeMail(String message){
        UserRegisteredPayload event = messageParser.parse(message,UserRegisteredPayload.class);
        welcomeUserNotifier.notify(event);
    }



}


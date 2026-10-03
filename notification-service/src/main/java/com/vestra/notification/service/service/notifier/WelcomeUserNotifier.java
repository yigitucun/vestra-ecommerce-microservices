package com.vestra.notification.service.service.notifier;

import com.vestra.notification.service.sender.NotificationSender;
import com.vestra.notification.service.template.TemplateRenderer;
import com.vestra.common.event.payloads.UserRegisteredPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class WelcomeUserNotifier {
    private final NotificationSender sender;
    private final TemplateRenderer templateRenderer;
    @Value("${app.client-url}")
    private String clientUrl;

    public void notify(UserRegisteredPayload event){
        String loginLink = clientUrl + "/auth/login";
        String html = templateRenderer.render("welcome", Map.of("fullName",event.fullName(),"loginLink",loginLink));
        sender.send(event.email(),"Vestraya Hoşgeldin!",html);
    }

}

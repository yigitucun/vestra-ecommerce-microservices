package com.vestra.notification.service.service.notifier;

import com.vestra.notification.service.sender.NotificationSender;
import com.vestra.notification.service.template.TemplateRenderer;
import com.vestra.common.event.payloads.ResetPasswordPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PasswordResetNotifier {

    private final NotificationSender notificationSender;
    private final TemplateRenderer templateRenderer;

    @Value("${app.client-url}")
    private String clientUrl;

    public void notify(ResetPasswordPayload event){
        String resetLink = clientUrl + "/auth/reset-password?token="+event.token();
        System.out.println(event.email());
        String html = templateRenderer.render("password-reset",Map.of("resetLink",resetLink));
        notificationSender.send(event.email(),"Vesta - Şifre sıfırlama talebi",html);
    }


}

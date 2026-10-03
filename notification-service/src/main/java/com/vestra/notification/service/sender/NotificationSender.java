package com.vestra.notification.service.sender;

public interface NotificationSender {
    void send(String to,String subject,String htmlBody);
}

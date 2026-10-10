package com.vestra.notification.service.service.notifier;

import com.vestra.common.event.payloads.OrderCreatedPayload;
import com.vestra.notification.service.sender.NotificationSender;
import com.vestra.notification.service.template.TemplateRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
@Slf4j
public class OrderConfirmationNotifier {

    private final NotificationSender sender;
    private final TemplateRenderer templateRenderer;

    @Value("${app.client-url}")
    private String clientUrl;

    public void notify(OrderCreatedPayload event) {
        log.info("Sipariş onay e-postası hazırlanıyor: orderId={}, orderNumber={}, to={}",
                event.orderId(), event.orderNumber(), event.customerEmail());

        String orderLink = clientUrl + "/orders/" + event.orderId();

        Map<String, Object> variables = new HashMap<>();
        variables.put("orderNumber", event.orderNumber());
        variables.put("totalAmount", event.totalAmount());
        variables.put("shippingAddress", event.shippingAddress());
        variables.put("items", event.items());
        variables.put("orderLink", orderLink);

        String html = templateRenderer.render("order-confirmation", variables);
        sender.send(event.customerEmail(), "Vestra - Siparişiniz Alındı! (" + event.orderNumber() + ")", html);

        log.info("Sipariş onay e-postası başarıyla gönderildi: orderNumber={}, to={}",
                event.orderNumber(), event.customerEmail());
    }
}

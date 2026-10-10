package com.vestra.notification.service.service.notifier;

import com.vestra.common.event.payloads.OrderCreatedPayload;
import com.vestra.notification.service.sender.NotificationSender;
import com.vestra.notification.service.template.TemplateRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderConfirmationNotifierTest {

    @Mock
    private NotificationSender sender;

    @Mock
    private TemplateRenderer templateRenderer;

    @InjectMocks
    private OrderConfirmationNotifier notifier;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notifier, "clientUrl", "http://localhost:3000");
    }

    @Test
    void shouldRenderTemplateAndSendOrderConfirmationEmail() {
        OrderCreatedPayload payload = new OrderCreatedPayload(
                "order-123",
                "ORD-999",
                "user-456",
                "musteri@vestra.com",
                BigDecimal.valueOf(2500),
                "Kadıköy, İstanbul",
                List.of(new OrderCreatedPayload.OrderItemPayload(
                        "var-1",
                        "Laptop Çantası",
                        "BAG-01",
                        1,
                        BigDecimal.valueOf(2500)
                ))
        );

        when(templateRenderer.render(eq("order-confirmation"), anyMap()))
                .thenReturn("<html>Sipariş Onayı</html>");

        notifier.notify(payload);

        verify(templateRenderer).render(eq("order-confirmation"), anyMap());
        verify(sender).send(
                eq("musteri@vestra.com"),
                contains("ORD-999"),
                eq("<html>Sipariş Onayı</html>")
        );
    }
}

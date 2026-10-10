package com.vestra.gateway.service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "services")
@Getter
@Setter
public class ServiceUrisConfig {
    private String authServiceUri;
    private String notificationServiceUri;
    private String productServiceUri;
    private String itemServiceUri;
    private String orderServiceUri;
    private String paymentServiceUri;
}

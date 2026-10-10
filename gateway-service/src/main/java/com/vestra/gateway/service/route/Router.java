package com.vestra.gateway.service.route;


import com.vestra.gateway.service.config.ServiceUrisConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class Router {

    private final ServiceUrisConfig urisConfig;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder){
        return builder.routes()
                .route("auth-public-route",r -> r
                        .path("/api/auth/**")
                        .uri(urisConfig.getAuthServiceUri())
                )
                .route("auth-oauth2-route", r -> r
                        .path("/login/oauth2/**", "/oauth2/**")
                        .uri(urisConfig.getAuthServiceUri())
                )
                .route("product-public-route",r -> r
                        .path("/api/products/**")
                        .uri(urisConfig.getProductServiceUri())
                )

                .route("category-public-route",r -> r
                        .path("/api/categories/**")
                        .uri(urisConfig.getProductServiceUri())
                )
                .route("category-admin-route",r -> r
                        .path("/api/admin/categories/**")
                        .uri(urisConfig.getProductServiceUri())
                )

                .route("user-admin-route", r -> r
                        .path("/api/admin/users/**")
                        .uri(urisConfig.getAuthServiceUri())
                )
                .route("user-public-route", r -> r
                        .path("/api/users/**")
                        .uri(urisConfig.getAuthServiceUri())
                )

                .route("product-admin-route",r -> r
                        .path("/api/admin/products/**")
                        .uri(urisConfig.getProductServiceUri())
                )
                .route("item-public-route", r -> r
                        .path("/api/items/**")
                        .uri(urisConfig.getItemServiceUri())
                )
                .route("item-admin-route", r -> r
                        .path("/api/admin/items/**")
                        .uri(urisConfig.getItemServiceUri())
                )
                .route("order-public-route", r -> r
                        .path("/api/orders/**")
                        .uri(urisConfig.getOrderServiceUri())
                )
                .route("order-admin-route", r -> r
                        .path("/api/admin/orders/**")
                        .uri(urisConfig.getOrderServiceUri())
                )
                .route("payment-public-route", r -> r
                        .path("/api/payments/**")
                        .uri(urisConfig.getPaymentServiceUri())
                )
                .route("payment-admin-route", r -> r
                        .path("/api/admin/payments/**")
                        .uri(urisConfig.getPaymentServiceUri())
                )
                .build();
    }

}

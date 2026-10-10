package com.vestra.order.service.entity;

public enum OrderStatus {
    PENDING,
    STOCK_CONFIRMED,
    STOCK_FAILED,
    PAYMENT_PENDING,
    PAID,
    PAYMENT_FAILED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    COMPLETED
}

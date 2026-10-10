package com.vestra.common.event.types;

public final class PaymentEvents {
    private PaymentEvents() {}

    public static final String PAYMENT_COMPLETED = "PaymentCompleted";
    public static final String PAYMENT_FAILED = "PaymentFailed";
    public static final String PAYMENT_REFUNDED = "PaymentRefunded";
}

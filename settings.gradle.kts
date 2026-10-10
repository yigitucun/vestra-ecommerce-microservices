rootProject.name="vestra"

include(
    "auth-service",
    "notification-service",
    "product-service",
    "gateway-service",
    "item-service",
    "order-service",
    "payment-service"
)
include("common")
include("common-web")
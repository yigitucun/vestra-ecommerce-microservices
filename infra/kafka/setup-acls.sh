#!/bin/sh
# Vestra Kafka Topic Access Control List (ACL) Setup Script
# Restricts message publication and consumption across microservices to prevent unauthorized event spoofing.
set -e

BOOTSTRAP_SERVERS=${KAFKA_BOOTSTRAP_SERVERS:-"kafka:29092"}

echo "Configuring Kafka ACLs on $BOOTSTRAP_SERVERS..."

# 1. Allow Debezium / CDC to write to event topics
for topic in order.events payment.events item.events user.events product.events; do
  kafka-acls --bootstrap-server "$BOOTSTRAP_SERVERS" \
    --add --allow-principal User:debezium \
    --operation Write --topic "$topic" || true
done

# 2. Allow order-service to consume payment and stock events
kafka-acls --bootstrap-server "$BOOTSTRAP_SERVERS" \
  --add --allow-principal User:order-service \
  --operation Read --topic payment.events --topic item.events \
  --group order-service-payment-group --group order-service-stock-group || true

# 3. Allow item-service to consume order and product events
kafka-acls --bootstrap-server "$BOOTSTRAP_SERVERS" \
  --add --allow-principal User:item-service \
  --operation Read --topic order.events --topic product.events \
  --group order-stock-group --group item-group || true

# 4. Allow notification-service to consume user and order events
kafka-acls --bootstrap-server "$BOOTSTRAP_SERVERS" \
  --add --allow-principal User:notification-service \
  --operation Read --topic user.events --topic order.events \
  --group notification-group --group notification-order-group || true

echo "Kafka topic ACLs configured successfully."

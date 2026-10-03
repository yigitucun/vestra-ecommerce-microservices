package com.vestra.common.dto;


public record OutboxEventDTO(
        String aggregateType,
        String aggregateId,
        String eventType,
        Object payload
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String aggregateType;
        private String aggregateId;
        private String eventType;
        private Object payload;

        public Builder aggregateType(String aggregateType) {
            this.aggregateType = aggregateType;
            return this;
        }

        public Builder aggregateId(String aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }

        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder payload(Object payload) {
            this.payload = payload;
            return this;
        }

        public OutboxEventDTO build() {
            return new OutboxEventDTO(
                    aggregateType,
                    aggregateId,
                    eventType,
                    payload
            );
        }
    }
}
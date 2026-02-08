package com.opticstore.notifications.model;

public enum NotificationType {
    ORDER_CREATED("Order Created"),
    ORDER_STATUS_CHANGED("Order Status Changed"),
    PAYMENT_RECEIVED("Payment Received"),
    LOW_STOCK("Low Stock Alert"),
    NEW_CUSTOMER("New Customer"),
    SYSTEM_ALERT("System Alert");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return this.name(); // Returns "ORDER_CREATED", "ORDER_STATUS_CHANGED", etc.
    }
}
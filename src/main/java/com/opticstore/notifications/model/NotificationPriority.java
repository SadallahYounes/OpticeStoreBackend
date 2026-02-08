package com.opticstore.notifications.model;

public enum NotificationPriority {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    URGENT("Urgent");

    private final String displayName;

    NotificationPriority(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return this.name(); // Returns "LOW", "MEDIUM", "HIGH", "URGENT"
    }
}
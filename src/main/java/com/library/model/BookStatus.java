package com.library.model;

public enum BookStatus {
    ACTIVE("Активно"),
    INACTIVE("Неактивно"),
    COMPLETED("Завершено"),
    IN_PROGRESS("В процессе"),
    OVERDUE("Просрочено");

    private final String displayName;

    BookStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
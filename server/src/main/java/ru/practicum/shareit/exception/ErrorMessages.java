package ru.practicum.shareit.exception;

import lombok.Getter;

@Getter
public enum ErrorMessages {

    USER_NOT_FOUND("user with id=%d not found"),
    REQUEST_NOT_FOUND("request with id=%d not found"),
    ITEM_NOT_FOUND("item with id=%d not found"),
    BOOKING_NOT_FOUND("booking with id=%d not found"),
    BOOKER_CANNOT_BE_OWNER("booker cannot be a owner"),
    STATUS_APPROVED("status already approved"),
    OWNER_UPDATE("only owner can update item"),
    OWNER_DELETE("only owner can delete item"),
    REVIEW_WITHOUT_BOOKING("you cannot create a review without booking");

    private final String message;

    ErrorMessages(String message) {
        this.message = message;
    }

    public String getFormatMessage(long arg) {
        return String.format(message, arg);
    }

    public String getFormatMessage(String arg) {
        return String.format(message, arg);
    }
}

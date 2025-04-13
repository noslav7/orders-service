package ru.javaops.cloudjava.ordersservice.storage.model;

import org.springframework.http.HttpStatus;
import ru.javaops.cloudjava.ordersservice.exception.OrderServiceException;

public enum OrderStatus {
    NEW,
    ACCEPTED,
    REJECTED;

    public static OrderStatus fromString(String str) {
        try {
            return OrderStatus.valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            var msg = "Failed to create OrderStatus from string: %s".formatted(str);
            throw new OrderServiceException(msg, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
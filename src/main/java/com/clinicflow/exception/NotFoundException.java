package com.clinicflow.exception;

/** Thrown when a requested resource doesn't exist → HTTP 404. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}

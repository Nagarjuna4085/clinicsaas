package com.clinicflow.exception;

/** Thrown for invalid business input → HTTP 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}

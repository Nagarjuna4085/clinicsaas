package com.clinicflow.exception;

/** Thrown when a client exceeds an allowed request rate → HTTP 429. */
public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}

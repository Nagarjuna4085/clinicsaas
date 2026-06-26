package com.clinicflow.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.assertj.core.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void notFoundMapsTo404() {
        ResponseEntity<GlobalExceptionHandler.ApiError> r = handler.handleNotFound(new NotFoundException("missing"));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(r.getBody().error()).isEqualTo("missing");
        assertThat(r.getBody().status()).isEqualTo(404);
    }

    @Test
    void badRequestMapsTo400() {
        ResponseEntity<GlobalExceptionHandler.ApiError> r = handler.handleBadRequest(new BadRequestException("bad"));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(r.getBody().error()).isEqualTo("bad");
    }

    @Test
    void rateLimitMapsTo429() {
        ResponseEntity<GlobalExceptionHandler.ApiError> r = handler.handleRateLimit(new RateLimitExceededException("slow down"));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void genericMapsTo500WithoutLeakingDetail() {
        ResponseEntity<GlobalExceptionHandler.ApiError> r = handler.handleGeneric(new RuntimeException("npe details"));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(r.getBody().error()).isEqualTo("Something went wrong");
    }
}

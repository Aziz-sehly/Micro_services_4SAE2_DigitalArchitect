package com.esprit.microservice_proposal.Controller;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Error";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (msg.contains("not found")) {
            status = HttpStatus.NOT_FOUND;
        } else if (msg.contains("Unauthorized")) {
            status = HttpStatus.FORBIDDEN;
        }
        return ResponseEntity.status(status).body(Map.of("error", msg));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, String>> handleFeign(FeignException ex) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", "Upstream service error");
        body.put("detail", ex.getMessage());
        body.put("httpStatus", String.valueOf(ex.status()));
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }
}

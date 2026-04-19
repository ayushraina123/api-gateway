package com.ayush.gateway.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/fallback")
public class FallbackController {
    private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

    @RequestMapping
    public ResponseEntity<ProblemDetail> fallback() {
        log.warn("Gateway fallback triggered for devotee-service");

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);

        problemDetail.setTitle("Service Unavailable");
        problemDetail.setDetail("Devotee service is currently unavailable");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problemDetail);
    }
}

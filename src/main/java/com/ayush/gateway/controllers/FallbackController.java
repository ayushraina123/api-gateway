package com.ayush.gateway.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/fallback")
public class FallbackController {

    @RequestMapping
    public ResponseEntity<ProblemDetail> fallback() {

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);

        problemDetail.setTitle("Service Unavailable");
        problemDetail.setDetail("Devotee service is currently unavailable");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problemDetail);
    }
}

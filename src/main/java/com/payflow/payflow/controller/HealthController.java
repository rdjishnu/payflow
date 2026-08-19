package com.payflow.payflow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Check", description = "System health monitoring endpoints")
public class HealthController {

    @GetMapping
    @Operation(summary = "Check API status", description = "Returns 200 OK if the system is up and running.")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("PayFlow API is running smoothly! 🚀");
    }
}
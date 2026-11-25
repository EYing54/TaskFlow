package com.taskflow.backend.dto;

public record LoginRequest(
        String username,
        String password) {
}
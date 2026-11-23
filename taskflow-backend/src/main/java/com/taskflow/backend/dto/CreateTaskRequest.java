package com.taskflow.backend.dto;

import java.time.LocalDate;

public record CreateTaskRequest(
        String title,
        String description,
        Integer priority,
        String category,
        LocalDate dueDate) {
}
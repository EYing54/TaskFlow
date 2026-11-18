package com.taskflow.backend.controller;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskflow.backend.service.TaskService;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://127.0.0.1:5173"
}, allowCredentials = "true")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<?> getTasks(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("userId") instanceof Long userId)) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "请先登录"));
        }

        return ResponseEntity.ok(
                taskService.getTasksByUserId(userId));
    }
}

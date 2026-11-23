package com.taskflow.backend.controller;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskflow.backend.dto.CreateTaskRequest;
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
    public ResponseEntity<?> getTasks(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);

        if (session == null || !(session.getAttribute("userId") instanceof Long userId)) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        return ResponseEntity.status(200).body(taskService.getTasksByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody CreateTaskRequest taskRequest, HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);

        if (session == null || !(session.getAttribute("userId") instanceof Long userId)) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        if (taskRequest.title() == null || taskRequest.title().isBlank()) {
            return ResponseEntity.status(400).body(Map.of("message", "任务标题不能为空"));
        }
        if (taskRequest.title().trim().length() > 100) {
            return ResponseEntity.status(400).body(Map.of("message", "任务标题不能超过100个字符"));
        }
        if (taskRequest.priority() != null && (taskRequest.priority() < 1 || taskRequest.priority() > 3)) {
            return ResponseEntity.status(400).body(Map.of("message", "任务优先级无效"));
        }
        if (taskRequest.category() != null && taskRequest.category().length() > 30) {
            return ResponseEntity.status(400).body(Map.of("message", "分类不能超过30个字符"));
        }
        return ResponseEntity.status(201).body(taskService.createTask(userId, taskRequest));
    }

}

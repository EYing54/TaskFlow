package com.taskflow.backend.controller;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskflow.backend.dto.CreateTaskRequest;
import com.taskflow.backend.dto.UpdateTaskStatusRequest;
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "请先登录"));
        }

        return ResponseEntity.ok(taskService.getTasksByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<?> createTask(
            @RequestBody CreateTaskRequest taskRequest,
            HttpServletRequest httpRequest) {

        HttpSession session = httpRequest.getSession(false);

        if (session == null || !(session.getAttribute("userId") instanceof Long userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "请先登录"));
        }

        if (taskRequest.title() == null || taskRequest.title().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "任务标题不能为空"));
        }

        if (taskRequest.title().trim().length() > 100) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "任务标题不能超过100个字符"));
        }

        if (taskRequest.priority() != null
                && (taskRequest.priority() < 1 || taskRequest.priority() > 3)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "任务优先级无效"));
        }

        if (taskRequest.category() != null && taskRequest.category().length() > 30) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "分类不能超过30个字符"));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(userId, taskRequest));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateTaskStatus(
            @PathVariable("id") Long taskId,
            @RequestBody UpdateTaskStatusRequest taskRequest,
            HttpServletRequest httpRequest) {

        HttpSession session = httpRequest.getSession(false);

        if (session == null || !(session.getAttribute("userId") instanceof Long userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "请先登录"));
        }

        Integer status = taskRequest.status();

        if (status == null || status < 0 || status > 2) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "任务状态无效"));
        }

        if (!taskService.updateTaskStatus(userId, taskId, status)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "任务不存在或无权限修改"));
        }

        return ResponseEntity.ok(Map.of("message", "任务状态修改成功"));
    }
}
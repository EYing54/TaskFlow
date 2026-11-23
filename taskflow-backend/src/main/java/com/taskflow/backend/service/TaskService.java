package com.taskflow.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taskflow.backend.dto.CreateTaskRequest;
import com.taskflow.backend.entity.Task;
import com.taskflow.backend.mapper.TaskMapper;

@Service
public class TaskService {

    private final TaskMapper taskMapper;

    public TaskService(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    public List<Task> getTasksByUserId(Long userId) {
        LambdaQueryWrapper<Task> query = new LambdaQueryWrapper<>();

        query.eq(Task::getUserId, userId)
                .orderByDesc(Task::getCreateTime);

        return taskMapper.selectList(query);
    }

    public Task createTask(Long userId, CreateTaskRequest request) {
        Task task = new Task();

        task.setUserId(userId);
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setPriority(request.priority() == null ? 2 : request.priority());
        task.setStatus(0);
        task.setCategory(request.category() == null || request.category().isBlank()
                ? "学习"
                : request.category().trim());
        task.setDueDate(request.dueDate());
        task.setSource(1);

        taskMapper.insert(task);

        return task;
    }
}

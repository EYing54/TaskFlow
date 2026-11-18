package com.taskflow.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
}

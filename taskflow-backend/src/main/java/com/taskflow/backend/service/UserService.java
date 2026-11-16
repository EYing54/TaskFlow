package com.taskflow.backend.service;

import org.springframework.stereotype.Service;

import com.taskflow.backend.mapper.UserMapper;

@Service
public class UserService {
    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
}

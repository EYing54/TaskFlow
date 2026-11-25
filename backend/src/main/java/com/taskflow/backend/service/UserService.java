package com.taskflow.backend.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.mapper.UserMapper;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User findByUsername(String username) {
        LambdaQueryWrapper<User> queryWapper = new LambdaQueryWrapper<>();
        queryWapper.eq(User::getUsername, username);
        return userMapper.selectOne(queryWapper);
    }

    public boolean login(String username, String password) {
        User user = findByUsername(username);
        if (user == null) {
            return false;
        }
        return passwordEncoder.matches(password, user.getPassword());
    }
}

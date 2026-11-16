package com.taskflow.backend.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskflow.backend.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}

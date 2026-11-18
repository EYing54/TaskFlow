package com.taskflow.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskflow.backend.entity.Task;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
}

package com.tm.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tm.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}

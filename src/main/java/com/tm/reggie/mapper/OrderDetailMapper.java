package com.tm.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tm.reggie.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
}

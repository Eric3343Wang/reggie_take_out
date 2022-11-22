package com.tm.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tm.reggie.entity.Orders;

public interface OrderService extends IService<Orders> {
    void submit(Orders orders);
}

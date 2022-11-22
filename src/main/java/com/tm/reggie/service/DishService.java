package com.tm.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tm.reggie.DTO.DishDto;
import com.tm.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表
    public void saveWithFlavor(DishDto dishDto);

    DishDto getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDto dishDto);
}

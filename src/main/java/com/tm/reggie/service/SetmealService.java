package com.tm.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tm.reggie.DTO.SetmealDto;
import com.tm.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    void saveWithDish(SetmealDto setmealDto);

    void removeWithDish(List<Long> ids);

    void updateWithDish(SetmealDto setmealDto);
}

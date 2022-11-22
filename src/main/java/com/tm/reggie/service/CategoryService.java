package com.tm.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tm.reggie.entity.Category;

public interface CategoryService extends IService<Category> {
    public void remove(Long ids);
}

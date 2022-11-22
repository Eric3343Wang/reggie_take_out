package com.tm.reggie.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tm.reggie.common.CustomException;
import com.tm.reggie.entity.Category;
import com.tm.reggie.entity.Dish;
import com.tm.reggie.entity.Setmeal;
import com.tm.reggie.mapper.CategoryMapper;
import com.tm.reggie.service.CategoryService;
import com.tm.reggie.service.DishService;
import com.tm.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类，删除之前查看是否有关联的菜品和套餐
     * @param ids
     */
    @Override
    public void remove(Long ids) {
        LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据分类id进行查询
        dishQueryWrapper.eq(Dish::getCategoryId,ids);
        //查询当前分类下是否关联菜品，如果已经关联，抛出一个异常
        int count1 = dishService.count(dishQueryWrapper);
        if(count1 > 0){
            //已关联菜品，抛出异常
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }

        LambdaQueryWrapper<Setmeal> setmealQueryWrapper = new LambdaQueryWrapper<>();
        setmealQueryWrapper.eq(Setmeal::getCategoryId,ids);
        //查询当前分类下是否关联套餐，如果已经关联，抛出一个异常
        int count2 = setmealService.count(setmealQueryWrapper);
        if(count2 > 0){
            //已关联套餐，抛出异常
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }

        //正常删除
        super.removeById(ids);
    }
}

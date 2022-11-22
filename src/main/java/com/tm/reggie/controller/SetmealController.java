package com.tm.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tm.reggie.DTO.SetmealDto;
import com.tm.reggie.common.R;
import com.tm.reggie.entity.Category;
import com.tm.reggie.entity.Setmeal;
import com.tm.reggie.entity.SetmealDish;
import com.tm.reggie.service.CategoryService;
import com.tm.reggie.service.SetmealDishService;
import com.tm.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired

    private SetmealDishService setmealDishService;
    /**
     * 套餐分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //创建page对象
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //查询条件
        setmealLambdaQueryWrapper.eq(name != null, Setmeal::getName, name);
        //排序条件
        setmealLambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //查询
        setmealService.page(setmealPage, setmealLambdaQueryWrapper);

        //拷贝除records以外得所有属性
        BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");
        //取出查询结果集
        List<Setmeal> records = setmealPage.getRecords();
        List<SetmealDto> collect = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                setmealDto.setCategoryName(category.getName());
            }
            return setmealDto;
        }).collect(Collectors.toList());
        setmealDtoPage.setRecords(collect);
        return R.success(setmealDtoPage);
    }

    /**
     * 新增套餐
     *
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        setmealService.saveWithDish(setmealDto);
        return R.success("新增成功");
    }

    /**
     * 单个或批量删除套餐
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids) {
        setmealService.removeWithDish(ids);
        return R.success("删除成功");
    }

    /**
     * 根据条件查询套餐数据
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key="#setmeal.categoryId+'_'+#setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        setmealLambdaQueryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        setmealLambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(setmealLambdaQueryWrapper);
        return R.success(list);
    }

    /**
     * 回显，根据id查询
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id){
        //创建setmealDto
        SetmealDto setmealDto = new SetmealDto();
        //查询套餐信息
        Setmeal setmeal = setmealService.getById(id);
        //复制给DTO
        BeanUtils.copyProperties(setmeal,setmealDto);
        //查询套餐详细信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> dishList = setmealDishService.list(queryWrapper);
        //查询套餐分类名
        Category category = categoryService.getById(setmeal.getCategoryId());
        //赋给创建setmealDto
        setmealDto.setSetmealDishes(dishList);
        setmealDto.setCategoryName(category.getName());
        return R.success(setmealDto);
    }

    /**
     * 修改套餐
     * @param setmealDto
     * @return
     */
    @PutMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithDish(setmealDto);
        return R.success("修改成功");
    }
}

package com.tm.reggie.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tm.reggie.DTO.DishDto;
import com.tm.reggie.common.R;
import com.tm.reggie.entity.Category;
import com.tm.reggie.entity.Dish;
import com.tm.reggie.entity.DishFlavor;
import com.tm.reggie.service.CategoryService;
import com.tm.reggie.service.DishFlavorService;
import com.tm.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 菜品分页查询
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("当前页：{}，每页条数：{}，名称条件：{}",page,pageSize,name);
        //分页构造器
        Page<Dish> dishPage = new Page<>(page, pageSize);
        //为了获取菜品分类名称，创建一个page,并将属性复制
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //查询条件
        dishLambdaQueryWrapper.like(StringUtils.isNotEmpty(name),Dish::getName,name);
        //排序条件
        dishLambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);

        //查询
        dishService.page(dishPage,dishLambdaQueryWrapper);

        //拷贝属性,不拷贝records属性
        BeanUtils.copyProperties(dishPage,dishDtoPage,"records");

        List<Dish> records = dishPage.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //菜品的分类Id
            Long categoryId = item.getCategoryId();
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //将dish的属性拷贝到dishDto
            BeanUtils.copyProperties(item,dishDto);
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        dishService.saveWithFlavor(dishDto);
        //清理redis菜品缓存
        clearRedis(dishDto.getCategoryId());
        return R.success("新增成功");
    }

    /**
     * 编辑菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        //清理redis菜品缓存
        clearRedis(dishDto.getCategoryId());
        return R.success("修改成功");
    }

    /**
     * //清理redis菜品缓存
     * @param categoryId
     */
    private void clearRedis(Long categoryId) {
        String key = "dish_" + categoryId +"_"+ 1;
        redisTemplate.delete(key);
        log.info("已清除redis:{}",key);
    }

    /**
     * 编辑回显信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        log.info("id:{}",id);
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 新增套餐时，显示菜品分类信息
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        List<DishDto> dishDtoList = null;
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        //从redis中查找数据
        String value = (String) redisTemplate.opsForValue().get(key);
        dishDtoList = (List<DishDto>)JSONArray.parse(value);
        //判断是否存在
        if(dishDtoList != null)
            //存在则直接返回，否则从数据库查询并放入redis
            return R.success(dishDtoList);


        //条件构造器
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //分类条件
        dishLambdaQueryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        //过滤停售条件
        dishLambdaQueryWrapper.eq(Dish::getStatus,1);
        //名称条件
        dishLambdaQueryWrapper.like(dish.getName() != null,Dish::getName,dish.getName());
        //排序条件
        dishLambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //查询
        List<Dish> list = dishService.list(dishLambdaQueryWrapper);

        dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            //菜品的分类Id
            Long categoryId = item.getCategoryId();
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DishFlavor::getDishId,item.getId());
            List<DishFlavor> dishFlavorList = dishFlavorService.list(queryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());
        //放入redis
        redisTemplate.opsForValue().set(key, JSONObject.toJSONString(dishDtoList),60, TimeUnit.MINUTES);
        return R.success(dishDtoList);
    }
}

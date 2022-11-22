package com.tm.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tm.reggie.common.BaseContext;
import com.tm.reggie.common.R;
import com.tm.reggie.entity.ShoppingCart;
import com.tm.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 查询购物车
     * @param
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> shoppingCartQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartQueryWrapper.eq(ShoppingCart::getUserId,currentId);
        shoppingCartQueryWrapper.orderByDesc(ShoppingCart::getUpdateTime);

        List<ShoppingCart> list = shoppingCartService.list(shoppingCartQueryWrapper);
        return R.success(list);
    }

    /**
     * 加入购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        //设置用户ID
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        //条件构造器
        LambdaQueryWrapper<ShoppingCart> shoppingCartQueryWrapper = new LambdaQueryWrapper();
        shoppingCartQueryWrapper.eq(shoppingCart.getUserId() != null, ShoppingCart::getUserId, shoppingCart.getUserId());
        //查询当前菜品是否已经添加到购物车
        Long dishId = shoppingCart.getDishId();
        if(dishId != null) {
            shoppingCartQueryWrapper.eq(ShoppingCart::getDishId, dishId);
        }else {
            shoppingCartQueryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart cartServiceOne = shoppingCartService.getOne(shoppingCartQueryWrapper);

        if(cartServiceOne != null){
            //如果已存在则份数加1
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        }else {
            //不存在则新增数据,手动设置number
            shoppingCart.setNumber(1);
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }
        return R.success(cartServiceOne);
    }

    /**
     * 购物车菜品套餐减一
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        //获取用户ID
        Long userId = BaseContext.getCurrentId();

        //构建条件
        LambdaQueryWrapper<ShoppingCart> shoppingCartQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartQueryWrapper.eq(ShoppingCart::getUserId,userId);

        //判断是菜品还是套餐
        Long dishId = shoppingCart.getDishId();
        if(dishId != null){
            shoppingCartQueryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else {
            shoppingCartQueryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        //查询购物车现有份数
        ShoppingCart cartServiceOne = shoppingCartService.getOne(shoppingCartQueryWrapper);
        
        if(cartServiceOne.getNumber() > 0){
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number - 1);
            shoppingCartService.updateById(cartServiceOne);
            return R.success(cartServiceOne);
        }

        //如果份数为0，将该菜品套餐移出购物车
        shoppingCartService.updateById(cartServiceOne);
        return R.error("已移出购物车");
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        //获取用户ID
        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> shoppingCartQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartQueryWrapper.eq(ShoppingCart::getUserId,userId);

        shoppingCartService.remove(shoppingCartQueryWrapper);

        return R.success("已清空购物车");
    }

}

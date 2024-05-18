package com.cc.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cc.common.Result;
import com.cc.pojo.ShoppingCart;
import com.cc.pojo.User;
import com.cc.service.ShoppingCartService;
import com.cc.utils.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 购物车 前端控制器
 * </p>
 *
 * @author cc
 * @since 2022-05-30
 */
@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;


    /**
     * 往购物车内部添加
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public Result<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("购物车数据:{}",shoppingCart.toString());
        //解析一下接受的对象不难发现没有用户ID，所以我们得设置一下用户Id，也就是当前购物车是谁的
        Long userId=BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        //判断当前传来的Id是菜品还是套餐，这两个肯定会有一个是Null
        Long dishId=shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();

        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, userId);

        //动态拼接一下添加的查询条件
        if (dishId!=null){
            //传过来的是菜品而不是套餐
            lambdaQueryWrapper.eq(ShoppingCart::getDishId, dishId);
        }
        if (setmealId!=null){
            //传过来的是套餐而不是菜品
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId, setmealId);
        }
        /*
          SQL:select * from shopping_cart where user_Id=? and dish_Id=?/setmealId=?
          如果可以查出来，说明购物车已经加入了相关菜品
         */
        ShoppingCart cartServiceOne=shoppingCartService.getOne(lambdaQueryWrapper);

        //已经存在在购物车里
        if (cartServiceOne!=null){
            //在数量原有基础上+1
            Integer count = cartServiceOne.getNumber();
            cartServiceOne.setNumber(count + 1);
            /*
                update shopping_cart set number=（更新后数量）
            */
            shoppingCartService.updateById(cartServiceOne);
        }else {
            //尚未存在购物车，就添加到购物车
            shoppingCart.setNumber(1);
            /*
                insert into shopping_cart (ShoppingCart解析出来的字段) values (ShoppingCart解析出来的数据)
            */
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            //因为这个分支的cartServiceOne是null，所以要覆盖一下
            cartServiceOne = shoppingCart;
        }
        return Result.success(cartServiceOne);
    }

    /**
     * @return 购物车列表
     */
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list(){

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseContext.getCurrentId()!=null,ShoppingCart::getUserId, BaseContext.getCurrentId());

        // 最晚下单的 菜品或套餐在购物车中最先展示
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return Result.success(list);
    }

    /**
     * 一次性清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public Result<String> clean(){
        //获取当前购物车用户Id
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, userId);
        shoppingCartService.remove(lambdaQueryWrapper);
        return Result.success("清空成功");
    }

}

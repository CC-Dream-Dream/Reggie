package com.cc.controller;


import com.cc.common.Result;
import com.cc.pojo.Orders;
import com.cc.service.OrdersService;
import com.cc.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author cc
 * @since 2022-05-30
 */
@RestController
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;


    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Orders orders){
        //比较繁琐，在service实现
        ordersService.submit(orders);
        return Result.success("下单成功");
    }

}

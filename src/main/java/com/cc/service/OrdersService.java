package com.cc.service;

import com.cc.pojo.Orders;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author cc
 * @since 2022-05-30
 */
public interface OrdersService extends IService<Orders> {

    public void submit(Orders orders);
}

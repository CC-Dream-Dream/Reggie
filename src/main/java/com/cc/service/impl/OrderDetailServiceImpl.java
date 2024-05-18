package com.cc.service.impl;

import com.cc.pojo.OrderDetail;
import com.cc.mapper.OrderDetailMapper;
import com.cc.service.OrderDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单明细表 服务实现类
 * </p>
 *
 * @author cc
 * @since 2022-05-30
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {

}

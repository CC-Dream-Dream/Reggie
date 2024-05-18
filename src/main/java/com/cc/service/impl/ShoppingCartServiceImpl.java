package com.cc.service.impl;

import com.cc.pojo.ShoppingCart;
import com.cc.mapper.ShoppingCartMapper;
import com.cc.service.ShoppingCartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 购物车 服务实现类
 * </p>
 *
 * @author cc
 * @since 2022-05-30
 */
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

}

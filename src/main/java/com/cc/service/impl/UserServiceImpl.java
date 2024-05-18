package com.cc.service.impl;

import com.cc.pojo.User;
import com.cc.mapper.UserMapper;
import com.cc.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户信息 服务实现类
 * </p>
 *
 * @author cc
 * @since 2022-05-30
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}

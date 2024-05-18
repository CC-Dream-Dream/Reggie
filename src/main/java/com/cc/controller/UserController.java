package com.cc.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cc.common.Result;
import com.cc.dto.UserDto;
import com.cc.pojo.User;
import com.cc.service.UserService;
import com.cc.utils.SMSUtils;
import com.cc.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户信息 前端控制器
 * </p>
 *
 * @author cc
 * @since 2022-05-30
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 验证码发送
     * @param user 接收用户电话号码
     * @param session 把验证码存入session，后续登陆验证要用
     * @return
     */
    @PostMapping("/sendMsg")
    public Result<String> sendMsg(@RequestBody User user, HttpSession session) {
        //获取手机号
        String userPhone = user.getPhone();
        //判断手机号是否为空
        if (StringUtils.isNotEmpty(userPhone)) {
            //利用验证码生成类来生成验证码
            String code = ValidateCodeUtils.generateValidateCode4String(4);
            //这里不太可能去真的发验证码，所以把生成的验证码在后台看一眼就好
            log.info("手机号Phone:{}   验证码Code:{}",userPhone,code);
            //如果要发短信应该出现的代码
            //SMSUtils.sendMessage("外卖", "模板", userPhone, code);
            //把验证码存入Session，验证用，phone为Key，code为value
            //session.setAttribute(userPhone, code);
            //将验证码存入redis，并设置好失效时间为5分钟
            redisTemplate.opsForValue().set(userPhone, code, 5, TimeUnit.MINUTES);

            return Result.success("验证码发送成功，有效时间为5分钟");
        }

        return Result.error("验证码发送失败");
    }


    /**
     * 前台登陆功能
     * @param userDto 对User类进行了扩展，原有user类没有code属性
     * @param codeInSession 从session中拿code（验证码），方便后需验证
     * @return
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody UserDto userDto, HttpSession codeInSession) {
        //拿到验证码和手机号
        String code = userDto.getCode();
        String phone = userDto.getPhone();
        //从session中拿到对应的验证码
        //String tempCode = (String) codeInSession.getAttribute(phone);

        //从Redis中拿验证
        String tempCode = (String) redisTemplate.opsForValue().get(phone);

        //验证码相等
        if (code.equals(tempCode) && codeInSession != null) {
            //是否为新用户，如果是新用户顺手注册了
            LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(User::getPhone, phone);
            //只能用getOne来匹配，不能用getById，因为没有Id给你匹配，都是空的
            User user = userService.getOne(lambdaQueryWrapper);
            if (user==null){
                //用户不存在，注册一下，注册完放行
                //用户的ID是有自动生成策略的，不用管
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            //把用户的ID存入Session，留给过滤器进行验证放行
            //codeInSession.setAttribute("user", user.getId());

            //此时已经登陆成功，向Redis中存入userId的信息留给过滤器进行验证放行
            redisTemplate.opsForValue().set("user", user.getId());
            //再删掉验证码
            redisTemplate.delete(phone);


            return Result.success("登陆成功，欢迎~");
        }
        return Result.error("验证码错误");
    }

}

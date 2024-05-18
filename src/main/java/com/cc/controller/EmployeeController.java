package com.cc.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.common.Result;
import com.cc.pojo.Employee;
import com.cc.service.EmployeeService;
import com.cc.utils.BaseContext;
import com.cc.utils.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * <p>
 * 员工信息 前端控制器
 * </p>
 *
 * @author cc
 * @since 2022-05-30
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    ThreadLocal threadLocal = new ThreadLocal();

    @Autowired
    private EmployeeService employeeService;

    /**
     * @param request 如果登陆成功把对象放入Session中，方便后续拿取
     * @param employee 利用@RequestBody注解来解析前端传来的Json，同时用对象来封装
     * @return
     */
    @PostMapping("/login")
    public Result login(HttpServletRequest request, @RequestBody Employee employee) {
        String password=employee.getPassword();
        String username = employee.getUsername();
        log.info("登陆");
        //MD5加密
        MD5Util md5Util = new MD5Util();
        password=MD5Util.getMD5(password);
        //通过账户查这个员工对象，这里就不走Service层了
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(Employee::getUsername, username);
        Employee empResult=employeeService.getOne(lambdaQueryWrapper);
            //判断用户是否存在
        if (!empResult.getUsername().equals(username)){
            return Result.error("账户不存在");
            //密码是否正确
        }else if (!empResult.getPassword().equals(password)){
            return Result.error("账户密码错误");
            //员工账户状态是否正常，1状态正常，0封禁
        }else if (empResult.getStatus()!=1){
            return Result.error("当前账户正在封禁");
            //状态正常允许登陆
        }else {
            log.info("登陆成功，账户存入session");
            //员工id存入session，
            request.getSession().setAttribute("employee",empResult.getId());
            //引入BaseContext的工具类，将存入session的员工信息拿出来，保存到ThreadLocal下，方便拿不到Request的类获取用户Id
            BaseContext.setCurrentId(empResult.getId());
            //把员工对象存入localStorage作用域
            return Result.success(employee);
        }
    }

    /**
     * @param request 删除request作用域中的session对象，就按登陆的request.getSession().setAttribute("employ",empResult.getId());删除employee就行
     * @return
     */
    @PostMapping("/logout")
    public Result login(HttpServletRequest request) {
        //尝试删除
        try {
            request.getSession().removeAttribute("employee");
        }catch (Exception e){
            //删除失败
            return Result.error("登出失败");
        }
        return Result.success("登出成功");
    }

    /**
     * @param httpServletRequest 获取当前操作人员的session id用
     * @param employee 将员工的数据解析为employee对象
     *                 前端json{name: "", phone: "", sex: "", idNumber: "", username: ""}
     * @return
     */
    @PostMapping("")
    public Result addEmployee(HttpServletRequest httpServletRequest,@RequestBody Employee employee) {
        //设置默认密码，顺手加密了
        employee.setPassword(MD5Util.getMD5("123456"));
        //设置修改时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        //账户默认状态0
        employee.setStatus(0);
        //获取当前新增操作人员的id
        Long empId= (Long) httpServletRequest.getSession().getAttribute("employee");
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);
        //MP自动CRUD的功能，封装好了save方法
        employeeService.save(employee);
        return Result.success("插入成功");
    }

    /**
     * 分页展示员工列表接口、查询某个员工
     * @param page 查询第几页
     * @param pageSize 每页一共几条数据
     * @param name 查询名字=name的数据
     * @return 返回Page页
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize,String name){
        //分页构造器,Page(第几页, 查几条)
        Page pageInfo = new Page(page, pageSize);
        //查询构造器
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper();
        //过滤条件.like(什么条件下启用模糊查询，模糊查询字段，被模糊插叙的名称)
        lambdaQueryWrapper.like(!StringUtils.isEmpty(name), Employee::getName, name);
        //添加排序
        lambdaQueryWrapper.orderByDesc(Employee::getCreateTime);
        //查询分页、自动更新
        employeeService.page(pageInfo, lambdaQueryWrapper);
        //返回查询结果
        return Result.success(pageInfo);
    }

    /**
     * 更新员工状态，是PUT请求
     * @param httpServletRequest
     * @param employee
     * @return
     */
    @PutMapping()
    public Result<Employee> update(HttpServletRequest httpServletRequest,@RequestBody Employee employee){
        System.out.println("更新"+Thread.currentThread().getName());
        //从Request作用域中拿到员工ID
        Long empId = (Long) httpServletRequest.getSession().getAttribute("employee");
        //拿新的状态值
        employee.setStatus(employee.getStatus());
        //更新时间
        employee.setUpdateTime(LocalDateTime.now());
        //更新处理人id
        employee.setUpdateUser(empId);
        employeeService.updateById(employee);
        return Result.success(employee);
    }


    /**
     * 拿到员工资料，前端自动填充列表，更新的时候复用上面的update方法
     * @param id ResultFul风格传入参数，用@PathVariable来接收同名参数
     * @return 返回员工对象
     */
    @GetMapping("/{id}")
    public Result<Employee> getEmployee(@PathVariable Long id){
        Employee employee = employeeService.getById(id);
        if (employee!=null){
            return Result.success(employee);
        }
        return Result.error("没有查到员工信息");
    }

}

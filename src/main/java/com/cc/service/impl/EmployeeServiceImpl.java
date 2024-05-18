package com.cc.service.impl;

import com.cc.pojo.Employee;
import com.cc.mapper.EmployeeMapper;
import com.cc.service.EmployeeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.utils.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * <p>
 * 员工信息 服务实现类
 * </p>
 *
 * @author cc
 * @since 2022-05-30
 */
@Service
@Slf4j
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

}

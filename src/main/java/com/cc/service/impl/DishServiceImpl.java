package com.cc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cc.dto.DishDto;
import com.cc.pojo.Dish;
import com.cc.mapper.DishMapper;
import com.cc.pojo.DishFlavor;
import com.cc.service.DishFlavorService;
import com.cc.service.DishService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜品管理 服务实现类
 * </p>
 *
 * @author cc
 * @since 2022-05-30
 */
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishService dishService;

    /**
     * 多表操作只能一个一个来，MP没有办法一次性操作多张表
     * 因为涉及到多表的问题，所以还要加入注解来处理事务
     * @Transactional 开启事务
     * @EnableTransactionManagement 在启动类加入，支持事务开启
     * @param dishDto
     */
    @Transactional
    @Override
    public void addDishWithFlavor(DishDto dishDto) {
        //因为DishDto是包含了Dish的信息，所以可以先存Dish信息到Dish表中，DishDto扩展的数据可以下一步再存
        //为什么这里传dishDto可以，因为DishDto是Dish的子类
        dishService.save(dishDto);
        //拿ID和口味List，为存DishDto做准备
        Long dishId = dishDto.getId();
        List<DishFlavor> flavor = dishDto.getFlavors();
        //遍历
        for (DishFlavor dishFlavors:flavor) {
            dishFlavors.setDishId(dishId);
        }
        //saveBatch是批量集合的存储
        dishFlavorService.saveBatch(flavor);
    }

    /**
     * 更新口味操作，和上面的添加操作异曲同工
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateDishWithFlavor(DishDto dishDto) {
        //Dish表是可以直接更新操作的,这里也是一样的，传入的是Dish的子类，可以直接操作，默认也就是按Dish类更新了
        dishService.updateById(dishDto);
        //Dish_Flavor表比较特殊，所以需要先删除再插入
        //Dish_Flavor表字段删除，所有当前dish id的口味
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper();
        //子类可以直接获取父类的内容了
        lambdaQueryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(lambdaQueryWrapper);
        //再插入
        List<DishFlavor> flavorList=dishDto.getFlavors();
        //遍历
        for (DishFlavor dishFlavors:flavorList) {
            dishFlavors.setDishId(dishDto.getId());
        }
        //saveBatch是批量集合的存储
        dishFlavorService.saveBatch(flavorList);
    }




    /**
     * 通过id查询口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //先把普通信息查出来
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        //搬运
        BeanUtils.copyProperties(dish, dishDto);
        //在通过dish的分类信息查口味List
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> listFlavor=dishFlavorService.list(lambdaQueryWrapper);
        //填充DishDto
        dishDto.setFlavors(listFlavor);
        return dishDto;
    }

}

package com.cc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cc.common.CustomerException;
import com.cc.pojo.Category;
import com.cc.mapper.CategoryMapper;
import com.cc.pojo.Dish;
import com.cc.pojo.Setmeal;
import com.cc.service.CategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.service.DishService;
import com.cc.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 菜品及套餐分类 服务实现类
 * </p>
 *
 * @author cc
 * @since 2022-05-30
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    //注入菜品服务
    @Autowired
    private DishService dishService;
    //注入套餐服务
    @Autowired
    private SetmealService setmealService;

    @Override
    public void removeCategory(Long id) {
        //查询当前菜品分类下是否还有菜品，如果有菜品就不能删除，抛出异常打断
        LambdaQueryWrapper<Dish> dishLambdaQueryWarpper = new LambdaQueryWrapper();
        //看看所有的菜品下有没有目标分类与之关联
        dishLambdaQueryWarpper.eq(Dish::getCategoryId, id);
        int countInDishById=dishService.count(dishLambdaQueryWarpper);
        if (countInDishById>0){
            //已经与菜品关联了，抛异常，不许删
            throw new CustomerException("已经与菜品关联了，请删除完菜品再来删除");
        }
        //查询当前菜品分类是否关联了套餐，如果有套餐就不能删除，抛出异常打断
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWarpper = new LambdaQueryWrapper();
        setmealLambdaQueryWarpper.eq(Setmeal::getCategoryId, id);
        //看看所有的套餐下有没有目标分类与之关联
        int countInSetmealById = setmealService.count(setmealLambdaQueryWarpper);
        if (countInSetmealById>0){
            //已经与套餐关联了，抛异常，不许删
            throw new CustomerException("已经与套餐关联了，请删除完套餐再来删除");
        }
        //上面的测试都通过了，没有任何阻碍了，允许删除，直接调用父接口继承的MP的删除方法就行
        super.removeById(id);
    }

}

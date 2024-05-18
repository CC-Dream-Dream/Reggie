package com.cc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cc.common.CustomerException;
import com.cc.dto.SetmealDto;
import com.cc.pojo.Setmeal;
import com.cc.mapper.SetmealMapper;
import com.cc.pojo.SetmealDish;
import com.cc.service.SetmealDishService;
import com.cc.service.SetmealService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 套餐 服务实现类
 * </p>
 *
 * @author cc
 * @since 2022-05-30
 */
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 保存套餐信息，菜品信息一起就保存了
     * 保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //直接存，因为setmealDto是Setmeal的子类，所以会把SetmealDto中的Setmeal内容存入表中
        setmealService.save(setmealDto);
        //下一步就是拿到与套餐关联的菜品列表
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //菜品列表里面还没赋值SetmealId，所以要遍历赋值关联的setmealDish的id
        for (SetmealDish s : setmealDishes) {
            s.setSetmealId(setmealDto.getId());
        }
        //批量存储套餐信息，插入多条数据
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 获取套餐详细信息，填充到页面上
     * @param id
     * @return
     */
    @Override
    public SetmealDto getSetmealData(Long id) {
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(id != null,SetmealDish::getSetmealId,id);

        if (setmeal != null){
            BeanUtils.copyProperties(setmeal,setmealDto);

            List<SetmealDish> dishes = setmealDishService.list(queryWrapper);
            setmealDto.setSetmealDishes(dishes);

            return setmealDto;
        }

        return null;
    }

    /**
     * 删除套餐操作
     * 删除的时候，套餐下的关联关系也需要删除掉，要同时处理两张表
     * @param ids 接收多个id，id可以单个也可以多个，批量删或者单个删都可，毕竟走的都是遍历删除
     */
    @Override
    public void removeWithDish(List<Long> ids) {
        //统计符合条件删除的对象，确定是停售状态才可以删除
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper();
        //利用SQL中的in，来查询这个表内有ids的内容
        lambdaQueryWrapper.in(Setmeal::getId, ids);
        lambdaQueryWrapper.eq(Setmeal::getStatus, 1);
        //统计符合删除条件的套餐个数
        int count = this.count(lambdaQueryWrapper);
        //不能删除抛异常
        if (count>0){
            throw new CustomerException("不符合删除条件");
        }
        //先删除套餐表setmeal内的信息
        this.removeByIds(ids);
        //再删除和套餐表相关的信息，直接调用setmealDish的删除方法传id是不行的，因为是Setmeal的id
        //所以先构造一个查询条件，把setmealDish中id关联setmealId字段的内容查出来
        LambdaQueryWrapper<SetmealDish> setmealDishlambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishlambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        //条件构造好了，直接remove就行
        setmealDishService.remove(setmealDishlambdaQueryWrapper);
    }
}

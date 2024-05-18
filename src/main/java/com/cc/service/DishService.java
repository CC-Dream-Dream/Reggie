package com.cc.service;

import com.cc.dto.DishDto;
import com.cc.pojo.Dish;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 菜品管理 服务类
 * </p>
 *
 * @author cc
 * @since 2022-05-30
 */
public interface DishService extends IService<Dish> {
    public DishDto getByIdWithFlavor(Long id);

    public void addDishWithFlavor(DishDto dishDto);

    public void updateDishWithFlavor(DishDto dishDto);

}

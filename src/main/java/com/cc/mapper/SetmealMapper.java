package com.cc.mapper;

import com.cc.dto.SetmealDto;
import com.cc.pojo.Setmeal;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cc.pojo.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 套餐 Mapper 接口
 * </p>
 *
 * @author cc
 * @since 2022-05-30
 */
@Mapper
public interface SetmealMapper extends BaseMapper<Setmeal> {

    List<SetmealDish> listSetmeal(int page, int pageSize, String name);

    SetmealDto getSetmealData(Long id);
}

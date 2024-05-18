package com.cc.dto;


import com.cc.pojo.Dish;
import com.cc.pojo.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 原本的dish不够用了，对dish类进行扩展
 */
@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}

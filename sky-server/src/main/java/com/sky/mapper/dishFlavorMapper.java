package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface dishFlavorMapper {
    void insertBatch(List flavors);

    List<DishFlavor> getByDishId(Long id);

    void deleteByDishId(Long id);
}

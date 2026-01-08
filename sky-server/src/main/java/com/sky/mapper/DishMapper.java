package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.Annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {
@AutoFill(value = OperationType.INSERT)
    void  insert(Dish dish);
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);
    @Select("select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = #{setmealId}")
    List<Dish> getBySetmealId(Long id);

    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    Dish getById(Long id);

    List<Dish> list(Long categoryId);

    void update(Dish dishDTO);

    void deletelist(List<Long> ids);

    void startOrStop(Dish dish);

    @Select("select count(id) from dish where status = #{status}")
    Integer countByStatus(Integer status);
}

package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /*
    * 批量插入菜品口味数据
    * @param flavors
    * */
//    @AutoFill(OperationType.INSERT)
    void insertBatch(List<DishFlavor> flavors);


    /*
    * 根据菜品id查询对应的口味数据
    * @param id
    * */
    @Delete("delete from dish_flavor where dish_id = #{id}")
    void deleteByDishId(Long id);

    /*
    * 根据菜品id批量删除对应的口味数据
    * @param dishIds
    * */
    void deleteByDishIds(List<Long> dishIds);

    /*
    * 根据菜品id查询对应的口味数据
    * @param dishId
    * @return
    * */
    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> getByDishId(Long dishId);
}

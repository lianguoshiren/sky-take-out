package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /*
    * 根据菜品id查询套餐id
    * @param dishIds
    * @return
    * */
    List<Long> getSetmealIdByDishId(List<Long> dishIds);

    @AutoFill(value = OperationType.INSERT)
    void insertBatch(List<SetmealDish> setmealDishes);

    @Select("select dish_id from setmeal_dish where setmeal_id = #{id}")
    List<Long> getDishIdBySetmealId(Long id);

    void deleteBatch(List<Long> dishIds);

    void deleteBatchBySetmealId(List<Long> ids);

    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> getBySetmealId(Long id);
}

package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    /*
    * 动态条件查询购物车数据
    * @param shoppingCart
    * @return
    * */
    List<ShoppingCart> list(ShoppingCart shoppingCart);


    /*
    * 根据id修改购物车中商品的数量
    * @param shoppingCart
    * @return
    * */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /*
    * 插入购物车数据
    * @param shoppingCart
    * */
    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time)" +
            "values (#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{createTime})")
    void insert(ShoppingCart shoppingCart);
}

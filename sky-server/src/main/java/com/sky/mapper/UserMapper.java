package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    /*
     * 根据用户id查询用户
     * @param id
     * @return
     * */
    @Select("select * from user where openid = #{id}")
    User getById(String id);

    /*
    * 插入用户数据
    * @param user
    * */
    void insert(User user);
}

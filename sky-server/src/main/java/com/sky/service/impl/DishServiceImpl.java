package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        // 向菜品表插入1条数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
//        获取insert语句生成的主键值
        Long dishId = dish.getId();
        // 向口味表插入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && !flavors.isEmpty()) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
        }
        log.info("新增菜品口味：{}", flavors);
        dishFlavorMapper.insertBatch(flavors);
        }

    @Override
    /*
    * 菜品分页查询
    * @param dishPageQueryDTO
    * */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /*
    * 菜品批量删除
    * @param ids
    * */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 先判断当前菜品是否能够删除，即是否存在起售中的菜品
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        // 菜品是否被套餐关联
        List<Long> setmealIds = setmealDishMapper.getSetmealIdByDishId(ids);
        if(setmealIds != null && setmealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        // 删除菜品数据
//        for (Long id : ids) {
//            dishMapper.deleteById(id);
//
//            // 删除菜品口味数据
//            dishFlavorMapper.deleteByDishId(id);
//        }

//        批量删除菜品
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);


    }


    /*
    * 根据id查询菜品和对应的口味数据
    * @param id
    * @return DishVO
    * */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //根据id查询菜品数据
        Dish dish = dishMapper.getById(id);
        //根据菜品id查询对应的口味数据
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);

        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }


    /*
    * 根据id修改菜品和对应的口味数据
    * @param dishDTO
    * */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

//        修改菜品表
        dishMapper.update(dish);
//        删除原有的口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

//        重新插入新的口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dish.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    /*
    * 根据分类ID查找菜品
    * @param id
    * @return
    * */
    @Override
    public List<Dish> list(Long id) {
//        List<Dish> dishes = dishMapper.getByCategoryId(id);
        Dish dish = Dish.builder()
                .categoryId(id)
                .status(StatusConstant.ENABLE)
                .build();
        List<Dish> dishes = dishMapper.list(dish);
        return dishes;
    }

}

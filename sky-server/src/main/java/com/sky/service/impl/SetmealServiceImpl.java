package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishService dishService;
    /*
    * 插入套餐
    * @param setmealDTO
    * */
    @Override
    @Transactional
    public void saveWithDishes(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        //插入套餐表
        setmealMapper.insert(setmeal);

        //批量插入套餐-菜品表
        Long setmealId = setmeal.getId();
        if(setmealDishes != null && !setmealDishes.isEmpty()) {
            for (SetmealDish setmealDish : setmealDishes) {
                setmealDish.setSetmealId(setmealId);
            }
            setmealDishMapper.insertBatch(setmealDishes);
        }


    }

    /*
    * 套餐分页查询
    * @param setmealPageQueryDTO
    * @return
    * */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<Setmeal> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /*
    * 批量删除套餐
    * @param ids
    * */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
//        在套餐表中完成删除，只能删除在停售状态的套餐
//        根据id查找套餐
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if(Objects.equals(setmeal.getStatus(), StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        setmealMapper.deleteBatch(ids);

//        在套餐-菜品关联表中完成删除
        if(!ids.isEmpty()) {
            setmealDishMapper.deleteBatchBySetmealId(ids);
        }
    }

    /*
    * 根据id和status进行更新
    * @param id status
    * */
    @Override
    public void updateById(Long id, Integer status) {
//        status为0，则直接将套餐的status更新为0
        if(status == 0) {
            Setmeal setmeal = Setmeal.builder()
                    .id(id)
                    .status(StatusConstant.DISABLE)
                    .build();

            setmealMapper.update(setmeal);
        }
        else {
//            status为1，查找套餐中是否存在禁售的菜品
//            在套餐-菜品关联表中获取关联的dishId
//            查找对应id的状态
            List<Long> dishIds = setmealDishMapper.getDishIdBySetmealId(id);
            for (Long did : dishIds) {
                DishVO byIdWithFlavor = dishService.getByIdWithFlavor(did);
                if(Objects.equals(byIdWithFlavor.getStatus(), StatusConstant.DISABLE)) {
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }

            }
            Setmeal setmeal = Setmeal.builder()
                    .id(id)
                    .status(StatusConstant.ENABLE)
                    .build();
            setmealMapper.update(setmeal);

        }


    }

    @Override
    public SetmealVO getByIdWithDish(Long id) {
        Setmeal setmeal = setmealMapper.getById(id);
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);

        List<SetmealDish> dishIds = setmealDishMapper.getBySetmealId(id);
        setmealVO.setSetmealDishes(dishIds);

        return setmealVO;
    }

    @Override
    public void updateWithDishes(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        /*if(setmeal.getStatus().equals(StatusConstant.ENABLE)) {
//            起售中的套餐无法修改
            throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ON_SALE);
        }*/

//        先更新setmeal表
        setmealMapper.update(setmeal);

//        删除原有的套餐-菜品关联数据，重新插入
        setmealDishMapper.deleteBatchBySetmealId(Arrays.asList(setmeal.getId()));
        for (SetmealDish setmealDish : setmealDTO.getSetmealDishes()) {
            setmealDish.setSetmealId(setmealDTO.getId());
        }
        setmealDishMapper.insertBatch(setmealDTO.getSetmealDishes());
    }
}

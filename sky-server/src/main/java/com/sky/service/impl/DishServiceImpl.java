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

    /**
     * 保存菜品信息，并关联口味
     * @param dishDTO
     */
    @Override
    public void savaWithFlavor(DishDTO dishDTO) {

        //插入一条菜品数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);

        Long id = dish.getId();
        //插入n条口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //判断口味列表是否为空，不为空则插入数据
        if (flavors != null && flavors.size() > 0){
            //为改口味设置对应的设置菜品id
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(id);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        //分页插件进行分页查询
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO>page=dishMapper.pageQuary(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 删除菜品信息
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //根据当前菜品是否能删除？--是否存在启售中的菜品？
        for (Long id : ids) {
            Dish dish=dishMapper.getById(id);
            //如果状态为启售
            if(dish.getStatus()== StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //根据当前菜品是否能删除？--是否被套餐关联了？
        List<Long>setmealIds=setmealDishMapper.getSetmealIdsByDishIds(ids);
        if(setmealIds!=null&& setmealIds.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        for (Long id : ids) {
            //删除菜品表中的菜品数据
            dishMapper.deleteById(id);
            //删除菜品所关联的对应口味数据
            dishFlavorMapper.deleteByDishId(id);
        }
    }

    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //根据菜品id查询菜品信息
        Dish dish=dishMapper.getById(id);
        //根据菜品id查询对应的口味信息
        List<DishFlavor>flavors=dishFlavorMapper.getByDishId(id);
        //封装菜品信息以及口味信息
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        //设置口味信息
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //更新菜品信息
        dishMapper.update(dish);
        //删除原有口味
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //插入新的口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //判断口味列表是否为空，不为空则插入数据
        if (flavors != null && flavors.size() > 0){
            //为改口味设置对应的设置菜品id
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }
}

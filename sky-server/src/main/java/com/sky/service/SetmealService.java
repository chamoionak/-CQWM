package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {

    /**
     * 套餐新增菜品
     * @param setmealDTO
     */
    void saveWithDish(SetmealDTO setmealDTO);

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */

    PageResult pageQuary(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 套餐删除菜品
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询套餐详情
     * @param id
     * @return
     */
    SetmealVO getByIdWithDish(Long id);

    /**
     * 套餐更新菜品
     * @param setmealDTO
     */
    void updateWithDish(SetmealDTO setmealDTO);
}

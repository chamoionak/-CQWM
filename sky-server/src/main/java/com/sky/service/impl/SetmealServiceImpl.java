package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {


    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDTO
     */
    @Override
    @Transactional
    //把整个方法放进一个数据库事务里：
    //任何一步抛运行时异常都会回滚；
    //保证“套餐主表 + 套餐菜品关系表”要么一起成功，要么一起失败。
    public void saveWithDish(SetmealDTO setmealDTO) {
        //先将套餐价格等信息保存再将其中关联的菜品一起保存
        Setmeal setmeal = new Setmeal();
        //将套餐DTO中的数据拷贝到setmeal对象中
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //保存套餐基本信息
        setmealMapper.insert(setmeal);

        //数据库里有两张表：
        //① setmeal（套餐主表，主键 id 自增）
        //② setmeal_dish（套餐-菜品关系表，字段：id,setmeal_id,dish_id,copies…）
        //拿到套餐ID
        Long setmealId = setmeal.getId();
        //把前端传来的菜品清单取出来
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //遍历菜品清单，设置套餐ID
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        //批量插入套餐-菜品关系表中
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 分页查询套餐信息
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuary(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO>page=setmealMapper.pageQuary(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 批量删除套餐信息
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        ids.forEach(id -> {
            Setmeal setmeal = setmealMapper.getById(id);
            if(StatusConstant.ENABLE == setmeal.getStatus()){
                //起售中的套餐不能删除
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });

        ids.forEach(setmealId -> {
            //删除套餐表中的数据
            setmealMapper.deleteById(setmealId);
            //删除套餐菜品关系表中的数据
            setmealDishMapper.deleteBySetmealId(setmealId);
        });
        //    public void deleteBatch(List<Long> ids) {
//        //该套餐是否起售
//        for (Long id : ids) {
//            Setmeal setmeal=setmealMapper.getById(id);
//            if (setmeal.getStatus()== StatusConstant.ENABLE){
//                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
//            }
//        }
//        for (Long id : ids) {
//            //删除套餐菜品关系
//            setmealMapper.deleteById(id);
//            //删除套餐信息
//            setmealDishMapper.deleteBySetmealId(id);
//        }
//
//    }
    }

    /**
     * 根据套餐ID查询套餐信息以及关联的菜品信息
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {
        //根据套餐ID查询套餐信息
        Setmeal setmeal=setmealMapper.getById(id);
        //根据套餐ID查询套餐菜品关系信息
        List<SetmealDish> dishIds = setmealDishMapper.getByDishIds(id);

        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(dishIds);

        return setmealVO;
    }

    @Override
    public void updateWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //删除套餐
        setmealMapper.update(setmeal);
        //删除套餐菜品关系
        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());
        //插入新的菜品关系以达到修改功能
        //插入新的菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //判断口味列表是否为空，不为空则插入数据
        if(setmealDishes!= null &&setmealDishes.size()>0){
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealDTO.getId());
            });
            //批量插入套餐菜品关系表
            setmealDishMapper.insertBatch(setmealDishes);
        }
//        if (flavors != null && flavors.size() > 0){
//            //为改口味设置对应的设置菜品id
//            flavors.forEach(dishFlavor -> {
//                dishFlavor.setDishId(dishDTO.getId());
//            });
//            dishFlavorMapper.insertBatch(flavors);
//        }
    }
}

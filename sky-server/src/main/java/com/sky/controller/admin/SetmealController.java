package com.sky.controller.admin;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐管理")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;
    @PostMapping
    @ApiOperation("保存套餐信息")
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("保存套餐信息{}", setmealDTO);
        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("分页查询套餐信息{}", setmealPageQueryDTO);
        PageResult pageResult=setmealService.pageQuary(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("删除套餐信息")
    public Result delete(@RequestParam List<Long>ids){
        log.info("删除套餐信息{}", ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐信息")
    public Result<SetmealVO>getById(@PathVariable Long id){
        log.info("根据id查询套餐信息{}", id);
        SetmealVO setmealVO=setmealService.getByIdWithDish(id);
        return Result.success(setmealVO);
    }

    @PutMapping
    @ApiOperation("更新套餐信息")
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("更新套餐信息{}", setmealDTO);
        setmealService.updateWithDish(setmealDTO);
        return Result.success();
    }
}

package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishServer;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin/dish")
public class DishController {
    @Autowired
    private DishServer dishServer;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishServer.saveWithFlavor(dishDTO);
        String key = "dish" + dishDTO.getCategoryId();
        redisTemplate.delete(key);
    return Result.success();
    }
    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询:{}", dishPageQueryDTO);
        PageResult pageResult = dishServer.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品：{}", id);
        DishVO dishVO = dishServer.getByIdWithFlavor(id);
        log.info("查询结果：{}", dishVO);
        return Result.success(dishVO);
    }
    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId) {
        List<Dish> list = dishServer.list(categoryId);
        return Result.success(list);
    }
    @PutMapping
    @ApiOperation("修改菜品信息")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品信息：{}", dishDTO);
        dishServer.update(dishDTO);
        log.info("修改菜品信息：{}", dishDTO);
        Collection keys = redisTemplate.keys("dish*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        return Result.success();
    }
@DeleteMapping
@ApiOperation("批量删除菜品")
    public Result deletelist(@RequestParam List<Long> ids){
        log.info("批量删除菜品信息: {}",ids);
        dishServer.deletelist(ids);
        Collection keys = redisTemplate.keys("dish*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        return Result.success();
    }
@PostMapping("/status/{status}")
@ApiOperation("菜品起售、停售")
    public Result startOrStop(@PathVariable Integer status,Long id)
{
    dishServer.startOrStop(status,id);
    Collection keys = redisTemplate.keys("dish*");
    if (keys != null && !keys.isEmpty()) {
        redisTemplate.delete(keys);
    }
    return Result.success();
}
}

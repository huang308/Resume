package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
public class ShopController {

    @Autowired
    private RedisTemplate redisTemplate;

   private static final String SHOP_STATUS = "SHOP_STATUS";
    @PutMapping("/{status}")
    public Result statusset(@PathVariable Integer status) {
        redisTemplate.opsForValue().set(SHOP_STATUS,status);

        return Result.success();

    }
    @GetMapping("/status")
    public Result<Integer> getStatus() {
        Object status = redisTemplate.opsForValue().get(SHOP_STATUS);
        Integer statusInt = (status != null) ? (Integer) status : 0;
        return Result.success(statusInt);

    }
}

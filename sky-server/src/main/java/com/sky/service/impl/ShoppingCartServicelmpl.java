package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServicelmpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart query = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, query);
        query.setUserId(userId);
        List<ShoppingCart> list = shoppingCartMapper.list(query);
        if (list != null && !list.isEmpty()) {
            ShoppingCart existing = list.get(0);
            existing.setNumber(existing.getNumber() + 1);
            shoppingCartMapper.updateNumberById(existing);
            return;
        }
        ShoppingCart toInsert = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, toInsert);
        toInsert.setUserId(userId);
        toInsert.setNumber(1);
        toInsert.setCreateTime(LocalDateTime.now());
        if (shoppingCartDTO.getDishId() != null) {
            Dish dish = dishMapper.getById(shoppingCartDTO.getDishId());
            if (dish != null) {
                toInsert.setName(dish.getName());
                toInsert.setImage(dish.getImage());
                toInsert.setAmount(dish.getPrice());
            }
        } else if (shoppingCartDTO.getSetmealId() != null) {
            Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
            if (setmeal != null) {
                toInsert.setName(setmeal.getName());
                toInsert.setImage(setmeal.getImage());
                toInsert.setAmount(setmeal.getPrice());
            }
        }
        shoppingCartMapper.insert(toInsert);
    }
    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart query = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, query);
        query.setUserId(userId);
        List<ShoppingCart> list = shoppingCartMapper.list(query);
        if (list == null || list.isEmpty()) {
            return;
        }
        ShoppingCart existing = list.get(0);
        Integer number = existing.getNumber() == null ? 0 : existing.getNumber();
        if (number <= 1) {
            shoppingCartMapper.deleteById(existing.getId());
        } else {
            existing.setNumber(number - 1);
            shoppingCartMapper.updateNumberById(existing);
        }
    }
    @Override
    public List<ShoppingCart> list() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart query = new ShoppingCart();
        query.setUserId(userId);
        return shoppingCartMapper.list(query);
    }
    @Override
    public void clean() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }
}

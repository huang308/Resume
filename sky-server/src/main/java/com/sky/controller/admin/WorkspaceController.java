package com.sky.controller.admin;

import com.sky.constant.StatusConstant;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/workspace")
public class WorkspaceController {

    @Autowired
    private ReportService reportService;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private OrderMapper orderMapper;

    @GetMapping("/businessData")
    public Result<BusinessDataVO> businessData() {
        LocalDate today = LocalDate.now();
        BusinessDataVO vo = reportService.getBusinessData(today, today);
        return Result.success(vo);
    }

    @GetMapping("/overviewSetmeals")
    public Result<SetmealOverViewVO> overviewSetmeals() {
        Map<String, Object> enableMap = new HashMap<>();
        enableMap.put("status", StatusConstant.ENABLE);
        Integer sold = setmealMapper.countByMap(enableMap);

        Map<String, Object> disableMap = new HashMap<>();
        disableMap.put("status", StatusConstant.DISABLE);
        Integer discontinued = setmealMapper.countByMap(disableMap);

        SetmealOverViewVO vo = SetmealOverViewVO.builder()
                .sold(sold == null ? 0 : sold)
                .discontinued(discontinued == null ? 0 : discontinued)
                .build();
        return Result.success(vo);
    }

    @GetMapping("/overviewDishes")
    public Result<DishOverViewVO> overviewDishes() {
        Integer sold = dishMapper.countByStatus(StatusConstant.ENABLE);
        Integer discontinued = dishMapper.countByStatus(StatusConstant.DISABLE);
        DishOverViewVO vo = DishOverViewVO.builder()
                .sold(sold == null ? 0 : sold)
                .discontinued(discontinued == null ? 0 : discontinued)
                .build();
        return Result.success(vo);
    }

    @GetMapping("/overviewOrders")
    public Result<OrderOverViewVO> overviewOrders() {
        Integer waiting = orderMapper.orderStageStatistics(com.sky.entity.Orders.TO_BE_CONFIRMED);
        Integer delivering = orderMapper.orderStageStatistics(com.sky.entity.Orders.DELIVERY_IN_PROGRESS);
        Integer completed = orderMapper.orderStageStatistics(com.sky.entity.Orders.COMPLETED);
        Integer cancelled = orderMapper.orderStageStatistics(com.sky.entity.Orders.CANCELLED);
        Integer all = orderMapper.countAll();

        OrderOverViewVO vo = OrderOverViewVO.builder()
                .waitingOrders(waiting == null ? 0 : waiting)
                .deliveredOrders(delivering == null ? 0 : delivering)
                .completedOrders(completed == null ? 0 : completed)
                .cancelledOrders(cancelled == null ? 0 : cancelled)
                .allOrders(all == null ? 0 : all)
                .build();
        return Result.success(vo);
    }
}

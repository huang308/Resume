package com.sky.controller.admin;

import com.sky.service.ReportService;
import com.sky.result.Result;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/admin/report")
public class ReportController {
    @Autowired
    private ReportService reportService;
    @GetMapping("/export")
    public void export(HttpServletResponse response){
        reportService.exportBusinessData(response);

    }
    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(@RequestParam("begin") java.time.LocalDate begin,
                                                       @RequestParam("end") java.time.LocalDate end) {
        return Result.success(reportService.getTurnoverStatistics(begin, end));
    }

    @GetMapping("/userStatistics")
    public Result<UserReportVO> userStatistics(@RequestParam("begin") java.time.LocalDate begin,
                                               @RequestParam("end") java.time.LocalDate end) {
        return Result.success(reportService.getUserStatistics(begin, end));
    }

    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> ordersStatistics(@RequestParam("begin") java.time.LocalDate begin,
                                                  @RequestParam("end") java.time.LocalDate end) {
        return Result.success(reportService.getOrdersStatistics(begin, end));
    }

    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> top10(@RequestParam("begin") java.time.LocalDate begin,
                                            @RequestParam("end") java.time.LocalDate end) {
        return Result.success(reportService.getSalesTop10(begin, end));
    }
}

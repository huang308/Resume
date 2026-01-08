package com.sky.service.impl;

import com.sky.mapper.ExportMapper;
import com.sky.service.ReportService;
import com.sky.vo.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class ExportBusinessData implements ReportService {
    @Autowired
    private ExportMapper exportMapper;
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        LocalDate begin= LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        HashMap<Object, Object> map = new HashMap<>();
        map.put("begin",begin);
        map.put("end",end);
        map.put("status",5);
        Double sum = exportMapper.sumByMap(map);//算总金额的
        HashMap<Object, Object> countByMap = new HashMap<>();;
        countByMap.put("begin",begin);
        countByMap.put("end",end);
        countByMap.put("status",5);
        Integer valid = exportMapper.countByMap(countByMap);//算有效总订单
        map.remove("status");
        Integer total = exportMapper.totaiOrder(map);//算总订单
        Integer newUser = exportMapper.newUser(map);//新用户
        try {
            InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook(resourceAsStream);
            XSSFSheet sheet1 = workbook.getSheetAt(0);
            sheet1.getRow(1).getCell(1).setCellValue("时间"+begin+"至"+end);
            sheet1.getRow(3).getCell(2).setCellValue(sum);
            double completionRate = (total == null || total == 0) ? 0.0 : (double) valid / total;
            sheet1.getRow(3).getCell(4).setCellValue(completionRate * 100 + "%");
            sheet1.getRow(3).getCell(6).setCellValue(newUser);
            sheet1.getRow(4).getCell(2).setCellValue(valid);
            double unitPrice = (valid == null || valid == 0) ? 0.0 : (sum == null ? 0.0 : sum) / valid;
            sheet1.getRow(4).getCell(4).setCellValue(unitPrice);
            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);

                // 【关键修改】这里必须 new HashMap<Object, Object>()，才能匹配你的接口定义
                // 同时变量名改为 queryMap，避免和循环外的 map 冲突
                HashMap<Object, Object> queryMap = new HashMap<>();

                // LocalDate 需要转为 LocalDateTime 保证时分秒查询准确
                queryMap.put("begin", LocalDateTime.of(date, LocalTime.MIN));
                queryMap.put("end", LocalDateTime.of(date, LocalTime.MAX));

                // --- 执行查询 ---
                queryMap.put("status", 5);
                Double turnover = exportMapper.sumByMap(queryMap);
                Integer validOrderCount = exportMapper.countByMap(queryMap);

                queryMap.remove("status"); // 查总单和新用户不需要 status=5
                Integer totalOrderCount = exportMapper.totaiOrder(queryMap);
                Integer newUserCount = exportMapper.newUser(queryMap);

                // --- 计算完成率 (注意强转 double) ---
                Double orderCompletionRate = 0.0;
                if (totalOrderCount != null && totalOrderCount != 0) {
                    orderCompletionRate = (double) validOrderCount / totalOrderCount;
                }

                XSSFRow row = sheet1.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(turnover == null ? 0.0 : turnover);
                row.getCell(3).setCellValue(validOrderCount == null ? 0 : validOrderCount);
                row.getCell(4).setCellValue(totalOrderCount == null ? 0 : totalOrderCount);
                row.getCell(5).setCellValue(orderCompletionRate);
                row.getCell(6).setCellValue(newUserCount == null ? 0 : newUserCount);
            }
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<String> dateList = new ArrayList<>();
        List<String> turnoverList = new ArrayList<>();

        long days = end.toEpochDay() - begin.toEpochDay() + 1;
        for (int i = 0; i < days; i++) {
            LocalDate date = begin.plusDays(i);
            HashMap<Object, Object> queryMap = new HashMap<>();
            queryMap.put("begin", LocalDateTime.of(date, LocalTime.MIN));
            queryMap.put("end", LocalDateTime.of(date, LocalTime.MAX));
            queryMap.put("status", 5);
            Double turnover = exportMapper.sumByMap(queryMap);
            dateList.add(date.toString());
            turnoverList.add(String.valueOf(turnover == null ? 0.0 : turnover));
        }

        return TurnoverReportVO.builder()
                .dateList(String.join(",", dateList))
                .turnoverList(String.join(",", turnoverList))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<String> dateList = new ArrayList<>();
        List<String> totalUserList = new ArrayList<>();
        List<String> newUserList = new ArrayList<>();

        long days = end.toEpochDay() - begin.toEpochDay() + 1;
        int cumulative = 0;
        for (int i = 0; i < days; i++) {
            LocalDate date = begin.plusDays(i);
            HashMap<Object, Object> queryMap = new HashMap<>();
            queryMap.put("begin", LocalDateTime.of(date, LocalTime.MIN));
            queryMap.put("end", LocalDateTime.of(date, LocalTime.MAX));
            Integer newUsers = exportMapper.newUser(queryMap);
            newUsers = newUsers == null ? 0 : newUsers;
            cumulative += newUsers;

            dateList.add(date.toString());
            newUserList.add(String.valueOf(newUsers));
            totalUserList.add(String.valueOf(cumulative));
        }

        return UserReportVO.builder()
                .dateList(String.join(",", dateList))
                .totalUserList(String.join(",", totalUserList))
                .newUserList(String.join(",", newUserList))
                .build();
    }

    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        List<String> dateList = new ArrayList<>();
        List<String> orderCountList = new ArrayList<>();
        List<String> validOrderCountList = new ArrayList<>();

        long days = end.toEpochDay() - begin.toEpochDay() + 1;
        int totalOrderCount = 0;
        int validOrderCount = 0;
        for (int i = 0; i < days; i++) {
            LocalDate date = begin.plusDays(i);
            HashMap<Object, Object> queryMap = new HashMap<>();
            queryMap.put("begin", LocalDateTime.of(date, LocalTime.MIN));
            queryMap.put("end", LocalDateTime.of(date, LocalTime.MAX));

            Integer total = exportMapper.totaiOrder(queryMap);
            queryMap.put("status", 5);
            Integer valid = exportMapper.countByMap(queryMap);

            total = total == null ? 0 : total;
            valid = valid == null ? 0 : valid;

            totalOrderCount += total;
            validOrderCount += valid;

            dateList.add(date.toString());
            orderCountList.add(String.valueOf(total));
            validOrderCountList.add(String.valueOf(valid));
        }

        double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = (double) validOrderCount / totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(String.join(",", dateList))
                .orderCountList(String.join(",", orderCountList))
                .validOrderCountList(String.join(",", validOrderCountList))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public BusinessDataVO getBusinessData(LocalDate begin, LocalDate end) {
        HashMap<Object, Object> map = new HashMap<>();
        map.put("begin", LocalDateTime.of(begin, LocalTime.MIN));
        map.put("end", LocalDateTime.of(end, LocalTime.MAX));

        HashMap<Object, Object> validMap = new HashMap<>(map);
        validMap.put("status", 5);

        Double turnover = exportMapper.sumByMap(validMap);
        Integer validOrderCount = exportMapper.countByMap(validMap);

        Integer totalOrderCount = exportMapper.totaiOrder(map);
        Integer newUserCount = exportMapper.newUser(map);

        double orderCompletionRate = 0.0;
        if (totalOrderCount != null && totalOrderCount > 0 && validOrderCount != null) {
            orderCompletionRate = (double) validOrderCount / totalOrderCount;
        }

        double unitPrice = 0.0;
        if (validOrderCount != null && validOrderCount > 0 && turnover != null) {
            unitPrice = turnover / validOrderCount;
        }

        return BusinessDataVO.builder()
                .turnover(turnover == null ? 0.0 : turnover)
                .validOrderCount(validOrderCount == null ? 0 : validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUserCount == null ? 0 : newUserCount)
                .build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        HashMap<Object, Object> map = new HashMap<>();
        map.put("begin", LocalDateTime.of(begin, LocalTime.MIN));
        map.put("end", LocalDateTime.of(end, LocalTime.MAX));
        map.put("status", 5);

        List<Map<String, Object>> rows = exportMapper.top10Sales(map);

        List<String> names = new ArrayList<>();
        List<String> numbers = new ArrayList<>();
        if (rows != null) {
            for (Map<String, Object> r : rows) {
                names.add(String.valueOf(r.get("name")));
                numbers.add(String.valueOf(r.get("number")));
            }
        }

        return SalesTop10ReportVO.builder()
                .nameList(String.join(",", names))
                .numberList(String.join(",", numbers))
                .build();
    }
}

package com.zlf.es.spring.boot.autoconfigure.service.utils;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author zlf
 * @description:
 * @time: 2022/06/24
 */
@Slf4j
public class DatesUtils {

    public List<String> calculationDays(String startTime, String endTime) {
        List<String> allDays = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDt = LocalDate.parse(startTime, dateTimeFormatter1);
        int start_Y = startDt.getYear();
        int start_M = startDt.getMonth().getValue();
        int start_D = startDt.getDayOfMonth();
        log.info("Y=" + start_Y + ",M=" + start_M + ",D=" + start_D);

        LocalDate endDt = LocalDate.parse(endTime, dateTimeFormatter1);
        int start_Y1 = endDt.getYear();
        int start_M1 = endDt.getMonth().getValue();
        int start_D1 = endDt.getDayOfMonth();
        log.info("Y1=" + start_Y1 + ",M1=" + start_M1 + ",D1=" + start_D1);

        if (startDt.compareTo(endDt) > 1) {
            //开始时间大于结束时间返回空！
            return null;
        }
        String endTimeStr = dateTimeFormatter1.format(endDt);

        Period period = Period.between(LocalDate.parse(startTime), endDt);

        StringBuffer sb = new StringBuffer();
        sb.append(period.getYears()).append(",")
                .append(period.getMonths()).append(",")
                .append(period.getDays());
        log.info("=======时间分量：=======" + sb.toString());

        int py = start_Y1 - start_Y;
        int Y = 12;
        detailYear(allDays, start_Y, py, Y);
        log.info("=======allDays--size()=======" + allDays.size());
        if (CollectionUtil.isNotEmpty(allDays)) {
            for (int i = 0; i < allDays.size(); i++) {
                log.info(allDays.get(i) + "--------allDays------>第" + (i + 1) + "天");
            }
            List<String> okResult = getOkResult(allDays, startTime, endTimeStr);
            System.out.println("=======okResult--size()=======" + okResult.size());
            for (int i = 0; i < okResult.size(); i++) {
                log.info(okResult.get(i) + "--------okResult-------->第" + (i + 1));
            }
            return okResult;
        }
        return null;
    }

    /**
     * 获取正确的List
     *
     * @param startTime
     * @param allDays
     * @return
     */
    private static List<String> getOkResult(List<String> allDays, String startTime, String endTime) {
        List<String> result = new ArrayList<>();
        int indexStart = 0;
        int indexEnd = 0;
        for (int i = 0; i < allDays.size(); i++) {
            if (allDays.get(i).equals(startTime)) {
                indexStart = i;
            }
        }
        for (int i = 0; i < allDays.size(); i++) {
            if (allDays.get(i).equals(endTime)) {
                indexEnd = i;
            }
        }
        result = allDays.subList(indexStart, indexEnd + 1);
        return result;
    }

    /**
     * 处理整年
     *
     * @param allDays
     * @param start_Y
     * @param py
     * @param y
     */
    private void detailYear(List<String> allDays, int start_Y, int py, int y) {
        //处理年的天
        for (int i = start_Y; i < start_Y + py + 1; i++) {
            for (int j = 1; j <= y; j++) {
                String fst = "";
                if (j <= 9) {
                    fst = i + "-0" + j + "-01";
                } else {
                    fst = i + "-" + j + "-01";
                }
                int diff_day = getDiff_day(fst);
                for (int k = 1; k <= diff_day + 1; k++) {
                    String d = "";
                    if (j <= 9) {
                        d = i + "-0" + j;
                        if (k <= 9) {
                            d += "-0" + k;
                        } else if (k > 9) {
                            d += "-" + k;
                        }
                    } else if (j > 9) {
                        d = i + "-" + j;
                        if (k <= 9) {
                            d += "-0" + k;
                        } else if (k > 9) {
                            d += "-" + k;
                        }
                    }
                    allDays.add(d);
                }
            }
        }
    }

    /**
     * 根据当月第一天计算本月的开始天和结束天
     *
     * @param fst
     * @return
     */
    private int getDiff_day(String fst) {
        LocalDate fstLd = LocalDate.parse(fst);
        //获取月的第一天
        LocalDate fstLd_fd = fstLd.with(TemporalAdjusters.firstDayOfMonth());
        //获取月的最后一天
        LocalDate fstLd_ld = fstLd.with(TemporalAdjusters.lastDayOfMonth());
        Period period2 = Period.between(fstLd_fd, fstLd_ld);
        int diff_day = period2.getDays();
        return diff_day;
    }

    /**
     * @return
     */
    public static String LocalDateTimeToString(LocalDateTime localDateTime) {
        DateTimeFormatter dateTimeFormatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dateTimeFormatter1.withZone(ZoneId.systemDefault());
        return dateTimeFormatter1.format(localDateTime);
    }

    /**
     * @return
     */
    public static String DateToString(Date date) {
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = instant.atZone(zoneId);
        LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();
        // 或者直接使用LocalDateTime.ofInstant
        LocalDateTime localDateTime1 = LocalDateTime.ofInstant(instant, zoneId);
        DateTimeFormatter dateTimeFormatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTimeFormatter1.format(localDateTime);
    }

}
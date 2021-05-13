package com.delicacy.apricot.spider;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.delicacy.apricot.spider.handler.FileOutputhandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Slf4j
public class CNMoneyNowTest extends ApricotSpiderApplicationTests {


    @Test
    public void sumcalc() {
        astock_money();
        analysis_astock_money();
    }

    @Test
    public void analysis_astock_money() {
        Query query = new Query();

        List<Map> maps = mongoTemplate.find(query, Map.class, "analysis_astock_money");
        maps.sort((a, b) -> {
            Double a_zongshizhi = getDouble(a.get("zongshizhi"));
            Double a_jl_mongy = getDouble(a.get("jl_zongshizhi"));
            Double b_zongshizhi = getDouble(b.get("zongshizhi"));
            Double b_jl_mongy = getDouble(b.get("jl_zongshizhi"));
            return (a_jl_mongy - a_zongshizhi)/a_zongshizhi > (b_jl_mongy - b_zongshizhi)/b_zongshizhi ? -1 : 1;
        });
        Map<String,Object> map = new LinkedHashMap<>();
        AtomicInteger i = new AtomicInteger(1);
        maps.forEach(e -> {
            System.out.println(e);
            int andIncrement = i.getAndIncrement();
            String s = String.valueOf(andIncrement);
            map.put(s,e);
        });

//        FileOutputhandler.builder()
//                .minSize( 512 * 1024)
//                .subDir("guzhi")
//                .fileName("guzhi")
//                .path("D:\\data\\")
//                .subffix("md").build().writer(map);
    }

    private Double getDouble(Object o) {
        if (o == null) return 0.0;
        return Double.parseDouble(o.toString());
    }

    private Long getTimestamp(String date){
        String year = date.substring(0, 4);
        String report = date.substring(4);
        String ymd = year;
        switch (report){
            case "一季报": ymd+="-03-31";break;
            case "中报": ymd+="-06-30";break;
            case "三季报": ymd+="-09-30";break;
            case "年报": ymd+="-12-31";break;
            default:throw new RuntimeException();
        }
        DateTime parse = DateUtil.parse(ymd, "yyyy-MM-dd");
        return parse.getTime();
    }


    /**
     * 获取两年财报数据，季报，年报，半年报
     * 计算：财报中 yingyeshourutongbizengzhang jingliruntongbizengzhang
     * 计算规则：
     * 至少5个
     * 最近三个正值
     * 去掉最大，去掉最小，求平均
     */
    @Test
    public void astock_money() {
        String analysis_table = "analysis_astock_money";
        dropCollection(analysis_table);

        List<String> lists = Arrays.asList("2021一季报", "2020年报", "2020三季报", "2020中报", "2020一季报", "2019年报", "2019三季报", "2019中报");

        Query query = new Query();
        List<Map> mapStocks = mongoTemplate.find(query, Map.class, "xueqiu_astock");
        Map<String, Map> stockMap = mapStocks.stream().collect(Collectors.toMap(e -> e.get("symbol").toString().replace("SH", "").replace("SZ", ""), e -> e));


        query.addCriteria(new Criteria().andOperator(
                Criteria.where("report_date").in(lists)
        ));
        List<Map> maps = mongoTemplate.find(query, Map.class, "xueqiu_astock_report");


        Map<Object, List<Map>> collect = maps.stream()
                .collect(Collectors.groupingBy(e ->
                        e.get("symbol")
                ));


        List<Map> mapReportList = new ArrayList<>();

        collect.entrySet().forEach(e -> {
            List<Map> mapList = e.getValue();
            int size = mapList.size();
            if (size >= 5) {
                Optional<Map> reportOptional = mapList.stream().filter(ee -> ee.get("report_date").equals("2020年报")).findFirst();
                if (!reportOptional.isPresent()) return;
                Map report = reportOptional.get();
                String jinglirun = report.get("jinglirun").toString();
                reportOptional = mapList.stream().filter(ee -> ee.get("report_date").equals("2021一季报")).findFirst();
                if (!reportOptional.isPresent()) return;
                report = reportOptional.get();
                Double jingliruntongbizengzhang_report =Double.parseDouble(report.get("jingliruntongbizengzhang").toString());
                // 排序
                mapList.sort((a,b)->{
                    Long aa = getTimestamp(a.get("report_date").toString());
                    Long bb = getTimestamp(b.get("report_date").toString());
                    return bb>aa?1:-1;
                });
                if (!ObjectUtils.isEmpty(jinglirun) && Double.parseDouble(jinglirun) > 0 && mapList.stream().limit(5)
                        .allMatch(ee -> !ObjectUtils.isEmpty(ee.get("jingliruntongbizengzhang")) && Double.parseDouble(ee.get("jingliruntongbizengzhang").toString()) > 0)) {
                    String s = e.getKey().toString();
                    double jingliruntongbizengzhang = getAverage(mapList, "jingliruntongbizengzhang",5);
                    double yingyeshourutongbizengzhang = getAverage(mapList, "yingyeshourutongbizengzhang",5);
                    if (jingliruntongbizengzhang<0||yingyeshourutongbizengzhang<0||jingliruntongbizengzhang_report<jingliruntongbizengzhang)return;
                    String s1 = valueCalc(jinglirun, "0.11", String.valueOf(jingliruntongbizengzhang / 100), String.valueOf(jingliruntongbizengzhang / 200), 5);
                    String s2 = valueCalc(jinglirun, "0.11", String.valueOf(yingyeshourutongbizengzhang / 100), String.valueOf(yingyeshourutongbizengzhang / 200), 5);
                    Map map = stockMap.get(e.getKey().toString());
                    if (map == null) return;
                    String shiyinglv_ttm = map.get("shiyinglv_TTM").toString();
                    String zongshizhi = getString(map.get("zongshizhi"));
                    String name = map.get("name").toString();
                    String current = map.get("current").toString();
                    String zongguben = map.get("zongguben").toString();

                    LinkedHashMap linkedHashMap = new LinkedHashMap();
                    linkedHashMap.put("symbol", s);
                    linkedHashMap.put("name", name);
                    linkedHashMap.put("shiyinglv_TTM", shiyinglv_ttm);
                    linkedHashMap.put("zongguben", zongguben);
                    linkedHashMap.put("zongshizhi", zongshizhi);
                    linkedHashMap.put("current", current);
                    linkedHashMap.put("yy_zongshizhi", getString(s2));
                    linkedHashMap.put("yy_current", getString(s2, zongguben));
                    linkedHashMap.put("jl_zongshizhi", getString(s1));
                    linkedHashMap.put("jl_current", getString(s1, zongguben));
                    mapReportList.add(linkedHashMap);
                }
            }
        });
        mapReportList.forEach(e -> addData(e, analysis_table, "symbol", "name", "shiyinglv_TTM","zongguben",  "zongshizhi","current", "yy_zongshizhi", "yy_current", "jl_zongshizhi", "jl_current"));
    }

    private String getString(Object o) {
        if (ObjectUtils.isEmpty(o)) {
            return "0";
        }
        return new BigDecimal(o.toString()).setScale(1, RoundingMode.HALF_DOWN).toString();
    }

    private String getString(Object o, Object o1) {
        if (ObjectUtils.isEmpty(o1) || ObjectUtils.isEmpty(o)) {
            return "0";
        }
        return new BigDecimal(o.toString()).divide(new BigDecimal(o1.toString()), 1, BigDecimal.ROUND_HALF_UP).setScale(1, RoundingMode.HALF_DOWN).toString();
    }

    private double getAverage(List<Map> mapList, String s,int limit) {
        List<Map> list = mapList.stream().filter(ee -> ee.get(s) != null).limit(limit).sorted(Comparator.comparing(ee -> Double.parseDouble(ee.get(s).toString()))).collect(Collectors.toList());
        List<Map> collect = list.stream().skip(1).limit(limit - 2).collect(Collectors.toList());
        return collect.stream().mapToDouble(ee -> Double.parseDouble(ee.get(s).toString())).average().getAsDouble();
    }


    private String valueCalc(String freeMoney, String tiexianlv, String zengzhanglv, String zengzhanglv_yihou, int year) {
        if (Double.parseDouble(tiexianlv) < Double.parseDouble(zengzhanglv_yihou)) {
            zengzhanglv_yihou = (Double.parseDouble(tiexianlv) - 0.01) + "";
//            throw new RuntimeException("贴现率应该大于年后增长率");
        }
        //todo 前十年贴现
        BigDecimal sum_zhexian = new BigDecimal(0);
        BigDecimal ziyouxianjin = new BigDecimal(0);
        for (int i = 0; i < year; i++) {
            ziyouxianjin = new BigDecimal(freeMoney).multiply(BigDecimal.valueOf(1 + Double.parseDouble(zengzhanglv)).pow(i + 1));
            BigDecimal zhexian = ziyouxianjin.divide(BigDecimal.valueOf(1 + Double.parseDouble(tiexianlv)).pow(i + 1), RoundingMode.HALF_DOWN);
            sum_zhexian = zhexian.add(sum_zhexian);
        }
        //todo 十年之后贴现
        BigDecimal ziyouxianjin_yihou = ziyouxianjin.multiply(BigDecimal.valueOf(1 + Double.parseDouble(zengzhanglv_yihou)))
                .multiply(BigDecimal.valueOf(1 + Double.parseDouble(tiexianlv))
                        .divide(BigDecimal.valueOf(Double.parseDouble(tiexianlv) - Double.parseDouble(zengzhanglv_yihou)), RoundingMode.HALF_DOWN));
        BigDecimal sum_zhexian_yihou = ziyouxianjin_yihou
                .divide(BigDecimal.valueOf(1 + Double.parseDouble(tiexianlv)).pow(year + 1), RoundingMode.HALF_DOWN);
        String sum = sum_zhexian_yihou.add(sum_zhexian).setScale(1, RoundingMode.HALF_DOWN).toString();
        return sum;
    }


}

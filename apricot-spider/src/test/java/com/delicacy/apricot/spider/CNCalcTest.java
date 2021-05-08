package com.delicacy.apricot.spider;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Slf4j
public class CNCalcTest extends ApricotSpiderApplicationTests {

    @Test
    public void sumcalc() {
        stock();
        stock_report();
        fund_rank_position();

        Query query = new Query();

        List<Map> maps = mongoTemplate.find(query, Map.class, "analysis_astock_report");
        List<Object> symbols = maps.stream().map(e -> e.get("symbol")).collect(Collectors.toList());

        query.addCriteria(new Criteria().andOperator(
                Criteria.where("symbol").in(symbols)
        ));
        List<Map> maps2 = mongoTemplate.find(query, Map.class, "analysis_fund_rank_position");
        symbols = maps2.stream().filter(e -> Integer.parseInt(e.get("count").toString()) >= 10
        ).map(e -> e.get("symbol")).collect(Collectors.toList());

        query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("symbol").in(symbols)
        ));
        List<Map> maps3 = mongoTemplate.find(query, Map.class, "analysis_astock");

        maps3.stream().map(e -> {
            Object symbol = e.get("symbol");
            Map map = maps2.stream().filter(ee -> symbol.equals(ee.get("symbol"))).findFirst().get();
            e.putAll(map);
            return e;
        }).sorted(Comparator.comparing(e -> -Integer.parseInt(e.get("count").toString()))).forEach(System.out::println);

    }


    @Test
    public void stock() {
        //todo 删除table
        String analysis_table = "analysis_astock";
        dropCollection(analysis_table);

        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("$where").is("this.shiyinglv_TTM * 1 < this.shiyinglv_jing * 1"),
                Criteria.where("$where").is("this.shiyinglv_TTM > 0"),
                Criteria.where("$where").is("this.current < 200"),
                Criteria.where("$where").is("this.shiyinglv_dong * 1 < this.shiyinglv_jing * 1"),
                Criteria.where("$where").is("this.shiyinglv_TTM < 120")
        ));
        List<Map> maps = mongoTemplate.find(query, Map.class, "xueqiu_astock");

        maps.stream().filter(e -> {
            try {
//                Double guxilv_TTM = percentData(e.get("guxilv_TTM").toString());
                Double zongshizhi = moneyData(e.get("zongshizhi").toString());
                return zongshizhi > 2000000000L; //&& guxilv_TTM > 0.5;//50亿
            } catch (IllegalArgumentException e1) {
                return false;
            }
        }).forEach(e -> {
            e.put("symbol", String.valueOf(e.get("symbol")).replace("SZ", "").replace("SH", ""));
            System.out.println(
                    String.format("%s_%s_%s_%s_%s_%s_%s_%s",
                            e.get("symbol"),
                            e.get("name"),
                            e.get("current"),
                            e.get("zongshizhi"),
                            e.get("guxilv_TTM"),
                            e.get("shiyinglv_TTM"),
                            e.get("shiyinglv_dong"),
                            e.get("shiyinglv_jing"))
            );
            addData(e, analysis_table, "symbol", "name", "current", "zongshizhi", "guxilv_TTM", "shiyinglv_TTM", "shiyinglv_dong", "shiyinglv_jing");
        });
    }


    @Test
    public void stock_report() {
        //todo 删除table
        String analysis_table = "analysis_astock_report";
        dropCollection(analysis_table);


        String data1 = "2021一季报";
        String data2 = "2020一季报";

        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("report_date").in(data1, data2)
        ));
        List<Map> maps = mongoTemplate.find(query, Map.class, "xueqiu_astock_report");

        Map<Object, List<Map>> collect = maps.stream().collect(Collectors.groupingBy(e ->
                e.get("symbol")
        ));

        ArrayList<String> objects = Lists.newArrayList();
        collect.entrySet().stream().forEach(e -> {
            List<Map> mapList = e.getValue();
            int size = mapList.size();
            if (size == 2) {
                Map map1, map2;
                if (mapList.get(0).get("report_date").toString().contains(data1)) {
                    map1 = mapList.get(0);
                    map2 = mapList.get(1);
                } else {
                    map1 = mapList.get(1);
                    map2 = mapList.get(0);
                }
                List<ControlParam> list = new ArrayList<>();
                list.add(new ControlParam("yingyeshouru", 1, true));
                list.add(new ControlParam("jinglirun", 1, true));
                list.add(new ControlParam("meigushouyi", 1, true));
                list.add(new ControlParam("jingzichanshouyilv", 1, true));
                list.add(new ControlParam("renlitouruhuibaolv", 2, true));
                list.add(new ControlParam("xiaoshoumaolilv", 2, true));
                list.add(new ControlParam("zichanfuzhailv", 2, false));
                list.add(new ControlParam("liudongbilv", 1, true));
                list.add(new ControlParam("sudongbilv", 1, true));
                list.add(new ControlParam("cunhuozhouzhuanlv", 2, true));
                list.add(new ControlParam("yingyezhouqi", 2, false));
                list.add(new ControlParam("xianjinxunhuanzhouqi", 2, false));


                Boolean result = calcResult(map1, map2, list);
                if (result) {
                    objects.add(String.format("%s_%s_%s",
                            e.getKey(), map1.get("name"),
                            map1.get("report_date")));
                    addData(map1, analysis_table, "symbol", "name", "report_date");
                }

            }
        });
        objects.forEach(System.out::println);

    }

    private Boolean calcResult(Map map1, Map map2, List<ControlParam> list) {
        boolean flag = true;
        AtomicInteger mustRealCount = new AtomicInteger(0);
        AtomicInteger mustCount = new AtomicInteger(0);
        AtomicInteger mayRealCount = new AtomicInteger(0);
        AtomicInteger mayCount = new AtomicInteger(0);

        list.forEach(e -> {
            String key = e.getKey();

            if (ObjectUtils.isEmpty(map1.get(key)) || ObjectUtils.isEmpty(map2.get(key))) {
                return;
            }

            String o1 = map1.get(key).toString();
            String o2 = map2.get(key).toString();
            if (o1.contains("-")) {
                mustCount.incrementAndGet();
                mayCount.incrementAndGet();
                return;
            }
            if (o2.contains("-")) {
                return;
            }
            if (e.getWeigth() == 1) {
                if (e.getDirect() && getDouble(o1) * (flag ? 1 : 0.7) >= getDouble(o2)) {
                    mustRealCount.incrementAndGet();
                } else if (!e.getDirect() && getDouble(o1) * (flag ? 1 : 1.3) <= getDouble(o2)) {
                    mustRealCount.incrementAndGet();
                }
                mustCount.incrementAndGet();
            } else {
                if (e.getDirect() && getDouble(o1) * (flag ? 1.05 : 1) >= getDouble(o2)) {
                    mayRealCount.incrementAndGet();
                } else if (!e.getDirect() && getDouble(o1) * (flag ? 0.95 : 1) <= getDouble(o2)) {
                    mayRealCount.incrementAndGet();
                }
                mayCount.incrementAndGet();
            }
        });
        if (mustRealCount.intValue() == mustCount.intValue()) {
            if (mayCount.intValue() - (flag ? mayCount.intValue() : 0) <= mayRealCount.intValue()) {
                return true;
            }
        }

        return false;
    }

    private Double getDouble(String val) {
        if (val.contains("次")) {
            return numData(val);
        } else if (val.contains("%")) {
            return percentData(val);
        } else if (val.contains("天")) {
            return dayData(val);
        } else {
            return moneyData(val);
        }

    }


    @Test
    public void analysis_fund_rank_position() {
        String analysis_table = "twice_analysis_fund_rank_position";
        fund_rank_position_do("jin1zhou");
        System.out.println("================================");
        dropCollection( analysis_table);
        Query query = new Query();

        List<Map> maps1 = mongoTemplate.find(query, Map.class, "analysis_fund_rank_position");

        List<Map> maps2 = mongoTemplate.find(query, Map.class, "analysis_fund_rank_position_2021_05_04");

//        List<Map> maps3 = mongoTemplate.find(query, Map.class, "analysis_fund_rank_position_2021_05_04");

        List<String> objects = new ArrayList<>();
        Map<String,Integer> keyValue = new HashMap<>();
        maps1.forEach(e->{
            String symbol = e.get("symbol").toString();
            String name = e.get("name").toString();
            String key = symbol + ":" + name;
            String count1 = e.get("count").toString();
            maps2.forEach(ee->{
                if (symbol.equals(ee.get("symbol"))){
                    keyValue.put(key,Integer.parseInt(count1) + Integer.parseInt(ee.get("count").toString()));
                }
            });
//            nextCalc(maps3, keyValue, symbol);
            Integer result = keyValue.get(key);
            if (result!=null){
                addRecord(objects,analysis_table, symbol, result, name);
            }
        });

        objects.sort(Comparator.comparing(e -> Integer.valueOf(((String) e).split("_")[2])).reversed());
        objects.forEach(System.out::println);
    }

    private void nextCalc(List<Map> maps3, Map<String, Integer> keyValue, Object symbol1) {
        maps3.forEach(ee->{
            Object symbol = ee.get("symbol");
            if (symbol1.equals(symbol)){
                 keyValue.forEach((k, v)->{
                    if (k.equals(symbol)){
                        int result = v + Integer.parseInt(ee.get("count").toString());
                        keyValue.put(k,result);
                    }
                });
            }
        });
    }

    private void addRecord(List<String> objects,String analysis_table, Object symbol, int result, Object name) {
        LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
        map.put("symbol", symbol);
        map.put("name", name);
        map.put("huanbi", result);
        addData(map, analysis_table, "symbol", "name", "huanbi");
        objects.add(String.format("%s_%s_%s", symbol, name,result ));
    }


    @Test
    public void fund_rank_position(){
        fund_rank_position_do(null);
    }

    private void fund_rank_position_do(String leixing) {
        //todo 删除table
        String analysis_table = "analysis_fund_rank_position";
        renameCollection("spider", analysis_table);

        // todo 近1月赢利最多的基金
        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("type").in("gpx", "hhx", "ETF").
                        and(Objects.nonNull(leixing)?"jin1yue":leixing).ne("")
        ));
        List<Map> list = mongoTemplate.find(query, Map.class, "aijijin_fund_rank");
        Collections.sort(list, Comparator.comparing(e -> Double.valueOf((String) ((Map) e).get(Objects.nonNull(leixing)?"jin1yue":leixing))).reversed());

        List<Object> symbol = list.stream().map(e -> e.get("symbol")).limit(1000).collect(Collectors.toList());

        query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("fund_code").in(symbol),
                Criteria.where("data_update_time").is("2020-12-31")
        ));
        List<Map> maps = mongoTemplate.find(query, Map.class, "aijijin_fund_position");

        // todo 分组排序 根据股票代码分组，占净值比例>4
        Map<Object, List<Map>> collect = maps.stream().filter(e -> {
            try {
                if (e.get("gupiaodaima") == null) {
                    return false;
                }
                Double zhanjingzhibili = percentData(e.get("zhanjingzhibi").toString());
                return zhanjingzhibili > 0;
            } catch (IllegalArgumentException e1) {
                return false;
            }
        }).collect(Collectors.groupingBy(e ->
                e.get("gupiaodaima")
        ));
        // todo 显示结果 股票代码 股票名称 买入总个数
        ArrayList<String> objects = Lists.newArrayList();
        collect.entrySet().stream().forEach(e -> {
            int size = e.getValue().size();
            if (size >= 5) {
                String s = String.format("%s_%s_%s", e.getKey(), e.getValue().get(0).get("gupiaomingcheng"), size);
                objects.add(s);
                LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
                map.put("symbol", e.getKey());
                map.put("name", e.getValue().get(0).get("gupiaomingcheng"));
                map.put("count", String.valueOf(size));
                addData(map, analysis_table, "symbol", "name", "count");
            }
        });
        Collections.sort(objects, Comparator.comparing(e -> Integer.valueOf(((String) e).split("_")[2])).reversed());
        objects.stream().forEach(System.out::println);
    }

    @Data
    @AllArgsConstructor
    static class ControlParam {
        private String key;
        private Integer weigth;
        private Boolean direct;
    }


}

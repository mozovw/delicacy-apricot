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
public class MongoCalcTest extends ApricotSpiderApplicationTests {

    @Test
    public void sumcalc() {
        astock();
        astock_report();
        fund_rank_position();

        Query query = new Query();
//        List<Map> maps = new ArrayList<>();
//        List<Object> symbols = new ArrayList<>();

        List<Map> maps = mongoTemplate.find(query, Map.class, "astock_report_analysis");
        List<Object> symbols = maps.stream().map(e -> e.get("symbol")).collect(Collectors.toList());

//        query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("symbol").in(symbols)
        ));
        List<Map> maps2 = mongoTemplate.find(query, Map.class, "fund_rank_position_analysis");
        symbols = maps2.stream().filter(e -> Integer.valueOf(e.get("count").toString()) >= 0).map(e -> e.get("symbol")).collect(Collectors.toList());
//        maps.stream().filter(e -> Integer.valueOf(e.get("count").toString()) >= 10).forEach(System.out::println);

        query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("symbol").in(symbols)
        ));
        List<Map> maps3 = mongoTemplate.find(query, Map.class, "astock_analysis");
        //symbols = maps.stream().map(e -> e.get("symbol")).collect(Collectors.toList());


        maps3.stream().map(e->{
            Object symbol = e.get("symbol");
            Map map = maps2.stream().filter(ee -> symbol.equals(ee.get("symbol"))).findFirst().get();
            e.putAll(map);
            return e;
        }).forEach(System.out::println);

    }


    @Test
    public void astock() {
        //todo 删除table
        String analysis_table = "astock_analysis";
        dropCollection(analysis_table);

        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("$where").is("this.shiyinglv_TTM < this.shiyinglv_jing"),
//                Criteria.where("$where").is("this.shiyinglv_dong * 1.2 < this.shiyinglv_jing"),
                Criteria.where("$where").is("this.shiyinglv_TTM < 100")
        ));
        List<Map> maps = mongoTemplate.find(query, Map.class, "stock");

        maps.stream().filter(e -> {
            try {
//                Double guxilv_TTM = percentData(e.get("guxilv_TTM").toString());
                Double zongshizhi = moneyData(e.get("zongshizhi").toString());
                return zongshizhi > 5000000000L; //&& guxilv_TTM > 0.5;//50亿
            } catch (IllegalArgumentException e1) {
                return false;
            }
        }).forEach(e -> {
            System.out.println(
                    String.format("%s_%s_%s_%s_%s_%s_%s_%s",
                            e.get("symbol"),
                            e.get("name"),
                            e.get("jinkai"),
                            e.get("zongshizhi"),
                            e.get("guxilv_TTM"),
                            e.get("shiyinglv_TTM"),
                            e.get("shiyinglv_dong"),
                            e.get("shiyinglv_jing"))
            );
            addData(e, analysis_table, "symbol", "name", "jinkai", "zongshizhi", "guxilv_TTM", "shiyinglv_TTM", "shiyinglv_dong", "shiyinglv_jing");
        });
    }


    @Test
    public void astock_report() {
        //todo 删除table
        String analysis_table = "astock_report_analysis";
        dropCollection(analysis_table);


        String data1 = "2020三季报";
        String data2 = "2019三季报";

        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("report_date").in(data1, data2)
        ));
        List<Map> maps = mongoTemplate.find(query, Map.class, "stock_report");

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
                list.add(new ControlParam("liudongbilv", 2, true));
                list.add(new ControlParam("cunhuozhouzhuanlv", 2, true));
                list.add(new ControlParam("yingyezhouqi", 2, false));
                list.add(new ControlParam("xianjinxunhuanzhouqi", 2, false));


                Boolean result = calcResult(map1, map2, list);
                if (result){
                    objects.add(String.format("%s_%s_%s",
                            e.getKey(), map1.get("name"),
                            map1.get("report_date")));
                    addData(map1, analysis_table, "symbol=" + e.getKey(), "name", "report_date");
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
            if (o1.contains("-")){
                mustCount.incrementAndGet();
                mayCount.incrementAndGet();
                return;
            }
            if (o2.contains("-")){
                return;
            }
            if (e.getWeigth() == 1) {
                if (e.getDirect() && getDouble(o1) *(flag?1:0.7) >= getDouble(o2)) {
                    mustRealCount.incrementAndGet();
                } else if(!e.getDirect() && getDouble(o1)*(flag?1:1.3) <= getDouble(o2)){
                    mustRealCount.incrementAndGet();
                }
                mustCount.incrementAndGet();
            } else {
                if (e.getDirect() && getDouble(o1) *(flag?1.05:1)  >= getDouble(o2)) {
                    mayRealCount.incrementAndGet();
                } else if(!e.getDirect() && getDouble(o1)* (flag?0.95:1) <= getDouble(o2)){
                    mayRealCount.incrementAndGet();
                }
                mayCount.incrementAndGet();
            }
        });
        if (mustRealCount.intValue()==mustCount.intValue()){
            if (mayCount.intValue()-(flag?mayCount.intValue():0)<=mayRealCount.intValue()){
                return true;
            }
        }

        return false;
    }

    private Double getDouble(String val){
         if (val.contains("次") ) {
            return numData(val);
        } else if (val.contains("%") ) {
            return  percentData(val);
        } else if (val.contains("天") ) {
            return dayData(val);
        } else {
            return moneyData(val);
        }

    }

    @Test
    public void fund_rank_position() {
        //todo 删除table
        String analysis_table = "fund_rank_position_analysis";
        dropCollection(analysis_table);

        // todo 近1月赢利最多的基金
        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("type").in("gpx", "hhx", "ETF").
                        and("jin1yue").ne("")
        ));
        List<Map> list = mongoTemplate.find(query, Map.class, "aijijin_fund_rank");
        Collections.sort(list, Comparator.comparing(e -> Double.valueOf((String) ((Map) e).get("jin3yue"))).reversed());

        List<Object> symbol = list.stream().map(e -> e.get("symbol")).limit(1000).collect(Collectors.toList());

        query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("fund_code").in(symbol),
                Criteria.where("data_update_time").is("2020-09-30")
        ));
        List<Map> maps = mongoTemplate.find(query, Map.class, "aijijin_fund_position");

        // todo 分组排序 根据股票代码分组，占净值比例>4
        Map<Object, List<Map>> collect = maps.stream().filter(e -> {
            try {
                Double zhanjingzhibili = percentData(e.get("zhanjingzhibi").toString());
                return zhanjingzhibili > 4;
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

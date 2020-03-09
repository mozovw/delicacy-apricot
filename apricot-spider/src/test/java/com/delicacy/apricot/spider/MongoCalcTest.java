package com.delicacy.apricot.spider;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
public class MongoCalcTest extends ApricotSpiderApplicationTests {

    @Test
    public void sumcalc() {
        astock();
        astock_report();
        fund_rank_position();

        Query query = new Query();
        List<Map> maps = mongoTemplate.find(query, Map.class, "astock_report_analysis");
        List<Object> symbols = maps.stream().map(e -> e.get("symbol")).collect(Collectors.toList());

        query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("symbol").in(symbols)
        ));
        maps = mongoTemplate.find(query, Map.class, "fund_rank_analysis");
        symbols = maps.stream().map(e -> e.get("symbol")).collect(Collectors.toList());
        maps.stream().forEach(System.out::println);

        query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("symbol").in(symbols)
        ));
        maps = mongoTemplate.find(query, Map.class, "astock_analysis");
        //symbols = maps.stream().map(e -> e.get("symbol")).collect(Collectors.toList());
        maps.stream().forEach(System.out::println);

    }


    @Test
    public void astock() {
        //todo 删除table
        String analysis_table = "astock_analysis";
        dropCollection(analysis_table);

        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("$where").is("this.shiyinglv_dong < this.shiyinglv_TTM"),
                Criteria.where("$where").is("this.shiyinglv_TTM < 100")
        ));
        List<Map> maps = mongoTemplate.find(query, Map.class, "astock");

        maps.stream().filter(e -> {
            try {
                Double guxilv_TTM = percentData(e.get("guxilv_TTM").toString());
                Double zongshizhi = moneyData(e.get("zongshizhi").toString());
                return zongshizhi > 1000000000; //&& guxilv_TTM > 0.5;//10亿
            } catch (IllegalArgumentException e1) {
                return false;
            }
        }).forEach(e -> {
            System.out.println(
                    String.format("%s_%s_%s_%s_%s_%s",
                            e.get("symbol") ,
                            e.get("name") ,
                            e.get("zongshizhi") ,
                            e.get("guxilv_TTM") ,
                            e.get("shiyinglv_dong") ,
                            e.get("shiyinglv_jing"))
            );
            addData(e, analysis_table,"symbol","name","zongshizhi","guxilv_TTM","shiyinglv_dong","shiyinglv_jing");
        });
    }

    private void addData(Map e,String table,String... keys) {
        LinkedHashMap<Object, Object> objectObjectLinkedHashMap = Maps.newLinkedHashMap();
        for (String key : keys) {
            if (key.contains("##")){
                String[] split = key.split("##");
                objectObjectLinkedHashMap.put(split[0], split[1]);
                continue;
            }
            objectObjectLinkedHashMap.put(key, e.get(key));
        }
        mongoTemplate.insert(objectObjectLinkedHashMap,table);
    }

    private void dropCollection(String analysis) {
        if(mongoTemplate.collectionExists(analysis)){
           mongoTemplate.dropCollection(analysis);
       }
    }

    @Test
    public void astock_report() {
        //todo 删除table
        String analysis_table = "astock_report_analysis";
        dropCollection(analysis_table);

        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("report_date").in("2019三季报", "2018三季报")
        ));
        List<Map> maps = mongoTemplate.find(query, Map.class, "astock_report");

        Map<Object, List<Map>> collect = maps.stream().collect(Collectors.groupingBy(e ->
                e.get("symbol")
        ));

        ArrayList<String> objects = Lists.newArrayList();
        collect.entrySet().stream().forEach(e -> {
            List<Map> mapList = e.getValue();
            int size = mapList.size();
            if (size == 2) {
                Map map1,map2;
                if(mapList.get(0).get("report_date").toString().contains("2019三季报")){
                    map1 = mapList.get(0);
                    map2 = mapList.get(1);
                }else {
                    map1 = mapList.get(1);
                    map2 = mapList.get(0);
                }
                Map<String, Integer> map = getMap(map1, map2, "yingyeshouru"
                        , "jinglirun"
                        , "meigushouyi"
                        , "meigujingyingxianjinliu"
                        , "jingzichanshouyilv"
                        , "renlitouruhuibaolv"
                        , "xianjinliuliangbilv"
                        , "yingyezhouqi"
                        , "cunhuozhouzhuanlv"
                        , "xiaoshoumaolilv"
                        , "xiaoshoujinglilv"
                );
                Map<Integer, List<Map.Entry<String, Integer>>> integerListMap = map.entrySet().stream().collect(Collectors.groupingBy(ee ->
                        ee.getValue()
                ));
                List<Map.Entry<String, Integer>> entries1 = integerListMap.get(1);
                List<Map.Entry<String, Integer>> entries2 = integerListMap.get(2);
                String collect1,collect2;
                int size1,size2;
                if(ObjectUtils.isEmpty(entries1)){
                    collect1 = "";
                    size1 = 0;
                }else{
                    collect1 = entries1.stream().map(eee ->
                            eee.getKey().toString()
                    ).collect(Collectors.joining("_"));
                    size1 = entries1.size();

                }

                if(ObjectUtils.isEmpty(entries2)){
                    collect2 = "";
                    size2 = 0;
                }else{
                    collect2 = entries2.stream().map(eee ->
                            eee.getKey().toString()
                    ).collect(Collectors.joining("_"));
                    size2 = entries2.size();
                }

                int num = 9;
                if (size1 > size2) {
                    if (size1 > num) {
                        objects.add(String.format("%s_%s_%s_%s",
                                e.getKey(),map1.get("name"),
                                map1.get("report_date"), collect1));
                        addData(map1, analysis_table,"symbol##"+e.getKey(),"name","report_date","collection##"+collect1);
                    }
                } else {
                    return;
//                    if (size2 > num) {
//                        String s = String.format("%s_%s_%s_%s",
//                                e.getKey(),map2.get("name"),
//                                map2.get("report_date"), collect2);
//                        objects.add(s);
//                    }
                }

            }
        });
        objects.stream().forEach(System.out::println);

    }

    private Map<String, Integer> getMap(Map map1, Map map2, String... keys) {
        Map<String, Integer> mapbool = new HashMap<>();

        Arrays.stream(keys).forEach(key -> {
            Object o1 = map1.get(key);
            Object o2 = map2.get(key);

            if (ObjectUtils.isEmpty(o1) || ObjectUtils.isEmpty(o2)) return;

            String string1 = o1.toString();
            String string2 = o2.toString();
            if(string1.contains("-")||string2.contains("-")) {

            }else if(string1.contains("次")||string2.contains("次")){

                if (numData(string1) >= numData(string2)) {
                    mapbool.put(key, 1);
                } else {
                    mapbool.put(key, 2);
                }
            }else if(string1.contains("%")||string2.contains("%")){
                if (percentData(string1) >= percentData(string2)) {
                    mapbool.put(key, 1);
                } else {
                    mapbool.put(key, 2);
                }
            }else if(string1.contains("天")||string2.contains("天")){
                if (dayData(string1) <= dayData(string2)) {
                    mapbool.put(key, 1);
                } else {
                    mapbool.put(key, 2);
                }
            }else {

                if (moneyData(string1) >= moneyData(string2)) {
                    mapbool.put(key, 1);
                } else {
                    mapbool.put(key, 2);
                }
            }
        });

        return mapbool;
    }


    @Test
    public void fund_rank_position() {
        //todo 删除table
        String analysis_table = "fund_rank_analysis";
        dropCollection(analysis_table);

        // todo 近3月赢利最多的基金
        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("type").in("hh", "gp").
                        and("jin3yue").ne("")
        ));
        List<Map> list = mongoTemplate.find(query, Map.class, "fund_rank");
        Collections.sort(list, Comparator.comparing(e -> Double.valueOf((String) ((Map) e).get("jin3yue"))).reversed());

        List<Object> symbol = list.stream().map(e -> e.get("symbol")).limit(500).collect(Collectors.toList());

        query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("fund_code").in(symbol),
                Criteria.where("fund_period_title").is("2019年4季度股票投资明细")
        ));
        List<Map> maps = mongoTemplate.find(query, Map.class, "fund_position");

        // todo 分组排序 根据股票代码分组，占净值比例>4
        Map<Object, List<Map>> collect = maps.stream().filter(e -> {
            try {
                Double zhanjingzhibili = percentData(e.get("zhanjingzhibili").toString());
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
            if (size > 4) {
                String s = String.format("%s_%s_%s", e.getKey(), e.getValue().get(0).get("gupiaomingcheng"), size);
                objects.add(s);
                addData(null, analysis_table,"symbol##"+e.getKey(),"name##"+e.getValue().get(0).get("gupiaomingcheng")
                       ,"count##"+size);

            }
        });
        Collections.sort(objects, Comparator.comparing(e -> Integer.valueOf(((String) e).split("_")[2])).reversed());
        objects.stream().forEach(System.out::println);
    }


    private Double percentData(String string) {
        if (string.equals("--")) throw new IllegalArgumentException("exists '--'");
        Double value;
        if (string.contains("万%")) {
            value = Double.valueOf(string.replace("万%", ""));

        }else    if (string.contains("%")) {
            value = Double.valueOf(string.replace("%", ""));
        } else {
            value = Double.valueOf(string);
        }
        return value;
    }

    private Double dayData(String string) {
        if (ObjectUtils.isEmpty(string)) return 0.0;
        Double value;
        if (string.contains("万天")) {
            value = Double.valueOf(string.replace("万天", ""))*10000;
        }else if (string.contains("天")) {
            value = Double.valueOf(string.replace("天", ""));
        }  else {
            value = Double.valueOf(string);
        }
        return value;
    }

    private Double numData(String string) {
        if (ObjectUtils.isEmpty(string)) return 0.0;
        Double value;
        if (string.contains("万次")) {
            value = Double.valueOf(string.replace("万次", ""));
        }else  if (string.contains("次")) {
            value = Double.valueOf(string.replace("次", ""));
        } else {
            value = Double.valueOf(string);
        }
        return value;
    }

    private Double moneyData(String string) {
        if (string.equals("--")) throw new IllegalArgumentException("exists '--'");
        Double value;
        if (string.contains("万亿")) {
            value = Double.valueOf(string.replace("万亿", "")) * 1000000000000L;
        } else if (string.contains("亿")) {
            value = Double.valueOf(string.replace("亿", "")) * 100000000L;
        } else if (string.contains("万")) {
            value = Double.valueOf(string.replace("万", "")) * 10000L; }
        else if (string.contains("元")) {
            value = Double.valueOf(string.replace("元", "")) * 1L;
        } else {
            value = Double.valueOf(string);
        }
        return value;
    }


}

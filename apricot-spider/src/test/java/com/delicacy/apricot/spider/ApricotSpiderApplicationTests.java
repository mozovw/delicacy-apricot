package com.delicacy.apricot.spider;

import cn.hutool.core.date.DateUtil;
import com.google.common.collect.Maps;
import com.mongodb.MongoNamespace;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@RunWith(value = SpringRunner.class)
@SpringBootTest
public class ApricotSpiderApplicationTests {
    @Autowired
    protected MongoTemplate mongoTemplate;

    @Test
    public void contextLoads() {

    }

    protected void addData(Map e, String table, String... keys) {
        LinkedHashMap<Object, Object> objectObjectLinkedHashMap = Maps.newLinkedHashMap();
        for (String key : keys) {
            if (key.contains("=")) {
                String[] split = key.split("=");
                objectObjectLinkedHashMap.put(split[0], split[1]);
                continue;
            }
            objectObjectLinkedHashMap.put(key, e.get(key));
        }
        mongoTemplate.insert(objectObjectLinkedHashMap, table);
    }

    protected boolean dropCollection(String analysis) {
        boolean collectionExists = mongoTemplate.collectionExists(analysis);
        if (collectionExists) {
            mongoTemplate.dropCollection(analysis);
        }
        return collectionExists;
    }

    protected void renameCollection(String dbName,String analysis) {
        String yyyy_mm_dd = DateUtil.format(new Date(), "yyyy_MM_dd");
        String fullName = analysis + "_" + yyyy_mm_dd;
        dropCollection(fullName);
        if (mongoTemplate.collectionExists(analysis)) {
            mongoTemplate.getCollection(analysis).renameCollection(new MongoNamespace(dbName,fullName));
        }
    }


    protected void todo(String collectionName, Consumer<List<Map>> consumer) {
        todo(null, collectionName, consumer);
    }

    protected void todo(String orderName, String collectionName, Consumer<List<Map>> consumer) {
        Query query = new Query();
        long count = mongoTemplate.count(query, Map.class, collectionName);
        for (int i = 0; i < count; i++) {
            if (i % 10 != 0) continue;
            if (!ObjectUtils.isEmpty(orderName)) {
                Sort datetime = new Sort(Sort.Direction.DESC, orderName);
                query = query.with(datetime);
            }
            List<Map> maps = mongoTemplate.find(query.skip(i).limit(10), Map.class, collectionName);
            consumer.accept(maps);
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    protected Double percentData(String string) {
        if (string.equals("--")) throw new IllegalArgumentException("exists '--'");
        Double value;
        if (string.contains("万%")) {
            value = Double.valueOf(string.replace("万%", ""));

        } else if (string.contains("%")) {
            value = Double.valueOf(string.replace("%", ""));
        } else {
            value = Double.valueOf(string);
        }
        return value;
    }

    protected Double dayData(String string) {
        if (ObjectUtils.isEmpty(string)) return 0.0;
        Double value;
        if (string.contains("万天")) {
            value = Double.valueOf(string.replace("万天", "")) * 10000;
        } else if (string.contains("天")) {
            value = Double.valueOf(string.replace("天", ""));
        } else {
            value = Double.valueOf(string);
        }
        return value;
    }

    protected Double numData(String string) {
        if (ObjectUtils.isEmpty(string)) return 0.0;
        Double value;
        if (string.contains("万次")) {
            value = Double.valueOf(string.replace("万次", ""));
        } else if (string.contains("次")) {
            value = Double.valueOf(string.replace("次", ""));
        } else {
            value = Double.valueOf(string);
        }
        return value;
    }

    protected Double moneyData(String string) {
        if (string.equals("--")) throw new IllegalArgumentException("exists '--'");
        Double value;
        if (string.contains("万亿")) {
            value = Double.valueOf(string.replace("万亿", "")) * 1000000000000L;
        } else if (string.contains("亿")) {
            value = Double.valueOf(string.replace("亿", "")) * 100000000L;
        } else if (string.contains("万")) {
            value = Double.valueOf(string.replace("万", "")) * 10000L;
        } else if (string.contains("元")) {
            value = Double.valueOf(string.replace("元", "")) * 1L;
        } else {
            value = Double.valueOf(string);
        }
        return value;
    }
}

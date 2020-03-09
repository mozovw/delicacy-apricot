package com.delicacy.apricot.spider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ObjectUtils;

import javax.swing.*;
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


	protected void todo(String collectionName,Consumer<List<Map>> consumer) {
		todo(null,collectionName,consumer);
	}

	protected void todo(String orderName,String collectionName,Consumer<List<Map>> consumer) {
		Query query = new Query();
		long count = mongoTemplate.count(query, Map.class, collectionName);
		for (int i = 0; i < count; i++) {
			if (i%10!=0)continue;
			if (!ObjectUtils.isEmpty(orderName)){
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
}

package com.delicacy.apricot.spider.runner;


import com.delicacy.apricot.spider.pipeline.MongoPipeline;
import com.delicacy.apricot.spider.processor.ximalaya.EnglishMonsterProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Spider;

@Component
@Order(value = 1)
@Slf4j
public class XiMaLaYaRunner extends AbstractRunner {

    @Override
    public void run(String... args) throws Exception {
       if(!checkArgs(2, getCommand(this), args))return;
        String waiyu = String.valueOf(args[1]);
        if (waiyu.equalsIgnoreCase("3965403"))
            Spider.create(new EnglishMonsterProcessor()).addUrl("https://www.ximalaya.com/waiyu/3965403/p1")
                    .addPipeline(new MongoPipeline(mongoTemplate, "english_monster","title"))
                    .thread(5)
                    .runAsync();
    }
}
package com.delicacy.apricot.spider.runner;


import com.delicacy.apricot.spider.pipeline.Map3MongoPipeline;
import com.delicacy.apricot.spider.pipeline.MongoPipeline;
import com.delicacy.apricot.spider.processor.xueqiu.AstockProcessor;
import com.delicacy.apricot.spider.processor.xueqiu.AstockReportProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.model.HttpRequestBody;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.utils.HttpConstant;

import java.util.HashMap;

@Component
@Order(value = 1)
@Slf4j
public class XueQiuRunner extends AbstractRunner {


    @Override
    public void run(String... args) throws Exception {
        if(!checkArgs(2, getCommand(this), args))return;
        Request request = getRequest("https://xueqiu.com/service/v5/stock/screener/quote/list?page=1&size=90&order=desc&orderby=percent&order_by=percent&market=CN&type=sh_sz&_=1574236784261");
        if (String.valueOf(args[1]).equalsIgnoreCase("cn")) {
            AstockProcessor astockProcessor = new AstockProcessor();
            astockProcessor.setSite(getSite("xueqiu.com"));
//            dropCollection("astock");
            Spider.create(astockProcessor)
                    .thread(5)
                    .addRequest(request)
//                    .addPipeline(new ConsolePipeline())
                    .addPipeline(new MongoPipeline(mongoTemplate, "astock","symbol"))
                    .run();
        }
        if (String.valueOf(args[1]).equalsIgnoreCase("cn_report")) {
            AstockReportProcessor astockProcessor = new AstockReportProcessor();
            astockProcessor.setSite(getSite("xueqiu.com"));
            Spider.create(astockProcessor)
                    .thread(5)
                    .addRequest(request)
//                    .addPipeline(new ConsolePipeline())
                    .addPipeline(new Map3MongoPipeline(mongoTemplate, "astock_report","symbol","reportDate"))
                    .runAsync();
        }
    }




}
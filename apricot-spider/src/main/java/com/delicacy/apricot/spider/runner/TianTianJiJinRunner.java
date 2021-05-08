package com.delicacy.apricot.spider.runner;


import com.delicacy.apricot.spider.pipeline.Map2MongoPipeline;
import com.delicacy.apricot.spider.pipeline.Map3MongoPipeline;
import com.delicacy.apricot.spider.processor.tiantianjijin.FundPositionProcessor;
import com.delicacy.apricot.spider.processor.tiantianjijin.FundRankProcessor;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;

import java.util.Arrays;
import java.util.List;

@Component
@Order(value = 1)
@Slf4j
public class TianTianJiJinRunner extends AbstractRunner {
    String url = "http://fund.eastmoney.com/data/rankhandler.aspx?op=ph&dt=kf&ft=all&rs=&gs=0&sc=zzf&st=desc&qdii=&tabSubtype=,,,,,&pi=1&pn=50&dx=1&v=0.9909794822903422";
    String[] urlList ={"http://fund.eastmoney.com/data/rankhandler.aspx?op=ph&dt=kf&ft=all&rs=&gs=0&sc=zzf&st=desc&sd=2019-11-18&ed=2020-11-18&qdii=&tabSubtype=,,,,,&pi=1&pn=50&dx=1&v=0.7734570510677579"};


    @Override
    public void run(String... args)  {
        if(!checkArgs(2, getCommand(this), args))return;

        Request request = getRequest(url);
        if (String.valueOf(args[1]).equalsIgnoreCase("fundposition")) {
            FundPositionProcessor processor = new FundPositionProcessor();
            processor.setSite(getSite("fund.eastmoney.com").setTimeOut(10000));
            dropCollection("fund_position");
            Spider.create(processor)
                    .thread(threadNum)
                    .addRequest(request)
                    .addPipeline(new ConsolePipeline())
                    .addPipeline(new Map3MongoPipeline(mongoTemplate, "fund_position","fund_period_title","fund_code","gupiaodaima"))
                    .run();
        }
        if (String.valueOf(args[1]).equalsIgnoreCase("fundrank")) {
            List<Request> list = Lists.newArrayList();
            Arrays.stream(urlList).forEach(e->{
               list.add(getRequest(e));
            });
            Request[] requests = list.toArray(new Request[]{});
            FundRankProcessor processor = new FundRankProcessor();
            processor.setSite(getSite("fund.eastmoney.com").setTimeOut(15000));
            dropCollection("fund_rank");
            Spider.create(processor)
                    .thread(threadNum)
                    .addRequest(requests)
                    .addPipeline(new ConsolePipeline())
                    .addPipeline(new Map2MongoPipeline(mongoTemplate, "fund_rank","symbol"))
                    .runAsync();
        }

    }




}
package com.delicacy.apricot.spider.runner;


import com.delicacy.apricot.spider.pipeline.Map2MongoPipeline;
import com.delicacy.apricot.spider.pipeline.Map3MongoPipeline;
import com.delicacy.apricot.spider.pipeline.MongoPipeline;
import com.delicacy.apricot.spider.processor.xueqiu.StockProcessor;
import com.delicacy.apricot.spider.processor.xueqiu.StockProcessorV2;
import com.delicacy.apricot.spider.processor.xueqiu.StockReportProcessor;
import com.delicacy.apricot.spider.processor.xueqiu.StockReportProcessorV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;

@Component
@Order(value = 1)
@Slf4j
public class XueQiuRunner extends AbstractRunner {

   private static String url = "https://xueqiu.com/service/v5/stock/screener/quote/list?page=1&size=90&order=desc&orderby=percent&order_by=percent&market=CN&type=sh_sz&_=1574236784261";
   private static String url_hk = "https://xueqiu.com/service/v5/stock/screener/quote/list?page=1&size=90&order=desc&orderby=percent&order_by=percent&market=HK&type=hk&_=1574236784261";

    @Override
    public void run(String... args) {
        if(!checkArgs(2, getCommand(this), args)){
            return;
        }
        if (String.valueOf(args[1]).equalsIgnoreCase("cn")) {
            Request request = getRequest(url);
            StockProcessorV2 astockProcessor = new StockProcessorV2();
            astockProcessor.setSite(getSite("xueqiu.com"));
            dropCollection("xueqiu_astock");
            Spider.create(astockProcessor)
                    .thread(threadNum)
                    .addRequest(request)
                    .addPipeline(new MongoPipeline(mongoTemplate, "xueqiu_astock","symbol"))
                    .runAsync();
        }
        if (String.valueOf(args[1]).equalsIgnoreCase("cn_report")) {
            Request request = getRequest(url);
            StockReportProcessorV2 astockProcessor = new StockReportProcessorV2();
            astockProcessor.setSite(getSite("xueqiu.com"));
            dropCollection("xueqiu_astock_report");
            Spider.create(astockProcessor)
                    .thread(threadNum)
                    .addRequest(request)
                    .addPipeline(new Map2MongoPipeline(mongoTemplate, "xueqiu_astock_report","symbol","report_date"))
                    .runAsync();
        }
        if (String.valueOf(args[1]).equalsIgnoreCase("hk")) {
            Request request = getRequest(url_hk);
            StockProcessorV2 astockProcessor = new StockProcessorV2();
            astockProcessor.setSite(getSite("xueqiu.com"));
            dropCollection("xueqiu_hkstock");
            Spider.create(astockProcessor)
                    .thread(threadNum)
                    .addRequest(request)
                    .addPipeline(new MongoPipeline(mongoTemplate, "xueqiu_hkstock","symbol"))
                    .runAsync();
        }
        if (String.valueOf(args[1]).equalsIgnoreCase("hk_report")) {
            Request request = getRequest(url_hk);
            StockReportProcessorV2 astockProcessor = new StockReportProcessorV2();
            astockProcessor.setSite(getSite("xueqiu.com"));
            dropCollection("xueqiu_hkstock_report");
            Spider.create(astockProcessor)
                    .thread(threadNum)
                    .addRequest(request)
                    .addPipeline(new Map2MongoPipeline(mongoTemplate, "xueqiu_hkstock_report","symbol","report_date"))
                    .runAsync();
        }
    }




}
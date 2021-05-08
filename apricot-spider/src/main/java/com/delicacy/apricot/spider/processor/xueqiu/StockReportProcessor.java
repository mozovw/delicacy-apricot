package com.delicacy.apricot.spider.processor.xueqiu;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.delicacy.apricot.spider.driver.WebDriverPool;
import com.delicacy.apricot.spider.processor.AbstactProcessor;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.util.ObjectUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Selectable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class StockReportProcessor extends AbstactProcessor {


    public static final String URL_POST = "https://xueqiu.com/snowman/S/S\\w\\d+/detail#/ZYCWZB";

    public static final String URL_PRE = "https://xueqiu.com/snowman/S/%s/detail#/ZYCWZB";

    boolean flag = false;


    @Override
    public void process(Page page) {

        if (page.getUrl().regex(URL_POST).match()) {
            log.info("【url】={}",page.getUrl().get());
            WebDriver webDriver = getWebDriver();
            webDriver.get(page.getUrl().toString());
            String string = webDriver.findElement(By.className("stock-info-name")).getText();

            List<WebElement> elements = webDriver.findElement(By.className("stock-info-btn-list")).findElements(By.tagName("span"));

            LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<String, String>>> mapMains = Maps.newLinkedHashMap();

            for (int i = 1; i < elements.size(); i++) {
                LinkedHashMap<Integer, LinkedHashMap<String, String>> mapMain = Maps.newLinkedHashMap();

                WebElement e = elements.get(i);
                e.click();
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                WebElement table;
                try {
                    table = webDriver.findElement(By.tagName("table"));
                }catch (Exception e1){
                    continue;
                }
                WebElement thead = table.findElement(By.tagName("thead"));
                List<WebElement> ths = thead.findElements(By.tagName("th"));
                for (int j = 1; j < ths.size(); j++) {
                    LinkedHashMap<String, String> map = Maps.newLinkedHashMap();
                    map.put("symbol", string.substring(string.indexOf("(") + 3, string.length() - 1));
                    map.put("name", string);
                    map.put("report_date",ths.get(j).getText());
                    mapMain.put(j,map);
                }

                List<WebElement> trs = table.findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
                for (int j = 1; j < trs.size(); j++) {
                    List<WebElement> tds = trs.get(j).findElements(By.tagName("td"));
                    if (Lists.newArrayList("关键指标","每股指标","盈利能力","财务风险","运营能力").contains(tds.get(0).getText())){
                        continue;
                    }

                    for (int k = 1; k < tds.size(); k++) {
                        mapMain.get(k).put(getPingYin(tds.get(0).getText(),false),tds.get(k).getText());
                    }
                }
                mapMains.put(i,mapMain);

            }

            page.putField("map",mapMains);
            returnToPool(webDriver);


        } else if (!ObjectUtils.isEmpty(page.getRawText())) {
            if (flag) return;
            String rawText = page.getRawText();
            JSONObject jsonObject = JSON.parseObject(rawText);
            rawText = jsonObject.get("data").toString();
            jsonObject = JSON.parseObject(rawText);
            rawText = jsonObject.get("list").toString();
            JSONArray jsonArray = JSON.parseArray(rawText);
            List<String> collect = jsonArray.stream().map(e -> {
                JSONObject e1 = (JSONObject) e;
                String symbol = e1.get("symbol").toString();
                String url = String.format(URL_PRE, symbol);
                return url;
            }).collect(Collectors.toList());
            page.addTargetRequests(collect);
            //todo update flag
            Map<String, List<String>> stringListMap = HttpUtil.decodeParams(page.getUrl().toString(), "utf-8");
            Long longPage = Long.valueOf(stringListMap.get("page").get(0));
            Long sum = longPage * Long.valueOf(stringListMap.get("size").get(0));
            Long count = Long.valueOf(jsonObject.get("count").toString());
            flag = count < sum;
            //todo update page
            //todo get newurl
            stringListMap.put("page", Lists.newArrayList(String.valueOf(longPage + 1)));
            String params = HttpUtil.toParams(stringListMap);
            String string = page.getUrl().toString();
            String newUrl = string.substring(0, string.indexOf("?") + 1) + params;
            page.addTargetRequest(newUrl);
        }
    }


}

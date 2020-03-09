package com.delicacy.apricot.spider.processor.xueqiu;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.delicacy.apricot.spider.processor.AbstactProcessor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.util.ObjectUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Selectable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class AstockProcessor extends AbstactProcessor {


    public static final String URL_POST = "https://xueqiu.com/S/S\\w\\d+";

    public static final String URL_PRE = "https://xueqiu.com/S/";

    boolean flag = false;

    @Override
    public void process(Page page) {
        if (page.getUrl().regex(URL_POST).match()) {
            String string = page.getHtml().$(".stock-name", "text").toString();
            page.putField("symbol", string.substring(string.indexOf(":") + 1, string.length() - 1));
            page.putField("name", string);

            Selectable selectable = page.getHtml().$(".quote-info td", "text").replace("：", "");
            Selectable selectable1 = page.getHtml().$(".quote-info td span", "text");
            List<String> all = selectable.all();
            List<String> all1 = selectable1.all();
            for (int i = 0; i < all.size(); i++) {
                String pingYin = getPingYin(all.get(i), false);
                if (pingYin.contains("(")) {
                    pingYin = pingYin.replace("(", "_").substring(0, pingYin.length() - 1);
                }
                page.putField(pingYin, all1.get(i));
            }
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
                String url = URL_PRE + symbol;
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

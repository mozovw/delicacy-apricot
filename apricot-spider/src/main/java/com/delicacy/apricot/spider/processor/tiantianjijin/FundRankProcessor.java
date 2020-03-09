package com.delicacy.apricot.spider.processor.tiantianjijin;

import cn.hutool.http.HttpUtil;
import com.delicacy.apricot.spider.processor.AbstactProcessor;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.util.ObjectUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.JsonPathSelector;
import us.codecraft.webmagic.selector.Selectable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class FundRankProcessor extends AbstactProcessor {


    ConcurrentHashMap<String,Boolean> currmap = new ConcurrentHashMap<>();


    @Override
    public void process(Page page) {
        if (!ObjectUtils.isEmpty(page.getRawText())) {
            Map<String, List<String>> stringListMap = HttpUtil.decodeParams(page.getUrl().toString(), "utf-8");
            String ft = stringListMap.get("ft").get(0);
            if (currmap.get(ft)!=null && currmap.get(ft)) return;
            String rawText = page.getRawText().replace("var rankData = ","");
            List<String> list = new JsonPathSelector("$.datas").selectList(rawText);

            LinkedHashMap<Integer,LinkedHashMap<String,String>>  mapMain = Maps.newLinkedHashMap();
            for (int i = 0; i < list.size(); i++) {
                String e = list.get(i);
                LinkedHashMap<String,String>  map = Maps.newLinkedHashMap();
                String[] split = e.split(",");
                map.put("symbol",split[0]);
                map.put("name",split[1]);
                map.put("type",ft);
                map.put("date",split[3]);
                map.put("danweijingzhi",split[4]);
                map.put("leijijingzhi",split[5]);
                map.put("rizengzhanglv",split[6]);
                map.put("jin1zhou",split[7]);
                map.put("jin1yue",split[8]);
                map.put("jin3yue",split[9]);
                map.put("jin6yue",split[10]);
                map.put("jin1nian",split[11]);
                map.put("jin2nian",split[12]);
                map.put("jin3nian",split[13]);
                map.put("jinninalai",split[14]);
                map.put("chenglilai",split[15]);
                map.put("chengliriqi",split[16]);
                map.put("zidingyi",split[18]);
                mapMain.put(i,map);
            }
            page.putField("map",mapMain);



            //todo update flag
            Long allRecords = Long.valueOf(new JsonPathSelector("$.allRecords").select(rawText));
            Long pageIndex = Long.valueOf(new JsonPathSelector("$.pageIndex").select(rawText));
            Long pageNum =Long.valueOf( new JsonPathSelector("$.pageNum").select(rawText));
            boolean flag = allRecords < pageIndex*pageNum;
            currmap.put(ft,flag);
            //todo update page
            //todo get newurl

            stringListMap.put("pi", Lists.newArrayList(String.valueOf(pageIndex + 1)));
            String params = HttpUtil.toParams(stringListMap);
            String string = page.getUrl().toString();
            String newUrl = string.substring(0, string.indexOf("?") + 1) + params;
            page.addTargetRequest(newUrl);
        }
    }




}

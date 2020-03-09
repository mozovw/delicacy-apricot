package com.delicacy.apricot.spider.processor.tiantianjijin;

import cn.hutool.http.HttpUtil;
import com.delicacy.apricot.spider.processor.AbstactProcessor;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.JsonPathSelector;
import us.codecraft.webmagic.selector.Selectable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Slf4j
public class FundPositionProcessor extends AbstactProcessor {


    public static final String URL_PRE = "http://fundf10.eastmoney.com/FundArchivesDatas.aspx?type=jjcc&code=";
    public static final String URL_SUFFIX = "&topline=10";

    public static final String URL_POST = "http://fundf10.eastmoney.com/FundArchivesDatas.aspx";

    volatile boolean flag = false;

    public static final Map<Integer,String> mapTitle = new ConcurrentHashMap<>();

    @Override
    public void process(Page page) {
        if (page.getUrl().regex(URL_POST).match()) {
            if (!ObjectUtils.isEmpty(page.getRawText())) {
                String rawText = page.getRawText().replace("var apidata=","");
                String select = new JsonPathSelector("$.content").select(rawText);
                Html html = new Html(select, page.getUrl().toString());
                List<Selectable> box = html.$(".box").nodes();
                Map<String, List<String>> stringListMap = HttpUtil.decodeParams(page.getUrl().toString(), "utf-8");
                String code = stringListMap.get("code").get(0);
                LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<String, String>>> mapMain = Maps.newLinkedHashMap();
                for (int k = 0; k < box.size(); k++) {
                    Selectable e = box.get(k);
                    String fundName = e.$(".left a", "text").get();
                    String fundPeriodTitle = e.$(".left ", "text").get();

                    LinkedHashMap<String, String> map = Maps.newLinkedHashMap();
                    map.put("fund_code",code.trim());
                    map.put("fund_name",fundName.trim());
                    map.put("fund_period_title",trim(fundPeriodTitle));
                    List<String> all = e.$("table th", "text").all();
                    ArrayList<String> strings = Lists.newArrayList( "最新价", "涨跌幅", "相关资讯");
                    if (mapTitle.size()==0){
                        for (int i = 0; i < all.size(); i++) {
                            String inputString = all.get(i);
                            String pingYin = getPinyinPlus(inputString);
                            mapTitle.put(i,pingYin);
                            if (strings.contains(inputString)) continue;
                            map.put(pingYin,"");
                        }
                    }

                    LinkedHashMap<Integer, LinkedHashMap<String, String>> map2 = Maps.newLinkedHashMap();
                    List<Selectable> table_tr = e.$("table tr").nodes();
                    for (int i = 1; i < table_tr.size(); i++) {
                        List<Selectable> tds = table_tr.get(i).$("td").nodes();
                        if (tds.size()==mapTitle.size()-2){
                            tds.add(3,null);
                            tds.add(4,null);
                        }
                        if (tds.size()!=mapTitle.size()){
                            continue;
                        }
                        LinkedHashMap<String, String>  map1 = (LinkedHashMap<String, String>) map.clone();
                        for (int j = 0; j < tds.size(); j++) {
                            String anObject = mapTitle.get(j);
                            boolean match = strings.stream().anyMatch(ee -> getPinyinPlus(ee).equals(anObject));
                            if (match) continue;
                            Selectable selectable = tds.get(j);
                            String s = selectable.$("td","text").get();
                            if (ObjectUtils.isEmpty(s)){
                                s = selectable.$("a","text").get();
                            }
                            map1.put(anObject, ObjectUtils.isEmpty(s)?"":s.trim());
                        }
                        map2.put(i,map1);
                    }
                    mapMain.put(k,map2);
                }
                page.putField("map",mapMain);
            }
        } else if (!ObjectUtils.isEmpty(page.getRawText())) {
            if (flag) return;
            String rawText = page.getRawText().replace("var rankData = ","");
            List<String> list = new JsonPathSelector("$.datas").selectList(rawText);
            List<String> collect = list.stream().map(e -> {
                String symbol = e.substring(0, 6);
                return URL_PRE + symbol+URL_SUFFIX;
            }).collect(Collectors.toList());
            page.addTargetRequests(collect);

            //todo update flag
            Long allRecords = Long.valueOf(new JsonPathSelector("$.allRecords").select(rawText));
            Long pageIndex = Long.valueOf(new JsonPathSelector("$.pageIndex").select(rawText));
            Long pageNum =Long.valueOf( new JsonPathSelector("$.pageNum").select(rawText));
            flag = allRecords < pageIndex*pageNum;

            //todo update page
            //todo get newurl
            Map<String, List<String>> stringListMap = HttpUtil.decodeParams(page.getUrl().toString(), "utf-8");
            stringListMap.put("pi", Lists.newArrayList(String.valueOf(pageIndex + 1)));
            String params = HttpUtil.toParams(stringListMap);
            String string = page.getUrl().toString();
            String newUrl = string.substring(0, string.indexOf("?") + 1) + params;
            page.addTargetRequest(newUrl);
        }
    }




}

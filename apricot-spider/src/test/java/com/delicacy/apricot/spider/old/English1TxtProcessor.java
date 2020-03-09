package com.delicacy.apricot.spider.old;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;


@Slf4j
public class English1TxtProcessor implements PageProcessor {

    public static final String URL_LIST = "https://www.ximalaya.com/waiyu/3965403/p\\d+";

    public static final String URL_POST = "https://www.ximalaya.com/waiyu/3965403/\\d+";
    Set<String> stringSet = new ConcurrentSkipListSet<>();
    private ObjectMapper objectMapper = new ObjectMapper();
    private Site site = Site
            .me()
            .setDomain("www.ximalaya.com")
            .setSleepTime(5000)
            .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");



    private boolean filerContent(String replace) {
        if (!stringSet.contains(replace)) {
            stringSet.add(replace);
        } else {
            stringSet.remove(replace);
            return true;
        }
        return false;
    }

    @Override
    public void process(Page page) {
        //列表页
        if (page.getUrl().regex(URL_LIST).match()) {
            // 列表url
            List<String> list = page.getHtml().xpath("//*[@id=\"anchor_sound_list\"]").links().regex(URL_POST).all();
            try {
                log.info("【列表url】：{}", objectMapper.writeValueAsString(list));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            page.addTargetRequests(list);
            // 网址page
            List<String> list2 = page.getHtml().links().regex(URL_LIST).all();
            try {
                log.info("【网址page】：{}", objectMapper.writeValueAsString(list2));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            page.addTargetRequests(list2);
        } else {
            //文章页
            String content = page.getHtml().xpath("//*[@id=\"award\"]/main/div[1]/div[2]/div/div[2]/div/h1")
                    .replace("【讲解版本】", "")
                    .replace("【朗读版本】", "").toString();
            if (filerContent(content)) return;
            page.putField("title", content);

            content = page.getHtml().xpath("//*[@id=\"award\"]/main/div[1]/div[2]/div/div[2]/div/div[1]/span[1]")
                    .toString();
            page.putField("datatime", content);

            content = page.getHtml().xpath("//*[@id=\"award\"]/main/div[1]/div[3]/div[1]/div[1]/article[1]/p")
                    .regex("^(?!.*?1048940327).*$").all()
                    .stream().filter(e->e.contains("十大主题")).collect(Collectors.joining("\n"));
            page.putField("content", content);


        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        Spider.create(new English1TxtProcessor()).addUrl("https://www.ximalaya.com/waiyu/3965403/p1")
                .addPipeline(new TextFilePipeline("F:\\data\\webmagic\\", "瞬间秒杀听力,一开口让人震撼", "md", 1024 * 1024 * 1))
                .run();
    }
}

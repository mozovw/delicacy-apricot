package com.delicacy.apricot.spider;

import com.delicacy.apricot.spider.handler.FileOutputhandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;


@Slf4j
public class Mongo2FileTest extends ApricotSpiderApplicationTests {

    @Test
    public void english_monster() {
        todo("datetime","english_monster", (maps) -> {
            maps.stream().forEach(e -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("title", e.get("title"));
                map.put("url", e.get("url"));
                map.put("datetime", e.get("datetime"));
                map.put("content", e.get("content"));
                FileOutputhandler.builder()
                        .minSize( 512 * 1024)
                        .subDir("english_monster")
                        .fileName("瞬间秒杀听力,一开口让人震撼")
                        .path("F:\\data\\webmagic\\")
                        .subffix("md").build().writer(map);

            });
        });
    }


}

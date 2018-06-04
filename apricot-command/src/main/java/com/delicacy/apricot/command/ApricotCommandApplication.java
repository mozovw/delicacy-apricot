package com.delicacy.apricot.command;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApricotCommandApplication {

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
//            args = new String[]{"-eaf",
//                    "F:\\workspaces\\idea_workspaces\\boss_workspace\\boss-config-server\\src\\main\\resources","*.properties","utf-8"};
//            args = new String[]{"-raf","E:\\新建文件夹","*.dea","b","c"};
//            args = new String[]{"-reafp","E:\\新建文件夹","*.dea"};
            args = new String[]{"-tfc","D:\\新建文本文档.txt"};
        }
        /*List<String> strings = Arrays.asList(args);
        strings = strings.stream().map(e -> {
            try {
                return new String(e.getBytes(),"gbk");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
            return "";
        }).collect(Collectors.toList());
        args = strings.toArray(new String[]{});*/
//        args = new String[]{"--debug"};
        SpringApplication.run(ApricotCommandApplication.class, args);


    }

}

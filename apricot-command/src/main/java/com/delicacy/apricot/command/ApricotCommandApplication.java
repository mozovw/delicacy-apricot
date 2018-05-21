package com.delicacy.apricot.command;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApricotCommandApplication {

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
//            args = new String[]{"-eaf","F:\\workspaces\\clion_workspaces\\classic_c_workspace\\jingdianshili","*.c","utf-8"};
//            args = new String[]{"-raf","E:\\新建文件夹","*.dea","b","c"};
//            args = new String[]{"-reafp","E:\\新建文件夹","*.dea"};
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

        SpringApplication.run(ApricotCommandApplication.class, args);


    }

}

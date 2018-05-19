package com.delicacy.apricot.command;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApricotCommandApplication {

    public static void main(String[] args) {
        /*if (args == null || args.length == 0) {
            args = new String[]{"-raf","E:\\新建文件夹f","a","b"};
        }*/
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

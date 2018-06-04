package com.delicacy.apricot.command.util;

import lombok.extern.slf4j.Slf4j;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.util.StringUtils;

import java.io.*;


@Slf4j
public class CharSetUtil {


    public static String getCharset(InputStream is) {
        byte[] buf = new byte[4096];
        // (1)
        UniversalDetector detector = new UniversalDetector(null);

        // (2)
        int nread;
        try {
            while ((nread = is.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // (3)
        detector.dataEnd();

        // (4)
        String encoding = detector.getDetectedCharset();
        if (StringUtils.isEmpty(encoding)) {
            ExchangeUtil s = new ExchangeUtil();
            encoding = ExchangeUtil.javaname[s.detectEncoding(buf)];
        }
        if (StringUtils.isEmpty(encoding)) {
            encoding = "UTF-8";
        }
        // (5)
        detector.reset();
        return encoding;
    }

//    public static void main(String[] args) {
//        ExchangeUtil s = new ExchangeUtil();
//
//        String ss = ExchangeUtil.javaname[s.detectEncoding(new File("F:\\workspaces\\idea_workspaces\\boss_workspace\\boss-config-server\\src\\main\\resources\\application.properties"))];
//        System.out.println(ss);
//    }

}
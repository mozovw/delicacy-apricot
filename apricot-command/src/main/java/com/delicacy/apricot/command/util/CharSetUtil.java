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
            /*boolean unicode = isUnicode(new String(buf));
            if (!unicode){
                encoding = "UTF-8";
            }else {
                encoding = "UNICODE";
            }*/
            encoding = "UTF-8";
        }
        // (5)
        detector.reset();
        return encoding;
    }

  /*  public static void main(String[] args) {
        try {
            String charset = getCharset(new FileInputStream(new File("F:\\workspaces\\idea_workspaces\\boss_workspace\\boss-constant-server\\src\\main\\resources\\application.properties")));
            System.out.println(charset);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }*/

    /*public static boolean isUnicode(String unicode) {
        StringBuffer string = new StringBuffer();
        String[] hex = unicode.split("\\\\u");
        for (int i = 1; i < hex.length; i++) {
            // 转换出每一个代码点
            int data;
            try {
                data = Integer.parseInt(hex[i], 16);
            }catch (NumberFormatException e){
                continue;
            }
            string.append((char) data);
        }
        if (isChinese(string.toString()))return true;
        return false;
    }


    public static final boolean isChinese(String chineseStr) {
        char[] charArray = chineseStr.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            if ((charArray[i] >= 0x4e00) && (charArray[i] <= 0x9fbb)) {
                return true;
            }
        }
        return false;
    }
*/

}
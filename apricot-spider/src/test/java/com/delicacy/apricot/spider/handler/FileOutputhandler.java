package com.delicacy.apricot.spider.handler;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author yutao
 * @create 2019-12-04 17:22
 **/
@Slf4j
@Builder
public class FileOutputhandler {
    public static String PATH_SEPERATOR = "/";

    static {
        String property = System.getProperties().getProperty("file.separator");
        if (property != null) {
            PATH_SEPERATOR = property;
        }
    }

    private int minSize = 1024 * 1024 * 1;
    private String fileName;
    private String subffix;
    private String path;
    private String subDir;

    public void writer(Map<String, Object> all) {
        String path = this.path + PATH_SEPERATOR + subDir + PATH_SEPERATOR;
        PrintWriter printWriter = null;

        try {
            File dir = new File(path);
            File file;
            File[] list = dir.listFiles(file1 -> {
                if (file1.isFile() && file1.getName().contains(this.fileName) && file1.getName().lastIndexOf(this.subffix) > 0)
                    return true;
                return false;
            });
            if (ObjectUtils.isEmpty(list)) {
                file = getFile(path + this.fileName + "." + subffix);
            } else {
                File fileTemp = Arrays.stream(list).max(Comparator.comparingInt(this::getNum)).get();

                if (fileTemp.length() > minSize) {
                    Integer num = getNum(fileTemp);
                    int next = num + 1;
                    file = getFile(path + this.fileName + "-" + next + "." + subffix);
                } else {
                    file = fileTemp;
                }
            }
            printWriter = new PrintWriter(new FileWriter(file, true));
            Iterator<Map.Entry<String, Object>> iterator = all.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> next = iterator.next();
                String string = handleNoHtml(next.getValue().toString());
                printWriter.println(string);
            }
            printWriter.println("\n");
            printWriter.flush();
        } catch (RuntimeException e) {
            log.error("【error】 : {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        } catch (IOException e) {
            log.error("【error】 : {}", e.getMessage());
        } finally {
            if (printWriter != null)
                printWriter.close();
        }
    }

    private String handleNoHtml(String s) {
        if (ObjectUtils.isEmpty(s))return "";
        String reg = "<([^>]*)>";
        Pattern pattern = Pattern.compile(reg);
        boolean matches = pattern.matcher(s).find();
        if (!matches)s+="<br/>";
        return s;
    }

    private Integer getNum(File o1) {
        String s;
        String name = o1.getName();
        if (!name.contains("-")) s = "0";
        else s = name.substring(0, name.lastIndexOf(".")).split("-")[1];
        return Integer.valueOf(s);
    }

    public File getFile(String fullName) {
        checkAndMakeParentDirecotry(fullName);
        return new File(fullName);
    }

    public void checkAndMakeParentDirecotry(String fullName) {
        int index = fullName.lastIndexOf(PATH_SEPERATOR);
        if (index > 0) {
            String path = fullName.substring(0, index);
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
    }
}

package com.delicacy.apricot.spider.old;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.utils.FilePersistentBase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

@Slf4j
public class TextFilePipeline extends FilePersistentBase implements Pipeline {


    int minSize = 1024 * 10;
    private String fileName;
    private String subffix;

    public TextFilePipeline(String path, String fileName, String subffix) {
        setPath(path);
        this.fileName = fileName;
        this.subffix = subffix;
    }

    public TextFilePipeline(String path, String fileName, String subffix, int minSize) {
        setPath(path);
        this.fileName = fileName;
        this.subffix = subffix;
        this.minSize = minSize;
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        String path = this.path + PATH_SEPERATOR + task.getUUID() + PATH_SEPERATOR;
        PrintWriter printWriter = null;
        Map<String, Object> all = resultItems.getAll();
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
                String string = next.getValue().toString();
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

    private Integer getNum(File o1) {
        String s;
        String name = o1.getName();
        if (!name.contains("-")) s = "0";
        else s = name.substring(0, name.lastIndexOf(".")).split("-")[1];
        return Integer.valueOf(s);
    }


}

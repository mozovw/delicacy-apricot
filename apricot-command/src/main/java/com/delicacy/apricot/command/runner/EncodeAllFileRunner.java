package com.delicacy.apricot.command.runner;

import com.delicacy.apricot.command.constant.CommandConstant;
import com.delicacy.apricot.command.util.CharSetUtil;
import com.delicacy.apricot.command.util.CommandUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;

import java.util.UUID;

@Component
@Order(value = 3)
@Slf4j
public class EncodeAllFileRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        if (!CommandUtil.checkArgs(4, CommandConstant.EAF, args)) return;
        String path = String.valueOf(args[1]);
        String value2 = String.valueOf(args[2]);
        String value3 = String.valueOf(args[3]);
        CommandUtil.operateFile(path, value2,e -> {
            String charset = getCharset(e);
            moveFile(value3, e, charset);
        });

    }

    private void moveFile(String value2, File e, String charset) {
        String substring = e.getName().substring(e.getName().lastIndexOf("."));
        UUID uuid = UUID.randomUUID();
        File newFile = new File(e.getParent() , uuid  + substring);
        try {
            newFile.createNewFile();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try (
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(e), charset));
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(newFile), StringUtils.isEmpty(value2)?"utf-8":value2))
        ) {
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                bufferedWriter.write(str);
                bufferedWriter.newLine();
            }
            bufferedWriter.flush();
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        String absolutePath = e.getAbsolutePath();
        log.info("new filepath is {}",absolutePath);
        boolean delete = e.delete();
        if (delete)newFile.renameTo(new File(absolutePath));
    }

    private String getCharset(File e) {
        String charset = null;
        try (FileInputStream in = new FileInputStream(e)){
            charset = CharSetUtil.getCharset(in);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return charset;
    }





}
package com.delicacy.apricot.command.runner;


import com.delicacy.apricot.command.util.CharSetUtil;
import com.delicacy.apricot.command.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.UUID;

@Component
@Order(value = 4)
@Slf4j
public class TestFileCodeRunner implements CommandLineRunner {


    @Override
    public void run(String... args) throws Exception {
        String command = CommonUtil.getCommand(this);
        if (!CommonUtil.checkArgs(2, command, args)) return;
        String path = String.valueOf(args[1]);
        String charset = getCharset(new File((path)));
        log.info("this file code is {}",charset);
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
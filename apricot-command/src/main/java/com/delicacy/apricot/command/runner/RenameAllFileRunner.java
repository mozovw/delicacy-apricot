package com.delicacy.apricot.command.runner;

import com.delicacy.apricot.command.constant.CommandConstant;
import com.delicacy.apricot.command.util.CommandUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashSet;
import java.util.function.Consumer;

@Component
@Order(value = 2)
@Slf4j
public class RenameAllFileRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        if (!CommandUtil.checkArgs(5,CommandConstant.RAF,args)) return;
        String path = String.valueOf(args[1]);
        String value2 = String.valueOf(args[2]);
        String value3 = String.valueOf(args[3]);
        String value4 = String.valueOf(args[4]);
        CommandUtil.operateFile(path,value2,e->{
            String s1 = e.getName().substring(e.getName().lastIndexOf("."));
            String s0 = e.getName().substring(0,e.getName().lastIndexOf("."));
            String replace = s0.replaceAll(value3, value4)+s1;
            File file = new File(e.getParent(), replace);
            log.info("new filepath is {}",file.getAbsoluteFile());
            e.renameTo(file);
        });

    }








}
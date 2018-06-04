package com.delicacy.apricot.command.runner;


import com.delicacy.apricot.command.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
@Order(value = 1)
@Slf4j
public class RenameEnAllFilePathRunner implements CommandLineRunner {



    @Override
    public void run(String... args) throws Exception {
        String command = CommonUtil.getCommand(this);
        if (!CommonUtil.checkArgs(3, command, args)) return;
        String path = String.valueOf(args[1]);
        String value = String.valueOf(args[2]);
        CommonUtil.operateFile(path,value, e -> {
            String absolutePath = e.getAbsolutePath();
            String replace = absolutePath.replace(path, "");
            String toTargetPath = path + CommonUtil.getPingYin(replace).replaceAll(" ", "");
            log.info("new filepath is {}", toTargetPath);
            File dest = new File(toTargetPath);
            File destParentDir = dest.getParentFile();
            try {
                if (!destParentDir.exists()) destParentDir.mkdirs();
                if (!dest.exists()) dest.createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            CommonUtil.copyFile(e, dest);
        });

    }


}
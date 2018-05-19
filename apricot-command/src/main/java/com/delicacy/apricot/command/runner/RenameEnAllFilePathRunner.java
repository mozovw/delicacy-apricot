package com.delicacy.apricot.command.runner;

import com.delicacy.apricot.command.constant.CommandConstant;
import com.delicacy.apricot.command.util.CommandUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.function.Consumer;

@Component
@Order(value = 1)
@Slf4j
public class RenameEnAllFilePathRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        if (!CommandUtil.checkArgs(2, CommandConstant.REAFP, args)) return;
        String path = String.valueOf(args[1]);
        operateFile(path, e -> {
            String absolutePath = e.getAbsolutePath();
            String replace = absolutePath.replace(path, "");
            String toTargetPath = path + CommandUtil.getPingYin(replace).replaceAll(" ", "");
            log.info("new filepath is {}", toTargetPath);
            File dest = new File(toTargetPath);
            File destParentDir = dest.getParentFile();
            try {
                if (!destParentDir.exists()) destParentDir.mkdirs();
                if (!dest.exists()) dest.createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            CommandUtil.copyFile(e, dest);
        });

    }


    private void operateFile(String path, Consumer<File> consumer) {
        log.info("operating where path is {}", path);
        File file = new File(path);
        HashSet<File> fileSet = new HashSet<>();
        CommandUtil.recursiveFiles(fileSet, file);
        fileSet.stream().forEach(e -> consumer.accept(e));
    }

}
package com.delicacy.apricot.command.util;

import com.delicacy.apricot.command.constant.CommandConstant;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 工具类
 *
 * @author zyt
 * @create 2018-05-18 11:28
 **/
@Slf4j
public class CommandUtil {


    public static boolean checkArgs(int len, String s1, String... args){
        if (args==null||args.length==0){
            return false;
        }
        if (args.length<1 || !args[0].toLowerCase().trim().equals(s1)){
            return false;
        }
        if (args.length!=len){
            log.error("args length is not {}",len);
            return false;
        }
        boolean b = Arrays.stream(args).anyMatch(e -> StringUtils.isEmpty(e));
        if (b){
            log.error("args hava null");
            return false;
        }
        boolean exists = new File(args[1]).exists();
        if (!exists){
            log.error("file is not exists");
            return false;
        }
        return true;
    }



    private static boolean checkString(String string){
        if (null==string||"".equals(string)){
            return false;
        }
        return true;
    }
    public static void recursiveFiles(Set<File> fileSet, File file){
        File files[] = file.listFiles();
        if(files == null){
            return;
        }
        if(files.length == 0)return;
        for (File f : files) {
            if (f.isFile())fileSet.add(f);
            if(f.isDirectory()){
                recursiveFiles(fileSet,f);
            }
        }
    }


    public static void copyFile(File source, File dest) {
        try (
                FileChannel inputChannel = new FileInputStream(source).getChannel();
                FileChannel outputChannel = new FileOutputStream(dest).getChannel();
        ){
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String getPingYin(String inputString) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
        String output = "";
        if (inputString != null && inputString.length() > 0
                && !"null".equals(inputString)) {
            char[] input = inputString.trim().toCharArray();
            try {
                for (int i = 0; i < input.length; i++) {
                    if (java.lang.Character.toString(input[i]).matches(
                            "[\\u4E00-\\u9FA5]+")) {
                        String[] temp = PinyinHelper.toHanyuPinyinStringArray(
                                input[i], format);
                        output += temp[0];
                    } else
                        output += java.lang.Character.toString(input[i]);
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }
        } else {
            return "*";
        }
        return output;
    }






}

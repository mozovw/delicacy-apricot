package com.delicacy.apricot.command.util;

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
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 工具类
 *
 * @author zyt
 * @create 2018-05-18 11:28
 **/
@Slf4j
public class CommonUtil {


    public static boolean checkArgs(int len, String s1, String... args){
        if (args==null||args.length==0){
            return false;
        }
        if (args.length<1 || !args[0].toLowerCase().trim().equalsIgnoreCase(s1)){
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


    public static void recursiveFiles(Set<File> fileSet,String suffix, File file){
        if (fileSet==null)return;
        if(StringUtils.isEmpty(suffix))return;
        if (!file.exists())return;
        File files[] = file.listFiles(e->{
            if (e.isDirectory())return true;
            if (!e.getName().contains("."))return false;
            String substring = e.getName().substring(e.getName().lastIndexOf("."));
            if ("*".equals(suffix)){
                return true;
            }
            if (("*"+substring).equalsIgnoreCase(suffix)){
                return true;
            }
            return false;
        });
        if(files == null){
            return;
        }
        if(files.length == 0)return;
        for (File f : files) {
            if(f.isDirectory()){
                recursiveFiles(fileSet,suffix,f);
            }
            if (f.isFile())fileSet.add(f);
        }
    }

    public static void operateFile(String path,String suffix, Consumer<File> consumer) {
        log.info("operating where path is {}", path);
        File file = new File(path);
        HashSet<File> fileSet = new HashSet<>();
        CommonUtil.recursiveFiles(fileSet,suffix, file);
        fileSet.stream().forEach(e -> consumer.accept(e));
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


    public static String getCommand(Object object) {
        String simpleName = getCapitalInitials(object.getClass().getSimpleName());
        String command = simpleName.substring(0,simpleName.length()-1);
        String capitalInitials = "-"+getCapitalInitials(command);
        return capitalInitials;
    }

    public static String getCapitalInitials(String string){
        char[] chars = string.toCharArray();
        StringBuilder s = new StringBuilder();
        for (Character c :
                chars) {
            if (Character.isUpperCase(c)){
                s.append(c);
            }
        }
        return s.toString();
    }




}

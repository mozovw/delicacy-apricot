package com.delicacy.apricot.spider;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Slf4j
public class MoneyNowTest extends ApricotSpiderApplicationTests {

    @Test
    public void test() {
        String freeMoney = "8.28";
        String tiexianlv = "0.09";
        String zengzhanglv = "0.15";
        String zengzhanglv_yihou = "0.05";
        int year = 10;
        //todo 前十年贴现
        BigDecimal sum_zhexian = new BigDecimal(0);
        BigDecimal ziyouxianjin = new BigDecimal(0);
        for (int i = 0; i < year; i++) {
            ziyouxianjin = new BigDecimal(freeMoney).multiply(new BigDecimal(1 + Double.valueOf(zengzhanglv)).pow(i+1));
            BigDecimal zhexian = ziyouxianjin.divide(new BigDecimal(1 + Double.valueOf(tiexianlv)).pow(i+1), RoundingMode.HALF_DOWN);
            sum_zhexian = zhexian.add(sum_zhexian);
        }
        //todo 十年之后贴现
        BigDecimal ziyouxianjin_yihou = ziyouxianjin.multiply(new BigDecimal(1 + Double.valueOf(zengzhanglv_yihou)))
                .multiply(new BigDecimal(1 + Double.valueOf(tiexianlv))
                .divide(new BigDecimal(Double.valueOf(tiexianlv) - Double.valueOf(zengzhanglv_yihou)), RoundingMode.HALF_DOWN));
        BigDecimal sum_zhexian_yihou = ziyouxianjin_yihou
                .divide(new BigDecimal(1 + Double.valueOf(tiexianlv)).pow(year+1),RoundingMode.HALF_DOWN);
        String sum = sum_zhexian_yihou.add(sum_zhexian).toString();
        System.out.println(sum);
    }

    @Test
    public void test2() {
        String freeMoney = "100";
        String tiexianlv = "0.08";
        String zengzhanglv = "0.20";
        String zengzhanglv_yihou = "0.07";
        int year = 10;
        //todo 前十年贴现
        BigDecimal sum_zhexian = new BigDecimal(0);
        BigDecimal ziyouxianjin = new BigDecimal(0);
        for (int i = 0; i < year; i++) {
            ziyouxianjin = new BigDecimal(freeMoney).multiply(new BigDecimal(1 + Double.valueOf(zengzhanglv)).pow(i+1));
            BigDecimal zhexian = ziyouxianjin.divide(new BigDecimal(1 + Double.valueOf(tiexianlv)).pow(i+1), RoundingMode.HALF_DOWN);
            sum_zhexian = zhexian.add(sum_zhexian);
        }
        //todo 十年之后贴现
        BigDecimal ziyouxianjin_yihou = ziyouxianjin.multiply(new BigDecimal(1 + Double.valueOf(zengzhanglv_yihou)))
                .multiply(new BigDecimal(1 + Double.valueOf(tiexianlv))
                        .divide(new BigDecimal(Double.valueOf(tiexianlv) - Double.valueOf(zengzhanglv_yihou)), RoundingMode.HALF_DOWN));
        BigDecimal sum_zhexian_yihou = ziyouxianjin_yihou
                .divide(new BigDecimal(1 + Double.valueOf(tiexianlv)).pow(year+1),RoundingMode.HALF_DOWN);
        String sum = sum_zhexian_yihou.add(sum_zhexian).toString();
        System.out.println(sum);
    }

    @Test
    public void test3() {
        String freeMoney = "35";
        String tiexianlv = "0.08";
        String zengzhanglv = "0.18";
        String zengzhanglv_yihou = "0.06";
        int year = 10;
        //todo 前十年贴现
        BigDecimal sum_zhexian = new BigDecimal(0);
        BigDecimal ziyouxianjin = new BigDecimal(0);
        for (int i = 0; i < year; i++) {
            ziyouxianjin = new BigDecimal(freeMoney).multiply(new BigDecimal(1 + Double.valueOf(zengzhanglv)).pow(i+1));
            BigDecimal zhexian = ziyouxianjin.divide(new BigDecimal(1 + Double.valueOf(tiexianlv)).pow(i+1), RoundingMode.HALF_DOWN);
            sum_zhexian = zhexian.add(sum_zhexian);
        }
        //todo 十年之后贴现
        BigDecimal ziyouxianjin_yihou = ziyouxianjin.multiply(new BigDecimal(1 + Double.valueOf(zengzhanglv_yihou)))
                .multiply(new BigDecimal(1 + Double.valueOf(tiexianlv))
                        .divide(new BigDecimal(Double.valueOf(tiexianlv) - Double.valueOf(zengzhanglv_yihou)), RoundingMode.HALF_DOWN));
        BigDecimal sum_zhexian_yihou = ziyouxianjin_yihou
                .divide(new BigDecimal(1 + Double.valueOf(tiexianlv)).pow(year+1),RoundingMode.HALF_DOWN);
        String sum = sum_zhexian_yihou.add(sum_zhexian).toString();
        System.out.println(sum);
    }

}

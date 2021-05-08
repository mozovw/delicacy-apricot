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
        valueCalc(freeMoney, tiexianlv, zengzhanglv, zengzhanglv_yihou, year);
    }

    private void valueCalc(String freeMoney, String tiexianlv, String zengzhanglv, String zengzhanglv_yihou, int year) {
        if (Double.parseDouble(tiexianlv)<Double.parseDouble(zengzhanglv_yihou)){
            throw new RuntimeException("贴现率应该大于年后增长率");
        }
        //todo 前十年贴现
        BigDecimal sum_zhexian = new BigDecimal(0);
        BigDecimal ziyouxianjin = new BigDecimal(0);
        for (int i = 0; i < year; i++) {
            ziyouxianjin = new BigDecimal(freeMoney).multiply(BigDecimal.valueOf(1 + Double.parseDouble(zengzhanglv)).pow(i+1));
            BigDecimal zhexian = ziyouxianjin.divide(BigDecimal.valueOf(1 + Double.parseDouble(tiexianlv)).pow(i+1), RoundingMode.HALF_DOWN);
            sum_zhexian = zhexian.add(sum_zhexian);
        }
        //todo 十年之后贴现
        BigDecimal ziyouxianjin_yihou = ziyouxianjin.multiply(BigDecimal.valueOf(1 + Double.parseDouble(zengzhanglv_yihou)))
                .multiply(BigDecimal.valueOf(1 + Double.parseDouble(tiexianlv))
                .divide(BigDecimal.valueOf(Double.parseDouble(tiexianlv) - Double.parseDouble(zengzhanglv_yihou)), RoundingMode.HALF_DOWN));
        BigDecimal sum_zhexian_yihou = ziyouxianjin_yihou
                .divide(BigDecimal.valueOf(1 + Double.parseDouble(tiexianlv)).pow(year +1),RoundingMode.HALF_DOWN);
        String sum = sum_zhexian_yihou.add(sum_zhexian).toString();
        System.out.println(sum);
    }

    /**
     * 45 900
     * 53 1800
     * 58 1260
     * 62 1800
     * 66 2460
     * 伊利
     * 2592
     */
    @Test
    public void test2() {
        String freeMoney = "66";
        String tiexianlv = "0.09";
        String zengzhanglv = "0.10";
        String zengzhanglv_yihou = "0.05";
        int year = 10;
        valueCalc(freeMoney, tiexianlv, zengzhanglv, zengzhanglv_yihou, year);
    }

    /**
     * 27 750
     * 33 1376
     * 41 1792
     * 50 2816
     * 61 6400
     * 海天
     * 13149
     */
    @Test
    public void test22() {
        String freeMoney = "61";
        String tiexianlv = "0.09";
        String zengzhanglv = "0.16";
        String zengzhanglv_yihou = "0.08";
        int year = 10;
        valueCalc(freeMoney, tiexianlv, zengzhanglv, zengzhanglv_yihou, year);
    }

    @Test
    public void test222() {
        String freeMoney = "470";
        String tiexianlv = "0.09";
        String zengzhanglv = "0.12";
        String zengzhanglv_yihou = "0.06";
        int year = 10;
        valueCalc(freeMoney, tiexianlv, zengzhanglv, zengzhanglv_yihou, year);
    }


}

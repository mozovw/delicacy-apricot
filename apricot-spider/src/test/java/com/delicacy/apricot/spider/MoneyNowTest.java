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
     * 2027
     */
    @Test
    public void haitian() {
        String freeMoney = "61";
        String tiexianlv = "0.115";
        String zengzhanglv = "0.19";
        String zengzhanglv_yihou = "0.10";
        int year = 5;
        valueCalc(freeMoney, tiexianlv, zengzhanglv, zengzhanglv_yihou, year);
    }

    /**
     * 茅台
     */
    @Test
    public void maotai() {
        String freeMoney = "979";
        String tiexianlv = "0.1";
        String zengzhanglv = "0.06";
        String zengzhanglv_yihou = "0.03";
        int year = 5;
        valueCalc(freeMoney, tiexianlv, zengzhanglv, zengzhanglv_yihou, year);
    }

    /**
     * 泸州
     * 9966
     */
    @Test
    public void luzhou() {
        String freeMoney = "60";
        String tiexianlv = "0.115";
        String zengzhanglv = "0.30";
        String zengzhanglv_yihou = "0.10";
        int year = 5;
        valueCalc(freeMoney, tiexianlv, zengzhanglv, zengzhanglv_yihou, year);
    }

    /**
     * 五粮液
     * 9966
     */
    @Test
    public void wuliangye() {
        String freeMoney = "200";
        String tiexianlv = "0.10";
        String zengzhanglv = "0.15";
        String zengzhanglv_yihou = "0.07";
        int year = 5;
        valueCalc(freeMoney, tiexianlv, zengzhanglv, zengzhanglv_yihou, year);
    }

    /**
     * 爱尔眼科
     * 2355
     */
    @Test
    public void aieryanke() {
        String freeMoney = "21";
        String tiexianlv = "0.115";
        String zengzhanglv = "0.20";
        String zengzhanglv_yihou = "0.10";
        int year = 5;
        valueCalc(freeMoney, tiexianlv, zengzhanglv, zengzhanglv_yihou, year);
    }


    /**
     * 迈瑞
     * 4499
     */
    @Test
    public void mairui() {
        String freeMoney = "65";
        String tiexianlv = "0.115";
        String zengzhanglv = "0.20";
        String zengzhanglv_yihou = "0.10";
        int year = 5;
        valueCalc(freeMoney, tiexianlv, zengzhanglv, zengzhanglv_yihou, year);
    }

    /**
     * 亿纬锂能
     * 2136
     */
    @Test
    public void yiweilineng() {
        String freeMoney = "15";
        String tiexianlv = "0.115";
        String zengzhanglv = "0.26";
        String zengzhanglv_yihou = "0.10";
        int year = 5;
        valueCalc(freeMoney, tiexianlv, zengzhanglv, zengzhanglv_yihou, year);
    }


}

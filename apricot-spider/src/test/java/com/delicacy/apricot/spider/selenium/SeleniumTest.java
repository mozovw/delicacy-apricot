package com.delicacy.apricot.spider.selenium;

import com.delicacy.apricot.spider.driver.WebDriverPool;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebDriver;


/**
 * @author code4crafter@gmail.com <br>
 * Date: 13-7-26 <br>
 * Time: 下午12:27 <br>
 */
public class SeleniumTest {

    @Ignore("need chrome driver")
    @Test
    public void testSelenium() {

        String path = this.getClass().getResource("/chromedriver.exe").getPath();
//        String path = this.getClass().getResource("/phantomjs.exe").getPath();
        System.getProperties().setProperty("webdriver.chrome.driver", path);
//        System.getProperties().setProperty("phantomjs.binary.path", path);

        WebDriverPool webDriverPool = new WebDriverPool(5);

        for (int i = 0; i < 1; i++) {
            try {
                WebDriver webDriver = webDriverPool.get();
                //webDriver.manage().window().maximize();
                webDriver.get("https://www.baidu.com");

                System.out.println(webDriver.getCurrentUrl());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        webDriverPool.closeAll();


    }


}

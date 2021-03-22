package com.kokokozhina;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.concurrent.TimeUnit;

public class DriverUtils {
    private static WebDriver driver;

    public static WebDriver getDriver() {
        if (driver == null) {
//            System.setProperty("webdriver.chrome.driver",
//                    "<path>");
//            ChromeOptions chromeOptions = new ChromeOptions();
//            driver = new ChromeDriver(chromeOptions);
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        }

        return driver;
    }

    @Attachment(value = "Screenshot", type = "image/png")
    public static byte[] makeScreenshot(WebDriver driver) {
        return ((TakesScreenshot)driver).getScreenshotAs(OutputType.BYTES);
    }
}

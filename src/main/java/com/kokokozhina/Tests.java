package com.kokokozhina;


import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.List;

import org.openqa.selenium.NoSuchElementException;

public class Tests {

    private WebDriver driver = DriverUtils.getDriver();

    private void init() {
        driver.get("http://www.e-katalog.ru");
        System.setProperty("javax.xml.bind.JAXBContextFactory",
                "org.eclipse.persistence.jaxb.JAXBContextFactory");
    }

    private void login() {
        driver.findElement(By.className("wu_entr")).click();
        driver.findElement(By.xpath("//div[@id='mui_user_login_window']/form/div/div/div[3]")).click();
        driver.findElement(By.name("l_")).sendKeys("testlogin");
        driver.findElement(By.name("pw_")).sendKeys("testpassword");
        driver.findElement(By.cssSelector("button[class='ek-form-btn blue']")).click();
    }

    private void logout() {
        driver.findElement(By.className("help2")).click();
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            final byte[] screenshot = DriverUtils.makeScreenshot(driver);
        }
    }

    @Test
    public void testLogin() {
        init();
        login();
        Assert.assertEquals(
                driver.findElement(By.className("info-nick")).getText(),
                "testlogin"
        );
        logout();
    }

    @Test(dependsOnMethods = "testLogin")
    public void testFavouritesAndVisitedGoods() {
        init();
        login();

        //click gadget
        driver.findElement(By.xpath("//div[@class='s-width']/ul/li[1]/a")).click();

        //click action camera
        driver.findElement(By.xpath("//a[@class='mainmenu-subitem mainmenu-icon695']")).click();

        //scroll and choose sony
        WebElement sony = driver.findElement(By.xpath("//*[@id='li_br156']/label"));
        JavascriptExecutor scroller = (JavascriptExecutor) driver;
        scroller.executeScript("arguments[0].scrollIntoView(true);", sony);
        sony.click();

        //scroll and choose nfc
        WebElement nfc = driver.findElement(By.xpath("//*[@id='preset18920']/li[8]/label"));
        scroller.executeScript("arguments[0].scrollIntoView(true);", nfc);
        nfc.click();

        //scroll to submit button and click
        WebElement submit = driver.findElement(By.xpath("//input[@id='match_submit']"));
        scroller.executeScript("arguments[0].scrollIntoView(true);", submit);
        submit.click();

        //assert that hdr-az1vb exists by finding it
        WebElement camera = driver.findElement(By.xpath("//a[@id='product_625603']"));
        scroller.executeScript("arguments[0].scrollIntoView(true);", camera);
        camera.click();

        //add in favourites
        driver.findElement(By.xpath("//div[@class='ib']")).click();

        //go to my menu
        driver.findElement(By.xpath("//div[@id='mui_user_login_row']/a")).click();

        //assert that hdr-az1vb exists in favourites
        driver.findElement(By.xpath("//div[@class='user-div'][1]" +
                "//a[@href='/SONY-HDR-AZ1VB.htm']"));

        //assert that hdr-az1vb exists in viewed goods
        driver.findElement(By.xpath("//div[@class='user-history-div'][1]" +
                "//a[@href='/SONY-HDR-AZ1VB.htm']"));

        //delete hdr-az1vb from viewed goods
        driver.findElement(By.xpath("//div[@class='user-history-div'][1]" +
                "//a[@href='/SONY-HDR-AZ1VB.htm']//div[@class='user-history-close']")).click();

        //delete hdr-az1vb from favourites
        WebElement buttonDeleteFromFavourite = driver.findElement(
                By.xpath("//div[@class='user-div'][1]//a[@href='/SONY-HDR-AZ1VB.htm']/" +
                        "..//div[@class='whishlist-action--remove']"
                )
        );
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].click();", buttonDeleteFromFavourite);

        logout();
    }

    public void testMaxPriceFilterForOneGood(WebElement tablet, String price) {
        WebElement minPrice;
        int priceInt = Integer.parseInt(price);
        //e-katalog thinks that tablet with price lower than 15000 can cost 15490
        int eps = 1000;

        try {
            minPrice = tablet.findElement(By.xpath(".//div[@class = 'model-price-range']//span[1]"));
        } catch (NoSuchElementException ex) {
            minPrice = tablet.findElement(By.xpath(".//div[@class = 'pr31 ib']/span"));
        }

        Assert.assertTrue(
                priceInt + eps > Integer.parseInt(minPrice.getText().replace(" ", ""))
        );
    }

    public void testMaxPriceFilterForOnePage(String price, boolean isPageLast) {
        WebElement tabletsHolder = driver.findElement(By.xpath("//form[@id='list_form1']"));
        List<WebElement> tablets = tabletsHolder.findElements(By.xpath("//form[@id='list_form1']/div[@id]"));
        Assert.assertTrue(!tablets.isEmpty());

        if (!isPageLast)
            tablets.remove(tablets.size() - 1);

        for (WebElement tablet : tablets)
            testMaxPriceFilterForOneGood(tablet, price);
    }

    @Test
    @Parameters({"price"})
    public void testMaxPriceFilter(String price) {
//    public void testMaxPriceFilter() {
//        String price = "10000";
        init();

        //click computer
        driver.findElement(By.xpath("//div[@class='s-width']/ul/li[2]/a")).click();

        //click tablet
        driver.findElement(By.xpath("//div[@class='s-width']/ul/li[2]/div/div/a[2]")).click();

        //set max price
        driver.findElement(By.id("maxPrice_")).sendKeys(price);

        //scroll to submit button and click
        WebElement submit = driver.findElement(By.xpath("//input[@id='match_submit']"));
        JavascriptExecutor scroller = (JavascriptExecutor) driver;
        scroller.executeScript("arguments[0].scrollIntoView(true);", submit);
        submit.click();

        int pagesCount;

        try {
            pagesCount = Integer.parseInt(driver.findElement(
                    By.xpath("//div[@class='ib page-num']//a[last()]")).getText());
        } catch (Exception ex) {
            pagesCount = 1;
        }

        for (int i = 1; i <= pagesCount; i++) {
            //test all tablets on the page
            testMaxPriceFilterForOnePage(price, i == pagesCount);

            //move to the next page
            if (i < pagesCount)
                driver.findElement(By.xpath("//div[@class='ib page-num']/a[text()=" + (i + 1) + "]")).click();

        }

    }
}

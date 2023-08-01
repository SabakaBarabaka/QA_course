import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class FirstTest {
    private WebDriver driver;
    private String baseUrl;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "src/test/chromedriver.exe");
        driver = new ChromeDriver();
        baseUrl = "https://sandbox.cardpay.com/MI/cardpayment2.html?orderXml=PE9SREVSIFdBTExFVF9JRD0nODI5OScgT1JERVJfTlVNQkVSPSc0NTgyMTEnIEFNT1VOVD0nMjkxLjg2JyBDVVJSRU5DWT0nRVVSJyAgRU1BSUw9J2N1c3RvbWVyQGV4YW1wbGUuY29tJz4KPEFERFJFU1MgQ09VTlRSWT0nVVNBJyBTVEFURT0nTlknIFpJUD0nMTAwMDEnIENJVFk9J05ZJyBTVFJFRVQ9JzY3NyBTVFJFRVQnIFBIT05FPSc4NzY5OTA5MCcgVFlQRT0nQklMTElORycvPgo8L09SREVSPg==&sha512=998150a2b27484b776a1628bfe7505a9cb430f276dfa35b14315c1c8f03381a90490f6608f0dcff789273e05926cd782e1bb941418a9673f43c47595aa7b8b0d";
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @DataProvider(name = "DataCards")
    public Object[][] dpMethod() {
        return new Object[][]{
                {"4000000000000051", "AUTHORIZED"},
                {"5100000000000651", "AUTHORIZED"},
                {"2400000000000077", "CONFIRMED"},
                {"4000000000000077", "CONFIRMED"}
        };
    }

    @Test(dataProvider = "DataCards")
    public void PODataPavTestPay(String cardNumber, String paymentItemStatus) throws InterruptedException, IOException {

        driver.get(baseUrl);

        PaymentPage paymentPage = new PaymentPage(driver);

        paymentPage.inputCardNumber().click();
        paymentPage.inputCardNumber().clear();
        paymentPage.enterCardNumber(cardNumber);
        paymentPage.inputCardHolder().click();
        paymentPage.inputCardHolder().clear();
        paymentPage.enterCardHolder("JONNY BUNGALO");

        // Получите список всех опций
        List<WebElement> optionsMonth = paymentPage.selectExpiresMonth().getOptions();
        // Используйте цикл для определения индекса максимальной опции
        int maxIndexMonth = 0;
        int maxOptionMonthValue = 0;
        for (int i = 1; i < optionsMonth.size(); i++) {
            int optionMonthValue = Integer.parseInt(optionsMonth.get(i).getAttribute("value"));
            if (optionMonthValue > maxOptionMonthValue) {
                maxOptionMonthValue = optionMonthValue;
                maxIndexMonth = i;
            }
        }
        paymentPage.selectExpiresMonth().selectByIndex(maxIndexMonth);

        // Получите список всех опций
        List<WebElement> optionsYear = paymentPage.selectExpiresYear().getOptions();
        // Используйте цикл для определения индекса максимальной опции
        int maxIndexYear = 0;
        int maxOptionYearValue = 0;
        for (int i = 1; i < optionsYear.size(); i++) {
            int optionYearValue = Integer.parseInt(optionsYear.get(i).getAttribute("value"));
            if (optionYearValue > maxOptionYearValue) {
                maxOptionYearValue = optionYearValue;
                maxIndexYear = i;
            }
        }
        paymentPage.selectExpiresYear().selectByIndex(maxIndexYear);

        paymentPage.inputCardCvc().click();
        paymentPage.inputCardCvc().clear();
        paymentPage.enterCardCvc("856");

        String OrderNumber = (String) paymentPage.OrderNumber().getText();
        String TotalAmount = (String) paymentPage.TotalAmount().getText();

        Actions action = new Actions(driver);
        action.moveToElement(driver.findElement(By.xpath("//*[@id=\"cvc-hint-toggle\"]"))).build().perform();

        File scFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        FileHandler.copy(scFile, new File("src/test/1.png"));

        paymentPage.clickActionSubmit();

        assertEquals(driver.findElement(By.xpath("//*[@id=\"payment-item-ordernumber\"]/div[2]")).getText(), OrderNumber);
        assertEquals(driver.findElement(By.xpath("//*[@id=\"payment-item-total-amount\"]")).getText(), TotalAmount);
        assertEquals(driver.findElement(By.xpath("//*[@id=\"payment-item-status\"]/div[2]")).getText(), paymentItemStatus);
    }

    @Test
    public void ConfirmedPay() {
        driver.get(baseUrl);
        WebElement inputCardNumber = driver.findElement(By.id("input-card-number"));
        inputCardNumber.click();
        inputCardNumber.clear();
        inputCardNumber.sendKeys("4000 0000 0000 0077");
        WebElement inputCardHolder = driver.findElement(By.id("input-card-holder"));
        inputCardHolder.click();
        inputCardHolder.clear();
        inputCardHolder.sendKeys("JONNY BUNGALO");
        driver.findElement(By.id("card-expires-month")).click();
        new Select(driver.findElement(By.id("card-expires-month"))).selectByVisibleText("05");
        driver.findElement(By.id("card-expires-year")).click();
        new Select(driver.findElement(By.id("card-expires-year"))).selectByVisibleText("2025");
        WebElement inputCardCvc = driver.findElement(By.id("input-card-cvc"));
        inputCardCvc.click();
        inputCardCvc.clear();
        inputCardCvc.sendKeys("856");

        String OrderNumber = (String) driver.findElement(By.id("order-number")).getText();
        String TotalAmount = (String) driver.findElement(By.id("total-amount")).getText();

        driver.findElement(By.id("action-submit")).click();


        assertEquals(driver.findElement(By.xpath("//*[@id=\"payment-item-ordernumber\"]/div[2]")).getText(), OrderNumber);
        assertEquals(driver.findElement(By.xpath("//*[@id=\"payment-item-total-amount\"]")).getText(), TotalAmount);
        assertEquals(driver.findElement(By.xpath("//*[@id=\"payment-item-status\"]/div[2]")).getText(), "CONFIRMED");
    }

    @Test
    public void DeclinedPay() {
        driver.get(baseUrl);
        WebElement inputCardNumber = driver.findElement(By.id("input-card-number"));
        inputCardNumber.click();
        inputCardNumber.clear();
        inputCardNumber.sendKeys("4444444444449777");
        WebElement inputCardHolder = driver.findElement(By.id("input-card-holder"));
        inputCardHolder.click();
        inputCardHolder.clear();
        inputCardHolder.sendKeys("JONNY BUNGALO");
        driver.findElement(By.id("card-expires-month")).click();
        new Select(driver.findElement(By.id("card-expires-month"))).selectByVisibleText("05");
        driver.findElement(By.id("card-expires-year")).click();
        new Select(driver.findElement(By.id("card-expires-year"))).selectByVisibleText("2025");
        WebElement inputCardCvc = driver.findElement(By.id("input-card-cvc"));
        inputCardCvc.click();
        inputCardCvc.clear();
        inputCardCvc.sendKeys("856");

        String OrderNumber = (String) driver.findElement(By.id("order-number")).getText();
        String TotalAmount = (String) driver.findElement(By.id("total-amount")).getText();

        driver.findElement(By.id("action-submit")).click();


        assertEquals(driver.findElement(By.xpath("//*[@id=\"payment-item-ordernumber\"]/div[2]")).getText(), OrderNumber);
        assertEquals(driver.findElement(By.xpath("//*[@id=\"payment-item-total-amount\"]")).getText(), TotalAmount);
        assertEquals(driver.findElement(By.xpath("//*[@id=\"payment-item-status\"]/div[2]")).getText(), "DECLINED");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        driver.quit();
    }
}

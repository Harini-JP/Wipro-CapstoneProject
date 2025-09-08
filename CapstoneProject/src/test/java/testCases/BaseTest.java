package testCases;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterMethod;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;

public class BaseTest {
    protected static WebDriver driver;

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        driver = new ChromeDriver(opts);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
        driver.get("https://bstackdemo.com/");
    }

    // 👇 Add pause after every test
    @AfterMethod(alwaysRun = true)
    public void pauseAfterEachTest() {
        try {
            Thread.sleep(4000); // 4s pause
        } catch (InterruptedException ignored) {}
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}

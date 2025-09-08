package utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Robust DriverFactory that uses WebDriverManager and stable ChromeOptions.
 * Replace your existing DriverFactory with this to reduce blank-page and CDP warnings.
 */
public class DriverFactory {
    private static WebDriver driver;   // shared singleton driver

    // Default implicit wait seconds 
    private static final int IMPLICIT_WAIT_SECONDS = 5;
    private static final int PAGE_LOAD_TIMEOUT_SECONDS = 60;

    
    public static WebDriver initDriver(String browser) {
        if (driver == null) {
            // Reduce Selenium noisy logs
            Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
            Logger.getLogger("org.openqa.selenium.remote").setLevel(Level.OFF);

            if (browser == null || browser.isEmpty()) browser = "chrome";

            if (browser.equalsIgnoreCase("chrome")) {
                driver = createChromeDriver(null);
            } else if (browser.equalsIgnoreCase("firefox")) {
                WebDriverManager.firefoxdriver().setup();
                driver = new FirefoxDriver();
                driver.manage().window().maximize();
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(IMPLICIT_WAIT_SECONDS));
                driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(PAGE_LOAD_TIMEOUT_SECONDS));
            } else {
                throw new IllegalArgumentException("Browser not supported: " + browser);
            }
        }
        return driver;
    }

    
    public static WebDriver initDriverWithDownload(String downloadDirAbsolutePath) {
        try {
            Path d = Path.of(downloadDirAbsolutePath);
            if (!Files.exists(d)) Files.createDirectories(d);
        } catch (Exception e) {
            System.err.println("Could not create download dir: " + e.getMessage());
        }

        // create Chrome with download prefs
        driver = createChromeDriver(downloadDirAbsolutePath);

        return driver;
    }

    /**
     * Creates and returns a ChromeDriver instance with stable options.
     * If downloadDir is null, no download prefs are set.
     */
    private static WebDriver createChromeDriver(String downloadDir) {
        // ensure chromedriver binary matches local chrome
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        // Recommended stable flags for CI / local automation
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-infobars");
        options.setExperimentalOption("excludeSwitches", new String[] {"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        // Accept insecure certs if your environment intercepts TLS
        options.setAcceptInsecureCerts(true);

        

        // Download prefs
        if (downloadDir != null && !downloadDir.isEmpty()) {
            Map<String, Object> prefs = new HashMap<>();
            prefs.put("download.default_directory", downloadDir);
            prefs.put("download.prompt_for_download", false);
            prefs.put("download.directory_upgrade", true);
            prefs.put("plugins.always_open_pdf_externally", true); // force PDF download instead of opening in viewer
            options.setExperimentalOption("prefs", prefs);
        }

        // Create driver
        WebDriver localDriver = new ChromeDriver(options);

        // Timeouts & window
        localDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(IMPLICIT_WAIT_SECONDS));
        localDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(PAGE_LOAD_TIMEOUT_SECONDS));
        localDriver.manage().window().maximize();

        return localDriver;
    }

    public static WebDriver getDriver() {
        return driver;
    }

    public static void quitDriver() {
        if (driver != null) {
            try { driver.quit(); } catch (Exception ignored) {}
            driver = null;
        }
    }
}

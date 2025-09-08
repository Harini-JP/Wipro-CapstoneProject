package utils;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.openqa.selenium.*;

import java.io.File;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportUtils {
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> testThread = new ThreadLocal<>();
    private static String reportDir;
    private static String screenshotsDir;

    public static synchronized void initReports() {
        if (extent != null) return; // already initialized

        try {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            reportDir = System.getProperty("user.dir") + File.separator + "target" + File.separator + "reports" + File.separator + timestamp;
            screenshotsDir = reportDir + File.separator + "screenshots";
            Files.createDirectories(Paths.get(screenshotsDir));

            ExtentSparkReporter spark = new ExtentSparkReporter(reportDir + File.separator + "extent.html");
            spark.config().setReportName("Automation Test Report");
            spark.config().setDocumentTitle("Test Execution Results");

            extent = new ExtentReports();
            extent.attachReporter(spark);

            // optional system info:
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("Java", System.getProperty("java.version"));
            extent.setSystemInfo("User", System.getProperty("user.name"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createTest(String testName) {
        if (extent == null) initReports();
        ExtentTest t = extent.createTest(testName);
        testThread.set(t);
    }

    public static ExtentTest getTest() {
        return testThread.get();
    }

    public static void logInfo(String msg) {
        ExtentTest t = getTest();
        if (t != null) t.info(msg);
    }

    public static void logPass(String msg) {
        ExtentTest t = getTest();
        if (t != null) t.pass(msg);
    }

    public static void logFail(String msg) {
        ExtentTest t = getTest();
        if (t != null) t.fail(msg);
    }

   
    public static String captureScreenshot(WebDriver driver, String namePrefix) {
        if (driver == null) return "";
        try {
            String ts = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
            String fileName = (namePrefix == null || namePrefix.isEmpty() ? "screenshot" : namePrefix) + "_" + ts + ".png";
            String full = screenshotsDir + File.separator + fileName;
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path dest = Paths.get(full);
            Files.copy(src.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            return dest.toAbsolutePath().toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static synchronized void flushReports() {
        if (extent != null) {
            extent.flush();
            // clear so future runs initialize a fresh report
            extent = null;
            testThread.remove();
        }
    }

   
    public static String getReportDir() {
        return reportDir;
    }
}

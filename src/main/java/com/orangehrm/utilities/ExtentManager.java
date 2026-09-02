/**
 * Created by Ajeet Vishwakarma
 * Date : 21-08-2026
 * Time : 09:23 pm
 */

package com.orangehrm.utilities;


import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ExtentManager {

    private static final Map<Long, WebDriver> driverMap = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerManager.getLogger(ExtentManager.class);
    private static final String REPORT_DIRECTORY = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "extentreport";
    private static final String REPORT_PATH = REPORT_DIRECTORY + File.separator + "ExtentReport.html";
    private static final String SCREENSHOT_DIRECTORY = REPORT_DIRECTORY + File.separator + "screenshots";
    private static final ThreadLocal<ExtentTest> test = new ThreadLocal<>();
    private static ExtentReports extentReports;
    private static final String REPORT_NAME = "Automation Test Report";
    private static final String DOCUMENT_TITLE = "Test Execution Report";
    private static final int MAX_FILE_NAME_LENGTH = 100;

    private ExtentManager() {
        // Prevent object creation
    }

    public static synchronized ExtentReports getReport() {
        if (extentReports != null) {
            return extentReports;
        } try {
            createDirectories();
            logger.info( "Initializing Extent Report" );
            logger.info( "Extent Report path: {}", REPORT_PATH );
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(REPORT_PATH);
            configureSparkReporter(sparkReporter);
            extentReports = new ExtentReports();
            extentReports.attachReporter( sparkReporter );
            addSystemInformation();
            logger.info( "Extent Report initialized successfully" );
            return extentReports;
        } catch (Exception e) {
            logger.error( "Failed to initialize Extent Report", e );
            throw new RuntimeException( "Unable to initialize Extent Report", e );
        }
    }

    private static void configureSparkReporter( ExtentSparkReporter sparkReporter) {
        sparkReporter.config() .setReportName(REPORT_NAME);
        sparkReporter.config() .setDocumentTitle(DOCUMENT_TITLE);
        sparkReporter.config() .setTheme(Theme.DARK);
    }

    public static ExtentTest startTest( String testName) {
        validateTestName(testName);
        try {
            ExtentTest extentTest = getReport().createTest(testName);
            test.set(extentTest); logger.info( "Extent test started: {} | Thread: {}", testName, Thread.currentThread().getId() );
            return extentTest;
        } catch (Exception e) {
            logger.error( "Unable to start Extent test: {}", testName, e );
            throw new RuntimeException( "Unable to start Extent test: " + testName, e );
        }
    }

    public static ExtentTest getTest() {
        ExtentTest currentTest = test.get();
        if (currentTest == null) {
            logger.warn("No active ExtentTest found for thread: {}", Thread.currentThread().getId());
        }
        return currentTest;
    }

    public static String getTestName() {
        ExtentTest currentTest = getTest();
        if (currentTest == null) {
            return "Unknown Test";
        } try {
            String testName = currentTest .getModel() .getName();
            return isBlank(testName) ? "Unknown Test" : testName;
        } catch (Exception e) {
            logger.warn( "Unable to retrieve current test name", e );
            return "Unknown Test";
        }
    }

    public static void logStep(String message) {
        if (!hasActiveTest()) {
            return;
        }if (isBlank(message)) {
            logger.warn( "Cannot log empty Extent step" );
            return;
        } try {
            getTest().info(message);
            logger.debug( "Extent INFO: {}", message );
        } catch (Exception e) {
            logger.error( "Unable to log Extent step: {}", message, e );
        }
    }

    public static void logPass(String message) {
        if (!hasActiveTest()) {
            return;
        } try {
            getTest().pass(message);
            logger.debug( "Extent PASS: {}", message );
        } catch (Exception e) {
            logger.error( "Unable to log PASS message: {}", message, e );
        }
    }

//    public static void logStepWithScreenshot(WebDriver driver, String logMessage, String screenshotMessage) {
//        getTest().pass(getTestName() + logMessage + screenshotMessage);
//    }

    public static void logFail(String message) {
        if (!hasActiveTest()) {
            return;
        } try {
            getTest().fail(message);
            logger.debug( "Extent FAIL: {}", message );
        } catch (Exception e) {
            logger.error( "Unable to log FAIL message: {}", message, e );
        }
    }

    public static void logSkip(String message) {
        if (!hasActiveTest()) {
            return;
        } try {
            getTest().skip(message);
            logger.debug( "Extent SKIP: {}", message );
        } catch (Exception e) {
            logger.error( "Unable to log SKIP message: {}", message, e );
        }
    }

    public static void logPassWithScreenshot( WebDriver driver, String message) {
        attachScreenshot( driver, message, ScreenshotStatus.PASS );
    }

    public static void logFailWithScreenshot( WebDriver driver, String message) {
        attachScreenshot( driver, message, ScreenshotStatus.FAIL );
    }

    public static void logSkipWithScreenshot( WebDriver driver, String message) {
        attachScreenshot( driver, message, ScreenshotStatus.SKIP );
    }

    private static void attachScreenshot( WebDriver driver, String message, ScreenshotStatus status) {
        if (!hasActiveTest()) {
            logger.warn( "Cannot attach screenshot. " + "No active ExtentTest." );
            return;
        }
        validateDriver(driver);
        try {
            String screenshotBase64 = takeScreenshot( driver, getTestName() );
            var mediaEntity = MediaEntityBuilder .createScreenCaptureFromBase64String( screenshotBase64 ) .build();
            switch (status) {
                case PASS: getTest().pass( message, mediaEntity );
                logger.info( "PASS screenshot attached: {}", message );
                break;
                case FAIL: getTest().fail( message, mediaEntity );
                logger.info( "FAIL screenshot attached: {}", message );
                break;
                case SKIP: getTest().skip( message, mediaEntity );
                logger.info( "SKIP screenshot attached: {}", message );
                break;
            }
        } catch (Exception e) {
            logger.error( "Unable to attach screenshot: {}", message, e );
        }
    }


    //take screenshot with date and time
    public static String takeScreenshot( WebDriver driver, String screenshotName) {
        validateDriver(driver); try {
            createDirectories(); TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
            File screenshotFile = takesScreenshot.getScreenshotAs( OutputType.FILE );
            String timestamp = LocalDateTime.now() .format( DateTimeFormatter.ofPattern( "yyyy-MM-dd_HH-mm-ss-SSS" ) );
            String safeName = sanitizeFileName( screenshotName ); String fileName = safeName + "_" + timestamp + ".png";
            Path destination = Paths.get( SCREENSHOT_DIRECTORY, fileName );
            FileUtils.copyFile( screenshotFile, destination.toFile() );
            logger.info( "Screenshot saved: {}", destination );
            return convertToBase64( screenshotFile );
        } catch (IOException e) {
            logger.error( "Unable to save screenshot: {}", screenshotName, e );
            throw new RuntimeException( "Unable to save screenshot: " + screenshotName, e );
        } catch (Exception e) {
            logger.error( "Unable to take screenshot: {}", screenshotName, e );
            throw new RuntimeException( "Unable to take screenshot: " + screenshotName, e );
        }
    }

    public static String convertToBase64( File screenshotFile) {
        if (screenshotFile == null) {
            throw new IllegalArgumentException( "Screenshot file cannot be null" );
        } if (!screenshotFile.exists()) {
            throw new IllegalArgumentException( "Screenshot file does not exist: " + screenshotFile );
        } try {
            byte[] fileContent = FileUtils.readFileToByteArray( screenshotFile );
            return Base64.getEncoder() .encodeToString(fileContent);
        } catch (IOException e) {
            logger.error( "Unable to convert screenshot to Base64", e );
            throw new RuntimeException( "Unable to convert screenshot to Base64", e );
        }
    }


    public static synchronized void flushReport() {
        if (extentReports == null) {
            logger.warn( "Extent Report is not initialized. " + "Nothing to flush." );
            return;
        } try {
            extentReports.flush();
            logger.info( "Extent Report flushed successfully" );
        } catch (Exception e) {
            logger.error( "Unable to flush Extent Report", e );
        }
    }

    public static void removeTest() {
        try {
            test.remove();
            logger.debug("ExtentTest removed from current thread: {}", Thread.currentThread().getId());
        } catch (Exception e) {
            logger.error("Unable to remove ExtentTest", e);
        }
    }


    private static void createDirectories() throws IOException {
        Files.createDirectories( Paths.get(REPORT_DIRECTORY) );
        Files.createDirectories( Paths.get(SCREENSHOT_DIRECTORY) );
    }

    private static void addSystemInformation() {
        extentReports.setSystemInfo( "User Directory", System.getProperty("user.dir") );
        extentReports.setSystemInfo( "Operating System", System.getProperty("os.name") );
        extentReports.setSystemInfo( "OS Version", System.getProperty("os.version") );
        extentReports.setSystemInfo( "OS Architecture", System.getProperty("os.arch") );
        extentReports.setSystemInfo( "Java Version", System.getProperty("java.version") );
        extentReports.setSystemInfo( "Java Vendor", System.getProperty("java.vendor") );
    }

    private static String sanitizeFileName( String fileName) {
        if (isBlank(fileName)) {
            return "screenshot";
        }
        String sanitized = fileName.replaceAll( "[^a-zA-Z0-9._-]", "_" );
        if (sanitized.length() > MAX_FILE_NAME_LENGTH) {
            sanitized = sanitized.substring( 0, MAX_FILE_NAME_LENGTH );
        }
        return sanitized;
    }

    private static boolean hasActiveTest() {
        return test.get() != null;
    }

    private static void validateDriver( WebDriver driver) {
        if (driver == null) {
            logger.error( "WebDriver cannot be null" );
            throw new IllegalArgumentException( "WebDriver cannot be null" );
        } if (!(driver instanceof TakesScreenshot)) {
            throw new IllegalArgumentException( "WebDriver does not support screenshots" );
        }
    }

    private static void validateTestName( String testName) {
        if (isBlank(testName)) {
            logger.error( "Test name cannot be null or empty" );
            throw new IllegalArgumentException( "Test name cannot be null or empty" );
        }
    }

    private static boolean isBlank( String value) {
        return value == null || value.isBlank();
    }

    private enum ScreenshotStatus { PASS, FAIL, SKIP }

    //attach ss into report using base64
//    public static void attachScreenShot(WebDriver driver, String message) throws IOException {
//        String screenshotBase64 = takeScreenshot(driver, getTestName());
//        getTest().info(getTestName() + message);
//        getTest().info(message, com.aventstack.extentreports.MediaEntityBuilder.createScreenCaptureFromBase64String(screenshotBase64).build());
//    }


    public static void registerDriver(WebDriver driver) {
        driverMap.put(Thread.currentThread().getId(), driver);
    }


}

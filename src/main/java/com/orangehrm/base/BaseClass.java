package com.orangehrm.base;

import com.orangehrm.actiondriver.ActionDriver;
import com.orangehrm.utilities.ExtentManager;
import com.orangehrm.utilities.LoggerManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class BaseClass {

    private static final Logger logger = LoggerManager.getLogger(BaseClass.class);
    private static final String CONFIG_PATH = "src/main/resources/config.properties";
    private static final String BROWSER = "browser";
    private static final String URL = "url";
    private static final String IMPLICIT_WAIT = "implicitWait";
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static final ThreadLocal<ActionDriver> actionDriver = new ThreadLocal<>();
    private static final Properties properties = new Properties();

    @BeforeSuite(alwaysRun = true)
    public void loadConfig() throws IOException {
        logger.info("========== Loading Test Configuration ==========");
        try (FileInputStream input = new FileInputStream(CONFIG_PATH)) {
            properties.load(input);
            validateConfiguration();
            logger.info("Configuration loaded and validated successfully");
        } catch (IOException e) {
            logger.error( "Failed to load configuration from: {}", CONFIG_PATH, e );
            throw e;
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void setup(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        long threadId = Thread.currentThread().getId();
        logger.info( "========== Starting Test: {} | Thread: {} ==========", testName, threadId );
        try {
            ExtentManager.startTest(testName);
            logger.debug( "Extent test started: {}", testName );
            WebDriver webDriver = createDriver();
            driver.set(webDriver);
            logger.info( "{} browser launched successfully", getBrowserName() );
            configureBrowser(webDriver);
            ActionDriver currentActionDriver = new ActionDriver(webDriver);
            actionDriver.set(currentActionDriver);
            logger.info( "ActionDriver initialized successfully" );
            // Register driver with Extent
            ExtentManager.registerDriver(webDriver);
            logger.debug( "WebDriver registered for thread: {}", threadId );
            logger.info( "Test setup completed successfully: {}", testName );
        } catch (Exception e) {
            logger.error( "Test setup failed: {}", testName, e );
            if (ExtentManager.getTest() != null) {
                ExtentManager.getTest().fail( "Test setup failed: " + e.getMessage() );
            }
            // Clean up partially initialized driver
            quitDriver();
            // Remove ThreadLocal values
            driver.remove();
            actionDriver.remove();
            throw e;
        }
    }

//    private void launchBrowser() {
//
//        String browser = properties.getProperty("browser");
//        if (browser == null || browser.isBlank()) {
//            logger.error("Browser is not configured in config.properties");
//            throw new IllegalArgumentException("Browser is not configured in config.properties");
//        }
//        logger.info("Launching browser: {}", browser);
//        switch (browser.toLowerCase()) {
//            case "chrome":
//                driver.set(new ChromeDriver());
//                logger.info("Chrome browser launched successfully");
//                break;
//            case "firefox":
//                driver.set(new FirefoxDriver());
//                logger.info("Firefox browser launched successfully");
//                break;
//            default:
//                logger.error("Unsupported browser configured: {}", browser);
//                throw new IllegalArgumentException("Unsupported browser: " + browser);
//        }
//    }

//    private void configureBrowser() {
//        logger.info("Starting browser configuration");
//        String implicitWaitValue = properties.getProperty("implicitWait");
//        if (implicitWaitValue == null || implicitWaitValue.isBlank()) {
//            logger.error("Implicit wait is not configured in config.properties");
//            throw new IllegalArgumentException("implicitWait is not configured in config.properties");
//        }
//        int implicitWait;
//        try {
//            implicitWait = Integer.parseInt(implicitWaitValue);
//        } catch (NumberFormatException e) {
//            logger.error("Invalid implicitWait value: {}", implicitWaitValue, e);
//            throw new IllegalArgumentException("Invalid implicitWait value: " + implicitWaitValue, e);
//        }
//        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
//        logger.info("Implicit wait configured to {} seconds", implicitWait);
//        getDriver().manage().window().maximize();
//        logger.info("Browser window maximized");
//        String url = properties.getProperty("url");
//        if (url == null || url.isBlank()) {
//            logger.error("URL is not configured in config.properties");
//            throw new IllegalArgumentException("URL is not configured in config.properties");
//        }
//        logger.info("Navigating to URL: {}", url);
//        getDriver().get(url);
//        logger.info("Successfully navigated to URL");
//    }

//    @AfterMethod(alwaysRun = true)
//    public void tearDown(ITestResult result) {
//        String testName = result.getMethod().getMethodName();
//        logger.info( "========== Starting Test Cleanup: {} ==========", testName );
//        try {
//            logTestResult(result, testName);
//        } catch (Exception e) {
//            logger.error( "Failed while updating Extent Report for: {}", testName, e );
//        } finally {
//            // Always close browser
//            quitDriver();
//            // Always remove ThreadLocal values
//            driver.remove();
//            actionDriver.remove();
//            // Remove Extent test from current thread
//            ExtentManager.removeTest();
//            logger.debug( "ThreadLocal WebDriver and ActionDriver removed" );
//            logger.info( "Test cleanup completed: {}", testName );
//        }
//    }
        @AfterMethod(alwaysRun = true)
        public void tearDown(ITestResult result) {

            String testName = result.getMethod().getMethodName();
            logger.info("Starting test cleanup: {}", testName);
            try { WebDriver currentDriver = getDriverSafely();
                  switch (result.getStatus()) {
                    case ITestResult.SUCCESS:
                        logger.info("Test PASSED: {}", testName);
                        if (currentDriver != null) {
                            ExtentManager.logPassWithScreenshot(currentDriver, "Test passed: " + testName);
                        } else {
                            ExtentManager.logPass("Test passed: " + testName);
                        }
                        break;
                    case ITestResult.FAILURE:
                        logger.error("Test FAILED: {}", testName, result.getThrowable());
                        String failureMessage = result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown failure";
                        if (currentDriver != null) {
                            ExtentManager.logFailWithScreenshot(currentDriver, "Test failed: " + testName + " | " + failureMessage);
                        } else {
                            ExtentManager.logFail("Test failed: " + testName + " | " + failureMessage);
                        }
                        break;
                    case ITestResult.SKIP:
                        logger.warn("Test SKIPPED: {}", testName);
                        if (currentDriver != null) {
                            ExtentManager.logSkipWithScreenshot(currentDriver, "Test skipped: " + testName);
                        } else {
                            ExtentManager.logSkip("Test skipped: " + testName);
                        }
                        break;
                    default:
                        logger.warn("Unknown test status: {}", result.getStatus());
                        ExtentManager.logStep("Unknown test status for: " + testName);
                }
            } catch (Exception e) {
                logger.error("Error while updating Extent Report for: {}", testName, e);
            } finally {
                WebDriver currentDriver = getDriverSafely();
                if (currentDriver != null) {
                    try {
                        currentDriver.quit();
                        logger.info("WebDriver closed successfully");
                    } catch (Exception e) {
                        logger.error("Failed to close WebDriver", e);
                    }
                }
                driver.remove();
                actionDriver.remove();
                ExtentManager.removeTest();
                logger.debug("ThreadLocal resources removed");
            }
        }

    private WebDriver getDriverSafely() {
        return driver.get();
    }

    @AfterSuite(alwaysRun = true)
    public void flushExtentReport() {
        logger.info( "========== Flushing Extent Report ==========" );
        try {
            ExtentManager.flushReport();
            logger.info( "Extent Report flushed successfully" );
        } catch (Exception e) {
            logger.error( "Failed to flush Extent Report", e );
        }
    }

    private WebDriver createDriver() {
        String browser = getBrowserName();
        logger.info( "Creating WebDriver for browser: {}", browser );
        return switch (browser) {
            case "chrome" -> new ChromeDriver();
            case "firefox" -> new FirefoxDriver();
            default -> {
                logger.error( "Unsupported browser configured: {}", browser );
                throw new IllegalArgumentException( "Unsupported browser: " + browser );
            }
        };
    }

    private void configureBrowser(WebDriver webDriver) {
        logger.info("Configuring browser");
        int implicitWait = getIntegerProperty(IMPLICIT_WAIT);
        String url = getRequiredProperty(URL);
        // Configure implicit wait
        webDriver.manage() .timeouts() .implicitlyWait( Duration.ofSeconds(implicitWait) );
        logger.info( "Implicit wait configured: {} seconds", implicitWait );
        // Maximize browser
        webDriver.manage() .window() .maximize();
        logger.info("Browser window maximized");
        // Navigate to application
        logger.info( "Navigating to application URL: {}", url );
        webDriver.get(url);
        logger.info( "Successfully navigated to application" );
    }
    private void quitDriver() {
        WebDriver webDriver = driver.get();
        if (webDriver == null) {
            logger.debug( "No WebDriver instance found for current thread" );
            return;
        } try {
            webDriver.quit();
            logger.info( "WebDriver closed successfully" );
        } catch (Exception e) {
            logger.error( "Failed to close WebDriver", e );
        }
    }
    private void logTestResult( ITestResult result, String testName) {
        if (ExtentManager.getTest() == null) {
            logger.warn( "Extent test instance is not available for: {}", testName );
            return;
        } switch (result.getStatus()) {
            case ITestResult.SUCCESS: ExtentManager.getTest() .pass("Test passed successfully");
            logger.info( "Test PASSED: {}", testName );
            break;
            case ITestResult.FAILURE: Throwable throwable = result.getThrowable();
            String failureMessage = throwable != null ? throwable.getMessage() : "Unknown test failure";
            ExtentManager.getTest() .fail( "Test failed: " + failureMessage );
            logger.error( "Test FAILED: {}", testName, throwable );
            break;
            case ITestResult.SKIP: ExtentManager.getTest() .skip("Test skipped");
            logger.warn( "Test SKIPPED: {}", testName );
            break;
            default: logger.warn( "Unknown test status: {} | Test: {}", result.getStatus(), testName );
        }
    }
    private void validateConfiguration() {
        logger.info( "Validating configuration properties" );
        getRequiredProperty(BROWSER);
        getRequiredProperty(URL);
        int implicitWait = getIntegerProperty(IMPLICIT_WAIT);
        if (implicitWait < 0) {
            throw new IllegalArgumentException( "implicitWait cannot be negative: " + implicitWait );
        }
        logger.info( "Configuration validation completed successfully" );
    }
    private String getRequiredProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            logger.error( "Required configuration property is missing: {}", key );
            throw new IllegalArgumentException( "Required configuration property is missing: " + key );
        }
        return value.trim();
    }
    private int getIntegerProperty(String key) {
        String value = getRequiredProperty(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.error( "Invalid integer value for property '{}' : {}", key, value, e );
            throw new IllegalArgumentException( "Invalid integer value for property '" + key + "': " + value, e );
        }
    }
    private String getBrowserName() {
        return getRequiredProperty(BROWSER) .toLowerCase(Locale.ROOT);
    }
    public WebDriver getDriver() {
        WebDriver webDriver = driver.get();
        if (webDriver == null) {
            logger.error( "WebDriver is not initialized for current thread" );
            throw new IllegalStateException( "WebDriver is not initialized. " + "Make sure setup() has executed." );
        }
        return webDriver;
    }
    public ActionDriver getActionDriver() {
        ActionDriver currentActionDriver = actionDriver.get();
        if (currentActionDriver == null) {
            logger.error( "ActionDriver is not initialized for current thread" );
            throw new IllegalStateException( "ActionDriver is not initialized. " + "Make sure setup() has executed." );
        }
        return currentActionDriver;
    }

    public static void pause(int seconds) {
        logger.warn("Using static pause of {} seconds. Prefer explicit waits where possible.", seconds);
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(seconds));
    }


}

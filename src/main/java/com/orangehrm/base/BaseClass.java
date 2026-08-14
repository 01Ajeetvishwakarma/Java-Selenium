package com.orangehrm.base;

import com.orangehrm.actiondriver.ActionDriver;
import com.orangehrm.utilities.LoggerManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class BaseClass {

    protected static Properties properties;
    protected WebDriver driver;
    protected ActionDriver actionDriver;
    private static final Logger logger = LoggerManager.getLogger(BaseClass.class);


    @BeforeSuite
    public void loadConfig() throws IOException {
        logger.info("Starting configuration loading");
        properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("src/main/resources/config.properties")) {
            properties.load(fileInputStream);
            logger.info("Configuration file loaded successfully");
        }catch (IOException e) {
            logger.error("Failed to load configuration file", e);
            throw e;
        }
    }

    @BeforeMethod
    public void setup() {
        logger.info("Starting test setup");
        try{
            launchBrowser();
            configureBrowser();
            actionDriver = new ActionDriver(driver);
            logger.info("ActionDriver initialized successfully");
        }catch (Exception e) {
            logger.error("Test setup failed", e);
            throw e;
        }
    }

    // initialize the web driver based on the browser in config file
    private void launchBrowser() {
        String browser = properties.getProperty("browser");

        if (browser == null || browser.isBlank()) {
            logger.error("Browser is not configured in config.properties");
            throw new IllegalArgumentException("Browser is not configured in config.properties");
        }
        logger.info("Launching browser: {}", browser);

        switch (browser.toLowerCase()) {
            case "chrome":
                driver = new ChromeDriver();
                logger.info("Chrome browser launched successfully");
                break;
            case "firefox":
                driver = new FirefoxDriver();
                logger.info("Firefox browser launched successfully");
                break;
            default:
                logger.error("Unsupported browser configured: {}", browser);
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
    }

    private void configureBrowser() {
        logger.info("Starting browser configuration");
        String implicitWaitValue = properties.getProperty("implicitWait");

        if (implicitWaitValue == null || implicitWaitValue.isBlank()) {
            logger.error("Implicit wait is not configured in config.properties");
            throw new IllegalArgumentException("implicitWait is not configured in config.properties");
        }
        int implicitWait;
         try{
             implicitWait = Integer.parseInt(implicitWaitValue);
         }catch (NumberFormatException e) {
             logger.error("Invalid implicitWait value: {}", implicitWaitValue, e);
             throw new IllegalArgumentException("Invalid implicitWait value: " + implicitWaitValue, e);
         }
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        logger.info("Implicit wait configured to {} seconds", implicitWait);
        driver.manage().window().maximize();
        logger.info("Browser window maximized");

        String url = properties.getProperty("url");
        if (url == null || url.isBlank()) {
            logger.error("URL is not configured in config.properties");
            throw new IllegalArgumentException("URL is not configured in config.properties");
        }
        logger.info("Navigating to URL: {}", url);
        driver.get(url);
        logger.info("Successfully navigated to URL");
    }

    @AfterMethod
    public void tearDown() {

        logger.info("Starting test cleanup");

        if (driver != null) {
            try {
                driver.quit();
                logger.info("WebDriver instance closed successfully");
            } catch (Exception e) {
                logger.error("Failed to close WebDriver", e);
            } finally {
                driver = null;
                actionDriver = null;
            }
        } else {
            logger.warn("WebDriver instance was already null during cleanup");
        }
    }

    public WebDriver getDriver() {
        if (driver == null) {
            logger.error("WebDriver instance is not initialized");
            throw new IllegalStateException("WebDriver is not initialized. Make sure setup() has executed.");
        }
        return driver;
    }

    public ActionDriver getActionDriver() {
        if (actionDriver == null) {
            logger.error("ActionDriver is not initialized");
            throw new IllegalStateException("ActionDriver is not initialized");
        }
        return actionDriver;
    }

    public static void pause(int seconds) {
        logger.warn("Using static pause of {} seconds. Prefer explicit waits where possible.", seconds);
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(seconds));
    }


}

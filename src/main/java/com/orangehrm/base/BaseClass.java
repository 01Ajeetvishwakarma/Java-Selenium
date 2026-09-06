/**
 * Created by Ajeet Vishwakarma
 * Date : 02-09-2026
 * Time : 11:11 pm
 */

package com.orangehrm.base;

import com.orangehrm.actiondriver.ActionDriver;
import com.orangehrm.utilities.LoggerManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import java.util.Properties;

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
            logger.error("Failed to load configuration from: {}", CONFIG_PATH, e);
            throw e;
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        long threadId = Thread.currentThread().threadId();
        logger.info("========== Starting Test Setup | Thread: {} ==========", threadId);
        try {
            WebDriver webDriver = createDriver();
            driver.set(webDriver);
            logger.info("{} browser launched successfully", getBrowserName());
            configureBrowser(webDriver);
            ActionDriver currentActionDriver = new ActionDriver(webDriver);
            actionDriver.set(currentActionDriver);
            logger.info("ActionDriver initialized successfully");
            logger.info("Test setup completed successfully | Thread: {}", threadId);
        } catch (Exception e) {
            logger.error("Test setup failed", e);
            cleanupDriver();
            throw e;
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        long threadId = Thread.currentThread().threadId();
        logger.info("========== Starting Test Cleanup | Thread: {} ==========", threadId);
        cleanupDriver();
        logger.info("========== Test Cleanup Completed | Thread: {} ==========", threadId);
    }

    private void cleanupDriver() {
        WebDriver currentDriver = driver.get();
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
        logger.debug("WebDriver and ActionDriver ThreadLocal values removed");
    }

    private WebDriver createDriver() {
        String browser = getBrowserName();

        logger.info("Creating WebDriver for browser: {}", browser);
        return switch (browser) {
            case "chrome" -> new ChromeDriver(getChromeOptions());
            case "firefox" -> new FirefoxDriver();
            default -> {
                logger.error("Unsupported browser configured: {}", browser);
                throw new IllegalArgumentException("Unsupported browser: " + browser);
            }
        };
    }

    private ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();

        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-notifications");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        return options;
    }

    private void configureBrowser(WebDriver webDriver) {
        logger.info("Configuring browser");
        int implicitWait = getIntegerProperty(IMPLICIT_WAIT);
        String url = getRequiredProperty(URL);
        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        logger.info("Implicit wait configured: {} seconds", implicitWait);
        webDriver.manage().window().maximize();
        logger.info("Browser window maximized");
        logger.info("Navigating to application URL: {}", url);
        webDriver.get(url);
        logger.info("Successfully navigated to application");
    }

    private void validateConfiguration() {
        logger.info("Validating configuration properties");
        getRequiredProperty(BROWSER);
        getRequiredProperty(URL);
        int implicitWait = getIntegerProperty(IMPLICIT_WAIT);
        if (implicitWait < 0) {
            throw new IllegalArgumentException("implicitWait cannot be negative: " + implicitWait);
        }
        logger.info("Configuration validation completed successfully");
    }

    private String getRequiredProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            logger.error("Required configuration property is missing: {}", key);
            throw new IllegalArgumentException("Required configuration property is missing: " + key);
        }
        return value.trim();
    }

    private int getIntegerProperty(String key) {
        String value = getRequiredProperty(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.error("Invalid integer value for property '{}': {}", key, value, e);
            throw new IllegalArgumentException("Invalid integer value for property '" + key + "': " + value, e);
        }
    }

    private String getBrowserName() {
        return getRequiredProperty(BROWSER).toLowerCase(Locale.ROOT);
    }


    public static WebDriver getDriver() {
        WebDriver webDriver = driver.get();
        if (webDriver == null) {
            logger.error("WebDriver is not initialized for current thread");
            throw new IllegalStateException("WebDriver is not initialized. " + "Make sure setup() has executed.");
        }
        return webDriver;
    }


    public static ActionDriver getActionDriver() {
        ActionDriver currentActionDriver = actionDriver.get();
        if (currentActionDriver == null) {
            logger.error("ActionDriver is not initialized for current thread");
            throw new IllegalStateException("ActionDriver is not initialized. " + "Make sure setup() has executed.");
        }
        return currentActionDriver;
    }



}


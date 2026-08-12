package com.orangehrm.base;

import com.orangehrm.actiondriver.ActionDriver;
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


    @BeforeSuite
    public void loadConfig() throws IOException {
        // load the configuration file
        properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("src/main/resources/config.properties")) {
            properties.load(fileInputStream);
        }
    }

    @BeforeMethod
    public void setup() {
        launchBrowser();
        configureBrowser();
        // Initialize ActionDriver for the current WebDriver instance
        actionDriver = new ActionDriver(driver);
        System.out.println("Action driver instance is created...");
    }

    // initialize the web driver based on the browser in config file
    private void launchBrowser() {
        String browser = properties.getProperty("browser");

        if (browser == null || browser.isBlank()) {
            throw new IllegalArgumentException("Browser is not configured in config.properties");
        }

        switch (browser.toLowerCase()) {

            case "chrome":
                driver = new ChromeDriver();
                break;

            case "firefox":
                driver = new FirefoxDriver();
                break;

            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
    }

    private void configureBrowser() {
        String implicitWaitValue = properties.getProperty("implicitWait");

        if (implicitWaitValue == null || implicitWaitValue.isBlank()) {
            throw new IllegalArgumentException("implicitWait is not configured in config.properties");
        }

        int implicitWait = Integer.parseInt(implicitWaitValue);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.manage().window().maximize();

        String url = properties.getProperty("url");

        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL is not configured in config.properties");
        }
        driver.get(url);
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        System.out.println("WebDriver instance is closed...");
        driver = null;
        actionDriver = null;
    }

    // driver getter method
    public WebDriver getDriver() {
        return driver;
    }

    // ActionDriver getter
    public ActionDriver getActionDriver() {
        if (actionDriver == null) {
            throw new IllegalStateException("ActionDriver is not initialized");
        }
        return actionDriver;
    }

    // Static pause for debugging/application-specific delays only
    public static void pause(int seconds) {
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(seconds));
    }


}

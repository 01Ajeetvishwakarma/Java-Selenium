package com.orangehrm.actiondriver;

import com.orangehrm.utilities.LoggerManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ActionDriver {

    private static final int EXPLICIT_WAIT = 10;

    private final WebDriverWait wait;

    private static final Logger logger = LoggerManager.getLogger(ActionDriver.class);

    public ActionDriver(WebDriver driver) {
        if (driver == null) {
            logger.error("WebDriver cannot be null");
            throw new IllegalArgumentException("WebDriver cannot be null");
        }
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
        logger.info("ActionDriver initialized with {} seconds explicit wait", EXPLICIT_WAIT);
    }

    public void click(By locator) {
        try {
            logger.debug("Attempting to click element: {}", locator);
            WebElement element = waitForElementToBeClickable(locator);
            element.click();
            logger.info("Successfully clicked element: {}", locator);
        } catch (Exception e) {
            logger.error("Unable to click element: {}", locator, e);
            throw new RuntimeException("Unable to click element: " + locator, e);
        }
    }

    public void enterText(By locator, String text) {
        try {
            logger.debug("Attempting to enter text into element: {}", locator);
            WebElement element = waitForElementToBeVisible(locator);
            element.clear();
            element.sendKeys(text);
            logger.info("Text entered successfully into element: {}", locator);
        } catch (Exception e) {
            logger.error("Unable to enter text into element: {}", locator, e);
            throw new RuntimeException("Unable to enter text into element: " + locator, e);
        }
    }

    public String getText(By locator) {
        try {
            logger.debug("Attempting to get text from element: {}", locator);
            WebElement element = waitForElementToBeVisible(locator);
            String actualText = element.getText();
            logger.info("Successfully retrieved text from element: {}", locator);
            return actualText;
        } catch (Exception e) {
            logger.error("Unable to get text from element: {}", locator, e);
            throw new RuntimeException("Unable to get text from element: " + locator, e);
        }
    }

    public void compareText(By locator, String expectedText) {
        try {
            logger.debug("Comparing text for element: {}", locator);
            WebElement element = waitForElementToBeVisible(locator);
            String actualText = element.getText();
            if (!expectedText.equals(actualText)) {
                logger.error("Text comparison failed. Expected: [{}], Actual: [{}]", expectedText, actualText);
                throw new AssertionError("Text comparison failed. Expected: [" + expectedText + "] but Actual: [" + actualText + "]");
            }
            logger.info("Text comparison successful. Expected: [{}], Actual: [{}]", expectedText, actualText);
        } catch (Exception e) {
            logger.error("Unable to compare text for element: {}", locator, e);
            throw new RuntimeException("Unable to compare text for element: " + locator, e);
        }
    }

    public boolean isElementDisplayed(By locator) {
        try {
            logger.debug("Checking if element is displayed: {}", locator);
            WebElement element = waitForElementToBeVisible(locator);
            boolean displayed = element.isDisplayed();

            if (displayed) {
                logger.info("Element is displayed: {}", locator);
            } else {
                logger.warn("Element is present but not displayed: {}", locator);
            }
            return displayed;
        } catch (Exception e) {
            logger.warn("Element is not displayed: {}", locator, e);
            return false;
        }
    }

    private WebElement waitForElementToBeClickable(By locator) {
        try {
            logger.debug("Waiting for element to be clickable: {}", locator);
            return wait.until(ExpectedConditions.elementToBeClickable(locator));
        } catch (TimeoutException e) {
            logger.error("Element was not clickable within {} seconds: {}", EXPLICIT_WAIT, locator, e);
            throw new RuntimeException("Element was not clickable within " + EXPLICIT_WAIT + " seconds: " + locator, e);
        }
    }

    private WebElement waitForElementToBeVisible(By locator) {
        try {
            logger.debug("Waiting for element to be visible: {}", locator);
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            logger.error("Element was not visible within {} seconds: {}", EXPLICIT_WAIT, locator, e);
            throw new RuntimeException("Element was not visible within " + EXPLICIT_WAIT + " seconds: " + locator, e);
        }
    }


}
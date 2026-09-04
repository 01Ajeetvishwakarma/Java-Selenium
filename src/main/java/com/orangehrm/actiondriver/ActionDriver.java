package com.orangehrm.actiondriver;


import com.orangehrm.utilities.LoggerManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ActionDriver {

    private static final int EXPLICIT_WAIT = 10;
    private static final int MAX_DESCRIPTION_LENGTH = 30;
    private static final Logger logger = LoggerManager.getLogger(ActionDriver.class);
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor javascriptExecutor;

    public ActionDriver(WebDriver driver) {
        if (driver == null) {
            logger.error("WebDriver cannot be null");
            throw new IllegalArgumentException("WebDriver cannot be null");
        }
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
        this.javascriptExecutor = (JavascriptExecutor) driver;
        logger.info("ActionDriver initialized with {} seconds explicit wait", EXPLICIT_WAIT);
    }

    public void click(By locator) {
        String description = getElementDescription(locator);
        try {
            logger.debug("Attempting to click: {}", description);
            WebElement element = waitForElementToBeClickable(locator);
            element.click();
            logger.info("Successfully clicked: {}", description);
        } catch (Exception e) {
            logger.error("Unable to click: {}", description, e);
            throw new RuntimeException("Unable to click: " + description, e);
        }
    }

    public void enterText(By locator, String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }
        String description = getElementDescription(locator);
        try {
            logger.debug("Entering text into: {}", description);
            WebElement element = waitForElementToBeVisible(locator);
            element.clear();
            element.sendKeys(text);
            logger.info("Text entered successfully into: {}", description);
        } catch (Exception e) {
            logger.error("Unable to enter text into: {}", description, e);
            throw new RuntimeException("Unable to enter text into: " + description, e);
        }
    }

    public String getText(By locator) {
        String description = getElementDescription(locator);
        try {
            logger.debug("Getting text from: {}", description);
            WebElement element = waitForElementToBeVisible(locator);
            String actualText = element.getText();
            logger.info("Text retrieved from {}: [{}]", description, actualText);
            return actualText;
        } catch (Exception e) {
            logger.error("Unable to get text from: {}", description, e);
            throw new RuntimeException("Unable to get text from: " + description, e);
        }
    }

//    public void compareText( By locator, String expectedText) {
//        validateLocator(locator);
//        if (expectedText == null) {
//            throw new IllegalArgumentException( "Expected text cannot be null" );
//        }
//        String description = getElementDescription(locator);
//        try {
//            logger.debug( "Comparing text for: {}", description );
//            WebElement element = waitForElementToBeVisible(locator);
//            String actualText = element.getText();
//            if (!expectedText.equals(actualText)) {
//                applyBorder( element, "red" );
//                String message = String.format( "Text mismatch for %s. Expected: [%s], Actual: [%s]", description, expectedText, actualText );
//                logger.error(message);
//
//                throw new AssertionError(message);
//            }
//            applyBorder( element, "green" );
//            String message = String.format( "Text matched for %s. Expected: [%s], Actual: [%s]", description, expectedText, actualText );
//            logger.info(message);
//            logStep(message);
//        }
//        catch (AssertionError e) {
//            throw e;
//        }
//        catch (Exception e) {
//            logger.error( "Unable to compare text for: {}", description, e );
//            throw new RuntimeException( "Unable to compare text for: " + description, e );
//        }
//    }


    public boolean isElementDisplayed(By locator) {
        String description = getElementDescription(locator);
        try {
            logger.debug("Checking visibility of: {}", description);
            WebElement element = waitForElementToBeVisible(locator);
            boolean displayed = element.isDisplayed();
            if (displayed) {
                logger.info("Element is displayed: {}", description);
            } else {
                logger.warn("Element is present but not displayed: {}", description);
            }
            return displayed;
        } catch (TimeoutException e) {
            logger.warn("Element was not displayed within {} seconds: {}", EXPLICIT_WAIT, description);
            return false;
        } catch (Exception e) {
            logger.error("Error while checking element visibility: {}", description, e);
            return false;
        }
    }

    private WebElement waitForElementToBeClickable(By locator) {
        try {
            logger.debug("Waiting for element to be clickable: {}", locator);
            return wait.until(ExpectedConditions.elementToBeClickable(locator));
        } catch (TimeoutException e) {
            String description = getLocatorDescription(locator);
            logger.error("Element was not clickable within {} seconds: {}", EXPLICIT_WAIT, description, e);
            throw new RuntimeException("Element was not clickable within " + EXPLICIT_WAIT + " seconds: " + description, e);
        }
    }

    private WebElement waitForElementToBeVisible(By locator) {
        try {
            logger.debug("Waiting for element to be visible: {}", locator);
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            String description = getLocatorDescription(locator);
            logger.error("Element was not visible within {} seconds: {}", EXPLICIT_WAIT, description, e);
            throw new RuntimeException("Element was not visible within " + EXPLICIT_WAIT + " seconds: " + description, e);
        }
    }

    public String getElementDescription(By locator) {
        if (locator == null) {
            return "Unknown element";
        }
        return getLocatorDescription(locator);
    }

    private String getLocatorDescription(By locator) {
        String locatorValue = locator.toString();
        if (locatorValue.isBlank()) {
            return "Unknown element";
        }
        return truncateText(locatorValue);
    }


//    private boolean isNotEmpty(String value) {
//        return value != null && !value.isBlank();
//    }

    private String truncateText(String text) {
        if (text == null || text.length() <= MAX_DESCRIPTION_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_DESCRIPTION_LENGTH - 3) + "...";
    }







}
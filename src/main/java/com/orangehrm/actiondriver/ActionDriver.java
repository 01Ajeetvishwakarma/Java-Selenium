package com.orangehrm.actiondriver;


import com.orangehrm.utilities.ExtentManager;
import com.orangehrm.utilities.LoggerManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


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
            throw new IllegalArgumentException( "WebDriver cannot be null" );
        }
        this.driver = driver; this.wait = new WebDriverWait( driver, Duration.ofSeconds(EXPLICIT_WAIT) );
        this.javascriptExecutor = (JavascriptExecutor) driver;
        logger.info( "ActionDriver initialized with {} seconds explicit wait", EXPLICIT_WAIT );
    }

    public void click(By locator) {
        validateLocator(locator);
        String description = getElementDescription(locator);
        try {
            logger.debug( "Attempting to click: {}", description );
            WebElement element = waitForElementToBeClickable(locator);
            element.click();
            logStep( "Clicked: " + description );
            logger.info( "Successfully clicked: {}", description );
        }
        catch (Exception e) {
            logger.error( "Unable to click: {}", description, e );
            logFailure( "Unable to click: " + description );
            throw new RuntimeException( "Unable to click: " + description, e );
        }
    }

    public void enterText( By locator, String text) {
        validateLocator(locator);
        if (text == null) {
            throw new IllegalArgumentException( "Text cannot be null" );
        }
        String description = getElementDescription(locator);
        try {
            logger.debug( "Entering text into: {}", description );
            WebElement element = waitForElementToBeVisible(locator);
            element.clear();
            logStep( "Cleared existing value from: " + description );
            element.sendKeys(text);
            logStep( "Entered text into: " + description );
            logger.info( "Text entered successfully into: {}", description );
        }
        catch (Exception e) {
            logger.error( "Unable to enter text into: {}", description, e );
            logFailure( "Unable to enter text into: " + description );
            throw new RuntimeException( "Unable to enter text into: " + description, e );
        }
    }

    public String getText(By locator) {
        validateLocator(locator);
        String description = getElementDescription(locator);
        try {
            logger.debug( "Getting text from: {}", description );
            WebElement element = waitForElementToBeVisible(locator);
            String actualText = element.getText();
            logger.info( "Text retrieved from {}: [{}]", description, actualText );
            return actualText;
        } catch (Exception e) {
            logger.error( "Unable to get text from: {}", description, e );
            throw new RuntimeException( "Unable to get text from: " + description, e );
        }
    }

    public void compareText( By locator, String expectedText) {
        validateLocator(locator);
        if (expectedText == null) {
            throw new IllegalArgumentException( "Expected text cannot be null" );
        }
        String description = getElementDescription(locator);
        try {
            logger.debug( "Comparing text for: {}", description );
            WebElement element = waitForElementToBeVisible(locator);
            String actualText = element.getText();
            if (!expectedText.equals(actualText)) {
                applyBorder( element, "red" );
                String message = String.format( "Text mismatch for %s. Expected: [%s], Actual: [%s]", description, expectedText, actualText );
                logger.error(message);
                logFailure(message);
                throw new AssertionError(message);
            }
            applyBorder( element, "green" );
            String message = String.format( "Text matched for %s. Expected: [%s], Actual: [%s]", description, expectedText, actualText );
            logger.info(message);
            logStep(message);
        }
        catch (AssertionError e) {
            throw e;
        }
        catch (Exception e) {
            logger.error( "Unable to compare text for: {}", description, e );
            throw new RuntimeException( "Unable to compare text for: " + description, e );
        }
    }


    public boolean isElementDisplayed(By locator) {
        validateLocator(locator);
        String description = getElementDescription(locator);
        try {
            logger.debug( "Checking visibility of: {}", description );
            WebElement element = waitForElementToBeVisible(locator);
            boolean displayed = element.isDisplayed();
            if (displayed) {
                logger.info( "Element is displayed: {}", description );
            } else {
                logger.warn( "Element is present but not displayed: {}", description );
            } return displayed;
        } catch (TimeoutException e) {
            logger.warn( "Element was not displayed within {} seconds: {}", EXPLICIT_WAIT, description );
            return false;
        } catch (Exception e) {
            logger.error( "Error while checking element visibility: {}", description, e );
            return false;
        }
    }

    private WebElement waitForElementToBeClickable( By locator) {
        try {
            logger.debug( "Waiting for element to be clickable: {}", locator );
            return wait.until( ExpectedConditions .elementToBeClickable(locator) );
        } catch (TimeoutException e) {
            String description = getLocatorDescription(locator);
            logger.error( "Element was not clickable within {} seconds: {}", EXPLICIT_WAIT, description, e );
            throw new RuntimeException( "Element was not clickable within " + EXPLICIT_WAIT + " seconds: " + description, e );
        }
    }

    private WebElement waitForElementToBeVisible( By locator) {
        try {
        logger.debug( "Waiting for element to be visible: {}", locator );
        return wait.until( ExpectedConditions .visibilityOfElementLocated(locator) );
        } catch (TimeoutException e) {
            String description = getLocatorDescription(locator);
            logger.error( "Element was not visible within {} seconds: {}", EXPLICIT_WAIT, description, e );
            throw new RuntimeException( "Element was not visible within " + EXPLICIT_WAIT + " seconds: " + description, e );
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

//    public String getElementDescription(By locator) {
//        try {
//            if (driver == null) {
//                logger.error("WebDriver is null. Cannot get element description.");
//                return "Driver is null";
//            }
//            if (locator == null) {
//                logger.error("Locator is null. Cannot get element description.");
//                return "Locator is null";
//            }
//            logger.debug("Getting element description for locator: {}", locator);
//
//            WebElement element = waitForElementToBeVisible(locator);
//
//            String name = element.getDomAttribute("name");
//            String id = element.getDomAttribute("id");
//            String className = element.getDomAttribute("class");
//            String text = element.getText();
//            String placeholder = element.getAttribute("placeholder");
//            String description = element.getAttribute("description");
//            String elementDescription;
//
//            if (isNotEmpty(name)) {
//                elementDescription = "Element with name: " + name;
//            } else if (isNotEmpty(id)) {
//                elementDescription = "Element with id: " + id;
//            } else if (isNotEmpty(className)) {
//                elementDescription = "Element with class name: " + className;
//            } else if (isNotEmpty(text)) {
//                elementDescription = "Element with text: " + truncateText(text);
//            } else if (isNotEmpty(placeholder)) {
//                elementDescription = "Element with placeholder: " + placeholder;
//            } else if (isNotEmpty(description)) {
//                elementDescription = "Element with description: " + description;
//
//            } else {
//                logger.warn("No meaningful description found for element: {}", locator);
//                return "Unknown element";
//            }
//            logger.debug("Element description found for {}: {}", locator, elementDescription);
//            return elementDescription;
//        } catch (Exception e) {
//            logger.error("Failed to get element description for locator: {}", locator, e);
//            return "Unable to determine element description";
//        }
//    }

    private boolean isNotEmpty(String value) {
        return value != null && !value.isBlank();
    }

    private String truncateText(String text) { if (text == null || text.length() <= MAX_DESCRIPTION_LENGTH) { return text; } return text.substring( 0, MAX_DESCRIPTION_LENGTH - 3 ) + "..."; }

    public void applyBorder( By locator, String color) {
        validateLocator(locator);
        validateColor(color);
        try {
            WebElement element = waitForElementToBeVisible(locator);
            applyBorder( element, color );
        } catch (Exception e) {
            logger.error( "Failed to apply {} border to: {}", color, locator, e );
            throw new RuntimeException( "Unable to apply border to element: " + locator, e );
        }
    }
    private void applyBorder( WebElement element, String color) {
        if (element == null) {
            throw new IllegalArgumentException( "WebElement cannot be null" );
        }
        validateColor(color);
        String script = "arguments[0].style.border='3px solid " + color + "';";
        javascriptExecutor.executeScript( script, element );
        logger.debug( "Applied {} border to element", color );
    }
    private void validateLocator(By locator) {
        if (locator == null) {
            logger.error( "Locator cannot be null" );
            throw new IllegalArgumentException( "Locator cannot be null" );
        }
    }
    private void validateColor(String color) {
        if (color == null || color.isBlank()) {
            logger.error( "Border color cannot be null or empty" );
            throw new IllegalArgumentException( "Border color cannot be null or empty" );
        }
    }
    private void logStep(String message) {
        try {
            ExtentManager.logStep(message);
        } catch (Exception e) {
            logger.warn( "Unable to log step to Extent: {}", message, e );
        }
    }
    private void logFailure(String message) {
        try { ExtentManager.logFail(message);
        } catch (Exception e) {
            logger.warn( "Unable to log failure to Extent: {}", message, e );
        }
    }



}
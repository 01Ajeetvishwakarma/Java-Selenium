package com.orangehrm.actiondriver;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ActionDriver {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public ActionDriver(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // Click an element
    public void click(By locator) {
        try {
            WebElement element = waitForElementToBeClickable(locator);
            element.click();
        } catch (Exception e) {
            throw new RuntimeException("Unable to click element: " + locator, e);
        }
    }

    // Enter text into input field
    public void enterText(By locator, String text) {
        try {
            WebElement element = waitForElementToBeVisible(locator);
            element.clear();
            element.sendKeys(text);
        } catch (Exception e) {
            throw new RuntimeException("Unable to enter text into element: " + locator, e);
        }
    }

    // Get text from an element
    public String getText(By locator) {
        try {
            WebElement element = waitForElementToBeVisible(locator);
            return element.getText();
        } catch (Exception e) {
            throw new RuntimeException("Unable to get text from element: " + locator, e);
        }
    }

    // Method to compare text
    public void compareText(By locator, String expectedText) {
        try {
            waitForElementToBeVisible(locator);
            String actualText = driver.findElement(locator).getText();
            if (!expectedText.equals(actualText)) {
                throw new AssertionError(
                        "Text comparison failed. Expected: [" + expectedText + "] but Actual: [" + actualText + "]");
            }
            System.out.println("Text comparison successful. Actual: [" + actualText + "]");
        }  catch (Exception e) {
            throw new RuntimeException("Unable to compare text for element: " + locator, e);
        }
    }

    // Check if an element is displayed
    public boolean isElementDisplayed(By locator) {
        try {
            WebElement element = waitForElementToBeVisible(locator);
            return element.isDisplayed();
        } catch (Exception e) {
            System.out.println("Element is not displayed: " + locator);
            return false;
        }
    }

    // Wait for element to be clickable
    private WebElement waitForElementToBeClickable(By locator) {
        try {
            return wait.until(ExpectedConditions.elementToBeClickable(locator));
        } catch (TimeoutException e) {
            throw new RuntimeException("Element was not clickable within 10 seconds: " + locator, e);
        }
    }

    // Wait for element to be visible
    private WebElement waitForElementToBeVisible(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            throw new RuntimeException("Element was not visible within 10 seconds: " + locator, e);
        }
    }
}
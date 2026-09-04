package com.orangehrm.pages;

import com.orangehrm.actiondriver.ActionDriver;
import com.orangehrm.utilities.ExtentManager;
import com.orangehrm.utilities.LoggerManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;



public class LoginPage {

    private static final Logger logger = LoggerManager.getLogger(LoginPage.class);
    private final ActionDriver actionDriver;
    private final By userNameField = By.name("username");
    private final By passwordField = By.cssSelector("input[type='password']");
    private final By loginButton = By.xpath("//*[text()=' Login ']");
    private final By errorMessageText = By.xpath("//*[text()='Invalid credentials']");

    public LoginPage(ActionDriver actionDriver) {
        if (actionDriver == null) {
            logger.error("ActionDriver cannot be null");
            ExtentManager.logFail("ActionDriver cannot be null");
            throw new IllegalArgumentException("ActionDriver cannot be null");
        }
        this.actionDriver = actionDriver;
        logger.debug("LoginPage initialized");
        ExtentManager.logStep("LoginPage initialized successfully");
    }


    public void login(String userName, String password) {

        logger.info("Starting login process for user: {}", userName);
        actionDriver.enterText(userNameField, userName);
        ExtentManager.logStep("Username entered successfully");
        actionDriver.enterText(passwordField, password);
        ExtentManager.logStep("Password entered successfully");
        actionDriver.click(loginButton);
        ExtentManager.logStep("Login button clicked successfully");
        logger.info("Login button clicked successfully");
    }

    public boolean isElementDisplayed() {
        logger.debug("Checking invalid credentials message");
        boolean displayed = actionDriver.isElementDisplayed(errorMessageText);
        if (displayed) {
            logger.info("Invalid credentials message is displayed");
            ExtentManager.logStep("Invalid credentials message is displayed");
        } else {
            logger.warn("Invalid credentials message is not displayed");
            ExtentManager.logStep("Invalid credentials message is not displayed");
        }
        return displayed;
    }


}

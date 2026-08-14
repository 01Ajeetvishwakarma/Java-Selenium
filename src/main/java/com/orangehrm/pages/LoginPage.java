package com.orangehrm.pages;

import com.orangehrm.actiondriver.ActionDriver;
import com.orangehrm.utilities.LoggerManager;
import org.openqa.selenium.By;

import org.apache.logging.log4j.Logger;;

public class LoginPage {

    private final ActionDriver actionDriver;
    private static final Logger logger = LoggerManager.getLogger(LoginPage.class);

    public LoginPage(ActionDriver actionDriver) {
        if (actionDriver == null) {
            logger.error("ActionDriver cannot be null");
            throw new IllegalArgumentException("ActionDriver cannot be null");
        }
        this.actionDriver = actionDriver;
        logger.debug("LoginPage initialized");
    }

    private final By userNameField = By.name("username");
    private final By passwordField = By.cssSelector("input[type='password']");
    private final By loginButton = By.xpath("//*[text()=' Login ']");
    private final By errorMessageText = By.xpath("//*[text()='Invalid credentials']");

    public void login(String userName, String password) {

        logger.info("Starting login process for user: {}", userName);
        actionDriver.enterText(userNameField, userName);
        logger.debug("Username entered successfully");
        actionDriver.enterText(passwordField, password);
        logger.debug("Password entered successfully");
        actionDriver.click(loginButton);
        logger.info("Login button clicked successfully");
    }

    public boolean isElementDisplayed() {
        logger.debug("Checking invalid credentials message");
        boolean displayed = actionDriver.isElementDisplayed(errorMessageText);
        if (displayed) {
            logger.info("Invalid credentials message is displayed");
        } else {
            logger.warn("Invalid credentials message is not displayed");
        }
        return displayed;
    }


}

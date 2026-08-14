package com.orangehrm.tests;

import com.orangehrm.base.BaseClass;
import com.orangehrm.pages.LoginPage;
import com.orangehrm.utilities.LoggerManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LoginPageTest extends BaseClass{

    private LoginPage loginPage;
    private static final Logger logger = LoggerManager.getLogger(LoginPageTest.class);

    @BeforeMethod
    public void setupPages() {
        logger.debug("Initializing LoginPage");
        loginPage = new LoginPage(getActionDriver());
        logger.debug("LoginPage initialized successfully");    }

    @Test
    public void loginWithValidCredentials() {
        logger.info("Starting test: loginWithValidCredentials");
        loginPage.login("Admin", "admin123");
        logger.info("Login action completed successfully");
        // Add actual verification here
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/dashboard"), "Login failed. Dashboard was not opened.");
        logger.info("Login verification successful");
    }

    @Test
    public void loginWithInvalidCredentials() {
        logger.info("Starting invalid login test");
        loginPage.login("Admin", "wrongPassword");
        Assert.assertTrue(loginPage.isElementDisplayed(), "Invalid credentials message was not displayed.");
        logger.info("Invalid credentials validation successful");
    }

}

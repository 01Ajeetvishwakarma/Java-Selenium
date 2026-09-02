package com.orangehrm.tests;

import com.orangehrm.base.BaseClass;
import com.orangehrm.pages.HomePage;
import com.orangehrm.pages.LoginPage;
import com.orangehrm.utilities.LoggerManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class HomePageTest extends BaseClass {

    private static final Logger logger = LoggerManager.getLogger(HomePageTest.class);
    private LoginPage loginPage;
    private HomePage homePage;

    @BeforeMethod
    public void setupPages() {
        logger.debug("Initializing LoginPage and HomePage");
        loginPage = new LoginPage(getActionDriver());
        homePage = new HomePage(getActionDriver());
        logger.debug("LoginPage and HomePage initialized successfully");
    }

    @Test
    public void verifyHomePage() {
        logger.info("Starting test: verifyHomePage");
        logger.info("Logging in with valid credentials");
        loginPage.login("Admin", "admin123");
        logger.info("Login completed successfully");
        logger.info("Verifying OrangeHRM logo on Home Page");
        boolean isLogoDisplayed = homePage.isHomePageLogoDisplayed();
        Assert.assertTrue(isLogoDisplayed, "OrangeHRM logo is not displayed on the home page");
        logger.info("Home Page verification completed successfully");
    }

}

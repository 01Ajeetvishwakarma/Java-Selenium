package com.orangehrm.tests;

import com.orangehrm.base.BaseClass;
import com.orangehrm.pages.LoginPage;
import com.orangehrm.utilities.ExcelReader;
import com.orangehrm.utilities.LoggerManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LoginPageTest extends BaseClass {

    private static final Logger logger = LoggerManager.getLogger(LoginPageTest.class);
    private LoginPage loginPage;
    private static final String TEST_DATA_PATH = "src/test/resources/testdata/TestData.xlsx";

    @BeforeMethod
    public void setupPages() {
        logger.debug("Initializing LoginPage");
        loginPage = new LoginPage(getActionDriver());
        logger.debug("LoginPage initialized successfully");
    }

    @DataProvider(name = "validLoginData")
    public Object[][] validLoginData() {
        return ExcelReader.getTestData(TEST_DATA_PATH, "validLogin");
    }
    @DataProvider(name = "invalidLoginData")
    public Object[][] invalidLoginData() {
        return ExcelReader.getTestData(TEST_DATA_PATH, "invalidLogin");
    }

    @Test(dataProvider = "validLoginData")
    public void loginWithValidCredentials(String username, String password) {
        logger.info("Starting test: loginWithValidCredentials");
        loginPage.login(username, password);
        logger.info("Login action completed successfully");
        // Add actual verification here
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/dashboard"), "Login failed. Dashboard was not opened.");
        logger.info("Login verification successful");
    }

    @Test(dataProvider = "invalidLoginData")
    public void loginWithInvalidCredentials(String username, String password) {
        logger.info("Starting invalid login test");
        loginPage.login(username, password);
        Assert.assertTrue(loginPage.isInvalidCredentialsMessageDisplayed(), "Invalid credentials message was not displayed.");
        logger.info("Invalid credentials validation successful");
    }

}

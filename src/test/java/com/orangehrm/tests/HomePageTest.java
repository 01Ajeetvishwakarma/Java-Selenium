package com.orangehrm.tests;

import com.orangehrm.base.BaseClass;
import com.orangehrm.pages.HomePage;
import com.orangehrm.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class HomePageTest extends BaseClass {

    private LoginPage loginPage;
    private HomePage homePage;

    @BeforeMethod
    public void setupPages() {

        loginPage = new LoginPage(getActionDriver());
        homePage = new HomePage(getActionDriver());
    }

    @Test
    public void verifyHomePage() {

        loginPage.login("Admin", "admin123");

        Assert.assertTrue(homePage.isHomePageLogoDisplayed(), "OrangeHRM logo is not displayed on the home page");
    }

}

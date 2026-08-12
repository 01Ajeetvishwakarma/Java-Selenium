package com.orangehrm.tests;

import com.orangehrm.base.BaseClass;
import com.orangehrm.pages.LoginPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LoginPageTest extends BaseClass{

    private LoginPage loginPage;

    @BeforeMethod
    public void setupPages() {
        loginPage = new LoginPage(getActionDriver());
    }

    @Test
    public void loginWithValidCredentials() {
        loginPage.login("Admin", "admin123");
    }

}

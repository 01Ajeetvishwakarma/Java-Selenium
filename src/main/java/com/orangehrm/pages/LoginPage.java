package com.orangehrm.pages;

import com.orangehrm.actiondriver.ActionDriver;
import com.orangehrm.base.BaseClass;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage {

    private final ActionDriver actionDriver;

    public LoginPage(ActionDriver actionDriver) {
        this.actionDriver = actionDriver;
    }

    private final By userNameField = By.name("username");
    private final By passwordField =
            By.cssSelector("input[type='password']");
    private final By loginButton =
            By.xpath("//*[text()=' Login ']");
    private final By errorMessageText =
            By.xpath("//*[text()='Invalid credentials']");

    public void login(String userName, String password) {

        actionDriver.enterText(userNameField, userName);
        actionDriver.enterText(passwordField, password);
        actionDriver.click(loginButton);
    }

    public boolean isInvalidCredentialsDisplayed() {
        return actionDriver.isElementDisplayed(errorMessageText);
    }

}

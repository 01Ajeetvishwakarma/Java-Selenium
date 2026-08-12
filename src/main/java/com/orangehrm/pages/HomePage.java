package com.orangehrm.pages;

import com.orangehrm.actiondriver.ActionDriver;
import com.orangehrm.base.BaseClass;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HomePage {

    private final ActionDriver actionDriver;

    private final By orangeHRMLogo =
            By.xpath("//*[@id='app']/div[1]/div[1]/aside/nav/div[1]/a/div[2]");

    public HomePage(ActionDriver actionDriver) {
        this.actionDriver = actionDriver;
    }

    public boolean isHomePageLogoDisplayed() {
        return actionDriver.isElementDisplayed(orangeHRMLogo);
    }


}

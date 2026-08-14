package com.orangehrm.pages;

import com.orangehrm.actiondriver.ActionDriver;
import com.orangehrm.utilities.LoggerManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;

public class HomePage {

    private final ActionDriver actionDriver;
    private static final Logger logger = LoggerManager.getLogger(HomePage.class);

    private final By orangeHRMLogo = By.xpath("//*[@id='app']/div[1]/div[1]/aside/nav/div[1]/a/div[2]");

    public HomePage(ActionDriver actionDriver) {
        if (actionDriver == null) {
            logger.error("ActionDriver cannot be null while initializing HomePage");
            throw new IllegalArgumentException("ActionDriver cannot be null");
        }
        this.actionDriver = actionDriver;
        logger.debug("HomePage initialized successfully");

    }

    public boolean isHomePageLogoDisplayed() {
        logger.info("Verifying OrangeHRM logo on Home Page");
        boolean isDisplayed = actionDriver.isElementDisplayed(orangeHRMLogo);
        if (isDisplayed) {
            logger.info("OrangeHRM logo is displayed successfully");
        } else {
            logger.warn("OrangeHRM logo is not displayed");
        }
        return isDisplayed;
    }



}

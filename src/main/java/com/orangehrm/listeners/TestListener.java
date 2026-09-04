package com.orangehrm.listeners;

import com.orangehrm.base.BaseClass;
import com.orangehrm.utilities.ExtentManager;
import com.orangehrm.utilities.LoggerManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    private static final Logger logger = LoggerManager.getLogger(TestListener.class);

    @Override
    public void onStart(ITestContext context) {
        logger.info("========== Test Suite Started: {} ==========", context.getName());
        ExtentManager.getReport();
    }

    @Override
    public void onTestStart(ITestResult result) {

        String testName = result.getMethod().getMethodName();

        logger.info("========== Test Started: {} ==========", testName);

        ExtentManager.startTest(testName);
        ExtentManager.logStep("Test started: " + testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        logger.info("Test PASSED: {}", testName);
        WebDriver driver = BaseClass.getDriver();
        if (driver != null) {
            ExtentManager.logPassWithScreenshot(driver, "Test passed: " + testName);
        } else {
            ExtentManager.logPass("Test passed: " + testName);
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {

        String testName = result.getMethod().getMethodName();

        Throwable throwable = result.getThrowable();

        String failureMessage = throwable != null
                ? throwable.getMessage()
                : "Unknown failure";

        logger.error(
                "Test FAILED: {} | {}",
                testName,
                failureMessage,
                throwable
        );

        ExtentManager.logStep(
                "Failure reason: " + failureMessage
        );

        WebDriver driver = BaseClass.getDriver();

        if (driver != null) {
            ExtentManager.logFailWithScreenshot(
                    driver,
                    "Test failed: " + testName + " | " + failureMessage
            );
        } else {
            ExtentManager.logFail(
                    "Test failed: " + testName + " | " + failureMessage
            );
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {

        String testName = result.getMethod().getMethodName();

        Throwable throwable = result.getThrowable();

        String skipMessage = throwable != null
                ? throwable.getMessage()
                : "Test skipped";

        logger.warn(
                "Test SKIPPED: {} | {}",
                testName,
                skipMessage
        );

        ExtentManager.logStep(
                "Skip reason: " + skipMessage
        );

        WebDriver driver = BaseClass.getDriver();

        if (driver != null) {
            ExtentManager.logSkipWithScreenshot(
                    driver,
                    "Test skipped: " + testName
            );
        } else {
            ExtentManager.logSkip(
                    "Test skipped: " + testName
            );
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        logger.info("========== Test Suite Finished: {} ==========", context.getName());
        ExtentManager.flushReport();
    }
}


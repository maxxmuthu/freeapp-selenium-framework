package com.freeapp.utility;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.freeapp.base.BaseClass;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/* Wires TestNG's own lifecycle into ExtentReports and Log: opens one report
   node per test method, records pass/fail/skip to both the HTML report and
   the log4j2 log, attaches a screenshot on failure, and flushes the HTML
   report once the whole suite finishes. No test class or page object calls
   this directly - TestNG invokes it automatically once it's registered in
   testng.xml. */
public class TestNGListener implements ITestListener
{
    @Override
    public void onTestStart(ITestResult result)
    {
        Log.info("Starting test: " + result.getMethod().getMethodName());
        ExtentManager.setTest(result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result)
    {
        Log.info("Test passed: " + result.getMethod().getMethodName());
        ExtentManager.getTest().log(Status.PASS, "Test passed");
    }

    @Override
    public void onTestFailure(ITestResult result)
    {
        Log.error("Test failed: " + result.getMethod().getMethodName(), result.getThrowable());

        ExtentTest test = ExtentManager.getTest();
        test.log(Status.FAIL, result.getThrowable());

        WebDriver driver = BaseClass.getDriver();
        if (driver != null)
        {
            String screenshotPath = Utils.captureScreenshot(driver, result.getMethod().getMethodName());
            if (screenshotPath != null)
            {
                test.fail("Screenshot on failure", MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result)
    {
        Log.warn("Test skipped: " + result.getMethod().getMethodName());
        ExtentManager.getTest().log(Status.SKIP, "Test skipped");
    }

    @Override
    public void onFinish(ITestContext context)
    {
        ExtentManager.flushReports();
    }
}

/*
the purpose of TestNGListener:

It's the bridge between TestNG's test lifecycle and ExtentReports. Without it, TestNG would run your tests and know pass/fail internally, but nothing would ever tell ExtentManager about it — the HTML report would stay empty.
TestNGListener plugs into TestNG's own event hooks so that every test, in every class, automatically gets logged — none of your test classes (LoginTest, CheckoutE2ETest, etc.) have to call ExtentManager themselves.

How it connects, mechanically:

1. It implements ITestListener — a TestNG interface with callback methods (onTestStart, onTestSuccess, onTestFailure, onTestSkipped, onFinish) that TestNG calls automatically at the right moments, the same way @BeforeMethod/@AfterMethod get called automatically on a test class.
2. It's registered once, in testng.xml:5 — <listener class-name="com.freeapp.utility.TestNGListener"/>. That's the only place anything "wires it in." No test imports or instantiates it directly.
3. Because it's global (registered at the suite level, not per test class), it fires for every test method in the whole run, so you get consistent reporting without repeating logging code in each test.

What each hook actually does, mapped to ExtentManager (which we just went over):
- onTestStart → setTest(methodName) — opens a new report row for this test.
- onTestSuccess → getTest().log(Status.PASS, ...) — marks that row passed.
- onTestFailure → getTest().log(Status.FAIL, ...) — grabs the driver via BaseClass.getDriver() and marks that row failed and attaches a screenshot to that row.
- onTestSkipped → getTest().log(Status.SKIP, ...).
- onFinish → flushReports() — writes the HTML file to disk once, after the entire suite (all test classes) finishes.

In short: TestNGListener is the "observer" that watches every test run and translates TestNG's own pass/fail/skip signals into ExtentReports calls, decoupling your test classes from reporting logic entirely.
 */
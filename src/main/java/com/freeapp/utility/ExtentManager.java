package com.freeapp.utility;

import com.aventstack.extentreports.AnalysisStrategy;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

/* Singleton ExtentReports wrapper with a per-thread "current test" via ThreadLocal. */
public class ExtentManager
{
    /* The one ExtentReports instance for the whole JVM run. */
    private static ExtentReports extent;

    /* Per-thread "current test" slot, so parallel threads never share one ExtentTest. */
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    /* Blocks external instantiation -- enforces the Singleton pattern. */
    private ExtentManager()
    {
    }

    /* Lazily creates extent once, then reuses it; synchronized so two
       parallel threads can't both see extent == null and each build a
       separate ExtentReports/reporter. */
    public static synchronized ExtentReports getInstance()
    {
        if (extent == null)
        {
            extent = createInstance();
        }
        return extent;
    }

    /* Builds and configures the ExtentReports HTML reporter. */
    private static ExtentReports createInstance()
    {
        String filePath = System.getProperty("user.dir") + "/test-output/extent-report.html";
        ExtentSparkReporter htmlReporter = new ExtentSparkReporter(filePath);
        htmlReporter.config().setDocumentTitle("FreeApp Selenium Automation Report");
        htmlReporter.config().setReportName("SauceDemo Regression Suite");
        htmlReporter.config().setTheme(Theme.STANDARD);

        ExtentReports reports = new ExtentReports();
        reports.attachReporter(htmlReporter);
        reports.setAnalysisStrategy(AnalysisStrategy.SUITE);

        reports.setSystemInfo("OS", System.getProperty("os.name"));
        reports.setSystemInfo("Java Version", System.getProperty("java.version"));
        reports.setSystemInfo("Executed on Browser", "Chrome");

        return reports;
    }

    /* Creates this test's ExtentTest node and stores it on the calling thread; synchronized to serialize access to the shared reporter. */
    public static synchronized ExtentTest setTest(String testName)
    {
        ExtentTest test = getInstance().createTest(testName);
        extentTest.set(test);
        return test;
    }

    /* Returns the calling thread's ExtentTest; throws instead of returning null if setTest() was never called. */
    public static synchronized ExtentTest getTest()
    {
        ExtentTest test = extentTest.get();
        if (test == null)
        {
            throw new IllegalStateException("ExtentTest not initialized. Call setTest() first.");
        }
        return test;
    }

    /* Clears the calling thread's ExtentTest slot (not yet called anywhere). */
    public static void clearTest()
    {
        extentTest.remove();
    }

    /* Writes the HTML report to disk -- call once, at suite end, not after every test. */
    public static void flushReports()
    {
        if (extent != null)
        {
            extent.flush();
        }
    }
}
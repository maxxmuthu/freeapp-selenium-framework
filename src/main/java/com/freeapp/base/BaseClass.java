package com.freeapp.base;

/* import io.github.bonigarcia.wdm.WebDriverManager; -- pre-Selenium 4.6 way to resolve
   chromedriver; superseded by Selenium Manager, built into selenium-java, which
   resolves it automatically. Left commented for recognition, not used. */
import com.freeapp.integration.Config;
import com.freeapp.utility.Log;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.HashMap;
import java.util.Map;

/* Parent of every test class and every page object. Test classes call launchBrowser()/closeBrowser() from their own @BeforeMethod/@AfterMethod;
   page objects extend this to reach the running browser instance via getDriver() for PageFactory.initElements(), without it being passed into
   their constructor. driver is a static ThreadLocal, not a plain static field, so that under TestNG's parallel="methods" execution
   each thread gets its own WebDriver instead of every thread overwriting one shared instance; static still lets a page object with a no-arg
   constructor reach it without the driver being passed in. */

public class BaseClass
{
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    /* browser and baseUrl come from Configuration/config.properties via
       Config, so switching either doesn't require touching this class. */

    public void launchBrowser()
    {
        /* WebDriverManager.chromedriver().setup(); */
        String browser = Config.get("browser");
        WebDriver driver;
        if (browser.equalsIgnoreCase("chrome"))
        {
            driver = new ChromeDriver(chromeOptions());
        }
        else if (browser.equalsIgnoreCase("firefox"))
        {
            driver = new FirefoxDriver();
        }
        else
        {
            throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
        driverThreadLocal.set(driver);

        try
        {
            driver.manage().window().maximize();
            driver.get(Config.get("baseUrl"));
            Log.info("Launched " + browser + " browser");
        }
        catch (RuntimeException e)
        {
            /* driver.get() can fail (e.g. a transient network/SSL error)
               after the browser process is already running - if that
               happens, TestNG never calls this test's @AfterMethod
               (closeBrowser()), since @AfterMethod only runs when
               @BeforeMethod itself succeeds. Without this catch, that
               leaves an orphaned Chrome window behind on every such
               failure. Quit it here, before rethrowing, so a setup
               failure never leaks a browser - the test still correctly
               fails, it just doesn't also leave a window open. */
            driver.quit();
            driverThreadLocal.remove();
            throw e;
        }
    }

    /* Suppresses Chrome's native password-manager prompts (breach warning, save-password, leak detection) and notification popups.
       These are real browser UI, not page elements, and sit on top of the page,
       blocking Selenium's clicks/typing on whatever is underneath until dismissed. */

    private ChromeOptions chromeOptions()
    {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications");
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        options.setExperimentalOption("prefs", prefs);
        return options;
    }

    public void closeBrowser()
    {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null)
        {
            Log.info("Closing browser");
            driver.quit();
            driverThreadLocal.remove();
        }
    }

    /* Plumbing accessor: driverThreadLocal is private, so Utils (a different
       package) and page objects' PageFactory.initElements() calls both need
       this to reach the current thread's WebDriver. */
    public static WebDriver getDriver()
    {
        return driverThreadLocal.get();
    }
}

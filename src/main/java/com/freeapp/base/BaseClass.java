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
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
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

    /* browser, baseUrl, executionMode and gridUrl come from
       Configuration/config.properties via Config, so switching any of them
       doesn't require touching this class. */

    public void launchBrowser()
    {
        /* WebDriverManager.chromedriver().setup(); */
        String browser = Config.get("browser");
        String executionMode = Config.get("executionMode");
        WebDriver driver;
        if (executionMode.equalsIgnoreCase("remote"))
        {
            driver = remoteDriver(browser);
        }
        else if (browser.equalsIgnoreCase("chrome"))
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
       blocking Selenium's clicks/typing on whatever is underneath until dismissed.

       --window-size, not driver.manage().window().maximize(): maximize()
       needs a CDP round-trip to actually resize the OS-level window, which
       requires a real window manager - under a CI runner's virtual display
       (xvfb, no window manager) that round-trip can fail outright
       ("unknown command: 'Runtime.evaluate' wasn't found"). A fixed launch
       size sets the window at startup with no CDP call involved, so it
       works the same way locally and in CI. */

    /* Points Selenium at a Grid hub (either docker-compose.yml locally, or
       CI's own services: block) instead of launching a local browser
       process - same ChromeOptions as the local path, since the Grid node
       runs a real (non-headless) Chrome behind its own display and hits
       the same password-manager popup. */

    private WebDriver remoteDriver(String browser)
    {
        /* URI.create(...).toURL(), not new URL(String) directly - the
           single-String URL constructor is deprecated (JDK 20+) in favor
           of parsing as a URI first. */
        URL gridUrl;
        try
        {
            gridUrl = URI.create(Config.get("gridUrl")).toURL();
        }
        catch (IllegalArgumentException | MalformedURLException e)
        {
            throw new RuntimeException("Invalid gridUrl in config.properties", e);
        }

        if (browser.equalsIgnoreCase("chrome"))
        {
            return new RemoteWebDriver(gridUrl, chromeOptions());
        }
        else if (browser.equalsIgnoreCase("firefox"))
        {
            return new RemoteWebDriver(gridUrl, new FirefoxOptions());
        }
        else
        {
            throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
    }

    private ChromeOptions chromeOptions()
    {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1920,1080");
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

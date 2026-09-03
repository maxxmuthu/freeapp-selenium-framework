package com.freeapp.utility;

import com.freeapp.base.BaseClass;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/* Generic Selenium action methods, parameterized by the WebElement to act
   on, so one method serves every field on every page object. */
public class Utils
{
    private static WebDriverWait webDriverWait()
    {
        return new WebDriverWait(BaseClass.getDriver(), Duration.ofSeconds(10));
    }

    public static void type(WebElement element, String value)
    {
        webDriverWait().until(ExpectedConditions.visibilityOf(element));
        element.sendKeys(value);
    }

    public static void click(WebElement element)
    {
        webDriverWait().until(ExpectedConditions.elementToBeClickable(element));
        element.click();
    }

    public static String getText(WebElement element)
    {
        webDriverWait().until(ExpectedConditions.visibilityOf(element));
        return element.getText();
    }

    public static boolean isDisplayed(WebElement element)
    {
        webDriverWait().until(ExpectedConditions.visibilityOf(element));
        return element.isDisplayed();
    }

    public static List<String> getTexts(List<WebElement> elements)
    {
        webDriverWait().until(ExpectedConditions.visibilityOfAllElements(elements));
        List<String> texts = new ArrayList<>();
        for (WebElement element : elements)
        {
            texts.add(element.getText());
        }
        return texts;
    }

    /* Saves a full-page screenshot under test-output/screenshots and
       returns its path, or null if the capture itself failed (a failing
       screenshot must never mask the real test failure it was taken for). */
    public static String captureScreenshot(WebDriver driver, String testName)
    {
        try
        {
            File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path destination = Paths.get(System.getProperty("user.dir"), "test-output", "screenshots",
                    testName + "_" + System.currentTimeMillis() + ".png");
            Files.createDirectories(destination.getParent());
            Files.copy(source.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            return destination.toString();
        }
        catch (IOException e)
        {
            return null;
        }
    }
}

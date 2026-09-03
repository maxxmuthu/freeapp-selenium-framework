package com.test.freeapp.module;

import com.freeapp.base.BaseClass;
import com.freeapp.pageobjects.LoginPage;
import com.freeapp.utility.ExcelReader;
import com.freeapp.utility.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.Map;

/* Module-level test: validates LoginPage's own field-level behavior without
   crossing into another page. */
public class LoginTest extends BaseClass
{
    @BeforeMethod
    public void setUp()
    {
        launchBrowser();
    }

    @AfterMethod
    public void tearDown()
    {
        closeBrowser();
    }

    /* Hard assert: stops at the first failed check, so only one condition
       (the error text) is verified per run. */

    @Test
    public void invalidLoginShowsErrorMessage()
    {
        LoginPage loginPage = new LoginPage();
        Utils.type(loginPage.usernameField, "standard_user");
        Utils.type(loginPage.passwordField, "wrong_password");
        Utils.click(loginPage.loginButton);

        String actualError = Utils.getText(loginPage.errorMessage);
        Assert.assertTrue(actualError.contains("Username and password do not match"));
    }

    /* Soft assert: every check below runs regardless of earlier failures,
       and assertAll() reports them together at the end. This is the
       pattern for verifying several independent conditions off one page
       state without one failure hiding the others. */

    @Test
    public void invalidLoginShowsAllErrorIndicators()
    {
        LoginPage loginPage = new LoginPage();
        loginPage.login("standard_user", "wrong_password");

        SoftAssert softAssert = new SoftAssert();
        softAssert.assertTrue(Utils.isDisplayed(loginPage.errorMessage),
                "Error message should be visible after an invalid login");
        softAssert.assertTrue(Utils.getText(loginPage.errorMessage).contains("Username and password do not match"),
                "Error message text should explain the mismatch");
        softAssert.assertFalse(getDriver().getCurrentUrl().contains("inventory.html"),
                "An invalid login should not navigate to the inventory page");
        softAssert.assertAll();
    }

    private static final String TEST_DATA_PATH = System.getProperty("user.dir") + "/TestData/TestData.xlsx";

    @Test
    public void validLogin()
    {
        Map<String, String> scenario = ExcelReader.getScenarioData(TEST_DATA_PATH, "LoginData", "TC01");

        LoginPage loginPage = new LoginPage();
        loginPage.login(scenario.get("username"), scenario.get("password"));

        Assert.assertTrue(getDriver().getCurrentUrl().contains("inventory.html"));
    }

    @Test
    public void lockedOutUserLogin()
    {
        Map<String, String> scenario = ExcelReader.getScenarioData(TEST_DATA_PATH, "LoginData", "TC02");

        LoginPage loginPage = new LoginPage();
        loginPage.login(scenario.get("username"), scenario.get("password"));

        Assert.assertEquals(Utils.getText(loginPage.errorMessage), scenario.get("expectedOutcome"));
    }

    @Test
    public void wrongPasswordLogin()
    {
        Map<String, String> scenario = ExcelReader.getScenarioData(TEST_DATA_PATH, "LoginData", "TC03");

        LoginPage loginPage = new LoginPage();
        loginPage.login(scenario.get("username"), scenario.get("password"));

        Assert.assertEquals(Utils.getText(loginPage.errorMessage), scenario.get("expectedOutcome"));
    }

    @Test
    public void missingUsernameLogin()
    {
        Map<String, String> scenario = ExcelReader.getScenarioData(TEST_DATA_PATH, "LoginData", "TC04");

        LoginPage loginPage = new LoginPage();
        loginPage.login(scenario.get("username"), scenario.get("password"));

        Assert.assertEquals(Utils.getText(loginPage.errorMessage), scenario.get("expectedOutcome"));
    }

    @Test
    public void missingPasswordLogin()
    {
        Map<String, String> scenario = ExcelReader.getScenarioData(TEST_DATA_PATH, "LoginData", "TC05");

        LoginPage loginPage = new LoginPage();
        loginPage.login(scenario.get("username"), scenario.get("password"));

        Assert.assertEquals(Utils.getText(loginPage.errorMessage), scenario.get("expectedOutcome"));
    }

    /* Unlike the scenarios above, every row here is the same "login fails"
       case, just a different bad username/password. */
    @DataProvider(name = "invalidPasswordsData") /* name is just a label; redundant here since it matches the method name */
    public Object[][] invalidPasswordsData()
    {
        return ExcelReader.getData(TEST_DATA_PATH, "InvalidPasswords");
    }

    /* TestNG calls invalidPasswordsData() once, then runs this method once
       per returned row, passing that row's columns in as parameters. */
    @Test(dataProvider = "invalidPasswordsData")
    public void invalidPasswordAttempts(String username, String password)
    {
        LoginPage loginPage = new LoginPage();
        loginPage.login(username, password);

        Assert.assertTrue(Utils.getText(loginPage.errorMessage).contains("Username and password do not match"));
    }
}

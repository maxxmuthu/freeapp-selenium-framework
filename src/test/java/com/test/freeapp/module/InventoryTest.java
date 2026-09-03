package com.test.freeapp.module;

import com.freeapp.base.BaseClass;
import com.freeapp.integration.ApiClient;
import com.freeapp.integration.DBQueryExecutor;
import com.freeapp.integration.LocalApiServer;
import com.freeapp.pageobjects.InventoryPage;
import com.freeapp.pageobjects.LoginPage;
import com.freeapp.utility.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.IOException;
import java.util.List;

/* Module-level test: validates InventoryPage's own displayed catalog against
   a local SQLite ground truth (DBQueryExecutor), not another page's
   behavior. login() is only the unavoidable precondition to reach the page
   under test, the same way LoginTest's own scenarios call login() before
   asserting on where it lands. */
public class InventoryTest extends BaseClass
{
    @BeforeClass
    public void startApi() throws IOException
    {
        LocalApiServer.start();
    }

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

    @Test
    public void displayedPricesMatchDatabase()
    {
        LoginPage loginPage = new LoginPage();
        loginPage.login("standard_user", "secret_sauce");

        InventoryPage inventoryPage = new InventoryPage();
        List<String> names = Utils.getTexts(inventoryPage.productNames);
        List<String> prices = Utils.getTexts(inventoryPage.productPrices);

        SoftAssert softAssert = new SoftAssert();
        for (int i = 0; i < names.size(); i++)
        {
            String expectedPrice = DBQueryExecutor.getPrice(names.get(i));

            softAssert.assertEquals(prices.get(i), expectedPrice,
                    "Price mismatch for " + names.get(i));
        }
        softAssert.assertAll();
    }

    /* Simple version of the test above: one product, one field, one hard
       assert - no loop, no SoftAssert, just UI vs DB for a single value. */
    @Test
    public void backpackPriceMatchesDatabase()
    {
        LoginPage loginPage = new LoginPage();
        loginPage.login("standard_user", "secret_sauce");

        InventoryPage inventoryPage = new InventoryPage();
        List<String> names = Utils.getTexts(inventoryPage.productNames);
        List<String> prices = Utils.getTexts(inventoryPage.productPrices);

        int backpackIndex = names.indexOf("Sauce Labs Backpack");
        String actualPrice = prices.get(backpackIndex);
        String expectedPrice = DBQueryExecutor.getPrice("Sauce Labs Backpack");

        Assert.assertEquals(actualPrice, expectedPrice);
    }

    /* Same idea as backpackPriceMatchesDatabase() above, but the ground
       truth comes from the local API instead of the local DB - UI vs API
       this time, not UI vs DB. */
    @Test
    public void backpackPriceMatchesApi()
    {
        LoginPage loginPage = new LoginPage();
        loginPage.login("standard_user", "secret_sauce");

        InventoryPage inventoryPage = new InventoryPage();
        List<String> names = Utils.getTexts(inventoryPage.productNames);
        List<String> prices = Utils.getTexts(inventoryPage.productPrices);

        int backpackIndex = names.indexOf("Sauce Labs Backpack");
        String actualPrice = prices.get(backpackIndex);
        String expectedPrice = ApiClient.getPrice("Sauce Labs Backpack");

        Assert.assertEquals(actualPrice, expectedPrice);
    }
}

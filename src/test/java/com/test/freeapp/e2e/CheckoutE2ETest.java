package com.test.freeapp.e2e;

import com.freeapp.base.BaseClass;
import com.freeapp.integration.ApiClient;
import com.freeapp.integration.DBQueryExecutor;
import com.freeapp.integration.LocalApiServer;
import com.freeapp.pageobjects.PageObjectManager;
import com.freeapp.utility.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

/* E2E-level test: drives the whole journey — login -> add to cart -> cart
   -> checkout info -> checkout overview -> finish — to one completed
   outcome, the order-confirmation page. Calls each page's one flow method
   rather than its individual fields, so the flow reads as one call per page. */
public class CheckoutE2ETest extends BaseClass
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

    /* pom is a local variable, not an instance field, so this test stays
       safe under TestNG's parallel="methods" execution: an instance field
       set in @BeforeMethod would be shared and overwritten across threads,
       since TestNG reuses one class instance across all of a class's test
       methods even when they run in parallel. */

    @Test
    public void checkoutCompletesFullOrder()
    {
        PageObjectManager pom = new PageObjectManager();
        pom.getLoginPage().login("standard_user", "secret_sauce");
        pom.getInventoryPage().addBackpackToCartAndViewCart();

        /* Cart page's price element only exists while the cart is on
           screen, so the value has to be captured here - but the actual
           assertion is deferred to the end, alongside the other checks. */
        String cartPrice = Utils.getText(pom.getCartPage().cartItemPrice);

        pom.getCartPage().proceedToCheckout();
        pom.getCheckoutInfoPage().submitCustomerInfo("John", "Doe", "12345");
        pom.getCheckoutOverviewPage().finishOrder();

        String actualCompletionMessage = Utils.getText(pom.getCheckoutCompletePage().completeHeader);
        Assert.assertEquals(actualCompletionMessage, "Thank you for your order!");

        /* DB and API validation last, grouped together: the same price
           captured from the cart mid-flow is checked against both ground
           truths - DBQueryExecutor and ApiClient - one field, one hard
           assert each. */
        String expectedPriceFromDb = DBQueryExecutor.getPrice("Sauce Labs Backpack");
        Assert.assertEquals(cartPrice, expectedPriceFromDb);

        String expectedPriceFromApi = ApiClient.getPrice("Sauce Labs Backpack");
        Assert.assertEquals(cartPrice, expectedPriceFromApi);
    }
}

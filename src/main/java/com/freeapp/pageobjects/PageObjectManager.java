package com.freeapp.pageobjects;

/* Lazy factory for page objects: each getXPage() constructs its page object
   on first call and returns the same cached instance on later calls, so a
   test only ever constructs the page objects it actually visits. */
public class PageObjectManager
{
    private LoginPage loginPage;
    private InventoryPage inventoryPage;
    private CartPage cartPage;
    private CheckoutInfoPage checkoutInfoPage;
    private CheckoutOverviewPage checkoutOverviewPage;
    private CheckoutCompletePage checkoutCompletePage;

    public LoginPage getLoginPage()
    {
        if (loginPage == null)
        {
            loginPage = new LoginPage();
        }
        return loginPage;
    }

    public InventoryPage getInventoryPage()
    {
        if (inventoryPage == null)
        {
            inventoryPage = new InventoryPage();
        }
        return inventoryPage;
    }

    public CartPage getCartPage()
    {
        if (cartPage == null)
        {
            cartPage = new CartPage();
        }
        return cartPage;
    }

    public CheckoutInfoPage getCheckoutInfoPage()
    {
        if (checkoutInfoPage == null)
        {
            checkoutInfoPage = new CheckoutInfoPage();
        }
        return checkoutInfoPage;
    }

    public CheckoutOverviewPage getCheckoutOverviewPage()
    {
        if (checkoutOverviewPage == null)
        {
            checkoutOverviewPage = new CheckoutOverviewPage();
        }
        return checkoutOverviewPage;
    }

    public CheckoutCompletePage getCheckoutCompletePage()
    {
        if (checkoutCompletePage == null)
        {
            checkoutCompletePage = new CheckoutCompletePage();
        }
        return checkoutCompletePage;
    }
}

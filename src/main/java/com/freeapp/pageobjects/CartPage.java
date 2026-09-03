package com.freeapp.pageobjects;

import com.freeapp.base.BaseClass;
import com.freeapp.utility.Utils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class CartPage extends BaseClass
{
    @FindBy(className = "cart_item")
    public WebElement cartItem;

    /* Cart page reuses the same "inventory_item_price" class the
       inventory page uses - only one item is ever added in this project's
       flow, so a single WebElement (not a List) is enough. */
    @FindBy(className = "inventory_item_price")
    public WebElement cartItemPrice;

    @FindBy(id = "checkout")
    public WebElement checkoutButton;

    @FindBy(id = "continue-shopping")
    public WebElement continueShoppingButton;

    public CartPage()
    {
        PageFactory.initElements(getDriver(), this);
    }

    public void proceedToCheckout()
    {
        Utils.click(checkoutButton);
    }
}

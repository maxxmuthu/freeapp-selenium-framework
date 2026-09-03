package com.freeapp.pageobjects;

import com.freeapp.base.BaseClass;
import com.freeapp.utility.Utils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

public class InventoryPage extends BaseClass
{
    @FindBy(className = "title")
    public WebElement pageTitle;

    @FindBy(id = "add-to-cart-sauce-labs-backpack")
    public WebElement addBackpackToCartButton;

    @FindBy(className = "shopping_cart_link")
    public WebElement cartIcon;

    @FindBy(className = "inventory_item_name")
    public List<WebElement> productNames;

    @FindBy(className = "inventory_item_price")
    public List<WebElement> productPrices;

    public InventoryPage()
    {
        PageFactory.initElements(getDriver(), this);
    }

    public void addBackpackToCartAndViewCart()
    {
        Utils.click(addBackpackToCartButton);
        Utils.click(cartIcon);
    }
}

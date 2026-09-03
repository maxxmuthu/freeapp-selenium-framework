package com.freeapp.pageobjects;

import com.freeapp.base.BaseClass;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/* checkout-complete.html: the order-confirmation page. */
public class CheckoutCompletePage extends BaseClass
{
    @FindBy(className = "complete-header")
    public WebElement completeHeader;

    @FindBy(id = "back-to-products")
    public WebElement backToProductsButton;

    public CheckoutCompletePage()
    {
        PageFactory.initElements(getDriver(), this);
    }
}

package com.freeapp.pageobjects;

import com.freeapp.base.BaseClass;
import com.freeapp.utility.Utils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/* checkout-step-two.html: order summary before final submission. */
public class CheckoutOverviewPage extends BaseClass
{
    @FindBy(className = "summary_total_label")
    public WebElement totalLabel;

    @FindBy(id = "finish")
    public WebElement finishButton;

    @FindBy(id = "cancel")
    public WebElement cancelButton;

    public CheckoutOverviewPage()
    {
        PageFactory.initElements(getDriver(), this);
    }

    public void finishOrder()
    {
        Utils.click(finishButton);
    }
}

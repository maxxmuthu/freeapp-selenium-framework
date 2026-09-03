package com.freeapp.pageobjects;

import com.freeapp.base.BaseClass;
import com.freeapp.utility.Utils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/* checkout-step-one.html: the customer-info form (first/last name, zip). */
public class CheckoutInfoPage extends BaseClass
{
    @FindBy(id = "first-name")
    public WebElement firstNameField;

    @FindBy(id = "last-name")
    public WebElement lastNameField;

    @FindBy(id = "postal-code")
    public WebElement postalCodeField;

    @FindBy(id = "continue")
    public WebElement continueButton;

    @FindBy(id = "cancel")
    public WebElement cancelButton;

    @FindBy(css = "h3[data-test='error']")
    public WebElement errorMessage;

    public CheckoutInfoPage()
    {
        PageFactory.initElements(getDriver(), this);
    }

    public void submitCustomerInfo(String firstName, String lastName, String postalCode)
    {
        Utils.type(firstNameField, firstName);
        Utils.type(lastNameField, lastName);
        Utils.type(postalCodeField, postalCode);
        Utils.click(continueButton);
    }
}

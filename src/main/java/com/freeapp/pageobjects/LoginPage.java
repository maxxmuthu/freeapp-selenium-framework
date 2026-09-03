package com.freeapp.pageobjects;

import com.freeapp.base.BaseClass;
import com.freeapp.utility.Utils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/* Fields are public so module-level tests can act on them directly through
   Utils's generic type()/click()/getText(). login() bundles this page's
   whole submission for E2E-level tests, so an E2E flow reads as one call
   per page instead of every field's calls inlined. */
public class LoginPage extends BaseClass
{
    @FindBy(id = "user-name")
    public WebElement usernameField;

    @FindBy(id = "password")
    public WebElement passwordField;

    @FindBy(id = "login-button")
    public WebElement loginButton;

    @FindBy(css = "h3[data-test='error']")
    public WebElement errorMessage;

    public LoginPage()
    {
        PageFactory.initElements(getDriver(), this);
    }

    public void login(String username, String password)
    {
        Utils.type(usernameField, username);
        Utils.type(passwordField, password);
        Utils.click(loginButton);
    }
}

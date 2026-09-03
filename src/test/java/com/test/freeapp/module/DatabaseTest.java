package com.test.freeapp.module;

import com.freeapp.integration.DBQueryExecutor;
import org.testng.Assert;
import org.testng.annotations.Test;

/* Pure DB-layer validation - no browser, no BaseClass, no page object.
   Checks DBQueryExecutor against a hardcoded expected value instead of
   anything scraped from the live site. */
public class DatabaseTest
{
    @Test
    public void backpackPriceMatchesSeededValue()
    {
        String expectedPrice = "$29.99";
        String actualPrice = DBQueryExecutor.getPrice("Sauce Labs Backpack");

        Assert.assertEquals(actualPrice, expectedPrice);
    }
}

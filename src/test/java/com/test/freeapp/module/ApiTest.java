package com.test.freeapp.module;

import com.freeapp.integration.ApiClient;
import com.freeapp.integration.LocalApiServer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/* Pure API-layer validation - no browser, no BaseClass, no page object.
   Checks ApiClient against a hardcoded expected value instead of anything
   scraped from the live site, the exact same shape as DatabaseTest but for
   the local API instead of the local DB. */
public class ApiTest
{
    @BeforeClass
    public void startApi() throws IOException
    {
        LocalApiServer.start();
    }

    @Test
    public void backpackPriceMatchesSeededValue()
    {
        String expectedPrice = "$29.99";
        String actualPrice = ApiClient.getPrice("Sauce Labs Backpack");

        Assert.assertEquals(actualPrice, expectedPrice);
    }

    /* ApiClient always attaches a valid token, so this test bypasses it
       deliberately - a raw request with no Authorization header at all -
       to prove the server actually enforces the token requirement rather
       than just having ApiClient send one out of politeness. */
    @Test
    public void apiRejectsRequestWithNoToken() throws Exception
    {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + LocalApiServer.PORT + "/products/Sauce%20Labs%20Backpack"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(response.statusCode(), 401);
    }
}

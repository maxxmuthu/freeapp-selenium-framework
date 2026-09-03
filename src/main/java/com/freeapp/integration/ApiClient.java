package com.freeapp.integration;

import io.restassured.response.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

/* RestAssured is the real, industry-standard Java library for calling a
   REST API: given() sets up the request, when() fires the call against a
   specific endpoint - written as a plain path literal right there, e.g.
   .get("/products/{name}", ...) or .post("/auth/token"), so it's always
   obvious which endpoint a given method hits without chasing a constant
   declared somewhere else - then() checks the response, and extract()
   pulls a value back out of it.

   Every method below follows the exact same two-step shape, one call
   then one or more reads off it, on purpose - not a shortcut for a
   single field and a different shape for multiple fields:

       Response response = given()....extract().response();
       return response.path("someField");

   Same getPrice()/getProduct() split as DBQueryExecutor, and the same
   shape too: each method runs its own independent call, no shared
   private helper between them. */
public class ApiClient
{
    private static final String BASE_URL = "http://localhost:" + LocalApiServer.PORT;

    private static String cachedToken;
    private static long tokenExpiresAt;

    /* GET /products/{name} -> one field out of the JSON response, no Map,
       no juggling multiple keys.

       "{name}" is a placeholder, not literal text sent to the server -
       productName (the next argument after the URL) fills that slot, and
       RestAssured URL-encodes it while doing so. That encoding step is
       why this isn't just .get("/products/" + productName): productName
       is "Sauce Labs Backpack", with real spaces in it, and a raw space
       in a URL is invalid - .get("/products/" + productName) would send
       the literal, broken "GET /products/Sauce Labs Backpack". Filling
       "{name}" instead sends the correct, encoded
       "GET /products/Sauce%20Labs%20Backpack". */
    public static String getPrice(String productName)
    {
        Response response = given()
                .baseUri(BASE_URL)
                .header("Authorization", "Bearer " + getAccessToken())
        .when()
                .get("/products/{name}", productName)
        .then()
                .statusCode(200)
                .extract().response();

        return response.path("price");
    }

    /* Same GET /products/{name} as getPrice() above, but TWO fields are
       needed here ("name" AND "price") from the same API call. This is
       exactly why every method here captures the whole response into a
       variable first, then reads from it, rather than getPrice() doing
       one thing and this method doing another: response.path(...) can be
       called as many times as needed on the SAME already-fetched
       response, no second network call, whether that's once (getPrice())
       or twice (here). */
    public static Map<String, String> getProduct(String productName)
    {
        Response response = given()
                .baseUri(BASE_URL)
                .header("Authorization", "Bearer " + getAccessToken())
        .when()
                .get("/products/{name}", productName)
        .then()
                .statusCode(200)
                .extract().response();

        Map<String, String> result = new HashMap<>();
        result.put("name", response.path("name"));
        result.put("price", response.path("price"));
        return result;
    }

    /* Reference example only - no test in this project calls this method,
       kept purely to show how to read a JSON ARRAY, not a single object,
       the same way DBQueryExecutor.getProduct() is kept as a side-by-side
       comparison even though nothing calls it right now.

       getPrice()/getProduct() above each read ONE JSON object:
       {"name":"Sauce Labs Backpack","price":"$29.99"} -> .path("price")
       gives back ONE String.

       GET /products (no {name}) returns an ARRAY of objects instead:
       [ {"name":"Sauce Labs Backpack","price":"$29.99"},
         {"name":"Sauce Labs Bike Light","price":"$9.99"}, ... ]
       .path("name") on an array root reads that key out of EVERY object
       in the array and hands back a List - one call reads the whole
       column, the same idea as looping "for each object, read its name"
       done in a single line. */
    public static List<String> getAllProductNames()
    {
        Response response = given()
                .baseUri(BASE_URL)
                .header("Authorization", "Bearer " + getAccessToken())
        .when()
                .get("/products")
        .then()
                .statusCode(200)
                .extract().response();

        return response.path("name");
    }

    /* The four methods below are reference examples only - nothing in
       this project's real test suite calls them, same precedent as
       getAllProductNames() above. This project's real data (products)
       never nests this deep, so a separate endpoint (GET /orders,
       LocalApiServer) and fixture (Api/orders.json) were added purely to
       have a REAL nested structure to read - not a hypothetical one - so
       every path expression below is verified against an actual running
       server, not just described in a comment. The shape being read:

       {
         "orders": [
           {
             "id": 1,
             "items": [
               { "sku": "A", "qty": 2 },
               { "sku": "B", "qty": 1 }
             ]
           },
           {
             "id": 2,
             "items": [
               { "sku": "C", "qty": 5 }
             ]
           }
         ]
       }

       An array nested inside an object, where each element of that array
       is itself an object holding its own nested array of objects -
       "orders" is the outer array, each order's "items" is an inner one. */

    /* One field read across every object in the "orders" array -> [1, 2].
       Same trick as getAllProductNames() above, just nested one level
       deeper under "orders" first. */
    public static List<Integer> getAllOrderIds()
    {
        Response response = given()
                .baseUri(BASE_URL)
                .header("Authorization", "Bearer " + getAccessToken())
        .when()
                .get("/orders")
        .then()
                .statusCode(200)
                .extract().response();

        return response.path("orders.id");
    }

    /* "[0]" picks ONE order out of the array by index first (order id 1),
       then "sku" is read across every object in THAT order's own "items"
       array -> ["A", "B"]. */
    public static List<String> getItemSkusForFirstOrder()
    {
        Response response = given()
                .baseUri(BASE_URL)
                .header("Authorization", "Bearer " + getAccessToken())
        .when()
                .get("/orders")
        .then()
                .statusCode(200)
                .extract().response();

        return response.path("orders[0].items.sku");
    }

    /* Index into the array twice: order at index 1 (order id 2), then the
       first item within that order's "items" array -> "C". Each "[n]"
       narrows down to one element; keep chaining ".field" or "[n]" until
       landing on the single value wanted. */
    public static String getSkuOfFirstItemInSecondOrder()
    {
        Response response = given()
                .baseUri(BASE_URL)
                .header("Authorization", "Bearer " + getAccessToken())
        .when()
                .get("/orders")
        .then()
                .statusCode(200)
                .extract().response();

        return response.path("orders[1].items[0].sku");
    }

    /* Every sku from every item in every order, in one flat list ->
       ["A", "B", "C"]. Without .flatten(), "orders.items" is a LIST OF
       LISTS (one items-list per order) instead of one combined list,
       since each order has its own "items" array - .flatten() collapses
       that nesting before reading "sku" so the result is one flat list
       instead of a list of lists of skus. */
    public static List<String> getAllItemSkusFlattened()
    {
        Response response = given()
                .baseUri(BASE_URL)
                .header("Authorization", "Bearer " + getAccessToken())
        .when()
                .get("/orders")
        .then()
                .statusCode(200)
                .extract().response();

        return response.path("orders.items.flatten().sku");
    }

    /* Returns the cached token if it's still valid; otherwise exchanges
       the client ID/secret from Configuration/config.properties for a
       fresh one via POST /auth/token - the OAuth2 "client credentials"
       grant, the standard way one app authenticates to another app's API.
       Every product request reuses this same token as a header
       ("Authorization: Bearer <token>") rather than calling this endpoint
       itself - exactly how the Postman collection does it too: get a
       token once, then attach it to every later request.

       synchronized so two TestNG threads (under parallel="methods")
       can't both see an expired token at once and both spend a request
       fetching a new one - the second thread just reuses whatever the
       first one already got. */
    private static synchronized String getAccessToken()
    {
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiresAt)
        {
            return cachedToken;
        }

        Map<String, String> credentials = new HashMap<>();
        credentials.put("clientId", Config.get("api.clientId"));
        credentials.put("clientSecret", Config.get("api.clientSecret"));

        Response response = given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body(credentials)
        .when()
                .post("/auth/token")
        .then()
                .statusCode(200)
                .extract().response();

        cachedToken = response.path("access_token");
        int expiresInSeconds = response.path("expires_in");
        tokenExpiresAt = System.currentTimeMillis() + (expiresInSeconds * 1000L);
        return cachedToken;
    }
}

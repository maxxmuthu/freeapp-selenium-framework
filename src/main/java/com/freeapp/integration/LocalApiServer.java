package com.freeapp.integration;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/* Stands in for a real backend for saucedemo.com, which has none. Serves
   Api/products.json over plain HTTP, so ApiClient (and Postman) can hit it
   exactly like any real REST API: GET /products for all of them,
   GET /products/{name} for one.

   Also guards /products behind a bearer token, the same OAuth2
   "client credentials" flow a real machine-to-machine API (e.g. Salesforce's
   REST API) uses: a caller first exchanges a client ID/secret for a
   short-lived access token at POST /auth/token, then sends that token as
   an "Authorization: Bearer <token>" header on every later request. No
   token, a wrong token, or an expired one all get 401, never the actual
   product data.

   start() is a synchronized static method, not an instance you construct -
   every test class that needs the API just calls start(), and only the
   first caller actually binds the port; every later call is a no-op. Same
   fix, same reason, as ExtentManager's synchronized getInstance(): several
   TestNG threads under parallel="methods" could otherwise race to bind
   port 9090 at once. */
public class LocalApiServer
{
    public static final int PORT = 9090;
    private static final String DATA_FILE = "Api/products.json";
    private static final String ORDERS_FILE = "Api/orders.json";
    private static final long TOKEN_LIFETIME_MILLIS = 60_000;
    private static boolean running = false;

    /* token -> the millis timestamp it expires at. A real auth server keeps
       this same kind of lookup, just backed by Redis/a DB instead of an
       in-memory Map, and the tokens themselves are usually signed JWTs
       instead of a random UUID - the concept (issue it, remember it,
       reject anything not in here or past its expiry) is identical. */
    private static final Map<String, Long> validTokens = new ConcurrentHashMap<>();

    public static synchronized void start() throws IOException
    {
        if (running)
        {
            return;
        }

        JSONArray products = new JSONArray(new String(Files.readAllBytes(Paths.get(DATA_FILE)), StandardCharsets.UTF_8));
        JSONObject orders = new JSONObject(new String(Files.readAllBytes(Paths.get(ORDERS_FILE)), StandardCharsets.UTF_8));

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/auth/token", exchange -> handleTokenRequest(exchange));
        server.createContext("/products", exchange -> handleProductsRequest(exchange, products));
        server.createContext("/orders", exchange -> handleOrdersRequest(exchange, orders));

        server.start();
        running = true;
    }

    /* POST /auth/token, body {"clientId":"...","clientSecret":"..."} ->
       {"access_token":"...","expires_in":60} on a match against the same
       Configuration/config.properties values ApiClient reads - the server
       and the client agree on one shared source of truth for the
       credentials instead of two hardcoded copies that could drift apart.
       A mismatch returns 401, same status a wrong password gets from a
       real login endpoint. */
    private static void handleTokenRequest(HttpExchange exchange) throws IOException
    {
        if (!"POST".equals(exchange.getRequestMethod()))
        {
            respond(exchange, 405, "{\"error\":\"Use POST\"}");
            return;
        }

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JSONObject credentials = new JSONObject(requestBody);

        boolean validClient = credentials.optString("clientId").equals(Config.get("api.clientId"))
                && credentials.optString("clientSecret").equals(Config.get("api.clientSecret"));

        if (!validClient)
        {
            respond(exchange, 401, "{\"error\":\"Invalid client credentials\"}");
            return;
        }

        String token = UUID.randomUUID().toString();
        validTokens.put(token, System.currentTimeMillis() + TOKEN_LIFETIME_MILLIS);

        JSONObject responseBody = new JSONObject();
        responseBody.put("access_token", token);
        responseBody.put("expires_in", TOKEN_LIFETIME_MILLIS / 1000);
        respond(exchange, 200, responseBody.toString());
    }

    private static void handleProductsRequest(HttpExchange exchange, JSONArray products) throws IOException
    {
        if (!isAuthorized(exchange))
        {
            respond(exchange, 401, "{\"error\":\"Missing, invalid, or expired access token\"}");
            return;
        }

        String path = exchange.getRequestURI().getPath();

        if (path.equals("/products"))
        {
            respond(exchange, 200, products.toString());
            return;
        }

        String requestedName = URLDecoder.decode(path.substring("/products/".length()), StandardCharsets.UTF_8);

        for (int i = 0; i < products.length(); i++)
        {
            JSONObject product = products.getJSONObject(i);
            if (product.getString("name").equals(requestedName))
            {
                respond(exchange, 200, product.toString());
                return;
            }
        }

        respond(exchange, 404, "{\"error\":\"Product not found\"}");
    }

    /* GET /orders -> Api/orders.json as-is, unchanged. Exists purely as a
       real endpoint to demonstrate reading a nested JSON structure
       (an array of objects, each holding its own nested array of objects)
       - see ApiClient's getAllOrderIds()/getItemSkusForFirstOrder()/etc.,
       kept as reference examples since this project's real data
       (products) is never nested this deep. */
    private static void handleOrdersRequest(HttpExchange exchange, JSONObject orders) throws IOException
    {
        if (!isAuthorized(exchange))
        {
            respond(exchange, 401, "{\"error\":\"Missing, invalid, or expired access token\"}");
            return;
        }

        respond(exchange, 200, orders.toString());
    }

    /* "Authorization: Bearer <token>" is the standard header shape - the
       word "Bearer" says the token itself is the proof (whoever holds it
       is trusted), then the token has to exist in validTokens and not be
       past its expiry. Checked before the 404 lookup above so an
       unauthenticated caller can't even tell which product names are
       real - a real API hides that the same way. */
    private static boolean isAuthorized(HttpExchange exchange)
    {
        String header = exchange.getRequestHeaders().getFirst("Authorization");
        if (header == null || !header.startsWith("Bearer "))
        {
            return false;
        }

        String token = header.substring("Bearer ".length());
        Long expiresAt = validTokens.get(token);
        return expiresAt != null && System.currentTimeMillis() < expiresAt;
    }

    private static void respond(HttpExchange exchange, int statusCode, String body) throws IOException
    {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream responseBody = exchange.getResponseBody())
        {
            responseBody.write(bytes);
        }
    }

    /* Runs the server standalone, kept alive for as long as this process
       runs - point Postman at it (see Postman/SauceDemoLocalApi.postman_collection.json)
       without needing a test to also be running. */
    public static void main(String[] args) throws IOException
    {
        start();
        System.out.println("Local API running - get a token first: POST http://localhost:" + PORT + "/auth/token");
    }
}

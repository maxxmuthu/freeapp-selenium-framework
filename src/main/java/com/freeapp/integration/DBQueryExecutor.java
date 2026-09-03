package com.freeapp.integration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/* Two shapes of the same kind of query, over the same products table:
   getPrice() returns one scalar column, the simplest possible JDBC query;
   getProduct() returns every column of the row as a Map. */

public class DBQueryExecutor
{
    /* Simplest possible query: one column, one value, no Map involved.
       SELECT price -> a single String back. */
    public static String getPrice(String productName)
    {
        /* '?' is a placeholder, filled in below - never the raw value
           concatenated into the SQL string directly. */
        String query = "SELECT price FROM products WHERE name = ?";
        String price = null;

        /* Both conn and stmt close automatically once this block ends,
           success or failure - no manual close() needed. */
        try (Connection conn = DBReader.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query))
        {
            /* Binds productName into the query's 1st '?' (1-indexed, not 0).
               Safe against SQL injection - the value is data, never SQL syntax. */
            stmt.setString(1, productName);
            try (ResultSet resultSet = stmt.executeQuery())
            {
                /* next() moves to the first row and returns false if there
                   isn't one - price stays null for an unknown product. */
                if (resultSet.next())
                {
                    price = resultSet.getString("price");
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("DBQueryExecutor query failed: " + query, e);
        }
        return price;
    }

    /* Same steps as getPrice() above, just reading two columns into a Map
       instead of one column into a String. */
    public static Map<String, String> getProduct(String productName)
    {
        String query = "SELECT name, price FROM products WHERE name = ?";
        Map<String, String> product = new HashMap<>();

        try (Connection conn = DBReader.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query))
        {
            stmt.setString(1, productName);
            try (ResultSet resultSet = stmt.executeQuery())
            {
                if (resultSet.next())
                {
                    product.put("name", resultSet.getString("name"));
                    product.put("price", resultSet.getString("price"));
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("DBQueryExecutor query failed: " + query, e);
        }
        return product;
    }
}

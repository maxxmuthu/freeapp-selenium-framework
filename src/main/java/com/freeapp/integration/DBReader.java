package com.freeapp.integration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/* Connection management for the local SQLite file standing in for a real
   backend DB, since saucedemo.com has none. The file itself
   (Database/sauceDemoData.db) is a pre-seeded fixture, generated once by a
   throwaway program and checked in as a static fixture - this class only
   ever connects to it. */
public class DBReader
{
    /* jdbc:sqlite: tells DriverManager which JDBC driver to use; everything
       after it is just the .db file's path on disk (no host/port/login -
       SQLite is a local file, not a server). */
    private static final String URL = "jdbc:sqlite:" + System.getProperty("user.dir") + "/Database/sauceDemoData.db";

    /* Returns a fresh Connection on every call rather than one shared static
       instance: under TestNG's parallel="methods" execution, a shared
       Connection would let one thread's close() invalidate another
       thread's in-flight query. */
    public static Connection getConnection()
    {
        try
        {
            return DriverManager.getConnection(URL);
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Failed to connect to " + URL, e);
        }
    }
}

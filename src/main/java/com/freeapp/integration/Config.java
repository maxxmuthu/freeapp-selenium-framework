package com.freeapp.integration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/* Loads Configuration/config.properties once, at class-load time, into a
   static Properties map; every other class reads a value by key via get(). */
public class Config
{
    private static final Properties props = new Properties();

    /* Runs once, automatically, the first time anything touches Config.
       Throws immediately if the file is missing or unreadable, so no test
       runs with missing config. */
    static
    {
        try (FileInputStream fis = new FileInputStream(System.getProperty("user.dir") + "/Configuration/config.properties"))
        {
            props.load(fis);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to load Configuration/config.properties", e);
        }
    }

    /* Throws instead of returning null so a missing or misspelled key fails
       here, at the point of use, rather than as a later NullPointerException. */
    public static String get(String key)
    {
        String value = props.getProperty(key);
        if (value == null)
        {
            throw new IllegalArgumentException("Config key not found: " + key);
        }
        return value;
    }
}

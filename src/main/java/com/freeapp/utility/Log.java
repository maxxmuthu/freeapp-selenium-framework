package com.freeapp.utility;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* Thin log4j2 wrapper so test/page-object/listener code logs through one
   facade, sharing one Logger instance, instead of every class pulling in
   Logger and naming its own. */

public class Log
{
    private static final Logger LOGGER = LogManager.getLogger("freeapp.qa");

    public static void info(String message)
    {
        LOGGER.info(message);
    }

    public static void warn(String message)
    {
        LOGGER.warn(message);
    }

    public static void error(String message, Throwable t)
    {
        LOGGER.error(message, t);
    }
}

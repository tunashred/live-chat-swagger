package com.github.tunashred.config;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JavalinConfig {
    private static final Logger logger = LogManager.getLogger(JavalinConfig.class);

    public static Javalin configureServer() {
        return Javalin.create(config -> {
            config.staticFiles.add("/static", Location.CLASSPATH);
        });
    }
}

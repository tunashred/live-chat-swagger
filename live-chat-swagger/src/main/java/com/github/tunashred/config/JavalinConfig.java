package com.github.tunashred.config;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class JavalinConfig {
    public static Javalin configureServer() {
        return Javalin.create(config -> {
            config.staticFiles.add("/static", Location.CLASSPATH);
        });
    }
}

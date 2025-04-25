package com.github.tunashred.config;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JavalinConfig {
    public static Javalin configureServer() {
        return Javalin.create(config -> {
            config.staticFiles.add("/static", Location.CLASSPATH);
        });
    }
}

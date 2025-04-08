package com.github.tunashred;

import com.github.tunashred.config.JavalinConfig;
import com.github.tunashred.controller.MessageController;

import io.javalin.Javalin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Application {
    private static final Logger logger = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        Javalin app = JavalinConfig.configureServer();

        MessageController.registerRoutes(app);

        app.start(7000);
    }
}

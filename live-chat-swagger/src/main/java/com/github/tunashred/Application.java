package com.github.tunashred;

import com.github.tunashred.config.JavalinConfig;
import com.github.tunashred.controller.ClientController;
import com.github.tunashred.controller.ManagerController;
import com.github.tunashred.controller.StreamerController;
import io.javalin.Javalin;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Application {
    public static void main(String[] args) {
        log.info("Starting Swagger server");
        Javalin app = JavalinConfig.configureServer();

        ClientController.registerRoutes(app);

        StreamerController.registerRoutes(app);

        ManagerController.registerRoutes(app);

        app.get("/", ctx -> ctx.redirect("swagger-ui.html"));

        Runtime.getRuntime().addShutdownHook(new Thread(ClientController::close));
        Runtime.getRuntime().addShutdownHook(new Thread(ManagerController::close));
        Runtime.getRuntime().addShutdownHook(new Thread(StreamerController::close));

        app.start(7000);
        log.info("Swagger application started");
    }
}

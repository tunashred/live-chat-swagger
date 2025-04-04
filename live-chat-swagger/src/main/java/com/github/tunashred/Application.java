package com.github.tunashred;

import com.github.tunashred.config.JavalinConfig;
import com.github.tunashred.controller.MessageController;
import com.github.tunashred.kafka.ClientConsumer;
import com.github.tunashred.kafka.ClientProducer;

import io.javalin.Javalin;

public class Application {
    public static void main(String[] args) {
        ClientProducer producerService = new ClientProducer();
        ClientConsumer consumerService = new ClientConsumer();

        Javalin app = JavalinConfig.configureServer();
        MessageController.registerRoutes(app, producerService, consumerService);

        app.start(7000);
    }
}

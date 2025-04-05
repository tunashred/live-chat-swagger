package com.github.tunashred.controller;

import com.github.tunashred.dto.BannedWordRequest;
import com.github.tunashred.dto.MessageRequest;
import com.github.tunashred.dtos.MessageInfo;

import com.github.tunashred.kafka.ClientConsumer;
import com.github.tunashred.kafka.ClientProducer;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.ValidationException;

public class MessageController {
    private static final Logger logger = LogManager.getLogger(MessageController.class);

    public static void registerRoutes(Javalin app, ClientProducer producerService, ClientConsumer consumerService) {
        app.post("/client-produce-message", ctx -> {
            MessageRequest messageRequest = getProduceParams(ctx);
            producerService.sendMessage(messageRequest.getMessage());
            ctx.result("Produced: " + messageRequest.getMessage());
        });

        app.get("/client-consume-message", ctx -> {
            MessageInfo message = consumerService.consumeMessage();
            if (message == null) {
                ctx.result("[WARNING] No records to fetch");
            } else {
                ctx.result(message.getUser().getName() + ": " + message.getMessage());
            }
        });

        app.post("admin-add-banned-word", ctx -> {
            BannedWordRequest request = getBannedWord(ctx);
            producerService.addBannedWordToTopic(request.getTopic(), request.getWord());
            ctx.result("Added banned word: " + request.getWord());
        });

        app.post("admin-remove-banned-word", ctx -> {
            BannedWordRequest request = getBannedWord(ctx);
            producerService.removeBannedWordFromTopic(request.getTopic(), request.getWord());
            ctx.result("Added banned word: " + request.getWord());
        });

        app.get("/", ctx -> ctx.redirect("swagger-ui.html"));
    }

    private static MessageRequest getProduceParams(Context ctx) throws ValidationException {
        String message = ctx.queryParam("message");
        String dummyParam = ctx.queryParam("dummy param");

        // validate the params
        if (message == null || message.trim().isEmpty()) {
            throw new ValidationException("Parameter 'message' is required");
        }

        if (dummyParam == null || dummyParam.trim().isEmpty()) {
            throw new ValidationException("Parameter 'dummy param' is required");
        }

        return new MessageRequest(message, Integer.parseInt(dummyParam));
    }

    private static BannedWordRequest getBannedWord(Context ctx) throws ValidationException {
        String topic = ctx.queryParam("topic");
        String word = ctx.queryParam("word");

        if (topic == null || topic.trim().isEmpty()) {
            throw new ValidationException("Parameter 'topic' is required");
        }

        if (word == null || word.trim().isEmpty()) {
            throw new ValidationException("Parameter 'word' is required");
        }
        return new BannedWordRequest(topic, word);
    }
}
